package dev.boarbot.commands.boar;

import dev.boarbot.commands.Subcommand;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.BoarUserFactory;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.interactives.InteractiveFactory;
import dev.boarbot.interactives.boar.market.MarketInteractive;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.generators.EmbedImageGenerator;
import dev.boarbot.util.interaction.SpecialReply;
import dev.boarbot.util.logging.ExceptionHandler;
import dev.boarbot.util.logging.Log;
import dev.boarbot.util.time.TimeUtil;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class MarketSubcommand extends Subcommand {
    public MarketSubcommand(SlashCommandInteractionEvent event) {
        super(event);
    }

    @Override
    public void execute() {
        if (!this.canInteract()) {
            return;
        }

        if (!CONFIG.getMainConfig().isMarketOpen()) {
            try {
                FileUpload fileUpload = new EmbedImageGenerator(STRS.getMarketClosed(), COLORS.get("error")).generate()
                    .getFileUpload();
                this.interaction.replyFiles(fileUpload).setEphemeral(true)
                    .queue(null, e -> ExceptionHandler.replyHandle(this.interaction, this, e));
            } catch (IOException exception) {
                SpecialReply.sendErrorMessage(this.interaction, this);
                Log.error(this.user, this.getClass(), "Failed to generate market closed message", exception);
            }

            return;
        }

        try (Connection connection = DataUtil.getConnection()) {
            BoarUser boarUser = BoarUserFactory.getBoarUser(this.user);
            long bannedTimestamp = boarUser.baseQuery().getBannedTime(connection);

            if (bannedTimestamp > TimeUtil.getCurMilli()) {
                String bannedStr = STRS.getBannedString().formatted(TimeUtil.getTimeDistance(bannedTimestamp, false));
                FileUpload fileUpload = new EmbedImageGenerator(bannedStr, COLORS.get("error")).generate()
                    .getFileUpload();

                this.interaction.replyFiles(fileUpload).setEphemeral(true)
                    .queue(null, e -> ExceptionHandler.replyHandle(this.interaction, this, e));
                return;
            }
        } catch (SQLException exception) {
            SpecialReply.sendErrorMessage(this.interaction, this);
            Log.error(this.user, this.getClass(), "Failed to do ban check", exception);
            return;
        } catch (IOException exception) {
            SpecialReply.sendErrorMessage(this.interaction, this);
            Log.error(this.user, this.getClass(), "Failed to generate banned message", exception);
            return;
        }

        this.interaction.deferReply(true).queue(null, e -> ExceptionHandler.deferHandle(this.interaction, this, e));

        Interactive interactive = InteractiveFactory.constructInteractive(this.event, MarketInteractive.class);
        interactive.execute(null);
        Log.debug(this.user, this.getClass(), "Sent MarketInteractive");
    }
}
