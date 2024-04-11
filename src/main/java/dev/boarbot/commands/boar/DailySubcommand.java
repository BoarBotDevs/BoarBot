package dev.boarbot.commands.boar;

import dev.boarbot.commands.Subcommand;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.util.generators.ItemImageGenerator;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.IOException;

@Log4j2
public class DailySubcommand extends Subcommand {
    public DailySubcommand(SlashCommandInteractionEvent event) {
        super(event);
    }

    @Override
    public void execute() throws InterruptedException {
        BoarUser boarUser;

        try {
            boarUser = new BoarUser(this.user, true);
        } catch (IOException exception) {
            log.error("Failed to create user (%s) file.".formatted(this.user.getId()), exception);
            return;
        }

        ItemImageGenerator itemGen = new ItemImageGenerator(
            this.config, this.event.getUser(), "Daily Boar!", "ascii"
        );

        FileUpload image;

        try {
            image = itemGen.generate();
        } catch (IOException exception) {
            log.error("Failed to create file from image data!", exception);
            return;
        }

        this.interaction.replyFiles(image)
            .setEphemeral(true)
            .queue();
    }
}
