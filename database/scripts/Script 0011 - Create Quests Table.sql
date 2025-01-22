CREATE TABLE `quests` (
    `quest_start_timestamp` timestamp(3) NOT NULL DEFAULT current_timestamp(3),
    `quest_one_id` tinytext NOT NULL,
    `quest_two_id` tinytext NOT NULL,
    `quest_three_id` tinytext NOT NULL,
    `quest_four_id` tinytext NOT NULL,
    `quest_five_id` tinytext NOT NULL,
    `quest_six_id` tinytext NOT NULL,
    `quest_seven_id` tinytext NOT NULL,
    PRIMARY KEY (`quest_start_timestamp`)
);