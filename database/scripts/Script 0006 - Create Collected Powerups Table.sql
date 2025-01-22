CREATE TABLE `collected_powerups` (
    `unique_id` bigint(20) NOT NULL AUTO_INCREMENT,
    `user_id` varchar(32) NOT NULL,
    `powerup_id` tinytext NOT NULL,
    `amount` bigint(20) NOT NULL DEFAULT 0,
    `highest_amount` bigint(20) NOT NULL DEFAULT 0,
    `amount_used` bigint(20) NOT NULL DEFAULT 0,
    PRIMARY KEY (`unique_id`),
    KEY `FOREIGN_KEY_POWERUP_USER` (`user_id`),
    CONSTRAINT `FOREIGN_KEY_POWERUP_USER` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
);