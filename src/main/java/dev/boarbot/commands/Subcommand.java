package dev.boarbot.commands;

import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.BotConfig;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

public abstract class Subcommand {
    protected final BotConfig config = BoarBotApp.getBot().getConfig();

    protected final SlashCommandInteractionEvent event;
    protected final SlashCommandInteraction interaction;
    protected final User user;

    public Subcommand(SlashCommandInteractionEvent event) {
        this.event = event;
        this.interaction = event.getInteraction();
        this.user = event.getUser();
    }

    public abstract void execute();
}
