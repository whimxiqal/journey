INSERT INTO journey_waypoints (
    player_uuid,
    name_id,
    name,
    domain_id,
    x,
    y,
    z,
    created,
    is_public
) VALUES (
    ?, ?, ?, ?, ?, ?, ?, ?, ?
);