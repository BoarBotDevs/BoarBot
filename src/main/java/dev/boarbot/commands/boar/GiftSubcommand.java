package dev.boarbot.commands.boar;

import dev.boarbot.commands.Subcommand;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.BoarUserFactory;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.interactives.InteractiveFactory;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.generators.EmbedImageGenerator;
import dev.boarbot.util.interaction.SpecialReply;
import dev.boarbot.util.logging.ExceptionHandler;
import dev.boarbot.util.logging.Log;
import dev.boarbot.util.time.TimeUtil;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;


public class GiftSubcommand extends Subcommand {
    public GiftSubcommand(SlashCommandInteractionEvent event) {
        super(event);
    }

    @Override
    public void execute() {
        if (!this.canInteract()) {
            return;
        }

        boolean noGift = true;
        boolean giftSent = true;

        try (Connection connection = DataUtil.getConnection()) {
            BoarUser boarUser = BoarUserFactory.getBoarUser(this.user);
            noGift = boarUser.powQuery().getPowerupAmount(connection, "gift") == 0;
            giftSent = boarUser.powQuery().getLastGiftSent(connection) > TimeUtil.getCurMilli() - NUMS.getGiftIdle();

            if (!noGift && !giftSent) {
                boarUser.powQuery().setLastGiftSent(connection, TimeUtil.getCurMilli());
            }
        } catch (SQLException exception) {
            SpecialReply.sendErrorMessage(this.interaction, this);
            Log.error(this.user, this.getClass(), "Failed to get number of gifts", exception);
        }

        if (noGift) {
            try {
                String replyStr = STRS.getNoPow().formatted(POWS.get("gift").getPluralName());
                FileUpload fileUpload = new EmbedImageGenerator(replyStr).generate().getFileUpload();
                MessageCreateBuilder messageBuilder = new MessageCreateBuilder().setFiles(fileUpload);

                this.interaction.reply(messageBuilder.build()).setEphemeral(true)
                    .queue(null, e -> ExceptionHandler.replyHandle(this.interaction, this, e));

                Log.debug(this.user, this.getClass(), "Failed to gift: Not enough");
            } catch (IOException exception) {
                SpecialReply.sendErrorMessage(this.interaction, this);
                Log.error(this.user, this.getClass(), "Failed to generate no gifts message", exception);
            }

            return;
        }

        if (giftSent) {
            try {
                FileUpload fileUpload = new EmbedImageGenerator(STRS.getGiftAlreadySent()).generate().getFileUpload();
                MessageCreateBuilder messageBuilder = new MessageCreateBuilder().setFiles(fileUpload);

                this.interaction.reply(messageBuilder.build()).setEphemeral(true)
                    .queue(null, e -> ExceptionHandler.replyHandle(this.interaction, this, e));

                Log.debug(this.user, this.getClass(), "Failed to gift: Already sent");
            } catch (IOException exception) {
                SpecialReply.sendErrorMessage(this.interaction, this);
                Log.error(this.user, this.getClass(), "Failed to generate gift sent message", exception);
            }

            return;
        }

        this.interaction.deferReply().queue(null, e -> ExceptionHandler.deferHandle(this.interaction, this, e));

        Interactive interactive = InteractiveFactory.constructGiftInteractive(this.event, false);
        interactive.execute(null);
        Log.debug(this.user, this.getClass(), "Sent BoarGiftInteractive");
    }
}
