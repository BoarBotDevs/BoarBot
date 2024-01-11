/**
 * {@link StringConfig StringConfig.ts}
 *
 * Stores string configurations for a bot instance.
 *
 * @copyright WeslayCodes & Contributors 2023
 */
export class StringConfig {
    // General interaction responses

    public readonly noPermission = '' as string;
    public readonly noSetup = '' as string;
    public readonly doingSetup = '' as string;
    public readonly wrongChannel = '' as string;
    public readonly nullFound = '' as string;
    public readonly onCooldown = '' as string;
    public readonly error = '' as string;
    public readonly maintenance = '' as string;
    public readonly bannedString = '' as string;
    public readonly permBannedString = '' as string;
    public readonly banSuccess = '' as string;

    // Setup command messages

    public readonly setupCancelled = '' as string;
    public readonly setupError = '' as string;
    public readonly setupFinishedAll = '' as string;
    public readonly setupExpired = '' as string;
    public readonly setupUnfinished1 = '' as string;
    public readonly setupUnfinished2 = '' as string;
    public readonly setupFinished1 = '' as string;
    public readonly setupFinished2 = '' as string;
    public readonly setupInfoResponse1 = '' as string;
    public readonly setupInfoResponse2 = '' as string;

    // Daily command messages/strings

    public readonly dailyPowUsed = '' as string;
    public readonly dailyPowFailed = '' as string;
    public readonly dailyUsed = '' as string;
    public readonly dailyFirstTime = '' as string;
    public readonly dailyBonus = '' as string;
    public readonly dailyUsedNotify = '' as string;
    public readonly dailyTitle = '' as string;
    public readonly extraTitle = '' as string;
    public readonly enhanceTitle = '' as string;
    public readonly cloneTitle = '' as string;
    public readonly dailyNoBoarFound = '' as string;

    // Give command messages/strings (also badges)

    public readonly giveBoarChoiceTag = '' as string;
    public readonly giveBadgeChoiceTag = '' as string;
    public readonly giveBoar = '' as string;
    public readonly giveBadID = '' as string;
    public readonly giveTitle = '' as string;
    public readonly giveBadge = '' as string;
    public readonly giveBadgeTitle = '' as string;
    public readonly giveBadgeHas = '' as string;
    public readonly giftOpenTitle = '' as string;

    // Collection command strings

    public readonly collNoBadges = '' as string;
    public readonly collDateLabel = '' as string;
    public readonly collUserExtra = '' as string;
    public readonly collScoreLabel = '' as string;
    public readonly collTotalLabel = '' as string;
    public readonly collUniquesLabel = '' as string;
    public readonly collDailiesLabel = '' as string;
    public readonly collStreakLabel = '' as string;
    public readonly collLastDailyLabel = '' as string;
    public readonly collFavLabel = '' as string;
    public readonly collRecentLabel = '' as string;
    public readonly collIndivTotalLabel = '' as string;
    public readonly collFirstObtainedLabel = '' as string;
    public readonly collLastObtainedLabel = '' as string;
    public readonly collDescriptionLabel = '' as string;
    public readonly collClaimsLabel = '' as string;
    public readonly collFastestClaimsLabel = '' as string;
    public readonly collFastestTimeLabel = '' as string;
    public readonly collBestPromptLabel = '' as string;
    public readonly collBlessLabel = '' as string;
    public readonly collGiftsLabel = '' as string;
    public readonly collClonesLabel = '' as string;
    public readonly collCellLabel = '' as string;
    public readonly collMiraclesClaimedLabel = '' as string;
    public readonly collMiraclesUsedLabel = '' as string;
    public readonly collMostMiraclesLabel = '' as string;
    public readonly collHighestMultiLabel = '' as string;
    public readonly collGiftsClaimedLabel = '' as string;
    public readonly collGiftsUsedLabel = '' as string;
    public readonly collGiftsOpenedLabel = '' as string;
    public readonly collMostGiftsLabel = '' as string;
    public readonly collClonesClaimedLabel = '' as string;
    public readonly collClonesUsedLabel = '' as string;
    public readonly collClonesSuccLabel = '' as string;
    public readonly collMostClonesLabel = '' as string;
    public readonly collEnhancersClaimedLabel = '' as string;
    public readonly collEnhancedLabel = '' as string;
    public readonly collDataChange = '' as string;
    public readonly collEnhanceNoBucks = '' as string;
    public readonly collEnhanceDetails = '' as string;
    public readonly collEditionTitle = '' as string;
    public readonly collEditionLine = '' as string;
    public readonly collDescriptionSB = '' as string;

    // Event strings

    public readonly eventTitle = '' as string;
    public readonly eventEndedTitle = '' as string;
    public readonly eventsDisabled = '' as string;
    public readonly eventParticipated = '' as string;
    public readonly eventNobody = '' as string;

    // Powerup strings

    public readonly powRightFull = '' as string;
    public readonly powRight = '' as string;
    public readonly powWrongFirst = '' as string;
    public readonly powWrongSecond = '' as string;
    public readonly powWrong = '' as string;
    public readonly powNoMore = '' as string;
    public readonly powTop = '' as string;
    public readonly powTopResult = '' as string;
    public readonly powAvg = '' as string;
    public readonly powAvgResult = '' as string;
    public readonly powAvgResultPlural = '' as string;
    public readonly powPrompt = '' as string;
    public readonly powResponse = '' as string;
    public readonly powResponseShort = '' as string;
    public readonly powReward = '' as string;

    // Market Strings

    public readonly marketConfirmInstaBuy = '' as string;
    public readonly marketUpdatedInstaBuy = '' as string;
    public readonly marketConfirmInstaSell = '' as string;
    public readonly marketUpdatedInstaSell = '' as string;
    public readonly marketInstaComplete = '' as string;
    public readonly marketConfirmBuyOrder = '' as string;
    public readonly marketConfirmSellOrder = '' as string;
    public readonly marketOrderComplete = '' as string;
    public readonly marketConfirmUpdateIncrease = '' as string;
    public readonly marketConfirmUpdateDecrease = '' as string;
    public readonly marketUpdateComplete = '' as string;
    public readonly marketClaimComplete = '' as string;
    public readonly marketMaxItems = '' as string;
    public readonly marketCancelComplete = '' as string;
    public readonly marketNoRoom = '' as string;
    public readonly marketMustClaim = '' as string;
    public readonly marketNoBucks = '' as string;
    public readonly marketNoEdition = '' as string;
    public readonly marketNoEditionOrders = '' as string;
    public readonly marketNoItems = '' as string;
    public readonly marketNoOrders = '' as string;
    public readonly marketMaxOrders = '' as string;
    public readonly marketInvalid = '' as string;
    public readonly marketWrongEdition = '' as string;
    public readonly marketTooMany = '' as string;
    public readonly marketTooHigh = '' as string;
    public readonly marketEditionHigh = '' as string;
    public readonly marketHasEdition = '' as string;
    public readonly marketClosed = '' as string;
    public readonly marketTooYoung = '' as string;
    public readonly marketTooCheap = '' as string;
    public readonly marketTooExpensive = '' as string;
    public readonly marketBestOrder = '' as string;
    public readonly marketBSBuyNowLabel = '' as string;
    public readonly marketBSSellNowLabel = '' as string;
    public readonly marketBSBuyOrdLabel = '' as string;
    public readonly marketBSSellOrdLabel = '' as string;
    public readonly marketOrdSell = '' as string;
    public readonly marketOrdBuy = '' as string;
    public readonly marketOrdList = '' as string;
    public readonly marketOrdExpire = '' as string;
    public readonly marketOrdPriceLabel = '' as string;
    public readonly marketOrdFillLabel = '' as string;
    public readonly marketOrdClaimLabel = '' as string;

    // Powerup strings

    public readonly giftConfirm = '' as string;
    public readonly giftFail = '' as string;
    public readonly giftOut = '' as string;
    public readonly giftSent = '' as string;
    public readonly giftNone = '' as string;
    public readonly giftFrom = '' as string;
    public readonly giftOpened = '' as string;
    public readonly giftOpenedWow = '' as string;
    public readonly miracleConfirm = '' as string;
    public readonly miracleSuccess = '' as string;
    public readonly cloneConfirm = '' as string;
    public readonly cloneFail = '' as string;

    // Notification strings

    public readonly notificationSuccess = '' as string;
    public readonly notificationFailed = '' as string;
    public readonly notificationSuccessReply = '' as string;
    public readonly notificationDailyReady = '' as string;
    public readonly notificationStopStr = '' as string;
    public readonly notificationExtras = [] as string[];
    public readonly notificationServerPing = '' as string;

    // Leaderboard strings

    public readonly notInBoard = '' as string;
    public readonly boardHeader = '' as string;
    public readonly boardFooter = '' as string;
    public readonly deletedUsername = '' as string;

    // Quest strings

    public readonly questCompletionBonus = '' as string;
    public readonly questFullyComplete = '' as string;
    public readonly questInvFull = '' as string;

    // Report/Self-wipe strings

    public readonly sentReport = '' as string;
    public readonly deletedData = '' as string;
    public readonly cancelDelete = '' as string;
    public readonly deleteMsgOne = '' as string;
    public readonly deleteMsgTwo = '' as string;

    // Miscellaneous strings

    public readonly noParentChannel = '' as string;
    public readonly notValidChannel = '' as string;
    public readonly defaultSelectPlaceholder = '' as string;
    public readonly emptySelect = '' as string;
    public readonly channelOptionLabel = '' as string;
    public readonly unavailable = '' as string;
    public readonly defaultImageName = '' as string;
    public readonly fontName = '' as string;
    public readonly commandDebugPrefix = '' as string;
    public readonly pullLink = '' as string;
    public readonly githubImg = '' as string;
    public readonly supportLink = '' as string;
    public readonly supportStr = '' as string;
    public readonly spookMessages = [] as string[];
}
