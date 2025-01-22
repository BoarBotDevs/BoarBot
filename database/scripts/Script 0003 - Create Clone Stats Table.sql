CREATE TABLE `clone_stats` (
    `user_id` varchar(32) NOT NULL,
    `rarity_id` varchar(64) NOT NULL,
    `amount` bigint(20) NOT NULL DEFAULT 0,
    KEY `FOREIGN_KEY_CLONE_USER` (`user_id`),
    CONSTRAINT `FOREIGN_KEY_CLONE_USER` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
);