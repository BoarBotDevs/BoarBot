/*M!999999\- enable the sandbox mode */ 
-- MariaDB dump 10.19-11.5.2-MariaDB, for Linux (x86_64)
--
-- Host: localhost    Database: boarbot
-- ------------------------------------------------------
-- Server version	11.5.2-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*M!100616 SET @OLD_NOTE_VERBOSITY=@@NOTE_VERBOSITY, NOTE_VERBOSITY=0 */;

--
-- Table structure for table `boars_info`
--

DROP TABLE IF EXISTS `boars_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `boars_info` (
  `boar_id` varchar(64) NOT NULL,
  `rarity_id` varchar(64) NOT NULL,
  `is_skyblock` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`boar_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `boars_info`
--

LOCK TABLES `boars_info` WRITE;
/*!40000 ALTER TABLE `boars_info` DISABLE KEYS */;
/*!40000 ALTER TABLE `boars_info` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_unicode_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'IGNORE_SPACE,STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`default`@`localhost`*/ /*!50003 trigger handle_boar_insert
    after insert
    on boars_info
    for each row
begin
    UPDATE collected_boars
    SET `exists` = true
    WHERE NEW.boar_id = collected_boars.boar_id AND `exists` = false;
end */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_unicode_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'IGNORE_SPACE,STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`default`@`localhost`*/ /*!50003 trigger handle_boar_delete
    before delete
    on boars_info
    for each row
begin
    UPDATE collected_boars
    SET `exists` = false
    WHERE OLD.boar_id = boar_id;
    
    DELETE FROM market_values
    WHERE item_id = OLD.boar_id;
    
    DELETE FROM market_editions
    WHERE item_id = OLD.boar_id;
end */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `clone_stats`
--

DROP TABLE IF EXISTS `clone_stats`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `clone_stats` (
  `user_id` varchar(32) NOT NULL,
  `rarity_id` varchar(64) NOT NULL,
  `amount` bigint(20) NOT NULL DEFAULT 0,
  KEY `FOREIGN_KEY_CLONE_USER` (`user_id`),
  CONSTRAINT `FOREIGN_KEY_CLONE_USER` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `positive_amount` CHECK (`amount` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `clone_stats`
--

LOCK TABLES `clone_stats` WRITE;
/*!40000 ALTER TABLE `clone_stats` DISABLE KEYS */;
/*!40000 ALTER TABLE `clone_stats` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `collected_badges`
--

DROP TABLE IF EXISTS `collected_badges`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `collected_badges` (
  `user_id` varchar(32) NOT NULL,
  `badge_id` varchar(64) NOT NULL,
  `badge_tier` int(11) NOT NULL DEFAULT 0,
  `obtained_timestamp` timestamp(3) NOT NULL DEFAULT current_timestamp(3),
  `first_obtained_timestamp` timestamp(3) NOT NULL DEFAULT current_timestamp(3),
  `exists` tinyint(1) NOT NULL DEFAULT 1,
  `update_user` tinyint(1) NOT NULL DEFAULT 1,
  KEY `FOREIGN_KEY_BADGE_USER` (`user_id`),
  CONSTRAINT `FOREIGN_KEY_BADGE_USER` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `collected_badges`
--

LOCK TABLES `collected_badges` WRITE;
/*!40000 ALTER TABLE `collected_badges` DISABLE KEYS */;
/*!40000 ALTER TABLE `collected_badges` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_unicode_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'IGNORE_SPACE,STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`default`@`localhost`*/ /*!50003 trigger badge_insert
    before insert
    on collected_badges
    for each row
begin
    IF NEW.update_user THEN
        UPDATE users
        SET last_changed_timestamp = current_timestamp(3)
        WHERE user_id = NEW.user_id;
    end if;

    SET NEW.update_user = true;
end */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_unicode_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'IGNORE_SPACE,STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`default`@`localhost`*/ /*!50003 trigger badge_update
    before update
    on collected_badges
    for each row
begin
    IF NEW.update_user THEN
        UPDATE users
        SET last_changed_timestamp = current_timestamp(3)
        WHERE user_id = NEW.user_id;
    end if;

    SET NEW.update_user = true;
end */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `collected_boars`
--

DROP TABLE IF EXISTS `collected_boars`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `collected_boars` (
  `user_id` varchar(32) DEFAULT NULL,
  `boar_id` varchar(64) NOT NULL,
  `edition` bigint(20) NOT NULL DEFAULT 1,
  `obtained_timestamp` timestamp(3) NOT NULL DEFAULT current_timestamp(3),
  `exists` tinyint(1) NOT NULL DEFAULT 1,
  `original_obtain_type` varchar(32) NOT NULL DEFAULT 'OTHER',
  `deleted` tinyint(1) NOT NULL DEFAULT 0,
  `bucks_gotten` int(11) NOT NULL DEFAULT 0,
  KEY `FOREIGN_KEY_BOAR_USER` (`user_id`),
  KEY `BOAR_ID` (`boar_id`),
  KEY `BOAR_EDITION` (`edition` DESC),
  CONSTRAINT `FOREIGN_KEY_BOAR_USER` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `positive_edition` CHECK (`edition` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `collected_boars`
--

LOCK TABLES `collected_boars` WRITE;
/*!40000 ALTER TABLE `collected_boars` DISABLE KEYS */;
/*!40000 ALTER TABLE `collected_boars` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'IGNORE_SPACE,STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`default`@`localhost`*/ /*!50003 trigger boar_main_updater
    before insert
    on collected_boars
    for each row
begin
    SET NEW.edition = (
        SELECT IFNULL(MAX(edition), 0) + 1
        FROM collected_boars
        WHERE boar_id = NEW.boar_id
    );
end */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_unicode_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'IGNORE_SPACE,STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`default`@`localhost`*/ /*!50003 trigger boar_update_user_before_insert
    before insert
    on collected_boars
    for each row
begin
    IF NEW.original_obtain_type = 'DAILY' OR NEW.original_obtain_type = 'EXTRA' THEN
        SET NEW.bucks_gotten = (
            SELECT ROUND(base_bucks * (RAND() * (1.1 - 0.9) + 0.9))
            FROM rarities_info, boars_info
            WHERE NEW.boar_id = boars_info.boar_id AND boars_info.rarity_id = rarities_info.rarity_id
        );
    end if;

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
    end if;

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
    end if;
end */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_unicode_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'IGNORE_SPACE,STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`default`@`localhost`*/ /*!50003 trigger boar_update_user_after_insert
    after insert
    on collected_boars
    for each row
begin
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
    end if;
end */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_unicode_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'IGNORE_SPACE,STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`default`@`localhost`*/ /*!50003 trigger boar_update_user_after_update
    after update
    on collected_boars
    for each row
begin
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
    end if;

    IF NEW.user_id IS NOT NULL AND (
        OLD.`exists` = false AND NEW.`exists` = true AND OLD.deleted = false OR 
        OLD.deleted = true AND NEW.deleted = false AND OLD.`exists` = true
    ) THEN
        UPDATE users
        SET total_boars = total_boars + 1, last_boar_id = NEW.boar_id
        WHERE user_id = NEW.user_id;
    end if;
end */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `collected_powerups`
--

DROP TABLE IF EXISTS `collected_powerups`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `collected_powerups` (
  `unique_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` varchar(32) NOT NULL,
  `powerup_id` tinytext NOT NULL,
  `amount` bigint(20) NOT NULL DEFAULT 0,
  `highest_amount` bigint(20) NOT NULL DEFAULT 0,
  `amount_used` bigint(20) NOT NULL DEFAULT 0,
  PRIMARY KEY (`unique_id`),
  KEY `FOREIGN_KEY_POWERUP_USER` (`user_id`),
  CONSTRAINT `FOREIGN_KEY_POWERUP_USER` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `positive_amount` CHECK (`amount` >= 0),
  CONSTRAINT `positive_amount_used` CHECK (`amount_used` >= 0)
) ENGINE=InnoDB AUTO_INCREMENT=642673 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `collected_powerups`
--

LOCK TABLES `collected_powerups` WRITE;
/*!40000 ALTER TABLE `collected_powerups` DISABLE KEYS */;
/*!40000 ALTER TABLE `collected_powerups` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_unicode_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'IGNORE_SPACE,STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`default`@`localhost`*/ /*!50003 trigger powerups_main_updater
    before update
    on collected_powerups
    for each row
begin
    IF NEW.amount > OLD.highest_amount THEN
        SET NEW.highest_amount = NEW.amount;
    end if;

    UPDATE users
    SET last_changed_timestamp = current_timestamp(3)
    WHERE user_id = OLD.user_id;
end */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `guilds`
--

DROP TABLE IF EXISTS `guilds`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `guilds` (
  `guild_id` varchar(32) NOT NULL,
  `is_skyblock_community` tinyint(1) NOT NULL DEFAULT 0,
  `channel_one` varchar(32) DEFAULT NULL,
  `channel_two` varchar(32) DEFAULT NULL,
  `channel_three` varchar(32) DEFAULT NULL,
  `powerup_message_one` varchar(32) DEFAULT NULL,
  `powerup_message_two` varchar(32) DEFAULT NULL,
  `powerup_message_three` varchar(32) DEFAULT NULL,
  `event_notify_flag` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`guild_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `guilds`
--

LOCK TABLES `guilds` WRITE;
/*!40000 ALTER TABLE `guilds` DISABLE KEYS */;
/*!40000 ALTER TABLE `guilds` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `market_editions`
--

DROP TABLE IF EXISTS `market_editions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `market_editions` (
  `item_id` varchar(32) NOT NULL,
  `edition` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `market_editions`
--

LOCK TABLES `market_editions` WRITE;
/*!40000 ALTER TABLE `market_editions` DISABLE KEYS */;
/*!40000 ALTER TABLE `market_editions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `market_values`
--

DROP TABLE IF EXISTS `market_values`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `market_values` (
  `item_id` varchar(32) NOT NULL,
  `stock` int(11) NOT NULL DEFAULT 0,
  `sell_price` bigint(20) NOT NULL DEFAULT 1,
  `buy_price` bigint(20) NOT NULL DEFAULT 2,
  `last_purchase` timestamp(3) NULL DEFAULT NULL,
  `last_sell` timestamp(3) NULL DEFAULT NULL,
  PRIMARY KEY (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `market_values`
--

LOCK TABLES `market_values` WRITE;
/*!40000 ALTER TABLE `market_values` DISABLE KEYS */;
/*!40000 ALTER TABLE `market_values` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `prompt_stats`
--

DROP TABLE IF EXISTS `prompt_stats`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `prompt_stats` (
  `user_id` varchar(64) NOT NULL,
  `prompt_id` varchar(64) NOT NULL,
  `average_placement` double NOT NULL DEFAULT 1,
  `wins` int(11) NOT NULL DEFAULT 0,
  KEY `FOREIGN_KEY_PROMPT_USER` (`user_id`),
  CONSTRAINT `FOREIGN_KEY_PROMPT_USER` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `prompt_stats`
--

LOCK TABLES `prompt_stats` WRITE;
/*!40000 ALTER TABLE `prompt_stats` DISABLE KEYS */;
/*!40000 ALTER TABLE `prompt_stats` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `quests`
--

DROP TABLE IF EXISTS `quests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `quests`
--

LOCK TABLES `quests` WRITE;
/*!40000 ALTER TABLE `quests` DISABLE KEYS */;
/*!40000 ALTER TABLE `quests` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_unicode_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'IGNORE_SPACE,STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`default`@`localhost`*/ /*!50003 trigger quest_insert_trigger
    after insert
    on quests
    for each row
begin
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
end */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `rarities_info`
--

DROP TABLE IF EXISTS `rarities_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `rarities_info` (
  `rarity_id` varchar(64) NOT NULL,
  `prior_rarity_id` varchar(64) DEFAULT NULL,
  `base_bucks` bigint(20) NOT NULL DEFAULT 0,
  `researcher_need` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`rarity_id`),
  CONSTRAINT `positive_score` CHECK (`base_bucks` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rarities_info`
--

LOCK TABLES `rarities_info` WRITE;
/*!40000 ALTER TABLE `rarities_info` DISABLE KEYS */;
/*!40000 ALTER TABLE `rarities_info` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_unicode_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'IGNORE_SPACE,STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`default`@`localhost`*/ /*!50003 trigger handle_rarity_delete
    before delete
    on rarities_info
    for each row
begin
    DELETE FROM boars_info
    WHERE OLD.rarity_id = boars_info.rarity_id;
end */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `transmute_stats`
--

DROP TABLE IF EXISTS `transmute_stats`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `transmute_stats` (
  `user_id` varchar(32) NOT NULL,
  `rarity_id` varchar(64) NOT NULL,
  `amount` bigint(20) NOT NULL DEFAULT 0,
  KEY `FOREIGN_KEY_TRANSMUTE_USER` (`user_id`),
  CONSTRAINT `FOREIGN_KEY_TRANSMUTE_USER` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  CONSTRAINT `positive_amount` CHECK (`amount` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transmute_stats`
--

LOCK TABLES `transmute_stats` WRITE;
/*!40000 ALTER TABLE `transmute_stats` DISABLE KEYS */;
/*!40000 ALTER TABLE `transmute_stats` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_quests`
--

DROP TABLE IF EXISTS `user_quests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_quests` (
  `user_id` varchar(32) NOT NULL,
  `one_progress` int(11) NOT NULL DEFAULT 0,
  `one_claimed` tinyint(1) NOT NULL DEFAULT 0,
  `two_progress` int(11) NOT NULL DEFAULT 0,
  `two_claimed` tinyint(1) NOT NULL DEFAULT 0,
  `three_progress` int(11) NOT NULL DEFAULT 0,
  `three_claimed` tinyint(1) NOT NULL DEFAULT 0,
  `four_progress` int(11) NOT NULL DEFAULT 0,
  `four_claimed` tinyint(1) NOT NULL DEFAULT 0,
  `five_progress` int(11) NOT NULL DEFAULT 0,
  `five_claimed` tinyint(1) NOT NULL DEFAULT 0,
  `six_progress` int(11) NOT NULL DEFAULT 0,
  `six_claimed` tinyint(1) NOT NULL DEFAULT 0,
  `seven_progress` int(11) NOT NULL DEFAULT 0,
  `seven_claimed` tinyint(1) NOT NULL DEFAULT 0,
  `num_completed` int(11) NOT NULL DEFAULT 0,
  `num_full_completed` int(11) NOT NULL DEFAULT 0,
  `full_claimed` tinyint(1) NOT NULL DEFAULT 0,
  `fastest_full_millis` mediumtext NOT NULL DEFAULT 604800001,
  `easy_completed` int(11) NOT NULL DEFAULT 0,
  `medium_completed` int(11) NOT NULL DEFAULT 0,
  `hard_completed` int(11) NOT NULL DEFAULT 0,
  `very_hard_completed` int(11) NOT NULL DEFAULT 0,
  `auto_claim` tinyint(1) NOT NULL DEFAULT 1,
  KEY `USER_ID` (`user_id`),
  CONSTRAINT `USER_ID` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_quests`
--

LOCK TABLES `user_quests` WRITE;
/*!40000 ALTER TABLE `user_quests` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_quests` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_unicode_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'IGNORE_SPACE,STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`default`@`localhost`*/ /*!50003 trigger user_quest_update_trigger
    before update
    on user_quests
    for each row
begin
    IF OLD.one_claimed < NEW.one_claimed THEN
        SET NEW.easy_completed = NEW.easy_completed + 1, NEW.num_completed = NEW.num_completed + 1;
    end if;

    IF OLD.two_claimed < NEW.two_claimed THEN
        SET NEW.easy_completed = NEW.easy_completed + 1, NEW.num_completed = NEW.num_completed + 1;
    end if;

    IF OLD.three_claimed < NEW.three_claimed THEN
        SET NEW.medium_completed = NEW.medium_completed + 1, NEW.num_completed = NEW.num_completed + 1;
    end if;

    IF OLD.four_claimed < NEW.four_claimed THEN
        SET NEW.medium_completed = NEW.medium_completed + 1, NEW.num_completed = NEW.num_completed + 1;
    end if;

    IF OLD.five_claimed < NEW.five_claimed THEN
        SET NEW.hard_completed = NEW.hard_completed + 1, NEW.num_completed = NEW.num_completed + 1;
    end if;

    IF OLD.six_claimed < NEW.six_claimed THEN
        SET NEW.hard_completed = NEW.hard_completed + 1, NEW.num_completed = NEW.num_completed + 1;
    end if;

    IF OLD.seven_claimed < NEW.seven_claimed THEN
        SET NEW.very_hard_completed = NEW.very_hard_completed + 1, NEW.num_completed = NEW.num_completed + 1;
    end if;
    
    IF OLD.full_claimed < NEW.full_claimed THEN
        SET NEW.num_full_completed = NEW.num_full_completed + 1;
    end if;

    UPDATE users
    SET
        quest_bless = LEAST(
            FLOOR(
                (
                    NEW.one_claimed + NEW.two_claimed + NEW.three_claimed + NEW.four_claimed + NEW.five_claimed + 
                    NEW.six_claimed + NEW.seven_claimed
                ) / 
                CAST(7 AS DECIMAL) * 250
            ),
            250
        )
    WHERE user_id = OLD.user_id;
end */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `user_id` varchar(32) NOT NULL,
  `username` varchar(32) NOT NULL DEFAULT 'UNKNOWN',
  `last_daily_timestamp` timestamp(3) NULL DEFAULT NULL,
  `num_dailies` int(11) NOT NULL DEFAULT 0,
  `num_dailies_missed` int(11) NOT NULL DEFAULT 0,
  `cur_dailies_missed` int(11) NOT NULL DEFAULT 0,
  `last_streak_fix` timestamp(3) NULL DEFAULT NULL,
  `boar_streak` int(11) NOT NULL DEFAULT 0,
  `highest_streak` int(11) NOT NULL DEFAULT 0,
  `total_boars` bigint(20) NOT NULL DEFAULT 0,
  `highest_boars` int(11) NOT NULL DEFAULT 0,
  `total_bucks` bigint(20) NOT NULL DEFAULT 0,
  `highest_bucks` bigint(20) NOT NULL DEFAULT 0,
  `unique_boars` int(11) NOT NULL DEFAULT 0,
  `highest_unique_boars` int(11) NOT NULL DEFAULT 0,
  `num_skyblock` int(11) NOT NULL DEFAULT 0,
  `num_non_researcher` int(11) NOT NULL DEFAULT 0,
  `favorite_boar_id` varchar(64) DEFAULT NULL,
  `last_boar_id` varchar(64) DEFAULT NULL,
  `first_joined_timestamp` timestamp(3) NOT NULL DEFAULT current_timestamp(3),
  `blessings` bigint(20) NOT NULL DEFAULT 0,
  `highest_blessings` bigint(20) NOT NULL DEFAULT 0,
  `streak_bless` int(11) NOT NULL DEFAULT 0,
  `highest_streak_bless` int(11) NOT NULL DEFAULT 0,
  `quest_bless` int(11) NOT NULL DEFAULT 0,
  `highest_quest_bless` int(11) NOT NULL DEFAULT 0,
  `unique_bless` int(11) NOT NULL DEFAULT 0,
  `highest_unique_bless` int(11) NOT NULL DEFAULT 0,
  `other_bless` int(11) NOT NULL DEFAULT 0,
  `highest_other_bless` int(11) NOT NULL DEFAULT 0,
  `notifications_on` tinyint(1) NOT NULL DEFAULT 0,
  `notification_channel` varchar(32) DEFAULT NULL,
  `unban_timestamp` timestamp(3) NULL DEFAULT NULL,
  `wipe_timestamp` timestamp(3) NULL DEFAULT NULL,
  `powerup_attempts` int(11) NOT NULL DEFAULT 0,
  `powerup_wins` int(11) NOT NULL DEFAULT 0,
  `powerup_perfects` int(11) NOT NULL DEFAULT 0,
  `powerup_fastest_time` int(11) NOT NULL DEFAULT 120000,
  `gift_last_sent` timestamp(3) NULL DEFAULT NULL,
  `gifts_opened` int(11) NOT NULL DEFAULT 0,
  `gift_fastest` int(11) NOT NULL DEFAULT 120000,
  `gift_handicap` int(11) NOT NULL DEFAULT 0,
  `gift_handicap_weight` int(11) NOT NULL DEFAULT 0,
  `gift_best_bucks` int(11) NOT NULL DEFAULT 0,
  `gift_best_rarity` varchar(64) DEFAULT NULL,
  `miracles_active` int(11) NOT NULL DEFAULT 0,
  `highest_miracles_active` int(11) NOT NULL DEFAULT 0,
  `miracle_rolls` int(11) NOT NULL DEFAULT 0,
  `miracle_best_bucks` int(11) NOT NULL DEFAULT 0,
  `miracle_best_rarity` varchar(64) DEFAULT NULL,
  `last_changed_timestamp` timestamp(3) NOT NULL DEFAULT current_timestamp(3),
  `filter_bits` int(11) NOT NULL DEFAULT 1,
  `sort_value` int(11) NOT NULL DEFAULT 0,
  `streak_frozen` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`user_id`),
  KEY `USERNAME` (`username`),
  CONSTRAINT `positive_daily` CHECK (`num_dailies` >= 0),
  CONSTRAINT `positive_total_boars` CHECK (`total_boars` >= 0),
  CONSTRAINT `positive_score` CHECK (`total_bucks` >= 0),
  CONSTRAINT `positive_streak` CHECK (`boar_streak` >= 0),
  CONSTRAINT `positive_powerup_attempts` CHECK (`powerup_attempts` >= 0),
  CONSTRAINT `positive_powerup_perfects` CHECK (`powerup_perfects` >= 0),
  CONSTRAINT `positive_powerup_fastest_time` CHECK (`powerup_fastest_time` >= 0),
  CONSTRAINT `positive_gifts_opened` CHECK (`gifts_opened` >= 0),
  CONSTRAINT `positive_active_miracles` CHECK (`miracles_active` >= 0),
  CONSTRAINT `positive_uniques` CHECK (`unique_boars` >= 0),
  CONSTRAINT `positive_blessings` CHECK (`blessings` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_unicode_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'IGNORE_SPACE,STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`default`@`localhost`*/ /*!50003 trigger handle_user_insert
    after insert
    on users
    for each row
begin
    INSERT INTO user_quests (user_id) 
    VALUES (NEW.user_id);
end */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_unicode_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'IGNORE_SPACE,STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`default`@`localhost`*/ /*!50003 trigger user_main_updater
    before update
    on users
    for each row
begin
    SET NEW.last_changed_timestamp = current_timestamp(3);
    
    IF OLD.num_dailies < NEW.num_dailies THEN
        SET NEW.boar_streak = OLD.boar_streak + 1;
    end if;

    IF NEW.boar_streak > OLD.highest_streak THEN
        SET NEW.highest_streak = NEW.boar_streak;
    end if;

    IF NEW.total_boars > OLD.highest_boars THEN
        SET NEW.highest_boars = NEW.total_boars;
    end if;

    IF NEW.total_bucks > OLD.highest_bucks THEN
        SET NEW.highest_bucks = NEW.total_bucks;
    end if;

    IF NEW.miracles_active > OLD.highest_miracles_active THEN
        SET NEW.highest_miracles_active = NEW.miracles_active;
    end if;

    SET NEW.unique_boars = (
        SELECT COUNT(DISTINCT boar_id)
        FROM collected_boars
        WHERE OLD.user_id = user_id AND `exists` = true AND deleted = false
    );

    SET NEW.streak_bless = LEAST(NEW.boar_streak, 250);

    IF OLD.unique_boars != NEW.unique_boars THEN
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
            end if;
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
            end if;
        end if;

        IF NEW.unique_boars > OLD.highest_unique_boars THEN
            SET NEW.highest_unique_boars = NEW.unique_boars;
        end if;

        IF NEW.unique_bless > OLD.highest_unique_bless THEN
            SET NEW.highest_unique_bless = NEW.unique_bless;
        end if;
    end if;

    SET NEW.blessings = NEW.streak_bless + NEW.quest_bless + NEW.unique_bless + NEW.other_bless;

    IF NEW.blessings > OLD.highest_blessings THEN
        SET NEW.highest_blessings = NEW.blessings;
    end if;

    IF NEW.streak_bless > OLD.highest_streak_bless THEN
        SET NEW.highest_streak_bless = NEW.streak_bless;
    end if;

    IF NEW.quest_bless > OLD.highest_quest_bless THEN
        SET NEW.highest_quest_bless = NEW.quest_bless;
    end if;

    IF NEW.other_bless > OLD.highest_other_bless THEN
        SET NEW.highest_other_bless = NEW.other_bless;
    end if;
end */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'IGNORE_SPACE,STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`default`@`localhost`*/ /*!50003 trigger handle_user_delete
    before delete
    on users
    for each row
begin
    UPDATE collected_boars
    SET deleted = true, user_id = null
    WHERE OLD.user_id = user_id;
end */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*M!100616 SET NOTE_VERBOSITY=@OLD_NOTE_VERBOSITY */;

-- Dump completed on 2024-10-19 10:51:19
