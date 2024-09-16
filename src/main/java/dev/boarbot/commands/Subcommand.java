package dev.boarbot.commands;

import dev.boarbot.api.util.Configured;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.data.GuildDataUtil;
import dev.boarbot.util.generators.EmbedImageGenerator;
import dev.boarbot.util.logging.Log;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public abstract class Subcommand implements Configured {
    protected final SlashCommandInteractionEvent event;
    protected final SlashCommandInteraction interaction;
    protected final User user;

    public Subcommand(SlashCommandInteractionEvent event) {
        this.event = event;
        this.interaction = event.getInteraction();
        this.user = event.getUser();
    }

    public abstract void execute();

    protected boolean canInteract() {
        try (Connection connection = DataUtil.getConnection()) {
            String guildID = Objects.requireNonNull(this.interaction.getGuild()).getId();
            String channelID = this.interaction.getChannelId();

            List<String> validChannelIDs = GuildDataUtil.getValidChannelIDs(connection, guildID);
            boolean isSetup = !validChannelIDs.isEmpty();
            boolean isValidChannel = validChannelIDs.contains(channelID);

            EmbedImageGenerator embedGen = new EmbedImageGenerator(STRS.getNoSetup(), COLORS.get("error"));

            if (!isSetup) {
                Log.debug(this.user, this.getClass(), "Guild not setup, not proceeding");
                this.interaction.replyFiles(embedGen.generate().getFileUpload()).setEphemeral(true).queue();
                return false;
            }

            if (!isValidChannel) {
                Log.debug(this.user, this.getClass(), "Invalid channel, not proceeding");
                embedGen.setStr(STRS.getWrongChannel());
                this.interaction.replyFiles(embedGen.generate().getFileUpload()).setEphemeral(true).queue();
                return false;
            }
        } catch (SQLException exception) {
            Log.error(this.user, this.getClass(), "Failed to get valid channel IDs", exception);
            return false;
        } catch (IOException exception) {
            Log.error(this.user, this.getClass(), "Failed to generate response image", exception);
            return false;
        }

        return true;
    }
}
