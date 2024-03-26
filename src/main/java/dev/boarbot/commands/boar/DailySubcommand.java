package dev.boarbot.commands.boar;

import dev.boarbot.commands.Subcommand;
import dev.boarbot.entities.boaruser.BoarUser;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

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
            log.error("Failed to create boar user");
            return;
        }

        log.info(boarUser.getData().getStats().getGeneral().getTotalBoars());

        this.interaction.reply(this.config.getStringConfig().getDailyTitle()).setEphemeral(true).queue();
    }
}
