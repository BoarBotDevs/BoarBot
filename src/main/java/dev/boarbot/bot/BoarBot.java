package dev.boarbot.bot;

import com.google.gson.Gson;
import dev.boarbot.api.bot.Bot;
import dev.boarbot.bot.config.BotConfig;
import dev.boarbot.bot.config.NumberConfig;
import dev.boarbot.bot.config.PathConfig;
import dev.boarbot.bot.config.QuestConfig;
import dev.boarbot.bot.config.commands.CommandConfig;
import dev.boarbot.bot.config.commands.SubcommandConfig;
import dev.boarbot.bot.config.items.IndivItemConfig;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.commands.Subcommand;
import dev.boarbot.listeners.CommandListener;
import dev.boarbot.listeners.ComponentListener;
import dev.boarbot.listeners.ModalListener;
import dev.boarbot.listeners.StopMessageListener;
import dev.boarbot.modals.ModalHandler;
import dev.boarbot.util.boar.BoarUtil;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.graphics.GraphicsUtil;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.data.DataObject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.sql.*;
import java.util.*;
import java.util.List;

@Slf4j
public class BoarBot implements Bot {
    private final Dotenv env = Dotenv.configure()
        .filename(".env")
        .load();
    private JDA jda;

    private BotConfig config;
    private Font font;

    private final Map<String, byte[]> byteCacheMap = new HashMap<>();
    private final Map<String, BufferedImage> imageCacheMap = new HashMap<>();

    private final Map<String, Constructor<? extends Subcommand>> subcommands = new HashMap<>();
    private final Map<String, Interactive> interactives = new HashMap<>();
    private final Map<String, ModalHandler> modalHandlers = new HashMap<>();

    private BotType botType;

    @Override
    public void create(BotType type) {
        this.botType = type;

        this.loadConfig();
        this.loadIntoDatabase("boars");
        this.loadIntoDatabase("rarities");
        this.loadIntoDatabase("quests");
        this.loadCache();

        this.jda = JDABuilder.createDefault(this.env.get("TOKEN"))
            .addEventListeners(
                new StopMessageListener(), new CommandListener(), new ComponentListener(), new ModalListener()
            )
            .setActivity(Activity.customStatus("/boar help | boarbot.dev"))
            .build();

        registerSubcommands();
    }

    @Override
    public BotType getBotType() {
        return this.botType;
    }

    @Override
    public JDA getJDA() {
        return this.jda;
    }

    @Override
    public void loadConfig() {
        try {
            log.info("Attempting to load 'config.json' from resources.");

            String configFile = "src/%s/resources/config.json".formatted(
                this.botType == BotType.TEST ? "test" : "main"
            );

            File file = new File(configFile);
            Scanner reader = new Scanner(file);
            StringBuilder jsonStr = new StringBuilder();
            Gson g = new Gson();

            while(reader.hasNextLine()) {
                jsonStr.append(reader.nextLine());
            }

            this.config = g.fromJson(jsonStr.toString(), BotConfig.class);

            for (IndivItemConfig boar : this.config.getItemConfig().getBoars().values()) {
                if (boar.getPluralName() == null) {
                    boar.setPluralName(boar.name + "s");
                }
            }

            for (IndivItemConfig badge : this.config.getItemConfig().getBadges().values()) {
                if (badge.getPluralName() == null) {
                    badge.setPluralName(badge.name + "s");
                }
            }

            for (IndivItemConfig powerup : this.config.getItemConfig().getPowerups().values()) {
                if (powerup.getPluralName() == null) {
                    powerup.setPluralName(powerup.name + "s");
                }
            }

            File fontFile = new File(
                this.config.getPathConfig().getFontAssets() + this.config.getPathConfig().getMainFont()
            );

            try {
                this.font = Font.createFont(Font.TRUETYPE_FONT, fontFile);
            } catch (Exception exception) {
                log.error("There was a problem when creating font from font file %s".formatted(fontFile.getPath()));
            }

            log.info("Successfully loaded config.");
        } catch (FileNotFoundException exception) {
            log.error("Unable to find 'config.json' in resources.");
            System.exit(-1);
        }
    }

    private void loadIntoDatabase(String databaseType) {
        try (
            Connection connection = DataUtil.getConnection();
            Statement statement = connection.createStatement()
        ) {
            StringBuilder sqlStatement = new StringBuilder();

            String tableColumns = "(boar_id, rarity_id, is_skyblock)";

            if (databaseType.equals("rarities")) {
                tableColumns = "(rarity_id, prior_rarity_id, base_bucks, hunter_need)";
            } else if (databaseType.equals("quests")) {
                tableColumns = "(quest_id, easy_value, medium_value, hard_value, very_hard_value, value_type)";
            }

            sqlStatement.append("DELETE FROM %s_info;".formatted(databaseType));

            statement.executeUpdate(sqlStatement.toString());
            sqlStatement.setLength(0);

            sqlStatement.append("INSERT INTO %s_info %s VALUES ".formatted(databaseType, tableColumns));

            switch (databaseType) {
                case "boars" -> {
                    for (String boarID : this.getConfig().getItemConfig().getBoars().keySet()) {
                        if (this.getConfig().getItemConfig().getBoars().get(boarID).isBlacklisted()) {
                            continue;
                        }

                        int isSB = this.getConfig().getItemConfig().getBoars().get(boarID).isSB() ? 1 : 0;
                        String rarityID = BoarUtil.findRarityKey(boarID);

                        sqlStatement.append("('%s','%s',%d),".formatted(boarID, rarityID, isSB));
                    }
                }
                case "rarities" -> {
                    String priorRarityID = null;
                    for (String rarityID : this.getConfig().getRarityConfigs().keySet()) {
                        int score = this.getConfig().getRarityConfigs().get(rarityID).getBaseScore();
                        int hunterNeed = this.getConfig().getRarityConfigs().get(rarityID).isHunterNeed() ? 1 : 0;

                        sqlStatement.append("('%s','%s',%d,%d),".formatted(rarityID, priorRarityID, score, hunterNeed));
                        priorRarityID = rarityID;
                    }
                }
                case "quests" -> {
                    for (String questID : this.getConfig().getQuestConfig().keySet()) {
                        QuestConfig questConfig = this.getConfig().getQuestConfig().get(questID);

                        sqlStatement.append("('%s','%s','%s','%s','%s','%s'),".formatted(
                            questID,
                            questConfig.getQuestVals()[0][0],
                            questConfig.getQuestVals()[1][0],
                            questConfig.getQuestVals()[2][0],
                            questConfig.getQuestVals()[3][0],
                            questConfig.getValType().toUpperCase()
                        ));
                    }
                }
            }

            sqlStatement.setLength(sqlStatement.length() - 1);
            sqlStatement.append(";");

            statement.executeUpdate(sqlStatement.toString());
        } catch (SQLException exception) {
            log.error("Something went wrong when loading config data into database.", exception);
            System.exit(-1);
        }
    }

    @Override
    public BotConfig getConfig() {
        return this.config;
    }

    private void loadCache() {
        NumberConfig nums = this.getConfig().getNumberConfig();
        PathConfig pathConfig = this.getConfig().getPathConfig();

        int[] origin = {0, 0};
        int[] bigBoarSize = nums.getBigBoarSize();
        int[] mediumBoarSize = nums.getMediumBoarSize();

        for (String boarID : this.getConfig().getItemConfig().getBoars().keySet()) {
            IndivItemConfig boarInfo = this.getConfig().getItemConfig().getBoars().get(boarID);

            String filePath = boarInfo.getStaticFile() != null
                ? pathConfig.getBoars() + boarInfo.getStaticFile()
                : pathConfig.getBoars() + boarInfo.getFile();

            BufferedImage bigBoarImage = new BufferedImage(bigBoarSize[0], bigBoarSize[1], BufferedImage.TYPE_INT_ARGB);
            Graphics2D bigBoarGraphics = bigBoarImage.createGraphics();

            BufferedImage mediumBoarImage = new BufferedImage(
                mediumBoarSize[0], mediumBoarSize[1], BufferedImage.TYPE_INT_ARGB
            );
            Graphics2D mediumBoarGraphics = mediumBoarImage.createGraphics();

            try {
                GraphicsUtil.drawImage(bigBoarGraphics, filePath, origin, bigBoarSize);
                this.imageCacheMap.put("big" + boarID, bigBoarImage);

                GraphicsUtil.drawImage(mediumBoarGraphics, filePath, origin, mediumBoarSize);
                this.imageCacheMap.put("medium" + boarID, mediumBoarImage);

//                ItemImageGenerator itemGen = new ItemImageGenerator(null, "Daily Boar!", boarID);
//                ByteArrayInputStream byteArrayIS = new ByteArrayInputStream(itemGen.generate(true));
//                ImageIO.write(ImageIO.read(byteArrayIS), "png", new FileOutputStream("bubble/" + boarID + ".png"));
            } catch (Exception exception) {
                log.error("Failed to generate cache image for %s".formatted(boarID), exception);
                System.exit(-1);
            }
        }

        log.info("Successfully loaded all boar images into cache");

        String rarityBorderPath = pathConfig.getMegaMenuAssets() + pathConfig.getRarityBorder();

        for (String rarityID : this.getConfig().getRarityConfigs().keySet()) {
            String color = this.getConfig().getColorConfig().get(rarityID);

            BufferedImage rarityBorderImage = new BufferedImage(
                mediumBoarSize[0], mediumBoarSize[1], BufferedImage.TYPE_INT_ARGB
            );
            Graphics2D rarityBorderG2D = rarityBorderImage.createGraphics();

            try {
                GraphicsUtil.drawRect(rarityBorderG2D, origin, mediumBoarSize, color);
                rarityBorderG2D.setComposite(AlphaComposite.DstIn);
                GraphicsUtil.drawImage(rarityBorderG2D, rarityBorderPath, origin, mediumBoarSize);

                this.imageCacheMap.put("border" + rarityID, rarityBorderImage);
            } catch (Exception exception) {
                log.error("Failed to generate cache image for %s border".formatted(rarityID), exception);
                System.exit(-1);
            }
        }

        log.info("Successfully loaded all rarity borders into cache");
    }

    @Override
    public Font getFont() {
        return this.font;
    }

    @Override
    public Map<String, byte[]> getByteCacheMap() {
        return this.byteCacheMap;
    }

    @Override
    public Map<String, BufferedImage> getImageCacheMap() {
        return this.imageCacheMap;
    }

    @Override
    public void deployCommands() {
        Map<String, CommandConfig> commandData = this.config.getCommandConfig();

        List<SlashCommandData> globalCommands = new ArrayList<>();
        List<SlashCommandData> guildCommands = new ArrayList<>();

        for (CommandConfig command : commandData.values()) {
            Integer defaultPerms = command.getDefault_member_permissions();

            if (defaultPerms != null && defaultPerms == 0) {
                guildCommands.add(
                    SlashCommandData.fromData(DataObject.fromJson(command.toString()))
                );

                continue;
            }

            globalCommands.add(
                SlashCommandData.fromData(DataObject.fromJson(command.toString()))
            );
        }

        Guild devGuild = this.jda.getGuildById(this.env.get("GUILD_ID"));

        if (devGuild != null) {
            devGuild.updateCommands().addCommands(guildCommands).complete();
        }

        this.jda.updateCommands().addCommands(globalCommands).complete();
    }

    @Override
    public void registerSubcommands() {
        Map<String, CommandConfig> commandData = config.getCommandConfig();

        for (CommandConfig commandVal : commandData.values()) {
            Map<String, SubcommandConfig> subcommandData = commandVal.getSubcommands();

            for (SubcommandConfig subcommandVal : subcommandData.values()) {
                try {
                    Class<? extends Subcommand> subcommandClass = Class.forName(
                        subcommandVal.getLocation()
                    ).asSubclass(Subcommand.class);
                    Constructor<? extends Subcommand> subcommandConstructor = subcommandClass.getDeclaredConstructor(
                        SlashCommandInteractionEvent.class
                    );

                    this.subcommands.put(commandVal.getName() + subcommandVal.getName(), subcommandConstructor);
                } catch (Exception exception) {
                    log.error(
                        "Failed to find constructor for '/%s %s'.".formatted(
                            commandVal.getName(), subcommandVal.getName()
                    ));
                    System.exit(-1);
                }
            }
        }
    }

    @Override
    public Map<String, Constructor<? extends Subcommand>> getSubcommands() {
        return this.subcommands;
    }

    @Override
    public Map<String, Interactive> getInteractives() {
        return this.interactives;
    }

    @Override
    public Map<String, ModalHandler> getModalHandlers() { return this.modalHandlers; }
}
