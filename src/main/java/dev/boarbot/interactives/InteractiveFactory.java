package dev.boarbot.interactives;

import dev.boarbot.commands.boar.DailySubcommand;
import dev.boarbot.interactives.boar.daily.DailyInteractive;
import dev.boarbot.interactives.boar.daily.DailyNotifyInteractive;
import dev.boarbot.interactives.boar.daily.DailyPowerupInteractive;
import dev.boarbot.interactives.boarmanage.SetupInteractive;
import dev.boarbot.util.generators.ItemImageGenerator;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.List;

public class InteractiveFactory {
    public static synchronized Interactive constructInteractive(
        SlashCommandInteractionEvent initEvent, Class<? extends Interactive> interactiveClass
    ) {
        if (interactiveClass == SetupInteractive.class) {
            return new SetupInteractive(initEvent);
        } else if (interactiveClass == DailyNotifyInteractive.class) {
            return new DailyNotifyInteractive(initEvent);
        }

        throw new IllegalArgumentException("Not a valid interactive class: " + interactiveClass);
    }

    public static synchronized Interactive constructDailyInteractive(
        SlashCommandInteractionEvent initEvent,
        List<ItemImageGenerator> itemGens,
        List<String> boarIDs,
        List<Integer> boarEditions
    ) {
        return new DailyInteractive(initEvent, itemGens, boarIDs, boarEditions);
    }

    public static synchronized Interactive constructDailyPowerupInteractive(
        SlashCommandInteractionEvent initEvent, DailySubcommand callingObj
    ) {
        return new DailyPowerupInteractive(initEvent, callingObj);
    }
}
