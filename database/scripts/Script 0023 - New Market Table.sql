DROP TABLE market_values;
DROP TABLE market_editions;

CREATE TABLE `market` (
    `item_id` varchar(64) NOT NULL,
    `edition` bigint(20),
    `price` bigint(20) NOT NULL,
    `amount` bigint(20),
    `listed_timestamp` timestamp(3) NOT NULL DEFAULT current_timestamp(3),
    `user_id` varchar(32) NOT NULL
);