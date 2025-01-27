CREATE TRIGGER boar_main_updater
    BEFORE INSERT
    ON collected_boars
    FOR EACH ROW
BEGIN
    SET NEW.edition = (
        SELECT IFNULL(MAX(edition), 0) + 1
        FROM collected_boars
        WHERE boar_id = NEW.boar_id
    );
END;

CREATE TRIGGER boar_update_user_before_insert
    BEFORE INSERT
    ON collected_boars
    FOR EACH ROW
BEGIN
    IF NEW.original_obtain_type = 'DAILY' OR NEW.original_obtain_type = 'EXTRA' THEN
        SET NEW.bucks_gotten = (
            SELECT ROUND(base_bucks * (RAND() * (1.1 - 0.9) + 0.9))
            FROM rarities_info, boars_info
            WHERE NEW.boar_id = boars_info.boar_id AND boars_info.rarity_id = rarities_info.rarity_id
        );
    END IF;

    IF NEW.original_obtain_type = 'TRANSMUTE' THEN
        INSERT INTO transmute_stats (user_id, rarity_id)
        SELECT NEW.user_id, (
            SELECT rarities_info.prior_rarity_id
            FROM boars_info, rarities_info
            WHERE NEW.boar_id = boars_info.boar_id AND boars_info.rarity_id = rarities_info.rarity_id
        )
        WHERE NOT EXISTS (
            SELECT 1
            FROM transmute_stats
            WHERE user_id = NEW.user_id AND rarity_id = (
                SELECT rarities_info.prior_rarity_id
                FROM boars_info, rarities_info
                WHERE NEW.boar_id = boars_info.boar_id AND boars_info.rarity_id = rarities_info.rarity_id
            )
        );

        UPDATE transmute_stats
        SET amount = amount + 1
        WHERE user_id = NEW.user_id AND rarity_id = (
            SELECT rarities_info.prior_rarity_id
            FROM boars_info, rarities_info
            WHERE NEW.boar_id = boars_info.boar_id AND boars_info.rarity_id = rarities_info.rarity_id
        );
    END IF;

    IF NEW.original_obtain_type = 'CLONE' THEN
        INSERT INTO clone_stats (user_id, rarity_id)
        SELECT NEW.user_id, (
            SELECT boars_info.rarity_id
            FROM boars_info
            WHERE NEW.boar_id = boars_info.boar_id
        )
        WHERE NOT EXISTS (
            SELECT 1
            FROM clone_stats
            WHERE user_id = NEW.user_id AND rarity_id = (
                SELECT boars_info.rarity_id
                FROM boars_info
                WHERE NEW.boar_id = boars_info.boar_id
            )
        );

        UPDATE clone_stats
        SET amount = amount + 1
        WHERE user_id = NEW.user_id AND rarity_id = (
            SELECT boars_info.rarity_id
            FROM boars_info
            WHERE NEW.boar_id = boars_info.boar_id
        );
    END IF;
END;

CREATE TRIGGER boar_update_user_after_insert
    AFTER INSERT
    ON collected_boars
    FOR EACH ROW
BEGIN
    IF NEW.original_obtain_type = 'DAILY' THEN
        UPDATE users
        SET last_boar_id = NEW.boar_id,
            total_boars = total_boars + 1,
            last_streak_fix = null,
            num_dailies = num_dailies + 1,
            total_bucks = total_bucks + NEW.bucks_gotten,
            last_daily_timestamp = current_timestamp(3),
            streak_frozen = 0,
            cur_dailies_missed = 0
        WHERE user_id = NEW.user_id;
    ELSEIF NEW.original_obtain_type = 'EXTRA' THEN
        UPDATE users
        SET last_boar_id = NEW.boar_id,
            total_boars = total_boars + 1,
            total_bucks = total_bucks + NEW.bucks_gotten
        WHERE user_id = NEW.user_id;
    ELSE
        UPDATE users
        SET last_boar_id = NEW.boar_id, total_boars = total_boars + 1
        WHERE user_id = NEW.user_id;
    END IF;
END;

CREATE TRIGGER boar_update_user_after_update
    AFTER UPDATE
    ON collected_boars
    FOR EACH ROW
BEGIN
    IF NEW.user_id IS NOT NULL AND (
        OLD.`exists` = true AND NEW.`exists` = false AND OLD.deleted = false OR
        OLD.deleted = false AND NEW.deleted = true AND OLD.`exists` = true
    ) THEN
        UPDATE users
        SET total_boars = total_boars - 1
        WHERE user_id = NEW.user_id;

        UPDATE users
        SET favorite_boar_id = null
        WHERE user_id = NEW.user_id AND favorite_boar_id = NEW.boar_id;
    END IF;

    IF NEW.user_id IS NOT NULL AND (
        OLD.`exists` = false AND NEW.`exists` = true AND OLD.deleted = false OR
        OLD.deleted = true AND NEW.deleted = false AND OLD.`exists` = true
    ) THEN
        UPDATE users
        SET total_boars = total_boars + 1, last_boar_id = NEW.boar_id
        WHERE user_id = NEW.user_id;
    END IF;
END;