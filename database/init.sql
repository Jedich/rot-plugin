CREATE TABLE IF NOT EXISTS rotr.user_claims
(
    id      INT(11) NOT NULL AUTO_INCREMENT,
    name    VARCHAR(64) DEFAULT NULL,
    chunk_x INT(11)     DEFAULT NULL,
    chunk_y INT(11)     DEFAULT NULL,
    owner   VARCHAR(64) DEFAULT NULL,
    type    ENUM ('Default', 'Home', 'Town'),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS rotr.kings
(
    id            INT(11) NOT NULL AUTO_INCREMENT,
    name          VARCHAR(64)   DEFAULT NULL,
    title         VARCHAR(64)   DEFAULT NULL,
    kingdom_name  VARCHAR(64)   DEFAULT NULL,
    home_chunk    VARCHAR(64)   DEFAULT NULL,
    kingdom_level INT(11)       DEFAULT NULL,
    current_gen   INT(11)       DEFAULT NULL,
    balance       DECIMAL(9, 3) DEFAULT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS rotr.houses
(
    id        INT(11) NOT NULL AUTO_INCREMENT,
    owner     VARCHAR(64)   DEFAULT NULL,
    bed_block VARCHAR(64)   DEFAULT NULL,
    level     INT(11)       DEFAULT NULL,
    area      INT(11)       DEFAULT NULL,
    benefits  INT(11)       DEFAULT NULL,
    income    DECIMAL(5, 3) DEFAULT NULL,
    PRIMARY KEY (id)
)