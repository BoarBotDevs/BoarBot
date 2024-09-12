package dev.boarbot.bot;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.*;
import dev.boarbot.bot.config.commands.CommandConfig;
import dev.boarbot.bot.config.components.ComponentConfig;
import dev.boarbot.bot.config.items.BadgeItemConfig;
import dev.boarbot.bot.config.items.BaseItemConfig;
import dev.boarbot.bot.config.items.BoarItemConfig;
import dev.boarbot.bot.config.items.PowerupItemConfig;
import dev.boarbot.bot.config.modals.ModalConfig;
import dev.boarbot.bot.config.prompts.PromptConfig;
import dev.boarbot.bot.config.quests.QuestConfig;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Scanner;

@Slf4j
class ConfigLoader {
    private static final BotConfig config = BoarBotApp.getBot().getConfig();
    
    public static void loadConfig() {
        try {
            log.info("Attempting to load configs from 'src/main/resources/config'");

            Gson g = new Gson();
            String basePath = "src/%s/resources/config/".formatted(
                BoarBotApp.getBot().getBotType() == BotType.TEST ? "test" : "main"
            );

            File langConfig = new File(basePath + "lang/en_us.json");
            config.setStringConfig(g.fromJson(getJson(langConfig), StringConfig.class));

            File colorConfig = new File(basePath + "util/colors.json");
            config.setColorConfig(
                g.fromJson(getJson(colorConfig), new TypeToken<Map<String, String>>(){}.getType())
            );

            File constantConfig = new File(basePath + "util/constants.json");
            config.setNumberConfig(g.fromJson(getJson(constantConfig), NumberConfig.class));

            File pathConfig = new File(basePath + "util/paths.json");
            config.setPathConfig(g.fromJson(getJson(pathConfig), PathConfig.class));

            File mainConfig = new File(basePath + "config.json");
            config.setMainConfig(g.fromJson(getJson(mainConfig), MainConfig.class));

            File commandConfig = new File(basePath + "discord/commands.json");
            config.setCommandConfig(
                g.fromJson(getJson(commandConfig), new TypeToken<Map<String, CommandConfig>>(){}.getType())
            );

            File componentConfig = new File(basePath + "discord/components.json");
            config.setComponentConfig(g.fromJson(getJson(componentConfig), ComponentConfig.class));

            File modalConfig = new File(basePath + "discord/modals.json");
            config.setModalConfig(
                g.fromJson(getJson(modalConfig), new TypeToken<Map<String, ModalConfig>>(){}.getType())
            );

            File promptConfig = new File(basePath + "game/pow_prompts.json");
            config.setPromptConfig(
                g.fromJson(getJson(promptConfig), new TypeToken<Map<String, PromptConfig>>(){}.getType())
            );

            File questConfig = new File(basePath + "game/quests.json");
            config.setQuestConfig(
                g.fromJson(getJson(questConfig), new TypeToken<Map<String, QuestConfig>>(){}.getType())
            );

            File rarityConfigs = new File(basePath + "game/rarities.json");
            config.setRarityConfigs(
                g.fromJson(getJson(rarityConfigs), new TypeToken<Map<String, RarityConfig>>(){}.getType())
            );

            File badgeConfig = new File(basePath + "items/badges.json");
            config.getItemConfig().setBadges(
                g.fromJson(getJson(badgeConfig), new TypeToken<Map<String, BadgeItemConfig>>(){}.getType())
            );

            File boarConfig = new File(basePath + "items/boars.json");
            config.getItemConfig().setBoars(
                g.fromJson(getJson(boarConfig), new TypeToken<Map<String, BoarItemConfig>>(){}.getType())
            );

            File powerupConfig = new File(basePath + "items/powerups.json");
            config.getItemConfig().setPowerups(
                g.fromJson(getJson(powerupConfig), new TypeToken<Map<String, PowerupItemConfig>>(){}.getType())
            );

            for (BoarItemConfig boar : config.getItemConfig().getBoars().values()) {
                setNames(boar);
            }

            for (PowerupItemConfig powerup : config.getItemConfig().getPowerups().values()) {
                setNames(powerup);
            }

            for (RarityConfig rarityConfig : config.getRarityConfigs().values()) {
                if (rarityConfig.getPluralName() == null) {
                    rarityConfig.setPluralName(rarityConfig.getName() + "s");
                }
            }

            File fontFile = new File(config.getPathConfig().getFontAssets() + config.getPathConfig().getMainFont());

            try {
                BoarBotApp.getBot().setFont(Font.createFont(Font.TRUETYPE_FONT, fontFile));
            } catch (Exception exception) {
                log.error(
                    "There was a problem when creating font from font file %s".formatted(fontFile.getPath()), exception
                );
            }

            fixStrings();

            log.info("Successfully loaded config");
        } catch (FileNotFoundException exception) {
            log.error("Unable to find one or more config files in 'src/main/resources/config'", exception);
            System.exit(-1);
        }
    }

    private static void setNames(BaseItemConfig item) {
        if (item.getPluralName() == null) {
            item.setPluralName(item.getName() + "s");
        }

        if (item.getShortName() == null) {
            item.setShortName(item.getName());
        }

        if (item.getShortPluralName() == null) {
            item.setShortPluralName(item.getShortName() + "s");
        }
    }

    private static void fixStrings() {
        StringConfig strs = config.getStringConfig();
        Map<String, PowerupItemConfig> pows = config.getItemConfig().getPowerups();
        Map<String, CommandConfig> cmds = config.getCommandConfig();

        strs.setNoSetup(strs.getNoSetup().formatted(
            cmds.get("manage").getName(), cmds.get("manage").getSubcommands().get("setup").getName()
        ));
        strs.setError(strs.getError().formatted(
            cmds.get("main").getName(), cmds.get("main").getSubcommands().get("report").getName()
        ));

        strs.setSetupFinishedAll(strs.getSetupFinishedAll().formatted(
            cmds.get("main").getName(), cmds.get("main").getSubcommands().get("daily").getName()
        ));
        strs.setSetupInfoResponse1(strs.getSetupInfoResponse1().formatted(
            cmds.get("manage").getName(), cmds.get("manage").getSubcommands().get("setup").getName()
        ));
        strs.setSetupInfoResponse2(strs.getSetupInfoResponse2().formatted(
            cmds.get("manage").getName(), cmds.get("manage").getSubcommands().get("setup").getName()
        ));

        strs.setDailyUsed(strs.getDailyUsed().formatted(
            cmds.get("main").getName(), cmds.get("main").getSubcommands().get("daily").getName(), "%s"
        ));
        strs.setDailyFirstTime(strs.getDailyFirstTime().formatted(
            pows.get("miracle").getPluralName(),
            pows.get("gift").getPluralName(),
            cmds.get("main").getName(),
            cmds.get("main").getSubcommands().get("help").getName()
        ));

        strs.setDailyTitle(strs.getDailyTitle().formatted(strs.getMainItemName()));

        strs.setProfileTotalLabel(strs.getProfileTotalLabel().formatted(strs.getMainItemPluralName()));
        strs.setProfileDailiesLabel(strs.getProfileDailiesLabel().formatted(strs.getMainItemPluralName()));
        strs.setProfileUniquesLabel(strs.getProfileUniquesLabel().formatted(strs.getMainItemPluralName()));
        strs.setProfileStreakLabel(strs.getProfileStreakLabel().formatted(strs.getMainItemName()));
        strs.setProfileNextDailyLabel(strs.getProfileNextDailyLabel().formatted(strs.getMainItemName()));

        strs.setCompFavoriteSuccess(
            strs.getCompFavoriteSuccess().formatted("%s", strs.getMainItemName().toLowerCase())
        );
        strs.setCompUnfavoriteSuccess(
            strs.getCompUnfavoriteSuccess().formatted("%s", strs.getMainItemName().toLowerCase())
        );
        strs.setCompCloneTitle(strs.getCompCloneTitle().formatted(strs.getMainItemName()));
        strs.setCompTransmuteConfirm(strs.getCompTransmuteConfirm().formatted("%s", "%s", strs.getMainItemName()));

        strs.setStatsDailiesLabel(strs.getStatsDailiesLabel().formatted(strs.getMainItemPluralName()));
        strs.setStatsDailiesMissedLabel(strs.getStatsDailiesMissedLabel().formatted(strs.getMainItemPluralName()));
        strs.setStatsLastDailyLabel(strs.getStatsLastDailyLabel().formatted(strs.getMainItemName()));
        strs.setStatsLastBoarLabel(strs.getStatsLastBoarLabel().formatted(strs.getMainItemName()));
        strs.setStatsFavBoarLabel(strs.getStatsFavBoarLabel().formatted(strs.getMainItemName()));
        strs.setStatsUniquesLabel(strs.getStatsUniquesLabel().formatted(strs.getMainItemPluralName()));
        strs.setStatsStreakLabel(strs.getStatsStreakLabel().formatted(strs.getMainItemName()));
        strs.setStatsMiraclesActiveLabel(
            strs.getStatsMiraclesActiveLabel().formatted(pows.get("miracle").getShortPluralName())
        );
        strs.setStatsMiracleRollsLabel(
            strs.getStatsMiracleRollsLabel().formatted(pows.get("miracle").getShortName())
        );

        strs.setNotificationSuccess(strs.getNotificationSuccess().formatted(
            cmds.get("main").getName(), cmds.get("main").getSubcommands().get("daily").getName()
        ));
        strs.setNotificationDailyReady(
            strs.getNotificationDailyReady().formatted(strs.getMainItemName().toLowerCase())
        );
    }

    private static String getJson(File file) throws FileNotFoundException {
        Scanner reader = new Scanner(file);
        StringBuilder jsonStr = new StringBuilder();

        while (reader.hasNextLine()) {
            jsonStr.append(reader.nextLine());
        }

        return jsonStr.toString();
    }
}
