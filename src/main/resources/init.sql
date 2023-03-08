CREATE TABLE IF NOT EXISTS user_claims
(
    id      INTEGER NOT NULL,
    name    VARCHAR(64) DEFAULT NULL,
    chunk_x INT(11)     DEFAULT NULL,
    chunk_y INT(11)     DEFAULT NULL,
    owner   VARCHAR(64) DEFAULT NULL,
    type    VARCHAR(64) DEFAULT 'Default',
    PRIMARY KEY ("id" AUTOINCREMENT)
);

CREATE TABLE IF NOT EXISTS kings
(
    id            INTEGER NOT NULL,
    name          VARCHAR(64)   DEFAULT NULL,
    title         VARCHAR(64)   DEFAULT NULL,
    kingdom_name  VARCHAR(64)   DEFAULT NULL,
    home_chunk    VARCHAR(64)   DEFAULT NULL,
    kingdom_level INT(11)       DEFAULT NULL,
    current_gen   INT(11)       DEFAULT NULL,
    balance       DECIMAL(9, 3) DEFAULT NULL,
    PRIMARY KEY ("id" AUTOINCREMENT)
);

CREATE TABLE IF NOT EXISTS houses
(
    id        INTEGER NOT NULL,
    owner     VARCHAR(64)   DEFAULT NULL,
    bed_block VARCHAR(64)   DEFAULT NULL,
    level     INT(11)       DEFAULT NULL,
    area      INT(11)       DEFAULT NULL,
    benefits  INT(11)       DEFAULT NULL,
    income    DECIMAL(5, 3) DEFAULT NULL,
    PRIMARY KEY ("id" AUTOINCREMENT)
);

CREATE TABLE IF NOT EXISTS house_blocks
(
    id   INTEGER NOT NULL,
    name VARCHAR(64) DEFAULT NULL,
    x    INT(11)     DEFAULT 0,
    y    INT(11)     DEFAULT 0,
    z    INT(11)     DEFAULT 0,
    PRIMARY KEY ("id" AUTOINCREMENT)
);

CREATE TABLE IF NOT EXISTS war_claims
(
    id         INTEGER NOT NULL,
    by_king    VARCHAR(64) DEFAULT NULL,
    chunk_name VARCHAR(64) DEFAULT NULL,
    PRIMARY KEY ("id" AUTOINCREMENT)
);

CREATE TABLE IF NOT EXISTS relations
(
    id         INTEGER NOT NULL,
    king       INT(11)     DEFAULT NULL,
    meaning_of VARCHAR(64) DEFAULT NULL,
    value      INT(11)     DEFAULT 50,
    PRIMARY KEY ("id" AUTOINCREMENT)
);

CREATE TABLE IF NOT EXISTS wars
(
    id         INTEGER NOT NULL,
    type       VARCHAR(64) DEFAULT NULL,
    atk        VARCHAR(64) DEFAULT NULL,
    def        VARCHAR(64) DEFAULT NULL,
    score      FLOAT(5)    DEFAULT 0,
    exhaustion INT(11)     DEFAULT NULL,
    start_time INT(11)     DEFAULT NULL,
    PRIMARY KEY ("id" AUTOINCREMENT)
);

CREATE TABLE IF NOT EXISTS war_helpers
(
    id     INTEGER NOT NULL,
    war_id INT(11)     DEFAULT NULL,
    atk    VARCHAR(64) DEFAULT NULL,
    helper VARCHAR(64) DEFAULT NULL,
    side   VARCHAR(64) DEFAULT NULL,
    PRIMARY KEY ("id" AUTOINCREMENT)
);

CREATE TABLE IF NOT EXISTS alliances
(
    id    INTEGER NOT NULL,
    king1 VARCHAR(64) DEFAULT NULL,
    king2 VARCHAR(64) DEFAULT NULL,
    PRIMARY KEY ("id" AUTOINCREMENT)
);

CREATE TABLE IF NOT EXISTS kingdom_helpers
(
    id      INTEGER NOT NULL,
    name    VARCHAR(64) DEFAULT NULL,
    of_king VARCHAR(64) DEFAULT NULL,
    PRIMARY KEY ("id" AUTOINCREMENT)
);

CREATE TABLE IF NOT EXISTS towns
(
    id           INTEGER NOT NULL,
    name         VARCHAR(64) DEFAULT NULL,
    owner        VARCHAR(64) DEFAULT NULL,
    house_number INT(11)     DEFAULT 0,
    level        INT(11)     DEFAULT 50,
    PRIMARY KEY ("id" AUTOINCREMENT)
);

CREATE TABLE IF NOT EXISTS deferred_events
(
    id           INTEGER     NOT NULL,
    affects_uuid VARCHAR(64) NOT NULL,
    issued_at    DATETIME    NOT NULL,
    type   VARCHAR(64) NOT NULL,
    PRIMARY KEY ("id" AUTOINCREMENT)
)