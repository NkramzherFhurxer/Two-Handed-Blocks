#!/usr/bin/env python3
"""Stage approved Two-Handed-Blocks v33 assets into Fabric's client resources."""
from __future__ import annotations
import argparse
import hashlib
import io
import re
import shutil
import tarfile
from pathlib import Path

EXPECTED_SHA256 = "cb432874752be0d64d76234361e062ce76b21b112e05257a661cb13d222eff7f"
EXPECTED_RESOURCE_COUNT = 238
EXPECTED_HELD_MODELS = 74

def stage(archive_path: Path, destination: Path, rules: Path) -> None:
    data = archive_path.read_bytes()
    actual = hashlib.sha256(data).hexdigest()
    if actual != EXPECTED_SHA256:
        raise SystemExit(f"Resource archive hash mismatch: {actual}")
    if destination.exists():
        shutil.rmtree(destination)
    destination.mkdir(parents=True)
    with tarfile.open(fileobj=io.BytesIO(data), mode="r:xz") as archive:
        members = archive.getmembers()
        if len(members) != EXPECTED_RESOURCE_COUNT:
            raise SystemExit(f"Expected {EXPECTED_RESOURCE_COUNT} resources; got {len(members)}")
        if not any(entry.name == "pack.mcmeta" for entry in members):
            raise SystemExit("Source pack missing pack.mcmeta")
        for entry in members:
            # pack.mcmeta is already tracked as a normal client resource.
            # Never stage it twice or Gradle may reject duplicate paths.
            if entry.name == "pack.mcmeta":
                continue
            target = (destination / entry.name).resolve()
            if not target.is_relative_to(destination.resolve()) or not entry.isfile():
                raise SystemExit(f"Unsafe archive member: {entry.name}")
            target.parent.mkdir(parents=True, exist_ok=True)
            with archive.extractfile(entry) as src, target.open("wb") as out:
                shutil.copyfileobj(src, out)
    held = destination / "assets/twohandedblocks/items/held"
    models = {p.stem for p in held.glob("*.json")}
    if len(models) != EXPECTED_HELD_MODELS:
        raise SystemExit(f"Expected {EXPECTED_HELD_MODELS} held models, got {len(models)}")
    source = rules.read_text(encoding="utf-8")
    match = re.search(r"PROTECTED\s*=\s*Set\.of\((.*?)\);", source, re.DOTALL)
    if not match:
        raise SystemExit("Cannot locate PROTECTED model list in Java source")
    expected = set(re.findall(r'"([a-z0-9_]+)"', match.group(1)))
    if models != expected:
        raise SystemExit(f"Model IDs mismatch: missing={expected-models}, extra={models-expected}")
    for path in ("LICENSE.txt", "ATRIBUCION-CAMPANA.txt",
                 "assets/minecraft/models/item/bell.json",
                 "assets/twohandedblocks/items/held/decorated_pot.json"):
        if not (destination / path).is_file():
            raise SystemExit(f"Missing required resource {path}")
    print(f"Staged {len(members)} files including {len(models)} protected model definitions.")

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--bundle", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--java-rules", type=Path, required=True)
    args = parser.parse_args()
    stage(args.bundle, args.output, args.java_rules)
