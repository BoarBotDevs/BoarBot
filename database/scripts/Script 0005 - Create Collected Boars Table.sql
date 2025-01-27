CREATE TABLE `collected_boars` (
    `user_id` varchar(32) DEFAULT NULL,
    `boar_id` varchar(64) NOT NULL,
    `edition` bigint(20) NOT NULL DEFAULT 1,
    `obtained_timestamp` timestamp(3) NOT NULL DEFAULT current_timestamp(3),
    `exists` tinyint(1) NOT NULL DEFAULT 1,
    `original_obtain_type` varchar(32) NOT NULL DEFAULT 'OTHER',
    `deleted` tinyint(1) NOT NULL DEFAULT 0,
    `bucks_gotten` int(11) NOT NULL DEFAULT 0,
    KEY `FOREIGN_KEY_BOAR_USER` (`user_id`),
    KEY `BOAR_ID` (`boar_id`),
    KEY `BOAR_EDITION` (`edition` DESC),
    CONSTRAINT `FOREIGN_KEY_BOAR_USER` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
);