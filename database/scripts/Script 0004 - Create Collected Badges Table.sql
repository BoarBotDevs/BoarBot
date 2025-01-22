CREATE TABLE `collected_badges` (
    `user_id` varchar(32) NOT NULL,
    `badge_id` varchar(64) NOT NULL,
    `badge_tier` int(11) NOT NULL DEFAULT 0,
    `obtained_timestamp` timestamp(3) NOT NULL DEFAULT current_timestamp(3),
    `first_obtained_timestamp` timestamp(3) NOT NULL DEFAULT current_timestamp(3),
    `exists` tinyint(1) NOT NULL DEFAULT 1,
    `update_user` tinyint(1) NOT NULL DEFAULT 1,
    KEY `FOREIGN_KEY_BADGE_USER` (`user_id`),
    CONSTRAINT `FOREIGN_KEY_BADGE_USER` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
);