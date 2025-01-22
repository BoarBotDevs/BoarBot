CREATE TABLE `market_values` (
    `item_id` varchar(32) NOT NULL,
    `stock` int(11) NOT NULL DEFAULT 0,
    `sell_price` bigint(20) NOT NULL DEFAULT 1,
    `buy_price` bigint(20) NOT NULL DEFAULT 2,
    `last_purchase` timestamp(3) NULL DEFAULT NULL,
    `last_sell` timestamp(3) NULL DEFAULT NULL,
    PRIMARY KEY (`item_id`)
);