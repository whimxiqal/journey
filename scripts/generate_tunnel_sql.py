#!/usr/bin/env python3
"""Generate journey_tunnels SQL from WHIMC-Portals portalData.yml."""

import math
import uuid
from pathlib import Path

import yaml

WORLDS_DIR = Path(r"d:\Backup\qrf-whimc-server-local\worlds")
PORTAL_FILE = Path(r"d:\Backup\qrf-whimc-server-local\plugins\WHIMC-Portals\portalData.yml")
OUT_FILE = Path(__file__).resolve().parent / "journey_tunnels_from_portals.sql"

# Same-world links from portal arrival pads down to underground habitat levels.
# WHIMC-Portals teleports to surface Y while NPCs live ~20-30 blocks below.
HABITAT_ELEVATORS = [
    ("ColderHot-surface-to-habitat", "ColderHot", -97, 78, 7, "ColderHot", -88, 60, -80),
    ("ColderCold-surface-to-habitat", "ColderCold", -90, 77, -10, "ColderCold", -77, 54, -50),
]


def read_uid(path: Path) -> uuid.UUID:
    return uuid.UUID(bytes=path.read_bytes())


def center_pos(pos1: dict, pos2: dict) -> tuple[int, int, int]:
    return (
        (pos1["x"] + pos2["x"]) // 2,
        (pos1["y"] + pos2["y"]) // 2,
        (pos1["z"] + pos2["z"]) // 2,
    )


def dest_block(destination: dict) -> tuple[int, int, int]:
    return (
        math.floor(destination["x"]),
        math.floor(destination["y"]),
        math.floor(destination["z"]),
    )


def main() -> None:
    world_uuids: dict[str, uuid.UUID] = {}
    for world_path in WORLDS_DIR.iterdir():
        uid_file = world_path / "uid.dat"
        if world_path.is_dir() and uid_file.exists():
            world_uuids[world_path.name] = read_uid(uid_file)

    with open(PORTAL_FILE, encoding="utf-8") as f:
        data = yaml.safe_load(f)

    destinations: dict = data["Destinations"]
    skipped: list[tuple[str, str]] = []
    rows: list[tuple] = []

    for name, portal in data["Portals"].items():
        dest_name = portal.get("destination")
        if not dest_name:
            skipped.append((name, "no destination"))
            continue

        dest_key = dest_name
        if dest_key not in destinations:
            matches = [k for k in destinations if k.lower() == dest_name.lower()]
            if len(matches) == 1:
                dest_key = matches[0]
            else:
                skipped.append((name, f"unknown destination {dest_name!r}"))
                continue

        origin_world = portal["world"]
        dest_world = destinations[dest_key]["world"]
        if origin_world not in world_uuids:
            skipped.append((name, f"unknown origin world {origin_world!r}"))
            continue
        if dest_world not in world_uuids:
            skipped.append((name, f"unknown dest world {dest_world!r}"))
            continue
        if origin_world == dest_world:
            skipped.append((name, "same-world (elevator/intra-world)"))
            continue

        ox, oy, oz = center_pos(portal["pos1"], portal["pos2"])
        dx, dy, dz = dest_block(destinations[dest_key])
        rows.append(
            (
                name,
                world_uuids[origin_world],
                ox,
                oy,
                oz,
                world_uuids[dest_world],
                dx,
                dy,
                dz,
            )
        )

    lines = [
        "-- Journey tunnels generated from WHIMC-Portals portalData.yml",
        "-- Cross-world links only; same-world portals skipped.",
        "-- tunnel_type 0 = NETHER (standard portal tunnel cost in Journey)",
        "",
        "DELETE FROM journey_tunnels;",
        "",
        "INSERT INTO journey_tunnels (",
        "  origin_domain_id, origin_x, origin_y, origin_z,",
        "  destination_domain_id, destination_x, destination_y, destination_z,",
        "  tunnel_type",
        ") VALUES",
    ]
    value_lines = []
    for name, origin_uuid, ox, oy, oz, dest_uuid, dx, dy, dz in rows:
        value_lines.append(
            f"  (UNHEX('{origin_uuid.hex}'), {ox}, {oy}, {oz}, "
            f"UNHEX('{dest_uuid.hex}'), {dx}, {dy}, {dz}, 0) -- {name}"
        )
    for name, world, ox, oy, oz, dw, dx, dy, dz in HABITAT_ELEVATORS:
        world_uuid = world_uuids[world]
        value_lines.append(
            f"  (UNHEX('{world_uuid.hex}'), {ox}, {oy}, {oz}, "
            f"UNHEX('{world_uuid.hex}'), {dx}, {dy}, {dz}, 0) -- {name}"
        )
    lines.append(",\n".join(value_lines))
    lines.append(";")
    lines.append("")
    lines.append(f"-- {len(rows)} cross-world tunnels + {len(HABITAT_ELEVATORS)} habitat elevators")
    lines.append("-- Skipped portals:")
    for portal_name, reason in skipped:
        lines.append(f"--   {portal_name}: {reason}")

    OUT_FILE.write_text("\n".join(lines), encoding="utf-8")
    print(f"Generated {len(rows)} cross-world tunnels -> {OUT_FILE}")
    print(f"Skipped {len(skipped)} portals")


if __name__ == "__main__":
    main()
