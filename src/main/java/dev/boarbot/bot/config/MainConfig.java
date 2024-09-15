package dev.boarbot.bot.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MainConfig {
    /**
     * All user IDs associated with developers
     */
    private String[] devs = {};

    /**
     * The guild ID the bot uses to deploy dev commands
     */
    private String devGuild = "";

    /**
     * The text channel ID the bot sends certain logs to
     */
    private String logChannel = "";

    /**
     * The forum channel ID the bot sends reports to
     */
    private String reportsChannel = "";

    /**
     * The text channel ID the bot attempts to ping for notifications (LEGACY SUPPORT)
     */
    private String pingChannel = "";

    /**
     * The text channel ID the channel to send spook messages to
     */
    private String spookChannel = "";

    /**
     * Boars can be obtained without waiting for the next day
     */
    private boolean unlimitedBoars = false;

    /**
     * Debug messages should be sent to logs
     */
    private boolean debugMode = true;

    /**
     * Bot is in maintenance mode
     */
    private boolean maintenanceMode = false;

    /**
     * Market can be opened using /boar market
     */
    private boolean marketOpen = false;

    /**
     * The ID of the boar that is given when a user gets the first of a boar
     */
    private String firstBoarID = "";
}
