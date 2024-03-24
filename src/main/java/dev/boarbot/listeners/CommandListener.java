package dev.boarbot.listeners;

import dev.boarbot.commands.Subcommand;
import dev.boarbot.commands.boar.*;
import dev.boarbot.commands.boardev.BanSubcommand;
import dev.boarbot.commands.boardev.GiveSubcommand;
import dev.boarbot.commands.boarmanage.SetupSubcommand;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@Log4j2
public class CommandListener extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        Subcommand subcommand = null;

        switch (event.getName()) {
            case "boar" -> subcommand = switch(event.getSubcommandName()) {
                case "collection" -> new CollectionSubcommand(event);
                case "daily" -> new DailySubcommand(event);
                case "gift" -> new GiftSubcommand(event);
                case "guess" -> new GuessSubcommand(event);
                case "help" -> new HelpSubcommand(event);
                case "market" -> new MarketSubcommand(event);
                case "quests" -> new QuestsSubcommand(event);
                case "report" -> new ReportSubcommand(event);
                case "support" -> new SupportSubcommand(event);
                case "top" -> new TopSubcommand(event);
                case null, default -> null;
            };
            case "boar-dev" -> subcommand = switch (event.getSubcommandName()) {
                case "ban" -> new BanSubcommand(event);
                case "give" -> new GiveSubcommand(event);
                case null, default -> null;
            };
            case "boar-manage" -> subcommand = switch (event.getSubcommandName()) {
                case "setup" -> new SetupSubcommand(event);
                case null, default -> null;
            };
        }

        if (subcommand == null) {
            log.error(
                "Command '/%s %s' was run but listener for it does not exist!".formatted(
                    event.getName(), event.getSubcommandName()
                )
            );

            return;
        }

        log.info(
            "[%s] (%s) ran '/%s %s'".formatted(
                event.getUser().getName(), event.getUser().getId(), event.getName(), event.getSubcommandName()
            )
        );

        subcommand.execute();
    }
}
