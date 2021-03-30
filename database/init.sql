﻿CREATE TABLE IF NOT EXISTS user_claims (
   id INT(11) NOT NULL AUTO_INCREMENT,
   name VARCHAR(64) DEFAULT NULL,
   chunk_x INT(11) DEFAULT NULL,
   chunk_y INT(11) DEFAULT NULL,
   owner VARCHAR(64) DEFAULT NULL,
   type ENUM('Default', 'Home', 'Town'),
   PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS kings(
    id INT(11) NOT NULL AUTO_INCREMENT,
    name VARCHAR(64) DEFAULT NULL,
    kingdom_name VARCHAR(64) DEFAULT NULL,
    home_chunk VARCHAR(64) DEFAULT NULL,
    kingdom_level INT(11) DEFAULT NULL,
    chunk_number INT(11) DEFAULT NULL,
    PRIMARY KEY (id)
)