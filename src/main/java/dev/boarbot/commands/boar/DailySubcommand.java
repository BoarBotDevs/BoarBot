package dev.boarbot.commands.boar;

import dev.boarbot.commands.Subcommand;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.BoarUserFactory;
import dev.boarbot.util.generators.ItemImageGenerator;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Log4j2
public class DailySubcommand extends Subcommand {
    public DailySubcommand(SlashCommandInteractionEvent event) {
        super(event);
    }

    @Override
    public void execute() throws InterruptedException {
        this.interaction.deferReply().queue();
        this.doDaily();
    }

    private void doDaily() {
        BoarUser boarUser;

        try {
            boarUser = BoarUserFactory.getBoarUser(this.user, true);
        } catch (IOException exception) {
            log.error("Failed to create user (%s) file.".formatted(this.user.getId()), exception);
            return;
        }

        List<String> boarIDs = new ArrayList<>();
        boarIDs.add("creepy");

        List<Integer> scores = new ArrayList<>();
        scores.add(50);

        try {
            boarUser.addBoars(boarIDs, scores);
        } catch (IOException exception) {
            log.error("Failed to add boar to user (%s) file.".formatted(this.user.getId()), exception);
            return;
        }

        ItemImageGenerator itemGen = new ItemImageGenerator(
            this.event.getUser(), "Daily Boar!", "creepy", false, null, 20
        );

        FileUpload image;

        try {
            image = itemGen.generate();
        } catch (Exception exception) {
            log.error("Failed to create file from image data!", exception);
            return;
        }

        this.interaction.getHook().editOriginalAttachments(image).queue();
    }
}
