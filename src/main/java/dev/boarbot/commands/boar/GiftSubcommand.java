package dev.boarbot.commands.boar;

import dev.boarbot.commands.Subcommand;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.BoarUserFactory;
import dev.boarbot.interactives.gift.BoarGiftInteractive;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.interactives.InteractiveFactory;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.generators.EmbedImageGenerator;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
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
            noGift = boarUser.getPowerupAmount(connection, "gift") == 0;
            boarUser.decRefs();
        } catch (SQLException exception) {
            log.error("Failed to get user's gift amount", exception);
        }

        if (noGift) {
            try {
                MessageCreateBuilder messageBuilder = new MessageCreateBuilder().setFiles(
                    new EmbedImageGenerator(
                        this.config.getStringConfig().getNoPow().formatted(
                            this.config.getItemConfig().getPowerups().get("gift").getPluralName()
                        )
                    ).generate().getFileUpload()
                );

                this.interaction.reply(messageBuilder.build()).setEphemeral(true).complete();
            } catch (IOException exception) {
                log.error("Failed to generate no gift message", exception);
            }

            return;
        }

        this.interaction.deferReply().complete();
        Interactive interactive = InteractiveFactory.constructInteractive(this.event, BoarGiftInteractive.class);
        interactive.execute(null);
    }
}
