package dev.boarbot.commands.boar;

import dev.boarbot.commands.Subcommand;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Log4j2
public class DailySubcommand extends Subcommand {
    public DailySubcommand(SlashCommandInteractionEvent event) {
        super(event);
    }

    @Override
    public void execute() {
        this.interaction.reply(this.config.getStringConfig().getDailyTitle()).setEphemeral(true).queue();
    }
}
