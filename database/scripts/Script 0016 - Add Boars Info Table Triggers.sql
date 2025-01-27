CREATE TRIGGER handle_boar_insert
    AFTER INSERT
    ON boars_info
    FOR EACH ROW
BEGIN
    UPDATE collected_boars
    SET `exists` = true
    WHERE NEW.boar_id = collected_boars.boar_id AND `exists` = false;
END;

CREATE TRIGGER handle_boar_delete
    BEFORE DELETE
    ON boars_info
    FOR EACH ROW
BEGIN
    UPDATE collected_boars
    SET `exists` = false
    WHERE OLD.boar_id = boar_id;

    DELETE FROM market_values
    WHERE item_id = OLD.boar_id;

    DELETE FROM market_editions
    WHERE item_id = OLD.boar_id;
END;