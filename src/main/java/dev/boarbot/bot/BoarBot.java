package dev.boarbot.bot;

import com.google.gson.Gson;
import dev.boarbot.api.bot.Bot;
import dev.boarbot.bot.config.BotConfig;
import dev.boarbot.listeners.CommandListener;
import dev.boarbot.listeners.StopMessageListener;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

@Log4j2
public class BoarBot implements Bot {
    private final Dotenv env = Dotenv.configure()
        .filename(".env")
        .load();
    private JDA jda;
    private BotConfig config;

    @Override
    public void create() {
        loadConfig();

        jda = JDABuilder.createDefault(env.get("TOKEN"))
            .addEventListeners(
                new StopMessageListener(),
                new CommandListener()
            )
            .setActivity(Activity.customStatus("/boar help | boarbot.dev"))
            .build();
    }

    @Override
    public JDA getJDA() {
        return jda;
    }

    public void loadConfig() {
        try {
            log.info("Attempting to load 'config.json' from resources.");

            File file = new File("src/main/resources/config.json");
            Scanner reader = new Scanner(file);
            StringBuilder jsonStr = new StringBuilder();
            Gson g = new Gson();

            while(reader.hasNextLine()) {
                jsonStr.append(reader.nextLine());
            }

            config = g.fromJson(jsonStr.toString(), BotConfig.class);

            log.info("Successfully loaded config.");
        } catch (FileNotFoundException e) {
            log.error("Unable to find 'config.json' in resources.");
            System.exit(-1);
        }
    }

    @Override
    public BotConfig getConfig() {
        return config;
    }
}
