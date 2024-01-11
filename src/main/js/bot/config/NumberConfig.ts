/**
 * {@link NumberConfig NumberConfig.ts}
 *
 * Stores number configurations for a bot instance.
 *
 * @copyright WeslayCodes & Contributors 2023
 */
export class NumberConfig {
    // First pixel of an image location
    public readonly originPos = [0, 0] as [number, number];

    // Font sizes

    public readonly fontHuge = 0 as number;
    public readonly fontBig = 0 as number;
    public readonly fontMedium = 0 as number;
    public readonly fontSmallMedium = 0 as number;
    public readonly fontSmallest = 0 as number;

    // Maximum values

    public readonly maxUsernameLength = 0 as number;
    public readonly maxTrackedEditions = 0 as number;
    public readonly maxScore = 0 as number;
    public readonly maxBoars = 0 as number;
    public readonly maxDailies = 0 as number;
    public readonly maxStreak = 0 as number;
    public readonly maxIndivBoars = 0 as number;
    public readonly maxPowBase = 0 as number;
    public readonly maxEnhancers = 0 as number;
    public readonly maxSmallPow = 0 as number;
    public readonly maxPowPages = 0 as number;

    // Important constants

    public readonly rarityIncreaseConst = 0 as number;
    public readonly miracleIncreaseMax = 0 as number;

    // General default values

    public readonly border = 0 as number;
    public readonly embedMaxWidth = 0 as number;
    public readonly embedMinHeight = 0 as number;

    // Time constants

    public readonly collectorIdle = 0 as number;
    public readonly orderExpire = 0 as number;
    public readonly openDelay = 0 as number; // Cooldown for opening gifts
    public readonly oneDay = 0 as number;
    public readonly notificationButtonDelay = 0 as number;

    // Custom confirmation image positions, sizes, and values

    public readonly enhanceDetailsPos = [0, 0] as [number, number];
    public readonly enhanceDetailsWidth = 0 as number;
    public readonly enhanceImageSize = [0, 0] as [number, number];
    public readonly enhanceCellPos = [0, 0] as [number, number];
    public readonly enhanceCellSize = [0, 0] as [number, number];
    public readonly enhanceBoarPos = [0, 0] as [number, number];
    public readonly enhanceBoarSize = [0, 0] as [number, number];
    public readonly enhanceRarityPos = [0, 0] as [number, number];
    public readonly giftImageSize = [0, 0] as [number, number];
    public readonly giftFromPos = [0, 0] as [number, number];
    public readonly giftFromWidth = 0 as number;

    // Item image positions, sizes, and values

    public readonly itemImageSize = [0, 0] as [number, number];
    public readonly itemPos = [0, 0] as [number, number];
    public readonly itemSize = [0, 0] as [number, number];
    public readonly itemTitlePos = [0, 0] as [number, number];
    public readonly itemNamePos = [0, 0] as [number, number];
    public readonly itemUserTagX = 0 as number;
    public readonly itemUserAvatarX = 0 as number;
    public readonly itemUserAvatarYOffset = 0 as number;
    public readonly itemUserAvatarWidth = 0 as number;
    public readonly itemUserBoxExtra = 0 as number;
    public readonly itemTextX = 0 as number;
    public readonly itemTextYOffset = 0 as number;
    public readonly itemTextBoxExtra = 0 as number;
    public readonly itemBoxX = 0 as number;
    public readonly itemBoxOneY = 0 as number;
    public readonly itemBoxTwoY = 0 as number;
    public readonly itemBoxThreeY = 0 as number;
    public readonly itemBoxFourY = 0 as number;
    public readonly itemBoxHeight = 0 as number;

    // Collection image positions, sizes, and values

    public readonly collBoarsPerPage = 0 as number;
    public readonly collImageSize = [0, 0] as [number, number];
    public readonly collUserAvatarPos = [0, 0] as [number, number];
    public readonly collUserAvatarWidth = 0 as number;
    public readonly collUserTagPos = [0, 0] as [number, number];
    public readonly collDateLabelPos = [0, 0] as [number, number];
    public readonly collDatePos = [0, 0] as [number, number];
    public readonly collNoBadgePos = [0, 0] as [number, number];
    public readonly collBadgeStart = 0 as number;
    public readonly collBadgeSpacing = 0 as number;
    public readonly collBadgeY = 0 as number;
    public readonly collBadgeSize = [0, 0] as [number, number];
    public readonly collScoreLabelPos = [0, 0] as [number, number];
    public readonly collScorePos = [0, 0] as [number, number];
    public readonly collTotalLabelPos = [0, 0] as [number, number];
    public readonly collTotalPos = [0, 0] as [number, number];
    public readonly collUniquesLabelPos = [0, 0] as [number, number];
    public readonly collUniquePos = [0, 0] as [number, number];
    public readonly collDailiesLabelPos = [0, 0] as [number, number];
    public readonly collDailiesPos = [0, 0] as [number, number];
    public readonly collStreakLabelPos = [0, 0] as [number, number];
    public readonly collStreakPos = [0, 0] as [number, number];
    public readonly collLastDailyLabelPos = [0, 0] as [number, number];
    public readonly collLastDailyPos = [0, 0] as [number, number];
    public readonly collIndivRarityPos = [0, 0] as [number, number];
    public readonly collIndivFavHeight = 0 as number;
    public readonly collIndivFavSize = [0, 0] as [number, number];
    public readonly collBoarNamePos = [0, 0] as [number, number];
    public readonly collBoarNameWidth = 0 as number;
    public readonly collIndivTotalLabelPos = [0, 0] as [number, number];
    public readonly collIndivTotalPos = [0, 0] as [number, number];
    public readonly collFirstObtainedLabelPos = [0, 0] as [number, number];
    public readonly collFirstObtainedPos = [0, 0] as [number, number];
    public readonly collLastObtainedLabelPos = [0, 0] as [number, number];
    public readonly collLastObtainedPos = [0, 0] as [number, number];
    public readonly collDescriptionLabelPos = [0, 0] as [number, number];
    public readonly collDescriptionPos = [0, 0] as [number, number];
    public readonly collDescriptionWidth = 0 as number;
    public readonly collAttemptsLabelPos = [0, 0] as [number, number];
    public readonly collAttemptsPos = [0, 0] as [number, number];
    public readonly collAttemptsTopLabelPos = [0, 0] as [number, number];
    public readonly collAttemptsTopPos = [0, 0] as [number, number];
    public readonly collFastestTimeLabelPos = [0, 0] as [number, number];
    public readonly collFastestTimePos = [0, 0] as [number, number];
    public readonly collBestPromptLabelPos = [0, 0] as [number, number];
    public readonly collBestPromptPos = [0, 0] as [number, number];
    public readonly collBlessLabelPos = [0, 0] as [number, number];
    public readonly collBlessPos = [0, 0] as [number, number];
    public readonly collMiraclesLabelPos = [0, 0] as [number, number];
    public readonly collMiraclesPos = [0, 0] as [number, number];
    public readonly collGiftsLabelPos = [0, 0] as [number, number];
    public readonly collGiftsPos = [0, 0] as [number, number];
    public readonly collClonesLabelPos = [0, 0] as [number, number];
    public readonly collClonesPos = [0, 0] as [number, number];
    public readonly collCellLabelPos = [0, 0] as [number, number];
    public readonly collCellPos = [0, 0] as [number, number];
    public readonly collCellSize = [0, 0] as [number, number];
    public readonly collChargePos = [0, 0] as [number, number];
    public readonly collLifetimeMiraclesLabelPos = [0, 0] as [number, number];
    public readonly collLifetimeMiraclesPos = [0, 0] as [number, number];
    public readonly collMiraclesUsedLabelPos = [0, 0] as [number, number];
    public readonly collMiraclesUsedPos = [0, 0] as [number, number];
    public readonly collMostMiraclesLabelPos = [0, 0] as [number, number];
    public readonly collMostMiraclesPos = [0, 0] as [number, number];
    public readonly collHighestMultiLabelPos = [0, 0] as [number, number];
    public readonly collHighestMultiPos = [0, 0] as [number, number];
    public readonly collGiftsClaimedLabelPos = [0, 0] as [number, number];
    public readonly collGiftsClaimedPos = [0, 0] as [number, number];
    public readonly collGiftsUsedLabelPos = [0, 0] as [number, number];
    public readonly collGiftsUsedPos = [0, 0] as [number, number];
    public readonly collGiftsOpenedLabelPos = [0, 0] as [number, number];
    public readonly collGiftsOpenedPos = [0, 0] as [number, number];
    public readonly collMostGiftsLabelPos = [0, 0] as [number, number];
    public readonly collMostGiftsPos = [0, 0] as [number, number];
    public readonly collClonesClaimedLabelPos = [0, 0] as [number, number];
    public readonly collClonesClaimedPos = [0, 0] as [number, number];
    public readonly collClonesUsedLabelPos = [0, 0] as [number, number];
    public readonly collClonesUsedPos = [0, 0] as [number, number];
    public readonly collClonesSuccLabelPos = [0, 0] as [number, number];
    public readonly collClonesSuccPos = [0, 0] as [number, number];
    public readonly collMostClonesLabelPos = [0, 0] as [number, number];
    public readonly collMostClonesPos = [0, 0] as [number, number];
    public readonly collEnhancersClaimedLabelPos = [0, 0] as [number, number];
    public readonly collEnhancersClaimedPos = [0, 0] as [number, number];
    public readonly collEnhancedLabelPositions = [] as [number, number][];
    public readonly collEnhancedPositions = [] as [number, number][];
    public readonly collPowDataWidth = 0 as number;
    public readonly collBoarStartX = 0 as number;
    public readonly collBoarStartY = 0 as number;
    public readonly collBoarSpacingX = 0 as number;
    public readonly collBoarSpacingY = 0 as number;
    public readonly collBoarCols = 0 as number;
    public readonly collBoarSize = [0, 0] as [number, number];
    public readonly collRarityStartX = 0 as number;
    public readonly collRarityStartY = 0 as number;
    public readonly collRarityHeight = 0 as number;
    public readonly collRarityWidth = 0 as number;
    public readonly collLastBoarPos = [0, 0] as [number, number];
    public readonly collLastBoarSize = [0, 0] as [number, number];
    public readonly collRecentLabelPos = [0, 0] as [number, number];
    public readonly collFavBoarPos = [0, 0] as [number, number];
    public readonly collFavBoarSize = [0, 0] as [number, number];
    public readonly collFavLabelPos = [0, 0] as [number, number];
    public readonly collIndivBoarPos = [0, 0] as [number, number];
    public readonly collIndivBoarSize = [0, 0] as [number, number];

    // Event image positions, sizes, and values

    public readonly eventSpawnSize = [0, 0] as [number, number];
    public readonly eventTitlePos = [0, 0] as [number, number];
    public readonly eventTitleWidth = 0 as number;
    public readonly eventCornerImgSize = [0, 0] as [number, number];
    public readonly eventCornerImgPos1 = [0, 0] as [number, number];
    public readonly eventCornerImgPos2 = [0, 0] as [number, number];

    // Powerup image positions, sizes, and values

    public readonly powSpawnDescriptionPos = [0, 0] as [number, number];
    public readonly powSpawnDescriptionWidth = 0 as number;
    public readonly powSpawnRewardPos = [0, 0] as [number, number];
    public readonly powTopLabelPos = [0, 0] as [number, number];
    public readonly powTopPos = [0, 0] as [number, number];
    public readonly powAvgLabelPos = [0, 0] as [number, number];
    public readonly powAvgPos = [0, 0] as [number, number];
    public readonly powPromptLabelPos = [0, 0] as [number, number];
    public readonly powPromptPos = [0, 0] as [number, number];
    public readonly powDataWidth = 0 as number;
    public readonly emojiRows = 0 as number;
    public readonly emojiCols = 0 as number;
    public readonly triviaRows = 0 as number;
    public readonly triviaCols = 0 as number;
    public readonly fastCols = 0 as number;
    public readonly powPlusMinusMins = 0 as number;
    public readonly powIntervalHours = 0 as number;
    public readonly powDurationMillis = 0 as number;
    public readonly powExperiencedNum = 0 as number;

    // Leaderboard image positions, sizes, and values

    public readonly leaderboardNumPlayers = 0 as number;
    public readonly leaderboardRows = 0 as number;
    public readonly leaderboardStart = [0, 0] as [number, number];
    public readonly leaderboardIncX = 0 as number;
    public readonly leaderboardIncY = 0 as number;
    public readonly leaderboardHeaderPos = [0, 0] as [number, number];
    public readonly leaderboardTopBotWidth = 0 as number;
    public readonly leaderboardFooterPos = [0, 0] as [number, number];
    public readonly leaderboardEntryWidth = 0 as number;

    // Market image positions, sizes, and values

    public readonly marketSize = [0, 0] as [number, number];
    public readonly marketPerPage = 0 as number;
    public readonly marketMaxOrders = 0 as number;
    public readonly marketMaxBucks = 0 as number;
    public readonly marketOverImgStart = [0, 0] as [number, number];
    public readonly marketOverBuyStart = [0, 0] as [number, number];
    public readonly marketOverSellStart = [0, 0] as [number, number];
    public readonly marketOverIncX = 0 as number;
    public readonly marketOverIncY = 0 as number;
    public readonly marketOverCols = 0 as number;
    public readonly marketOverImgSize = [0, 0] as [number, number];
    public readonly marketOverTextWidth = 0 as number;
    public readonly marketBSImgPos = [0, 0] as [number, number];
    public readonly marketBSImgSize = [0, 0] as [number, number];
    public readonly marketBSRarityPos = [0, 0] as [number, number];
    public readonly marketBSNamePos = [0, 0] as [number, number];
    public readonly marketBSNameWidth = 0 as number;
    public readonly marketBSBuyNowLabelPos = [0, 0] as [number, number];
    public readonly marketBSBuyNowPos = [0, 0] as [number, number];
    public readonly marketBSSellNowLabelPos = [0, 0] as [number, number];
    public readonly marketBSSellNowPos = [0, 0] as [number, number];
    public readonly marketBSBuyOrdLabelPos = [0, 0] as [number, number];
    public readonly marketBSBuyOrdPos = [0, 0] as [number, number];
    public readonly marketBSSellOrdLabelPos = [0, 0] as [number, number];
    public readonly marketBSSellOrdPos = [0, 0] as [number, number];
    public readonly marketOrdImgPos = [0, 0] as [number, number];
    public readonly marketOrdImgSize = [0, 0] as [number, number];
    public readonly marketOrdNamePos = [0, 0] as [number, number];
    public readonly marketOrdNameWidth = 0 as number;
    public readonly marketOrdListPos = [0, 0] as [number, number];
    public readonly marketOrdPriceLabelPos = [0, 0] as [number, number];
    public readonly marketOrdPricePos = [0, 0] as [number, number];
    public readonly marketOrdFillLabelPos = [0, 0] as [number, number];
    public readonly marketOrdFillPos = [0, 0] as [number, number];
    public readonly marketOrdClaimLabelPos = [0, 0] as [number, number];
    public readonly marketOrdClaimPos = [0, 0] as [number, number];
    public readonly marketOrdClaimWidth = 0 as number;
    public readonly marketRange = 0 as number;

    // Quest image positions, sizes, and values

    public readonly questFullAmt = 0 as number;
    public readonly questImgSize = [0, 0] as [number, number];
    public readonly questDatesPos = [0, 0] as [number, number];
    public readonly questStrStartPos = [0, 0] as [number, number];
    public readonly questSpacingY = 0 as number;
    public readonly questProgressYOffset = 0 as number;
    public readonly questBucksOffsets = [0, 0] as [number, number];
    public readonly questPowAmtOffsets = [0, 0] as [number, number];
    public readonly questPowImgOffsets = [0, 0] as [number, number];
    public readonly questStrWidth = 0 as number;
    public readonly questRewardImgSize = [0, 0] as [number, number];
    public readonly questCompletionLabelPos = [0, 0] as [number, number];
    public readonly questCompletionPos = [0, 0] as [number, number];
    public readonly questCompleteCheckPos = [0, 0] as [number, number];
    public readonly questCompleteStrPos = [0, 0] as [number, number];
}
