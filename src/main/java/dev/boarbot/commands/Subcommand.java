package dev.boarbot.commands;

import dev.boarbot.BoarBotApp;
import dev.boarbot.bot.config.BotConfig;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.data.GuildDataUtil;
import dev.boarbot.util.generators.EmbedGenerator;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

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

            List<String> validChannelIDs = GuildDataUtil.getValidChannelIDs(connection, guildID);
            boolean isSetup = !validChannelIDs.isEmpty();
            boolean isValidChannel = validChannelIDs.contains(channelID);

            EmbedGenerator embedGen = new EmbedGenerator(
                this.config.getStringConfig().getNoSetup(), this.config.getColorConfig().get("error")
            );

            if (!isSetup) {
                this.interaction.replyFiles(embedGen.generate()).setEphemeral(true).queue();
                return false;
            }

            if (!isValidChannel) {
                embedGen.setStr(this.config.getStringConfig().getWrongChannel());
                this.interaction.replyFiles(embedGen.generate()).setEphemeral(true).queue();
                return false;
            }
        } catch (SQLException exception) {
            log.error("Failed to find guild information.", exception);
            return false;
        } catch (IOException exception) {
            log.error("Failed to create invalid response image.", exception);
            return false;
        }

        return true;
    }
}
