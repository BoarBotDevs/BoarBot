package dev.boarbot.commands.boar;

import dev.boarbot.commands.Subcommand;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.BoarUserFactory;
import dev.boarbot.interactives.gift.BoarGiftInteractive;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.interactives.InteractiveFactory;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.generators.EmbedImageGenerator;
import dev.boarbot.util.logging.Log;
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

        try (Connection connection = DataUtil.getConnection()) {
            BoarUser boarUser = BoarUserFactory.getBoarUser(this.user);
            noGift = boarUser.powQuery().getPowerupAmount(connection, "gift") == 0;
        } catch (SQLException exception) {
            Log.error(this.user, this.getClass(), "Failed to get number of gifts", exception);
        }

        if (noGift) {
            try {
                String replyStr = STRS.getNoPow().formatted(POWS.get("gift").getPluralName());
                FileUpload fileUpload = new EmbedImageGenerator(replyStr).generate().getFileUpload();
                MessageCreateBuilder messageBuilder = new MessageCreateBuilder().setFiles(fileUpload);

                this.interaction.reply(messageBuilder.build()).setEphemeral(true).complete();
            } catch (IOException exception) {
                Log.error(this.user, this.getClass(), "Failed to generate no gifts message", exception);
            }

            return;
        }

        this.interaction.deferReply().complete();

        Log.debug(this.user, this.getClass(), "Sending BoarGiftInteractive");
        Interactive interactive = InteractiveFactory.constructInteractive(this.event, BoarGiftInteractive.class);
        interactive.execute(null);
    }
}
