CREATE TRIGGER handle_rarity_delete
    BEFORE DELETE
    ON rarities_info
    FOR EACH ROW
BEGIN
    DELETE FROM boars_info
    WHERE OLD.rarity_id = boars_info.rarity_id;
END;