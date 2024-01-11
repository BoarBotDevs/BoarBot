/**
 * {@link PathConfig PathConfig.ts}
 *
 * Stores path configurations for a bot instance.
 *
 * @copyright WeslayCodes & Contributors 2023
 */
export class PathConfig {
    readonly [pathKey: string]: string;

    // Path of folder storing all {@link Listener listeners}
    public readonly listeners = '' as string;

    // Path of folder storing all {@link Command commands}
    public readonly commands = '' as string;

    // Data folder/file paths

    public readonly databaseFolder = '' as string;
    public readonly guildDataFolder = '' as string;
    public readonly userDataFolder = '' as string;
    public readonly globalDataFolder = '' as string;
    public readonly itemDataFileName = '' as string;
    public readonly leaderboardsFileName = '' as string;
    public readonly bannedUsersFileName = '' as string;
    public readonly powerupDataFileName = '' as string;
    public readonly questDataFileName = '' as string;
    public readonly wipeUsersFileName = '' as string;
    public readonly githubFileName = '' as string;
    public readonly logsFolder = '' as string;

    // Production paths

    public readonly prodStartScript = '' as string;
    public readonly prodRemotePath = '' as string;

    // Base paths for images/assets

    public readonly boars = '' as string;
    public readonly badges = '' as string;
    public readonly powerups = '' as string;
    public readonly itemAssets = '' as string;
    public readonly tempItemAssets = '' as string;
    public readonly collAssets = '' as string;
    public readonly otherAssets = '' as string;
    public readonly fontAssets = '' as string;

    // Image/asset file names for item attachments (boars and badges)

    public readonly itemOverlay = '' as string;
    public readonly itemUnderlay = '' as string;
    public readonly itemBackplate = '' as string;

    // Image/asset file names for collection images

    public readonly collUnderlay = '' as string;
    public readonly collDetailOverlay = '' as string;
    public readonly collDetailUnderlay = '' as string;
    public readonly collPowerOverlay = '' as string;
    public readonly collPowerUnderlay = '' as string;
    public readonly collPowerUnderlay2 = '' as string;
    public readonly collPowerUnderlay3 = '' as string;
    public readonly collEnhanceUnderlay = '' as string;
    public readonly collGiftUnderlay = '' as string;
    public readonly cellNone = '' as string;
    public readonly cellCommon = '' as string;
    public readonly cellUncommon = '' as string;
    public readonly cellRare = '' as string;
    public readonly cellEpic = '' as string;
    public readonly cellLegendary = '' as string;
    public readonly cellMythic = '' as string;
    public readonly cellDivine = '' as string;
    public readonly favorite = '' as string;

    // Image/asset file names for market images

    public readonly marketOverviewUnderlay = '' as string;
    public readonly marketOverviewOverlay = '' as string;
    public readonly marketBuySellUnderlay = '' as string;
    public readonly marketBuySellOverlay = '' as string;
    public readonly marketOrdersUnderlay = '' as string;

    // Image/asset file names for help images

    public readonly helpGeneral1 = '' as string;
    public readonly helpGeneral2 = '' as string;
    public readonly helpPowerup1 = '' as string;
    public readonly helpPowerup2 = '' as string;
    public readonly helpMarket1 = '' as string;
    public readonly helpMarket2 = '' as string;
    public readonly helpBadgeBoar1 = '' as string;
    public readonly helpBadgeBoar2 = '' as string;
    public readonly helpBadgeBoar3 = '' as string;

    // Miscellaneous image/asset file names

    public readonly eventUnderlay = '' as string;
    public readonly questsUnderlay = '' as string;
    public readonly leaderboardUnderlay = '' as string;
    public readonly mainFont = '' as string;
    public readonly circleMask = '' as string;
    public readonly bucks = '' as string;
    public readonly powerup = '' as string;
    public readonly check = '' as string;
    public readonly noAvatar = '' as string;
    public readonly guessInterpreterFile = '' as string;

    // Python scripts

    public readonly dynamicImageScript = '' as string;
    public readonly userOverlayScript = '' as string;
}
