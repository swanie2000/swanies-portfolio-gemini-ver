"""Report string key parity: values/strings.xml vs each values-*/strings.xml."""
from __future__ import annotations

import re
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "res"
BASELINE = ROOT / "values" / "strings.xml"

# <string name="key" ...>value</string> — non-greedy value; skip translatable="false"
PAT = re.compile(
    r'<string\s+name="([^"]+)"([^>]*)>(.*?)</string>',
    re.DOTALL,
)


def parse_strings(path: pathlib.Path) -> tuple[list[str], dict[str, str]]:
    text = path.read_text(encoding="utf-8")
    order: list[str] = []
    vals: dict[str, str] = {}
    for m in PAT.finditer(text):
        name, attrs, val = m.group(1), m.group(2), m.group(3)
        if 'translatable="false"' in attrs:
            continue
        order.append(name)
        vals[name] = val.strip()
    return order, vals


def main() -> int:
    order, baseline_vals = parse_strings(BASELINE)
    baseline_keys = set(baseline_vals)
    locales = sorted(ROOT.glob("values-*/strings.xml"))
    print(f"Baseline: {len(baseline_keys)} translatable keys")
    print(f"Locale files: {len(locales)}")
    any_gap = False
    for loc in locales:
        _, loc_vals = parse_strings(loc)
        present = set(loc_vals)
        missing = [k for k in order if k not in present]
        extra = sorted(present - baseline_keys)
        if missing or extra:
            any_gap = True
            print(f"\n{loc.parent.name}: missing {len(missing)} extra {len(extra)}")
            if extra[:8]:
                print("  extra sample:", extra[:8])
            if missing[:20]:
                print("  missing sample:", missing[:20])
            if len(missing) > 20:
                print(f"  ... and {len(missing) - 20} more missing")
    if not any_gap:
        print("\nOK: all locales contain every baseline key; no extra keys.")
    return 1 if any_gap else 0


if __name__ == "__main__":
    sys.exit(main())
