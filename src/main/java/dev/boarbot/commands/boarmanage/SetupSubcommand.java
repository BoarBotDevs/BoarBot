package dev.boarbot.commands.boarmanage;

import dev.boarbot.commands.Subcommand;
import dev.boarbot.interactives.boarmanage.SetupInteractive;
import dev.boarbot.util.generators.EmbedGenerator;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

@Log4j2
public class SetupSubcommand extends Subcommand {
    public SetupSubcommand(SlashCommandInteractionEvent event) {
        super(event);
    }

    @Override
    public void execute() {
        this.interaction.deferReply().setEphemeral(true).queue();

        SetupInteractive interactive = new SetupInteractive(this.event);

        EmbedGenerator embedGen = new EmbedGenerator(this.config.getStringConfig().getSetupUnfinished1());
        FileUpload embed;

        try {
            embed = embedGen.generate();
        } catch (Exception exception) {
            log.error("Failed to create file from image data!", exception);
            return;
        }

        MessageEditBuilder editedMsg = new MessageEditBuilder()
            .setFiles(embed)
            .setComponents(interactive.getCurComponents());

        this.interaction.getHook().editOriginal(editedMsg.build()).queue();
    }
}
