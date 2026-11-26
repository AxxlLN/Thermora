"""Read-only verifier/viewer, adapted from the preserved model-negative-fixtures.py
and model-numeric-corpus.py preparation scripts. Does not establish independent
provenance of Full fixtures, expected DTOs or production mappers. Standard library only.
"""
import argparse
import base64
import copy
import gzip
import json
from pathlib import Path

RESOURCES = Path(__file__).resolve().parents[1] / "resources" / "model-v1"
ROOTS = ["SealedHousingLegacy", "InternalMixingLegacy", "ExternalCoolingLegacy", "FinnedHousingLegacy"]


def raw(value):
    return json.dumps(value, ensure_ascii=False, separators=(",", ":"))


def json_path(path):
    return "$" + "".join(f"[{key}]" if isinstance(key, int) else "." + key for key in path)


def at(value, path):
    for key in path:
        value = value[key]
    return value


def walk(value, path=()):
    yield path, value
    if isinstance(value, dict):
        for key, child in value.items():
            yield from walk(child, path + (key,))
    elif isinstance(value, list):
        for index, child in enumerate(value):
            yield from walk(child, path + (index,))


def encoded(value):
    return base64.b64encode(raw(value).encode("utf-8")).decode("ascii")


def expected_rows():
    negative, quoted = [], []
    for index, (root, direction) in enumerate((root, direction) for root in ROOTS for direction in ["Input", "Output"]):
        original = json.loads((RESOURCES / (root + direction + "Full.json")).read_text(encoding="utf-8"))
        for path, node in walk(original):
            if isinstance(node, dict):
                for key in node:
                    modified = copy.deepcopy(original)
                    del at(modified, path)[key]
                    prefix = f"{index}\tINVALID_PAYLOAD\t{json_path(path + (key,))}\t"
                    negative.append(prefix + encoded(modified))
                    nullable = not path and key in ["heatTransfer", "geometryResults", "externalHeatTransfer", "internalHeatTransfer", "heatTransferResults"] and root != "FinnedHousingLegacy"
                    if not nullable:
                        modified = copy.deepcopy(original)
                        at(modified, path)[key] = None
                        negative.append(prefix + encoded(modified))
                modified = copy.deepcopy(original)
                at(modified, path)["unexpected"] = 0
                negative.append(f"{index}\tINVALID_PAYLOAD\t{json_path(path + ('unexpected',))}\t" + encoded(modified))
            elif isinstance(node, (float, int)) and not isinstance(node, bool):
                modified = copy.deepcopy(original)
                at(modified, path[:-1])[path[-1]] = str(node)
                quoted.append(f"{index}\t{json_path(path)}\t" + encoded(modified))
        first = next(iter(original))
        for key_text in [raw(first), '"\\u' + format(ord(first[0]), "04x") + first[1:] + '"']:
            text = raw(original)[:-1] + "," + key_text + ":" + raw(original[first]) + "}"
            negative.append(f"{index}\tINVALID_PAYLOAD\t$.{first}\t" + base64.b64encode(text.encode("utf-8")).decode("ascii"))
    return {"negative-cases": negative, "quoted-numbers": quoted}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--show", choices=["negative-cases", "quoted-numbers"])
    parser.add_argument("--row", type=int, default=1, help="one-based row number")
    args = parser.parse_args()
    if args.show:
        with gzip.open(RESOURCES / (args.show + ".tsv.gz"), "rt", encoding="utf-8") as stream:
            rows = stream.read().splitlines()
        if not 1 <= args.row <= len(rows):
            parser.error("row out of bounds")
        columns = rows[args.row - 1].split("\t")
        print("metadata:", columns[:-1])
        print(base64.b64decode(columns[-1], validate=True).decode("utf-8"))
        return
    for name, expected in expected_rows().items():
        with gzip.open(RESOURCES / (name + ".tsv.gz"), "rt", encoding="utf-8") as stream:
            actual = stream.read().splitlines()
        if actual != expected:
            raise SystemExit(f"{name}: content differs; review fixtures and expectations manually")
        print(f"{name}: {len(actual)} rows verified")


if __name__ == "__main__":
    main()
