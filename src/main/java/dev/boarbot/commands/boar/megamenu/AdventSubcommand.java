package dev.boarbot.commands.boar.megamenu;

import dev.boarbot.commands.Subcommand;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.interactives.InteractiveFactory;
import dev.boarbot.interactives.boar.megamenu.MegaMenuView;
import dev.boarbot.util.logging.ExceptionHandler;
import dev.boarbot.util.logging.Log;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class AdventSubcommand extends Subcommand {
    public enum RewardType {
        BUCKS,
        BLESSINGS,
        CELESTICON,
        POWERUP,
        FESTIVE,
        EVENT
    }

    public AdventSubcommand(SlashCommandInteractionEvent event) {
        super(event);
    }

    @Override
    public void execute() {
        if (!this.canInteract()) {
            return;
        }

        this.interaction.deferReply().queue(null, e -> ExceptionHandler.deferHandle(this.interaction, this, e));

        Interactive interactive = InteractiveFactory.constructMegaMenuInteractive(this.event, MegaMenuView.ADVENT);
        interactive.execute(null);
        Log.debug(this.user, this.getClass(), "Sent MegaMenuInteractive (Advent)");
    }

    public static RewardType getBaseRewardType(int dayOfMonth) {
        dayOfMonth -= 1;

        if (dayOfMonth % 5 == 0 && dayOfMonth / 5 % 2 == 0 || dayOfMonth % 5 == 1 && dayOfMonth / 5 % 2 == 1) {
            return RewardType.BUCKS;
        }

        if (dayOfMonth % 5 == 0 && dayOfMonth / 5 % 2 == 1 || dayOfMonth % 5 == 1 && dayOfMonth / 5 % 2 == 0) {
            return RewardType.BLESSINGS;
        }

        if (dayOfMonth % 5 == 2 || dayOfMonth % 5 == 3 && dayOfMonth / 5 % 2 == 1) {
            return RewardType.CELESTICON;
        }

        if (dayOfMonth % 5 == 4 && dayOfMonth / 5 % 2 == 0) {
            return RewardType.FESTIVE;
        }

        return RewardType.POWERUP;
    }
}
