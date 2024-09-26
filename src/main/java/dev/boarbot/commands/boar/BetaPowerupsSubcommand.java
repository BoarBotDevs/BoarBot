package dev.boarbot.commands.boar;

import dev.boarbot.commands.Subcommand;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.BoarUserFactory;
import dev.boarbot.entities.boaruser.Synchronizable;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.interaction.SpecialReply;
import dev.boarbot.util.logging.ExceptionHandler;
import dev.boarbot.util.logging.Log;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.sql.Connection;
import java.sql.SQLException;

public class BetaPowerupsSubcommand extends Subcommand implements Synchronizable {
    public BetaPowerupsSubcommand(SlashCommandInteractionEvent event) {
        super(event);
    }

    @Override
    public void execute() {
        if (!this.canInteract()) {
            return;
        }

        try {
            BoarUser boarUser = BoarUserFactory.getBoarUser(this.user);
            boarUser.passSynchronizedAction(this);
        } catch (SQLException exception) {
            SpecialReply.sendErrorMessage(this.interaction, this);
            Log.error(this.user, this.getClass(), "Failed to update data", exception);
            return;
        }

        Log.debug(this.user, this.getClass(), "Given powerups");
        this.interaction.reply("+10 of each powerup").setEphemeral(true)
            .queue(null, e -> ExceptionHandler.replyHandle(this.interaction, this, e));
    }

    @Override
    public void doSynchronizedAction(BoarUser boarUser) {
        try (Connection connection = DataUtil.getConnection()) {
            boarUser.powQuery().addPowerup(connection, "miracle", 10);
            boarUser.powQuery().addPowerup(connection, "gift", 10);
            boarUser.powQuery().addPowerup(connection, "clone", 10);
            boarUser.powQuery().addPowerup(connection, "transmute", 10);
        } catch (SQLException exception) {
            SpecialReply.sendErrorMessage(this.interaction, this);
            Log.error(this.user, this.getClass(), "Failed to give powerups to user", exception);
        }
    }
}
