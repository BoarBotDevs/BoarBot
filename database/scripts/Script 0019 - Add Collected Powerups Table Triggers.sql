CREATE TRIGGER powerups_main_updater
    BEFORE UPDATE
    ON collected_powerups
    FOR EACH ROW
BEGIN
    IF NEW.amount > OLD.highest_amount THEN
        SET NEW.highest_amount = NEW.amount;
    END IF;

    UPDATE users
    SET last_changed_timestamp = current_timestamp(3)
    WHERE user_id = OLD.user_id;
END;