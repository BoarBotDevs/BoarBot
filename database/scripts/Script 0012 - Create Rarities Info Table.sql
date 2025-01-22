CREATE TABLE `rarities_info` (
    `rarity_id` varchar(64) NOT NULL,
    `prior_rarity_id` varchar(64) DEFAULT NULL,
    `base_bucks` bigint(20) NOT NULL DEFAULT 0,
    `researcher_need` tinyint(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`rarity_id`)
);