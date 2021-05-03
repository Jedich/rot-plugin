
CREATE TABLE IF NOT EXISTS user_claims
(
    id      INT(11) NOT NULL,
    name    VARCHAR(64) DEFAULT NULL,
    chunk_x INT(11)     DEFAULT NULL,
    chunk_y INT(11)     DEFAULT NULL,
    owner   VARCHAR(64) DEFAULT NULL,
    type    VARCHAR(64) DEFAULT 'Default',
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS kings
(
    id            INT(11) NOT NULL,
    name          VARCHAR(64)   DEFAULT NULL,
    title         VARCHAR(64)   DEFAULT NULL,
    kingdom_name  VARCHAR(64)   DEFAULT NULL,
    home_chunk    VARCHAR(64)   DEFAULT NULL,
    kingdom_level INT(11)       DEFAULT NULL,
    current_gen   INT(11)       DEFAULT NULL,
    balance       DECIMAL(9, 3) DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS houses
(
    id        INT(11) NOT NULL,
    owner     VARCHAR(64)   DEFAULT NULL,
    bed_block VARCHAR(64)   DEFAULT NULL,
    level     INT(11)       DEFAULT NULL,
    area      INT(11)       DEFAULT NULL,
    benefits  INT(11)       DEFAULT NULL,
    income    DECIMAL(5, 3) DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS house_blocks
(
    id   INTEGER NOT NULL,
    name VARCHAR(64) DEFAULT NULL,
    x    INT(11)     DEFAULT 0,
    y    INT(11)     DEFAULT 0,
    z    INT(11)     DEFAULT 0,
    PRIMARY KEY("id" AUTOINCREMENT)
);

CREATE TABLE IF NOT EXISTS war_claims
(
    id         INT(11) NOT NULL,
    by_king    VARCHAR(64) DEFAULT NULL,
    chunk_name VARCHAR(64) DEFAULT NULL,
    PRIMARY KEY (id)
)