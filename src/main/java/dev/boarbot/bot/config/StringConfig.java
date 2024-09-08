package dev.boarbot.bot.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link StringConfig StringConfig.java}
 *
 * Stores string configurations for a bot instance.
 *
 * @copyright WeslayCodes & Contributors 2023
 */
@Getter
@Setter
@ToString
public class StringConfig {
    // General interaction responses

    private String noPermission = "";
    private String noSetup = "";
    private String wrongChannel = "";
    private String onCooldown = "";
    private String error = "";
    private String maintenance = "";
    private String bannedString = "";
    private String permBannedString = "";
    private String banSuccess = "";
    private String invalidInput = "";

    // Important names and symbols used widely

    private String mainItemName = "";
    private String mainItemPluralName = "";
    private String bucksName = "";
    private String bucksPluralName = "";
    private String blessingsName = "";
    private String blessingsPluralName = "";
    private String blessingsNameShortened = "";
    private String blessingsPluralNameShortened = "";
    private String blessingsNameVeryShort = "";
    private String blessingsSymbol = "";
    private String blessCategory1 = "";
    private String blessCategory2 = "";
    private String blessCategory3 = "";
    private String blessCategory4 = "";

    // Setup command messages

    private String setupCancelled = "";
    private String setupError = "";
    private String setupFinishedAll = "";
    private String setupExpired = "";
    private String setupUnfinished1 = "";
    private String setupUnfinished2 = "";
    private String setupFinished2 = "";
    private String setupInfoResponse1 = "";
    private String setupInfoResponse2 = "";

    // Daily command messages/strings

    private String dailyPow = "";
    private String dailyUsed = "";
    private String dailyFirstTime = "";
    private String dailyUsedNotify = "";
    private String dailyTitle = "";
    private String firstTitle = "";
    private String dailyNoBoarFound = "";

    // Mega Menu command strings

    private String megaMenuNoBadges = "";
    private String megaMenuDateLabel = "";
    private String profileTotalLabel = "";
    private String profileDailiesLabel = "";
    private String profileUniquesLabel = "";
    private String profileStreakLabel = "";
    private String profileNextDailyLabel = "";
    private String profileDailyReady = "";
    private String profileQuestResetLabel = "";
    private String profileFavLabel = "";
    private String profileRecentLabel = "";
    private String compBlocked = "";
    private String compFavoriteSuccess = "";
    private String compUnfavoriteSuccess = "";
    private String compCloneConfirmOne = "";
    private String compCloneConfirmMultiple = "";
    private String compCloneTooMany = "";
    private String compCloneSuccess = "";
    private String compCloneFailed = "";
    private String compCloneTitle = "";
    private String compNoBoar = "";
    private String compTransmuteConfirm = "";
    private String compTransmuteSuccess = "";
    private String compTransmuteFirst = "";
    private String compTransmuteTitle = "";
    private String compAmountLabel = "";
    private String compOldestLabel = "";
    private String compNewestLabel = "";
    private String compSpeciesLabel = "";
    private String compNoSpecies = "";
    private String compUpdateLabel = "";
    private String compDefaultUpdate = "";
    private String statsTotalLabel = "";
    private String statsPeakLabel = "";
    private String statsUsedLabel = "";
    private String statsDailiesLabel = "";
    private String statsDailiesMissedLabel = "";
    private String statsLastDailyLabel = "";
    private String statsLastBoarLabel = "";
    private String statsFavBoarLabel = "";
    private String statsUniquesLabel = "";
    private String statsStreakLabel = "";
    private String statsNotificationLabel = "";
    private String statsBlessLabel = "";
    private String statsPowAttemptsLabel = "";
    private String statsPowWinsLabel = "";
    private String statsPowPerfectLabel = "";
    private String statsPowFastestLabel = "";
    private String statsPowAvgLabel = "";
    private String statsPowBestLabel1 = "";
    private String statsPowBestLabel2 = "";
    private String statsPowBestLabel3 = "";
    private String statsMiraclesActiveLabel = "";
    private String statsMiracleRollsLabel = "";
    private String statsPeakMiracleRollLabel = "";
    private String statsBestBucksLabel = "";
    private String statsBestRarityLabel = "";
    private String statsTransmuteLastLabel = "";
    private String statsTransmutedLabel = "";
    private String statsCloneLastLabel = "";
    private String statsClonedLabel = "";
    private String statsGiftHandicapLabel = "";
    private String statsGiftOpenedLabel = "";
    private String statsGiftFastestLabel = "";
    private String statsQuestsCompletedLabel = "";
    private String statsFullQuestsCompletedLabel = "";
    private String statsFastestFullQuestLabel = "";
    private String statsQuestAutoClaimLabel = "";
    private String statsEasyQuestsLabel = "";
    private String statsMediumQuestsLabel = "";
    private String statsHardQuestsLabel = "";
    private String statsVeryHardQuestsLabel = "";
    private String powCellLabel = "";
    private String powCellAmtLabel = "";
    private String powCellEmptyLabel = "";
    private String powCellErrorLabel = "";
    private String powCellDriftLabel = "";
    private String powCannotUse = "";
    private String powMiracleSuccess = "";

    // Notification strings

    private String notificationSuccess = "";
    private String notificationFailed = "";
    private String notificationSuccessReply = "";
    private String notificationDailyReady = "";
    private String notificationStopStr = "";
    private String notificationDisabledStr = "";
    private String[] notificationExtras = {};

    // Report/Self-wipe strings

    private String sentReport = "";
    private String deletedData = "";
    private String cancelDelete = "";
    private String deleteMsgOne = "";
    private String deleteMsgTwo = "";

    // Miscellaneous strings

    private String noPow = "";
    private String miracleAttempt = "";
    private String unavailable = "";
    private String pullLink = "";
    private String githubImg = "";
    private String supportLink = "";
    private String supportStr = "";
    private String[] spookMessages = {};
}
