-- Journey Waypoints

CREATE TABLE journey_waypoints (
    player_uuid CHAR(36),
    name_id     VARCHAR(32) NOT NULL,
    name        VARCHAR(32) NOT NULL,
    domain_id   CHAR(36)    NOT NULL,
    x           INT(7)      NOT NULL,
    y           INT(7)      NOT NULL,
    z           INT(7)      NOT NULL,
    created   INT         NOT NULL
);

-- Journey Path Cache

CREATE TABLE journey_cached_paths (
    id              INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
    timestamp       INT             NOT NULL,
    duration        INTEGER         NOT NULL,
    path_length     DOUBLE(12, 5)   NOT NULL,
    origin_x        INT(7)          NOT NULL,
    origin_y        INT(7)          NOT NULL,
    origin_z        INT(7)          NOT NULL,
    destination_x   INT(7)          NOT NULL,
    destination_y   INT(7)          NOT NULL,
    destination_z   INT(7)          NOT NULL,
    domain_id       CHAR(36)        NOT NULL
);
CREATE INDEX journey_cached_paths_idx ON journey_cached_paths (
    origin_x, origin_y, origin_z, destination_x, destination_y, destination_z, domain_id
);

CREATE TABLE journey_cached_paths_cells (
    path_record_id  INT     NOT NULL,
    x               INT(7)  NOT NULL,
    y               INT(7)  NOT NULL,
    z               INT(7)  NOT NULL,
    path_index      INT(10) NOT NULL,
    mode_type       INT(2)  NOT NULL,
    FOREIGN KEY     (path_record_id)
        REFERENCES  cached_paths    (id)
        ON DELETE   CASCADE
        ON UPDATE   CASCADE
);
CREATE INDEX journey_cached_paths_cells_idx ON journey_cached_path_cells (path_record_id);

CREATE TABLE journey_cached_path_modes (
    path_record_id  INT     NOT NULL,
    mode_type       INT(2)  NOT NULL,
    FOREIGN KEY     (path_record_id)
        REFERENCES  cached_paths    (id)
        ON DELETE   CASCADE
        ON UPDATE   CASCADE,
    UNIQUE          (path_record_id,    mode_type)
);
CREATE INDEX journey_cached_paths_modes_idx ON journey_cached_path_modes (path_record_id);

-- Journey Tunnel Cache

CREATE TABLE journey_tunnels (
    origin_domain_id        CHAR(36) NOT NULL,
    origin_x                INT(7) NOT NULL,
    origin_y                INT(7) NOT NULL,
    origin_z                INT(7) NOT NULL,
    destination_domain_id   CHAR(36) NOT NULL,
    destination_x           INT(7) NOT NULL,
    destination_y           INT(7) NOT NULL,
    destination_z           INT(7) NOT NULL,
    tunnel_type             INT(3) NOT NULL
);
CREATE INDEX journey_tunnels_origin_idx ON journey_tunnels (
    origin_domain_id, origin_x, origin_y, origin_z
);
CREATE INDEX journey_tunnels_destination_idx ON journey_tunnels (
    destination_domain_id, destination_x, destination_y, destination_z
);