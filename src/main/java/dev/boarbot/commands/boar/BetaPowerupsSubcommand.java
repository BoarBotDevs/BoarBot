package dev.boarbot.commands.boar;

import dev.boarbot.commands.Subcommand;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.BoarUserFactory;
import dev.boarbot.events.PowerupEventHandler;
import dev.boarbot.util.data.DataUtil;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
public class BetaPowerupsSubcommand extends Subcommand {
    public BetaPowerupsSubcommand(SlashCommandInteractionEvent event) {
        super(event);
    }

    @Override
    public void execute() {
        new PowerupEventHandler().sendEvent();

        try (Connection connection = DataUtil.getConnection()) {
            BoarUser boarUser = BoarUserFactory.getBoarUser(this.user);

            boarUser.addPowerup(connection, "miracle", 10);
            boarUser.addPowerup(connection, "gift", 10);
            boarUser.addPowerup(connection, "clone", 10);
            boarUser.addPowerup(connection, "transmute", 10);

            boarUser.decRefs();
        } catch (SQLException exception) {
            log.error("Failed to give user powerups", exception);
        }

        this.interaction.reply("+10 of each powerup").setEphemeral(true).complete();
    }
}
