package dev.boarbot.commands;

import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.BotConfig;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.data.GuildDataUtil;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import java.sql.Connection;
import java.sql.SQLException;

@Log4j2
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

    public abstract void execute() throws InterruptedException;

    protected boolean canInteract() {
        try (Connection connection = DataUtil.getConnection()) {
            String guildID = this.interaction.getGuild().getId();
            String channelID = this.interaction.getChannelId();

            if (!GuildDataUtil.isValidChannel(connection, guildID, channelID)) {
                this.interaction.reply("Not valid channel").setEphemeral(true).queue();
                return false;
            }
        } catch (SQLException exception) {
            log.error("Failed to find guild information", exception);
            this.interaction.reply("Error").setEphemeral(true).queue();
            return false;
        }

        return true;
    }
}
