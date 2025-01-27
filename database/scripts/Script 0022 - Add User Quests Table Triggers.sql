CREATE TRIGGER user_quest_update_trigger
    BEFORE UPDATE
    ON user_quests
    FOR EACH ROW
BEGIN
    IF OLD.one_claimed < NEW.one_claimed THEN
        SET NEW.easy_completed = NEW.easy_completed + 1, NEW.num_completed = NEW.num_completed + 1;
    END IF;

    IF OLD.two_claimed < NEW.two_claimed THEN
        SET NEW.easy_completed = NEW.easy_completed + 1, NEW.num_completed = NEW.num_completed + 1;
    END IF;

    IF OLD.three_claimed < NEW.three_claimed THEN
        SET NEW.medium_completed = NEW.medium_completed + 1, NEW.num_completed = NEW.num_completed + 1;
    END IF;

    IF OLD.four_claimed < NEW.four_claimed THEN
        SET NEW.medium_completed = NEW.medium_completed + 1, NEW.num_completed = NEW.num_completed + 1;
    END IF;

    IF OLD.five_claimed < NEW.five_claimed THEN
        SET NEW.hard_completed = NEW.hard_completed + 1, NEW.num_completed = NEW.num_completed + 1;
    END IF;

    IF OLD.six_claimed < NEW.six_claimed THEN
        SET NEW.hard_completed = NEW.hard_completed + 1, NEW.num_completed = NEW.num_completed + 1;
    END IF;

    IF OLD.seven_claimed < NEW.seven_claimed THEN
        SET NEW.very_hard_completed = NEW.very_hard_completed + 1, NEW.num_completed = NEW.num_completed + 1;
    END IF;

    IF OLD.full_claimed < NEW.full_claimed THEN
        SET NEW.num_full_completed = NEW.num_full_completed + 1;
    END IF;

    UPDATE users
    SET
        quest_bless = LEAST(
            FLOOR(
                (
                    NEW.one_claimed + NEW.two_claimed + NEW.three_claimed + NEW.four_claimed + NEW.five_claimed +
                    NEW.six_claimed + NEW.seven_claimed
                ) / CAST(7 AS DECIMAL) * 250
            ),
            250
        )
    WHERE user_id = OLD.user_id;
END;