#!/usr/bin/env python3
"""Reject empty device suites, runner crashes and incomplete test results."""

import argparse
from pathlib import Path
import sys
import xml.etree.ElementTree as ET


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--results", type=Path,
                        default=Path("app/build/outputs/androidTest-results/connected/debug"))
    parser.add_argument("--minimum-tests", type=int, default=1)
    args = parser.parse_args()
    reports = sorted(args.results.glob("TEST-*.xml"))
    tests = failures = errors = skipped = 0
    for report in reports:
        root = ET.parse(report).getroot()
        tests += int(root.get("tests", 0))
        failures += int(root.get("failures", 0))
        errors += int(root.get("errors", 0))
        skipped += int(root.get("skipped", 0))
    print(f"Device tests: {tests}; failures: {failures}; errors: {errors}; skipped: {skipped}.")
    if tests < max(1, args.minimum_tests) or failures or errors or skipped:
        print("Device acceptance requires a complete, nonempty successful suite.", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
