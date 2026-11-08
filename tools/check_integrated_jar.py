#!/usr/bin/env python3
"""Check that a distributable Fabric JAR includes all protected resource models."""
import sys
import zipfile
from pathlib import Path
if len(sys.argv) != 2:
    raise SystemExit("Usage: check_integrated_jar.py <jar>")
jar = Path(sys.argv[1])
with zipfile.ZipFile(jar) as zipfile_obj:
    if zipfile_obj.testzip() is not None:
        raise SystemExit("Corrupted JAR")
    names = set(zipfile_obj.namelist())
    root = "resourcepacks/twohanded_visuals/"
    prefix = root + "assets/twohandedblocks/items/held/"
    held = [n for n in names if n.startswith(prefix) and n.endswith(".json")]
    required = ["pack.mcmeta", "LICENSE.txt", "ATRIBUCION-CAMPANA.txt",
                "assets/minecraft/models/item/bell.json",
                "assets/twohandedblocks/items/held/decorated_pot.json"]
    missing = [p for p in required if root + p not in names]
    if missing or len(held) != 74:
        raise SystemExit(f"Not a complete protected JAR: missing={missing}; held models={len(held)}")
print("Verified complete JAR: 74 protected models and license metadata.")
