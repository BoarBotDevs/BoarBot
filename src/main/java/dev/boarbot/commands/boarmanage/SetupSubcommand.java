package dev.boarbot.commands.boarmanage;

import dev.boarbot.commands.Subcommand;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.interactives.InteractiveFactory;
import dev.boarbot.interactives.boarmanage.SetupInteractive;
import dev.boarbot.util.generators.EmbedGenerator;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

@Slf4j
public class SetupSubcommand extends Subcommand {
    public SetupSubcommand(SlashCommandInteractionEvent event) {
        super(event);
    }

    @Override
    public void execute() {
        this.interaction.deferReply().setEphemeral(true).queue();

        Interactive interactive = InteractiveFactory.constructInteractive(this.event, SetupInteractive.class);

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

        if (interactive.isStopped()) {
            return;
        }

        this.interaction.getHook().editOriginal(editedMsg.build()).queue();
    }
}
