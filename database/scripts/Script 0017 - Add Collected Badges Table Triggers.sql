CREATE TRIGGER badge_insert
    BEFORE INSERT
    ON collected_badges
    FOR EACH ROW
BEGIN
    IF NEW.update_user THEN
        UPDATE users
        SET last_changed_timestamp = current_timestamp(3)
        WHERE user_id = NEW.user_id;
    END IF;

    SET NEW.update_user = true;
END;

CREATE TRIGGER badge_update
    BEFORE UPDATE
    ON collected_badges
    FOR EACH ROW
BEGIN
    IF NEW.update_user THEN
        UPDATE users
        SET last_changed_timestamp = current_timestamp(3)
        WHERE user_id = NEW.user_id;
    END IF;

    SET NEW.update_user = true;
END;