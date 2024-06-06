package dev.boarbot.bot.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link PathConfig PathConfig.java}
 *
 * Stores path configurations for a bot instance.
 *
 * @copyright WeslayCodes & Contributors 2023
 */
@Getter
@Setter
@ToString
public class PathConfig {
    // Path of folder storing all {@link Listener listeners}
    private String listeners = "";

    // Path of folder storing all {@link Command commands}
    private String commands = "";

    // Data folder/file paths

    private String databaseFolder = "";
    private String guildDataFolder = "";
    private String userDataFolder = "";
    private String globalDataFolder = "";
    private String itemDataFileName = "";
    private String leaderboardsFileName = "";
    private String bannedUsersFileName = "";
    private String powerupDataFileName = "";
    private String questDataFileName = "";
    private String wipeUsersFileName = "";
    private String githubFileName = "";
    private String logsFolder = "";

    // Production paths

    private String prodStartScript = "";
    private String prodRemotePath = "";

    // Base paths for images/assets

    private String boars = "";
    private String badges = "";
    private String powerups = "";
    private String itemAssets = "";
    private String tempItemAssets = "";
    private String collAssets = "";
    private String otherAssets = "";
    private String fontAssets = "";

    // Image/asset file names for item attachments (boars and badges)

    private String itemOverlay = "";
    private String itemUnderlay = "";
    private String itemBackplate = "";
    private String itemShadowLeft = "";
    private String itemShadowRight = "";

    // Image/asset file names for collection images

    private String collUnderlay = "";
    private String collDetailOverlay = "";
    private String collDetailUnderlay = "";
    private String collPowerOverlay = "";
    private String collPowerUnderlay = "";
    private String collPowerUnderlay2 = "";
    private String collPowerUnderlay3 = "";
    private String collEnhanceUnderlay = "";
    private String collGiftUnderlay = "";
    private String cellNone = "";
    private String cellCommon = "";
    private String cellUncommon = "";
    private String cellRare = "";
    private String cellEpic = "";
    private String cellLegendary = "";
    private String cellMythic = "";
    private String cellDivine = "";
    private String favorite = "";

    // Image/asset file names for market images

    private String marketOverviewUnderlay = "";
    private String marketOverviewOverlay = "";
    private String marketBuySellUnderlay = "";
    private String marketBuySellOverlay = "";
    private String marketOrdersUnderlay = "";

    // Image/asset file names for help images

    private String helpGeneral1 = "";
    private String helpGeneral2 = "";
    private String helpPowerup1 = "";
    private String helpPowerup2 = "";
    private String helpMarket1 = "";
    private String helpMarket2 = "";
    private String helpBadgeBoar1 = "";
    private String helpBadgeBoar2 = "";
    private String helpBadgeBoar3 = "";

    // Miscellaneous image/asset file names

    private String eventUnderlay = "";
    private String questsUnderlay = "";
    private String leaderboardUnderlay = "";
    private String mainFont = "";
    private String circleMask = "";
    private String bucks = "";
    private String powerup = "";
    private String check = "";
    private String noAvatar = "";
    private String guessInterpreterFile = "";

    // Python scripts

    private String dynamicImageScript = "";
    private String userOverlayScript = "";
}
