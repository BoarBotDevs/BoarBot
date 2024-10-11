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
import dev.boarbot.util.boar.BoarUtil;
import dev.boarbot.util.logging.Log;
import dev.boarbot.util.resource.ResourceUtil;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Type;
import java.util.Map;

class ConfigLoader {
    private static final BotConfig config = BoarBotApp.getBot().getConfig();

    public static String basePath = "config/";
    public static String mainPath = basePath + "config.json";

    public static String langPath = basePath + "lang/";
    public static String strsPath = langPath + "en_us.json";

    public static String utilPath = basePath + "util/";
    public static String colorsPath = utilPath + "colors.json";
    public static String numsPath = utilPath + "constants.json";

    public static String discordPath = basePath + "discord/";
    public static String cmdsPath = discordPath + "commands.json";
    public static String compsPath = discordPath + "components.json";
    public static String modalsPath = discordPath + "modals.json";

    public static String gamePath = basePath + "game/";
    public static String promptsPath = gamePath + "pow_prompts.json";
    public static String questsPath = gamePath + "quests.json";
    public static String raritiesPath = gamePath + "rarities.json";

    public static String itemsPath = basePath + "items/";
    public static String badgesPath = itemsPath + "badges.json";
    public static String boarsPath = itemsPath + "boars.json";
    public static String powerupsPath = itemsPath + "powerups.json";
    
    public static void loadConfig() {
        try {
            Log.debug(ConfigLoader.class, "Attempting to load config...");

            config.setMainConfig(getFromJson(mainPath, MainConfig.class));

            config.setStringConfig(getFromJson(strsPath, StringConfig.class));

            config.setColorConfig(getFromJson(colorsPath, new TypeToken<Map<String, String>>(){}.getType()));
            config.setNumberConfig(getFromJson(numsPath, NumberConfig.class));

            config.setCommandConfig(getFromJson(cmdsPath, new TypeToken<Map<String, CommandConfig>>(){}.getType()));
            config.setComponentConfig(getFromJson(compsPath, ComponentConfig.class));
            config.setModalConfig(getFromJson(modalsPath, new TypeToken<Map<String, ModalConfig>>(){}.getType()));

            config.setPromptConfig(getFromJson(promptsPath, new TypeToken<Map<String, PromptConfig>>(){}.getType()));
            config.setQuestConfig(getFromJson(questsPath, new TypeToken<Map<String, QuestConfig>>(){}.getType()));
            config.setRarityConfigs(getFromJson(raritiesPath, new TypeToken<Map<String, RarityConfig>>(){}.getType()));

            config.getItemConfig().setBadges(getFromJson(
                badgesPath, new TypeToken<Map<String, BadgeItemConfig>>(){}.getType()
            ));
            config.getItemConfig().setBoars(getFromJson(
                boarsPath, new TypeToken<Map<String, BoarItemConfig>>(){}.getType()
            ));
            config.getItemConfig().setPowerups(getFromJson(
                powerupsPath, new TypeToken<Map<String, PowerupItemConfig>>(){}.getType()
            ));

            for (String boarID : config.getItemConfig().getBoars().keySet()) {
                setNames(config.getItemConfig().getBoars().get(boarID));

                if (BoarUtil.findRarityKey(boarID) == null) {
                    Log.error(
                        ConfigLoader.class,
                        "%s is not assigned a rarity".formatted(boarID),
                        new IllegalArgumentException()
                    );
                    System.exit(-1);
                }
            }

            for (PowerupItemConfig powerup : config.getItemConfig().getPowerups().values()) {
                setNames(powerup);
            }

            for (RarityConfig rarityConfig : config.getRarityConfigs().values()) {
                if (rarityConfig.getPluralName() == null) {
                    rarityConfig.setPluralName(rarityConfig.getName() + "s");
                }

                for (String boarID : rarityConfig.getBoars()) {
                    if (!config.getItemConfig().getBoars().containsKey(boarID)) {
                        Log.error(
                            ConfigLoader.class,
                            "%s is assigned a rarity but does not exist".formatted(boarID),
                            new IllegalArgumentException()
                        );
                        System.exit(-1);
                    }
                }
            }

            try {
                InputStream is = ResourceUtil.getResourceStream(ResourceUtil.fontPath);
                BoarBotApp.getBot().setFont(Font.createFont(Font.TRUETYPE_FONT, is));
            } catch (FontFormatException exception) {
                Log.error(ConfigLoader.class, "The font file is not a TTF file", exception);
            } catch (IOException exception) {
                Log.error(ConfigLoader.class, "The font file could not be read", exception);
            }

            fixStrings();

            Log.debug(ConfigLoader.class, "Successfully loaded config");
        } catch (IOException exception) {
            Log.error(ConfigLoader.class, "Unable to read one or more config files", exception);
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
            pows.get("gift").getName(),
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
    }

    private static <T> T getFromJson(String path, Class<T> clazz) throws IOException {
        try (InputStream stream = ResourceUtil.getResourceStream(path)) {
            if (stream == null) {
                throw new IOException("Could not find resource " + path);
            }

            InputStreamReader reader = new InputStreamReader(stream);
            return new Gson().fromJson(reader, clazz);
        }
    }

    private static <T> T getFromJson(String path, Type type) throws IOException {
        try (InputStream stream = ResourceUtil.getResourceStream(path)) {
            if (stream == null) {
                throw new IOException("Could not find resource " + path);
            }

            InputStreamReader reader = new InputStreamReader(stream);
            return new Gson().fromJson(reader, type);
        }
    }
}
