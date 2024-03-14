package dev.boarbot.bot.config.commands;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link BoarCommandsConfig BoarCommandsConfig.java}
 *
 * Stores configurations for the {@link dev.boarbot.commands.BoarCommands boar command}
 * for a bot instance.
 *
 * @copyright WeslayCodes & Contributors 2023
 */
@Getter
@Setter
@ToString
public class BoarCommandsConfig {
    /**
     * {@link SubcommandConfig Subcommand information} for {@link HelpSubcommand}
     */
    public SubcommandConfig help = new SubcommandConfig();

    /**
     * {@link SubcommandConfig Subcommand information} for {@link DailySubcommand}
     */
    public SubcommandConfig daily = new SubcommandConfig();

    /**
     * {@link SubcommandConfig Subcommand information} for {@link CollectionSubcommand}
     */
    public SubcommandConfig collection = new SubcommandConfig();

    /**
     * {@link SubcommandConfig Subcommand information} for {@link TopSubcommand}
     */
    public SubcommandConfig top = new SubcommandConfig();

    /**
     * {@link SubcommandConfig Subcommand information} for {@link MarketSubcommand}
     */
    public SubcommandConfig market = new SubcommandConfig();

    /**
     * {@link SubcommandConfig Subcommand information} for {@link ReportSubcommand}
     */
    public SubcommandConfig report = new SubcommandConfig();

    /**
     * {@link SubcommandConfig Subcommand information} for {@link SelfWipeSubcommand}
     */
    public SubcommandConfig selfWipe = new SubcommandConfig();

    /**
     * {@link SubcommandConfig Subcommand information} for {@link GiftSubcommand}
     */
    public SubcommandConfig gift = new SubcommandConfig();

    /**
     * {@link SubcommandConfig Subcommand information} for {@link QuestsSubcommand}
     */
    public SubcommandConfig quests = new SubcommandConfig();

    /**
     * {@link SubcommandConfig Subcommand information} for {@link SupportSubcommand}
     */
    public SubcommandConfig support = new SubcommandConfig();

    /**
     * {@link SubcommandConfig Subcommand information} for {@link GuessSubcommand}
     */
    public SubcommandConfig guess = new SubcommandConfig();
}
