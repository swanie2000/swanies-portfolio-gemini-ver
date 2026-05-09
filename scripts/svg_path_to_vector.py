"""Embed SVG <path d=...> into Android VectorDrawable (single path; large paths split for aapt)."""
from __future__ import annotations

import re
import sys
from pathlib import Path

MAX_CHUNK = 28000


def split_path_data(path: str) -> list[str]:
    out: list[str] = []
    i = 0
    n = len(path)
    while i < n:
        rest = n - i
        if rest <= MAX_CHUNK:
            out.append(path[i:])
            break
        end = i + MAX_CHUNK
        seg = path[i:end]
        rel = max(seg.rfind("z"), seg.rfind("Z"))
        if rel < 0:
            raise SystemExit(
                f"No z/Z in path slice [{i}, {end}); cannot split safely (need subpath boundaries)."
            )
        cut = i + rel
        out.append(path[i : cut + 1])
        i = cut + 1
    return out


def main() -> None:
    root = Path(__file__).resolve().parent.parent
    arg_svg = sys.argv[1] if len(sys.argv) > 1 else "docs/assets/swan_asset_launcher_vector_ultra_small.svg"
    arg_out = (
        sys.argv[2]
        if len(sys.argv) > 2
        else "app/src/main/res/drawable/ic_launcher_foreground_vector.xml"
    )
    svg = Path(arg_svg) if Path(arg_svg).is_absolute() else root / arg_svg
    out = Path(arg_out) if Path(arg_out).is_absolute() else root / arg_out

    text = svg.read_text(encoding="utf-8")
    m = re.search(r'<path[^>]+d="([^"]+)"', text)
    if not m:
        print("No <path d=\"...\"> found (single path required)", file=sys.stderr)
        sys.exit(1)
    d = m.group(1)
    vb = re.search(r'viewBox="0 0 (\d+) (\d+)"', text)
    if not vb:
        print('No viewBox="0 0 W H"', file=sys.stderr)
        sys.exit(1)
    vw, vh = int(vb.group(1)), int(vb.group(2))

    chunks = split_path_data(d)
    path_els = "\n".join(
        f'    <path\n        android:fillColor="#FFFFFF"\n        android:pathData="{chunk}" />'
        for chunk in chunks
    )
    out_xml = f"""<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="{vw}dp"
    android:height="{vh}dp"
    android:viewportWidth="{vw}"
    android:viewportHeight="{vh}">
{path_els}
</vector>
"""
    out.write_text(out_xml, encoding="utf-8")
    print("Input:", svg)
    print("pathData total length:", len(d))
    print("chunks:", len(chunks), [len(c) for c in chunks])
    print("Wrote:", out)


if __name__ == "__main__":
    main()
