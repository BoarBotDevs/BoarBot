CREATE TABLE `prompt_stats` (
    `user_id` varchar(64) NOT NULL,
    `prompt_id` varchar(64) NOT NULL,
    `average_placement` double NOT NULL DEFAULT 1,
    `wins` int(11) NOT NULL DEFAULT 0,
    KEY `FOREIGN_KEY_PROMPT_USER` (`user_id`),
    CONSTRAINT `FOREIGN_KEY_PROMPT_USER` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
);