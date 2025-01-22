CREATE TABLE `boars_info` (
    `boar_id` varchar(64) NOT NULL,
    `rarity_id` varchar(64) NOT NULL,
    `is_skyblock` tinyint(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`boar_id`)
);