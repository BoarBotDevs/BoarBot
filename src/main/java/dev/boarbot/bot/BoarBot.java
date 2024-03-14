package dev.boarbot.bot;

import dev.boarbot.api.bot.Bot;
import dev.boarbot.listeners.CommandListener;
import dev.boarbot.listeners.StopMessageListener;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

public class BoarBot implements Bot {
    private final Dotenv env = Dotenv.configure()
        .filename(".env")
        .load();
    private JDA jda;

    @Override
    public void create() {
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

    public void loadConfig() {}
}
