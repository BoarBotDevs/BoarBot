CREATE TRIGGER handle_user_insert
    AFTER INSERT
    ON users
    FOR EACH ROW
BEGIN
    INSERT INTO user_quests (user_id)
    VALUES (NEW.user_id);
END;

CREATE TRIGGER user_main_updater
    BEFORE UPDATE
    ON users
    FOR EACH ROW
BEGIN
    DECLARE force_uniques_update TINYINT(1);

    SET force_uniques_update = OLD.unique_boars != NEW.unique_boars;

    SET NEW.last_changed_timestamp = current_timestamp(3);

    IF OLD.num_dailies < NEW.num_dailies THEN
        SET NEW.boar_streak = OLD.boar_streak + 1;
    END IF;

    IF NEW.boar_streak > OLD.highest_streak THEN
        SET NEW.highest_streak = NEW.boar_streak;
    END IF;

    IF NEW.total_boars > OLD.highest_boars THEN
        SET NEW.highest_boars = NEW.total_boars;
    END IF;

    IF NEW.total_bucks > OLD.highest_bucks THEN
        SET NEW.highest_bucks = NEW.total_bucks;
    END IF;

    IF NEW.miracles_active > OLD.highest_miracles_active THEN
        SET NEW.highest_miracles_active = NEW.miracles_active;
    END IF;

    SET NEW.unique_boars = (
        SELECT COUNT(DISTINCT boar_id)
        FROM collected_boars
        WHERE OLD.user_id = user_id AND `exists` = true AND deleted = false
    );

    SET NEW.streak_bless = LEAST(NEW.boar_streak, 250);

    IF OLD.unique_boars != NEW.unique_boars OR force_uniques_update THEN
        SET NEW.num_skyblock = (
            SELECT COUNT(DISTINCT collected_boars.boar_id)
            FROM collected_boars, boars_info
            WHERE
                OLD.user_id = collected_boars.user_id AND
                collected_boars.`exists` = true AND
                collected_boars.deleted = false AND
                collected_boars.boar_id = boars_info.boar_id AND
                boars_info.is_skyblock = true
        );

        SET NEW.num_non_researcher = (
            SELECT COUNT(DISTINCT collected_boars.boar_id)
            FROM collected_boars, boars_info, rarities_info
            WHERE
                OLD.user_id = collected_boars.user_id AND
                collected_boars.`exists` = true AND
                collected_boars.deleted = false AND
                collected_boars.boar_id = boars_info.boar_id AND
                boars_info.rarity_id = rarities_info.rarity_id AND
                rarities_info.researcher_need = false
        );

        IF NEW.num_skyblock > 0 THEN
            SET NEW.unique_bless = LEAST(
                FLOOR(
                    (NEW.unique_boars - NEW.num_non_researcher) / CAST(
                        GREATEST(
                            CAST(
                                (
                                    SELECT COUNT(*)
                                    FROM boars_info, rarities_info
                                    WHERE
                                        boars_info.rarity_id = rarities_info.rarity_id AND
                                        rarities_info.researcher_need = true
                                ) AS DECIMAL
                            ),
                            1
                        ) AS DECIMAL
                    ) * 250
                ),
                250
            );

            IF NEW.unique_boars = (
                SELECT COUNT(*)
                FROM boars_info, rarities_info
                WHERE boars_info.rarity_id = rarities_info.rarity_id
            ) THEN
                INSERT INTO collected_badges (user_id, badge_id, first_obtained_timestamp, update_user)
                SELECT NEW.user_id, 'researcher', current_timestamp(3), false
                WHERE NOT EXISTS (
                    SELECT 1
                    FROM collected_badges
                    WHERE collected_badges.user_id = NEW.user_id AND badge_id = 'researcher'
                );

                UPDATE collected_badges
                SET badge_tier = 1, obtained_timestamp = current_timestamp(3), update_user = false
                WHERE user_id = NEW.user_id AND badge_id = 'researcher' AND badge_tier != 1;
            ELSEIF (NEW.unique_boars - NEW.num_non_researcher) = (
                SELECT COUNT(*)
                FROM boars_info, rarities_info
                WHERE boars_info.rarity_id = rarities_info.rarity_id AND rarities_info.researcher_need = true
            ) THEN
                INSERT INTO collected_badges (user_id, badge_id, first_obtained_timestamp, update_user)
                SELECT NEW.user_id, 'researcher', current_timestamp(3), false
                WHERE NOT EXISTS (
                    SELECT 1
                    FROM collected_badges
                    WHERE collected_badges.user_id = NEW.user_id AND badge_id = 'researcher'
                );

                UPDATE collected_badges
                SET badge_tier = 0, obtained_timestamp = current_timestamp(3), update_user = false
                WHERE user_id = NEW.user_id AND badge_id = 'researcher' AND badge_tier != 0;
            ELSE
                UPDATE collected_badges
                SET badge_tier = -1, update_user = false
                WHERE collected_badges.user_id = NEW.user_id AND badge_id = 'researcher' AND badge_tier != -1;
            END IF;
        ELSE
            SET NEW.unique_bless = LEAST(
                FLOOR(
                    (NEW.unique_boars - NEW.num_non_researcher) / CAST(
                        GREATEST(
                            CAST(
                                (
                                    SELECT COUNT(*)
                                    FROM boars_info, rarities_info
                                    WHERE
                                        boars_info.rarity_id = rarities_info.rarity_id AND
                                        rarities_info.researcher_need = true AND
                                        boars_info.is_skyblock = false
                                ) AS DECIMAL
                            ),
                            1
                        ) AS DECIMAL
                    ) * 250
                ),
                250
            );

            IF NEW.unique_boars = (
                SELECT COUNT(*)
                FROM boars_info, rarities_info
                WHERE boars_info.rarity_id = rarities_info.rarity_id
            ) THEN
                INSERT INTO collected_badges (user_id, badge_id, first_obtained_timestamp, update_user)
                SELECT NEW.user_id, 'researcher', current_timestamp(3), false
                WHERE NOT EXISTS (
                    SELECT 1
                    FROM collected_badges
                    WHERE collected_badges.user_id = NEW.user_id AND badge_id = 'researcher'
                );

                UPDATE collected_badges
                SET badge_tier = 1, obtained_timestamp = current_timestamp(3), update_user = false
                WHERE user_id = NEW.user_id AND badge_id = 'researcher' AND badge_tier != 1;
            ELSEIF (NEW.unique_boars - NEW.num_non_researcher) = (
                SELECT COUNT(*)
                FROM boars_info, rarities_info
                WHERE boars_info.rarity_id = rarities_info.rarity_id AND rarities_info.researcher_need = true
            ) THEN
                INSERT INTO collected_badges (user_id, badge_id, first_obtained_timestamp, update_user)
                SELECT NEW.user_id, 'researcher', current_timestamp(3), false
                WHERE NOT EXISTS (
                    SELECT 1
                    FROM collected_badges
                    WHERE collected_badges.user_id = NEW.user_id AND badge_id = 'researcher'
                );

                UPDATE collected_badges
                SET badge_tier = 0, obtained_timestamp = current_timestamp(3), update_user = false
                WHERE user_id = NEW.user_id AND badge_id = 'researcher' AND badge_tier != 0;
            ELSE
                UPDATE collected_badges
                SET badge_tier = -1, update_user = false
                WHERE collected_badges.user_id = NEW.user_id AND badge_id = 'researcher' AND badge_tier != -1;
            END IF;
        END IF;

        IF NEW.unique_boars > OLD.highest_unique_boars THEN
            SET NEW.highest_unique_boars = NEW.unique_boars;
        END IF;

        IF NEW.unique_bless > OLD.highest_unique_bless THEN
            SET NEW.highest_unique_bless = NEW.unique_bless;
        END IF;
    END IF;

    SET NEW.blessings = NEW.streak_bless + NEW.quest_bless + NEW.unique_bless + NEW.other_bless;

    IF NEW.blessings > OLD.highest_blessings THEN
        SET NEW.highest_blessings = NEW.blessings;
    END IF;

    IF NEW.streak_bless > OLD.highest_streak_bless THEN
        SET NEW.highest_streak_bless = NEW.streak_bless;
    END IF;

    IF NEW.quest_bless > OLD.highest_quest_bless THEN
        SET NEW.highest_quest_bless = NEW.quest_bless;
    END IF;

    IF NEW.other_bless > OLD.highest_other_bless THEN
        SET NEW.highest_other_bless = NEW.other_bless;
    END IF;
END;

CREATE TRIGGER handle_user_delete
    BEFORE DELETE
    ON users
    FOR EACH ROW
BEGIN
    UPDATE collected_boars
    SET deleted = true, user_id = null
    WHERE OLD.user_id = user_id;
END;