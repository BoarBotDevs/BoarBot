package dev.boarbot.commands.boardev;

import dev.boarbot.commands.Subcommand;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.data.UserDataUtil;
import dev.boarbot.util.interaction.SpecialReply;
import dev.boarbot.util.logging.ExceptionHandler;
import dev.boarbot.util.logging.Log;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

public class FreezeSubcommand extends Subcommand {
    public FreezeSubcommand(SlashCommandInteractionEvent event) {
        super(event);
    }

    @Override
    public void execute() {
        try (Connection connection = DataUtil.getConnection()) {
            boolean value = Objects.requireNonNull(this.event.getOption("value")).getAsBoolean();

            UserDataUtil.setStreakFreeze(connection, value);
            this.interaction.reply("Streak freeze set to: `" + value + "`")
                .setEphemeral(true).queue(null, e -> ExceptionHandler.replyHandle(this.interaction, this, e));
        } catch (SQLException exception) {
            SpecialReply.sendErrorMessage(this.interaction, exception);
            Log.error(this.user, this.getClass(), "Failed to set streak freeze", exception);
        }
    }
}
