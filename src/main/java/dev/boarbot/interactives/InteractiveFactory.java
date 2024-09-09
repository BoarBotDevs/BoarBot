package dev.boarbot.interactives;

import dev.boarbot.commands.boar.DailySubcommand;
import dev.boarbot.interactives.boar.megamenu.MegaMenuInteractive;
import dev.boarbot.interactives.boar.megamenu.MegaMenuView;
import dev.boarbot.interactives.boar.daily.DailyNotifyInteractive;
import dev.boarbot.interactives.boar.daily.DailyPowerupInteractive;
import dev.boarbot.interactives.boarmanage.SetupInteractive;
import dev.boarbot.interactives.gift.BoarGiftInteractive;
import dev.boarbot.util.generators.ItemImageGenerator;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.Interaction;

import java.sql.SQLException;
import java.util.List;

public class InteractiveFactory {
    public static synchronized Interactive constructInteractive(
        Interaction initEvent, Class<? extends Interactive> interactiveClass
    ) {
        return InteractiveFactory.constructInteractive(initEvent, false, interactiveClass);
    }

    public static synchronized Interactive constructInteractive(
        Interaction initEvent, boolean isMsg, Class<? extends Interactive> interactiveClass
    ) {
        if (interactiveClass == SetupInteractive.class) {
            return new SetupInteractive(initEvent);
        } else if (interactiveClass == DailyNotifyInteractive.class) {
            return new DailyNotifyInteractive(initEvent);
        } else if (interactiveClass == BoarGiftInteractive.class) {
            return new BoarGiftInteractive(initEvent, isMsg);
        }

        throw new IllegalArgumentException("Not a valid interactive class: " + interactiveClass);
    }

    public static synchronized Interactive constructItemInteractive(
        Interaction interaction,
        List<ItemImageGenerator> itemGens,
        List<String> boarIDs,
        List<Integer> boarEditions,
        boolean isMsg
    ) {
        return new ItemInteractive(interaction, itemGens, boarIDs, boarEditions, isMsg);
    }

    public static synchronized Interactive constructDailyPowerupInteractive(
        SlashCommandInteractionEvent initEvent, DailySubcommand callingObj
    ) {
        return new DailyPowerupInteractive(initEvent, callingObj);
    }

    public static synchronized Interactive constructMegaMenuInteractive(
        SlashCommandInteractionEvent initEvent, MegaMenuView curView
    ) throws SQLException {
        return new MegaMenuInteractive(initEvent, curView);
    }
}
