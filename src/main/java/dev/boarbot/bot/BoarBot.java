package dev.boarbot.bot;

import com.google.gson.Gson;
import dev.boarbot.api.bot.Bot;
import dev.boarbot.bot.config.BotConfig;
import dev.boarbot.bot.config.commands.CommandConfig;
import dev.boarbot.bot.config.commands.SubcommandConfig;
import dev.boarbot.commands.Subcommand;
import dev.boarbot.listeners.CommandListener;
import dev.boarbot.listeners.StopMessageListener;
import dev.boarbot.util.data.DataUtil;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.data.DataObject;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.List;

@Log4j2
public class BoarBot implements Bot {
    private final Dotenv env = Dotenv.configure()
        .filename(".env")
        .load();
    private JDA jda;

    private BotConfig config;
    private Font font;

    private final Map<String, Constructor<? extends Subcommand>> subcommands = new HashMap<>();
    private BotType botType;

    @Override
    public void create(BotType type) {
        this.botType = type;

        loadConfig();

        this.jda = JDABuilder.createDefault(this.env.get("TOKEN"))
            .addEventListeners(new StopMessageListener(), new CommandListener())
            .setActivity(Activity.customStatus("/boar help | boarbot.dev"))
            .build();

        registerSubcommands();
        DataUtil.updateAllData();
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

    @Override
    public BotConfig getConfig() {
        return this.config;
    }

    @Override
    public Font getFont() {
        return this.font;
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
}
