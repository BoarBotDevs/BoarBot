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
    private String dailyPowAttempt = "";
    private String dailyPowInvalid = "";
    private String dailyPowFailed = "";
    private String dailyUsed = "";
    private String dailyFirstTime = "";
    private String dailyUsedNotify = "";
    private String dailyTitle = "";
    private String firstTitle = "";
    private String firstBoarID = "";
    private String enhanceTitle = "";
    private String cloneTitle = "";
    private String dailyNoBoarFound = "";

    // Give command messages/strings (also badges)

    private String giveBoarChoiceTag = "";
    private String giveBadgeChoiceTag = "";
    private String giveBoar = "";
    private String giveBadID = "";
    private String giveTitle = "";
    private String giveBadge = "";
    private String giveBadgeTitle = "";
    private String giveBadgeHas = "";
    private String giftOpenTitle = "";

    // Collection command strings

    private String collNoBadges = "";
    private String collDateLabel = "";
    private String collUserExtra = "";
    private String collScoreLabel = "";
    private String collTotalLabel = "";
    private String collUniquesLabel = "";
    private String collDailiesLabel = "";
    private String collStreakLabel = "";
    private String collLastDailyLabel = "";
    private String collFavLabel = "";
    private String collRecentLabel = "";
    private String collIndivTotalLabel = "";
    private String collFirstObtainedLabel = "";
    private String collLastObtainedLabel = "";
    private String collDescriptionLabel = "";
    private String collClaimsLabel = "";
    private String collFastestClaimsLabel = "";
    private String collFastestTimeLabel = "";
    private String collBestPromptLabel = "";
    private String collBlessLabel = "";
    private String collGiftsLabel = "";
    private String collClonesLabel = "";
    private String collCellLabel = "";
    private String collMiraclesClaimedLabel = "";
    private String collMiraclesUsedLabel = "";
    private String collMostMiraclesLabel = "";
    private String collHighestMultiLabel = "";
    private String collGiftsClaimedLabel = "";
    private String collGiftsUsedLabel = "";
    private String collGiftsOpenedLabel = "";
    private String collMostGiftsLabel = "";
    private String collClonesClaimedLabel = "";
    private String collClonesUsedLabel = "";
    private String collClonesSuccLabel = "";
    private String collMostClonesLabel = "";
    private String collEnhancersClaimedLabel = "";
    private String collEnhancedLabel = "";
    private String collDataChange = "";
    private String collEnhanceNoBucks = "";
    private String collEnhanceDetails = "";
    private String collEditionTitle = "";
    private String collEditionLine = "";
    private String collDescriptionSB = "";

    // Event strings

    private String eventTitle = "";
    private String eventEndedTitle = "";
    private String eventsDisabled = "";
    private String eventParticipated = "";
    private String eventNobody = "";

    // Powerup strings

    private String powRightFull = "";
    private String powRight = "";
    private String powWrongFirst = "";
    private String powWrongSecond = "";
    private String powWrong = "";
    private String powNoMore = "";
    private String powTop = "";
    private String powTopResult = "";
    private String powAvg = "";
    private String powAvgResult = "";
    private String powAvgResultPlural = "";
    private String powPrompt = "";
    private String powResponse = "";
    private String powResponseShort = "";
    private String powReward = "";

    // Market Strings

    private String marketConfirmInstaBuy = "";
    private String marketUpdatedInstaBuy = "";
    private String marketConfirmInstaSell = "";
    private String marketUpdatedInstaSell = "";
    private String marketInstaComplete = "";
    private String marketConfirmBuyOrder = "";
    private String marketConfirmSellOrder = "";
    private String marketOrderComplete = "";
    private String marketConfirmUpdateIncrease = "";
    private String marketConfirmUpdateDecrease = "";
    private String marketUpdateComplete = "";
    private String marketClaimComplete = "";
    private String marketMaxItems = "";
    private String marketCancelComplete = "";
    private String marketNoRoom = "";
    private String marketMustClaim = "";
    private String marketNoBucks = "";
    private String marketNoEdition = "";
    private String marketNoEditionOrders = "";
    private String marketNoItems = "";
    private String marketNoOrders = "";
    private String marketMaxOrders = "";
    private String marketInvalid = "";
    private String marketWrongEdition = "";
    private String marketTooMany = "";
    private String marketTooHigh = "";
    private String marketEditionHigh = "";
    private String marketHasEdition = "";
    private String marketClosed = "";
    private String marketTooYoung = "";
    private String marketTooCheap = "";
    private String marketTooExpensive = "";
    private String marketBestOrder = "";
    private String marketBSBuyNowLabel = "";
    private String marketBSSellNowLabel = "";
    private String marketBSBuyOrdLabel = "";
    private String marketBSSellOrdLabel = "";
    private String marketOrdSell = "";
    private String marketOrdBuy = "";
    private String marketOrdList = "";
    private String marketOrdExpire = "";
    private String marketOrdPriceLabel = "";
    private String marketOrdFillLabel = "";
    private String marketOrdClaimLabel = "";

    // Powerup strings

    private String giftConfirm = "";
    private String giftFail = "";
    private String giftOut = "";
    private String giftSent = "";
    private String giftNone = "";
    private String giftFrom = "";
    private String giftOpened = "";
    private String giftOpenedWow = "";
    private String miracleConfirm = "";
    private String miracleSuccess = "";
    private String cloneConfirm = "";
    private String cloneFail = "";

    // Notification strings

    private String notificationSuccess = "";
    private String notificationFailed = "";
    private String notificationSuccessReply = "";
    private String notificationDailyReady = "";
    private String notificationStopStr = "";
    private String notificationDisabledStr = "";
    private String[] notificationExtras = {};
    private String notificationServerPing = "";

    // Leaderboard strings

    private String notInBoard = "";
    private String boardHeader = "";
    private String boardFooter = "";
    private String deletedUsername = "";

    // Quest strings

    private String questCompletionBonus = "";
    private String questFullyComplete = "";
    private String questInvFull = "";

    // Report/Self-wipe strings

    private String sentReport = "";
    private String deletedData = "";
    private String cancelDelete = "";
    private String deleteMsgOne = "";
    private String deleteMsgTwo = "";

    // Miscellaneous strings

    private String multiplierName = "";
    private String multiplierPluralName = "";
    private String multiplierSymbol = "";
    private String noParentChannel = "";
    private String notValidChannel = "";
    private String defaultSelectPlaceholder = "";
    private String emptySelect = "";
    private String channelOptionLabel = "";
    private String unavailable = "";
    private String defaultImageName = "";
    private String fontName = "";
    private String commandDebugPrefix = "";
    private String pullLink = "";
    private String githubImg = "";
    private String supportLink = "";
    private String supportStr = "";
    private String[] spookMessages = {};
}
