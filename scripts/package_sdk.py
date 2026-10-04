#!/usr/bin/env python3
"""Verify that all KMP targets exist and package a self-contained Maven repository."""
import argparse
import hashlib
import json
from pathlib import Path
import zipfile
import subprocess

p = argparse.ArgumentParser(description=__doc__)
p.add_argument('--repository', type=Path, required=True)
p.add_argument('--coordinate', required=True)
p.add_argument('--version', required=True)
p.add_argument('--source-repository', required=True)
p.add_argument('--source-sha', required=True)
p.add_argument('--desktop-only', action='store_true', help='Package only JVM desktop and KMP metadata')
a = p.parse_args()
group, artifact = a.coordinate.split(':')
base = a.repository / group.replace('.', '/')
root = base / artifact / a.version
required = [root / f'{artifact}-{a.version}.module']
targets = [('desktop', 'jar')] if a.desktop_only else [('android', 'aar'), ('iosarm64', 'klib'), ('iossimulatorarm64', 'klib'), ('desktop', 'jar')]
for suffix, extension in targets:
    target = f'{artifact}-{suffix}'
    required.append(base / target / a.version / f'{target}-{a.version}.{extension}')
for path in required:
    if not path.is_file() or not path.stat().st_size:
        raise SystemExit(f'Missing published target: {path}')
if not a.desktop_only:
    # An AAR stripped of its public API is not a reusable library.
    with zipfile.ZipFile(required[1]) as aar:
        if len(aar.read('classes.jar')) < 1000:
            raise SystemExit('Published Android classes are missing or unexpectedly empty')
        for abi in ('arm64-v8a', 'armeabi-v7a', 'x86_64'):
            native_lib = f'jni/{abi}/libliquidwave.so'
            if native_lib not in aar.namelist() or aar.getinfo(native_lib).file_size < 1000:
                raise SystemExit(f'Published Android native kernel is missing: {native_lib}')
# Only this version belongs in the archive, even when staging contains older local builds.
publication_dirs = [root, *(base / f'{artifact}-{suffix}' / a.version
                           for suffix, _ in targets)]
files = {str(f.relative_to(a.repository)): hashlib.sha256(f.read_bytes()).hexdigest()
         for directory in publication_dirs for f in directory.rglob('*') if f.is_file()}
info = dict(coordinate=a.coordinate, version=a.version, sourceRepository=a.source_repository,
            sourceSha=a.source_sha, files=files, targets=[suffix for suffix, _ in targets],
            sourceTreeSha256=hashlib.sha256(subprocess.check_output(
                ['git', 'archive', '--format=tar', a.source_sha], cwd=Path(__file__).resolve().parents[1])).hexdigest())
archive = a.repository.with_suffix('.zip')
with zipfile.ZipFile(archive, 'w', zipfile.ZIP_DEFLATED) as out:
    out.writestr('sdk-info.json', json.dumps(info, indent=2))
    for name in sorted(files):
        out.write(a.repository / name, 'maven/' + name)
digest = hashlib.sha256(archive.read_bytes()).hexdigest()
archive.with_suffix('.zip.sha256').write_text(f'{digest}  {archive.name}\n')
print(f'Validated {a.coordinate}:{a.version}: {info["targets"]} and KMP metadata')
