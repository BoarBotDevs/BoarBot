package dev.boarbot.bot;

import dev.boarbot.api.bot.Bot;
import dev.boarbot.bot.config.BotConfig;
import dev.boarbot.bot.config.commands.CommandConfig;
import dev.boarbot.bot.config.commands.SubcommandConfig;
import dev.boarbot.util.test.TestUtil;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestBoarBot {
    private final Dotenv env = Dotenv.configure()
        .filename(".env")
        .load();

    @Test
    public void testCreate() {
        Bot boarBot = TestUtil.getBot();

        assertNotNull(boarBot.getBotType(), "Bot type was not set");
        assertNotNull(boarBot.getConfig(), "Config not loaded");
        assertNotNull(boarBot.getJDA(), "JDA client not made");
        assertFalse(boarBot.getSubcommands().isEmpty(), "Subcommands not registered");
    }

    @Test
    public void testDeployCommands() {
        Bot boarBot = TestUtil.getBot();

        // Empty all commands for fresh slate
        boarBot.getJDA().updateCommands().addCommands().complete();

        // Empty all guild commands for fresh slate
        Guild devGuild = boarBot.getJDA().getGuildById(this.env.get("GUILD_ID"));
        if (devGuild != null) {
            devGuild.updateCommands().addCommands().complete();
        }

        boarBot.deployCommands();

        int count = 0;

        for (Command command : boarBot.getJDA().retrieveCommands().complete()) {
            for (Command.Subcommand subcommand : command.getSubcommands()) {
                assertTrue(
                    boarBot.getSubcommands().containsKey(command.getName() + subcommand.getName()),
                    "Command /%s %s is not in registered commands".formatted(command.getName(), subcommand.getName())
                );
                count++;
            }
        }

        if (devGuild != null) {
            for (Command command : devGuild.retrieveCommands().complete()) {
                for (Command.Subcommand subcommand : command.getSubcommands()) {
                    assertTrue(
                        boarBot.getSubcommands().containsKey(command.getName() + subcommand.getName()),
                        "Command /%s %s is not in registered commands".formatted(command.getName(), subcommand.getName())
                    );
                    count++;
                }
            }
        } else {
            count += 2;
        }

        assertEquals(boarBot.getSubcommands().size(), count, "Not all commands were deployed");
    }

    @Test
    public void testRegisterCommands() {
        Bot boarBot = TestUtil.getBot();

        BotConfig config = boarBot.getConfig();

        for (CommandConfig commandVal : config.getCommandConfig().values()) {
            for (SubcommandConfig subcommandVal : commandVal.getSubcommands().values()) {
                assertTrue(
                    boarBot.getSubcommands().containsKey(commandVal.getName() + subcommandVal.getName()),
                    "Command /%s %s failed to register".formatted(commandVal.getName(), subcommandVal.getName())
                );
            }
        }
    }
}
