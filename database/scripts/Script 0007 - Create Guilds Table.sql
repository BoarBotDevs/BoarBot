CREATE TABLE `guilds` (
    `guild_id` varchar(32) NOT NULL,
    `is_skyblock_community` tinyint(1) NOT NULL DEFAULT 0,
    `channel_one` varchar(32) DEFAULT NULL,
    `channel_two` varchar(32) DEFAULT NULL,
    `channel_three` varchar(32) DEFAULT NULL,
    `powerup_message_one` varchar(32) DEFAULT NULL,
    `powerup_message_two` varchar(32) DEFAULT NULL,
    `powerup_message_three` varchar(32) DEFAULT NULL,
    `event_notify_flag` tinyint(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`guild_id`)
);