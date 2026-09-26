#!/usr/bin/env python3
"""Check source-set and rendering boundaries without modifying the SDK."""

from pathlib import Path
import hashlib
import re
import sys


def main() -> int:
    root = Path(__file__).resolve().parents[1]
    sources = root / "sdk" / "src"
    errors = []
    implementations = {}
    files = sorted(sources.rglob("*.kt"))
    for path in files:
        relative = path.relative_to(sources)
        text = path.read_text()
        if not text.strip():
            errors.append(f"{relative}: empty Kotlin file")
        if relative.parts[0] == "main":
            errors.append(f"{relative}: use commonMain or a platform source set")
        package = re.search(r"^package ([\w.]+)", text, re.MULTILINE)
        if package and "kotlin" in relative.parts:
            expected = Path(*package.group(1).split("."))
            actual = path.parent.relative_to(sources / relative.parts[0] / "kotlin")
            if expected != actual:
                errors.append(f"{relative}: package does not match its directory")
        imports = re.findall(r"^import ([\w.]+)", text, re.MULTILINE)
        code = re.sub(r"/\*.*?\*/|//[^\n]*", "", text, flags=re.DOTALL)
        if relative.parts[0].endswith("Main"):
            body = re.sub(r"^(package|import) .*\n", "", code, flags=re.MULTILINE).strip()
            fingerprint = (relative.parts[0], hashlib.sha256(body.encode()).hexdigest())
            if body and fingerprint in implementations:
                errors.append(f"{relative}: duplicate implementation of {implementations[fingerprint]}")
            implementations[fingerprint] = relative
        if relative.parts[0] == "commonMain":
            for name in imports:
                if name.startswith(("android.", "java.", "javax.", "platform.")):
                    errors.append(f"{relative}: platform import in common code: {name}")
        if "/components/" in path.as_posix():
            for name in imports:
                if name.startswith((
                    "androidx.lifecycle.ViewModel", "androidx.lifecycle.viewmodel",
                    "io.ktor.client", "retrofit2.", "okhttp3.",
                    "androidx.datastore.", "com.russhwolf.settings.",
                    "android.content.SharedPreferences",
                )) or name.rsplit(".", 1)[-1].endswith(("Repository", "ViewModel")):
                    errors.append(f"{relative}: UI components must receive application state and events: {name}")
                if name in {"com.kyant.backdrop.drawBackdrop", "com.kyant.backdrop.drawPlainBackdrop"}:
                    errors.append(f"{relative}: components must use the shared glass renderers")
                if name == "androidx.compose.animation.core.spring":
                    errors.append(f"{relative}: use LiquidMotion for finite motion")
            if re.search(r"\bLiquidGlassScene\s*\(", code):
                errors.append(f"{relative}: a component must not create a nested scene")
        if "/components/" in path.as_posix() and "motion" not in relative.parts:
            if re.search(r"\b(?:Animatable|animateFloatAsState|animateDpAsState|animateColorAsState|rememberInfiniteTransition|infiniteRepeatable|keyframes|tween|spring|scaleIn|scaleOut|fadeIn|fadeOut|slideIn\w*|slideOut\w*|expandVertically|expandHorizontally|shrinkVertically|shrinkHorizontally)\s*(?:\(|\{)", code):
                errors.append(f"{relative}: animation construction belongs to the component's motion package")
        if "/components/" in path.as_posix() and "state" in relative.parts:
            if "@Composable" in code or any(name.startswith((
                "androidx.compose.foundation", "androidx.compose.material",
                "androidx.compose.animation", "com.kyant.backdrop",
            )) for name in imports):
                errors.append(f"{relative}: state holders must not depend on rendering or animation")

    for error in errors:
        print(error, file=sys.stderr)
    print(f"Checked {len(files)} Kotlin files; {len(errors)} structural violations.")
    return bool(errors)


if __name__ == "__main__":
    sys.exit(main())
