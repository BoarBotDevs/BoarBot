CREATE TRIGGER quest_insert_trigger
    AFTER INSERT
    ON quests
    FOR EACH ROW
BEGIN
    UPDATE user_quests
    SET
        one_progress = 0,
        one_claimed = 0,
        two_progress = 0,
        two_claimed = 0,
        three_progress = 0,
        three_claimed = 0,
        four_progress = 0,
        four_claimed = 0,
        five_progress = 0,
        five_claimed = 0,
        six_progress = 0,
        six_claimed = 0,
        seven_progress = 0,
        seven_claimed = 0,
        full_claimed = 0;

    UPDATE users
    SET quest_bless = 0;
END;