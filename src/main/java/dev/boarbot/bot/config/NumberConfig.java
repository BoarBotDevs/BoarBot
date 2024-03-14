package dev.boarbot.bot.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link NumberConfig NumberConfig.java}
 *
 * Stores number configurations for a bot instance.
 *
 * @copyright WeslayCodes & Contributors 2023
 */
@Getter
@Setter
@ToString
public class NumberConfig {
    // First pixel of an image location
    private int[] originPos = {0, 0};

    // Font sizes

    private int fontHuge = 0;
    private int fontBig = 0;
    private int fontMedium = 0;
    private int fontSmallMedium = 0;
    private int fontSmallest = 0;

    // Maximum values

    private int maxUsernameLength = 0;
    private int maxTrackedEditions = 0;
    private long maxScore = 0L;
    private int maxBoars = 0;
    private int maxDailies = 0;
    private int maxStreak = 0;
    private long maxIndivBoars = 0L;
    private long maxPowBase = 0L;
    private int maxEnhancers = 0;
    private int maxSmallPow = 0;
    private int maxPowPages = 0;

    // Important constants

    private int rarityIncreaseConst = 0;
    private int miracleIncreaseMax = 0;

    // General default values

    private int border = 0;
    private int embedMaxWidth = 0;
    private int embedMinHeight = 0;

    // Time constants

    private int collectorIdle = 0;
    private int orderExpire = 0;
    private int openDelay = 0; // Cooldown for opening gifts
    private int oneDay = 0;
    private int notificationButtonDelay = 0;

    // Custom confirmation image positions, sizes, and values

    private int[] enhanceDetailsPos = {0, 0};
    private int enhanceDetailsWidth = 0;
    private int[] enhanceImageSize = {0, 0};
    private int[] enhanceCellPos = {0, 0};
    private int[] enhanceCellSize = {0, 0};
    private int[] enhanceBoarPos = {0, 0};
    private int[] enhanceRarityPos = {0, 0};
    private int[] giftImageSize = {0, 0};
    private int[] giftFromPos = {0, 0};
    private int giftFromWidth = 0;

    // Item image positions, sizes, and values

    private int[] itemImageSize = {0, 0};
    private int[] itemPos = {0, 0};
    private int[] smallBoarSize = {0, 0};
    private int[] mediumBoarSize = {0, 0};
    private int[] bigBoarSize = {0, 0};
    private int[] largeBoarSize = {0, 0};
    private int[] itemTitlePos = {0, 0};
    private int[] itemNamePos = {0, 0};
    private int itemUserTagX = 0;
    private int itemUserAvatarX = 0;
    private int itemUserAvatarYOffset = 0;
    private int itemUserAvatarWidth = 0;
    private int itemUserBoxExtra = 0;
    private int itemTextX = 0;
    private int itemTextYOffset = 0;
    private int itemTextBoxExtra = 0;
    private int itemBoxX = 0;
    private int itemBoxOneY = 0;
    private int itemBoxTwoY = 0;
    private int itemBoxThreeY = 0;
    private int itemBoxFourY = 0;
    private int itemBoxHeight = 0;

    // Collection image positions, sizes, and values

    private int collBoarsPerPage = 0;
    private int[] collImageSize = {0, 0};
    private int[] collUserAvatarPos = {0, 0};
    private int collUserAvatarWidth = 0;
    private int[] collUserTagPos = {0, 0};
    private int[] collDateLabelPos = {0, 0};
    private int[] collDatePos = {0, 0};
    private int[] collNoBadgePos = {0, 0};
    private int collBadgeStart = 0;
    private int collBadgeSpacing = 0;
    private int collBadgeY = 0;
    private int[] collBadgeSize = {0, 0};
    private int[] collScoreLabelPos = {0, 0};
    private int[] collScorePos = {0, 0};
    private int[] collTotalLabelPos = {0, 0};
    private int[] collTotalPos = {0, 0};
    private int[] collUniquesLabelPos = {0, 0};
    private int[] collUniquePos = {0, 0};
    private int[] collDailiesLabelPos = {0, 0};
    private int[] collDailiesPos = {0, 0};
    private int[] collStreakLabelPos = {0, 0};
    private int[] collStreakPos = {0, 0};
    private int[] collLastDailyLabelPos = {0, 0};
    private int[] collLastDailyPos = {0, 0};
    private int[] collIndivRarityPos = {0, 0};
    private int collIndivFavHeight = 0;
    private int[] collIndivFavSize = {0, 0};
    private int[] collBoarNamePos = {0, 0};
    private int collBoarNameWidth = 0;
    private int[] collIndivTotalLabelPos = {0, 0};
    private int[] collIndivTotalPos = {0, 0};
    private int[] collFirstObtainedLabelPos = {0, 0};
    private int[] collFirstObtainedPos = {0, 0};
    private int[] collLastObtainedLabelPos = {0, 0};
    private int[] collLastObtainedPos = {0, 0};
    private int[] collDescriptionLabelPos = {0, 0};
    private int[] collDescriptionPos = {0, 0};
    private int collDescriptionWidth = 0;
    private int[] collAttemptsLabelPos = {0, 0};
    private int[] collAttemptsPos = {0, 0};
    private int[] collAttemptsTopLabelPos = {0, 0};
    private int[] collAttemptsTopPos = {0, 0};
    private int[] collFastestTimeLabelPos = {0, 0};
    private int[] collFastestTimePos = {0, 0};
    private int[] collBestPromptLabelPos = {0, 0};
    private int[] collBestPromptPos = {0, 0};
    private int[] collBlessLabelPos = {0, 0};
    private int[] collBlessPos = {0, 0};
    private int[] collMiraclesLabelPos = {0, 0};
    private int[] collMiraclesPos = {0, 0};
    private int[] collGiftsLabelPos = {0, 0};
    private int[] collGiftsPos = {0, 0};
    private int[] collClonesLabelPos = {0, 0};
    private int[] collClonesPos = {0, 0};
    private int[] collCellLabelPos = {0, 0};
    private int[] collCellPos = {0, 0};
    private int[] collCellSize = {0, 0};
    private int[] collChargePos = {0, 0};
    private int[] collLifetimeMiraclesLabelPos = {0, 0};
    private int[] collLifetimeMiraclesPos = {0, 0};
    private int[] collMiraclesUsedLabelPos = {0, 0};
    private int[] collMiraclesUsedPos = {0, 0};
    private int[] collMostMiraclesLabelPos = {0, 0};
    private int[] collMostMiraclesPos = {0, 0};
    private int[] collHighestMultiLabelPos = {0, 0};
    private int[] collHighestMultiPos = {0, 0};
    private int[] collGiftsClaimedLabelPos = {0, 0};
    private int[] collGiftsClaimedPos = {0, 0};
    private int[] collGiftsUsedLabelPos = {0, 0};
    private int[] collGiftsUsedPos = {0, 0};
    private int[] collGiftsOpenedLabelPos = {0, 0};
    private int[] collGiftsOpenedPos = {0, 0};
    private int[] collMostGiftsLabelPos = {0, 0};
    private int[] collMostGiftsPos = {0, 0};
    private int[] collClonesClaimedLabelPos = {0, 0};
    private int[] collClonesClaimedPos = {0, 0};
    private int[] collClonesUsedLabelPos = {0, 0};
    private int[] collClonesUsedPos = {0, 0};
    private int[] collClonesSuccLabelPos = {0, 0};
    private int[] collClonesSuccPos = {0, 0};
    private int[] collMostClonesLabelPos = {0, 0};
    private int[] collMostClonesPos = {0, 0};
    private int[] collEnhancersClaimedLabelPos = {0, 0};
    private int[] collEnhancersClaimedPos = {0, 0};
    private int[][] collEnhancedLabelPositions = {};
    private int[][] collEnhancedPositions = {};
    private int collPowDataWidth = 0;
    private int collBoarStartX = 0;
    private int collBoarStartY = 0;
    private int collBoarSpacingX = 0;
    private int collBoarSpacingY = 0;
    private int collBoarCols = 0;
    private int collRarityStartX = 0;
    private int collRarityStartY = 0;
    private int collRarityHeight = 0;
    private int collRarityWidth = 0;
    private int[] collLastBoarPos = {0, 0};
    private int[] collRecentLabelPos = {0, 0};
    private int[] collFavBoarPos = {0, 0};
    private int[] collFavLabelPos = {0, 0};
    private int[] collIndivBoarPos = {0, 0};

    // Event image positions, sizes, and values

    private int[] eventSpawnSize = {0, 0};
    private int[] eventTitlePos = {0, 0};
    private int eventTitleWidth = 0;
    private int[] eventCornerImgSize = {0, 0};
    private int[] eventCornerImgPos1 = {0, 0};
    private int[] eventCornerImgPos2 = {0, 0};

    // Powerup image positions, sizes, and values

    private int[] powSpawnDescriptionPos = {0, 0};
    private int powSpawnDescriptionWidth = 0;
    private int[] powSpawnRewardPos = {0, 0};
    private int[] powTopLabelPos = {0, 0};
    private int[] powTopPos = {0, 0};
    private int[] powAvgLabelPos = {0, 0};
    private int[] powAvgPos = {0, 0};
    private int[] powPromptLabelPos = {0, 0};
    private int[] powPromptPos = {0, 0};
    private int powDataWidth = 0;
    private int emojiRows = 0;
    private int emojiCols = 0;
    private int triviaRows = 0;
    private int triviaCols = 0;
    private int fastCols = 0;
    private int powPlusMinusMins = 0;
    private int powIntervalHours = 0;
    private int powDurationMillis = 0;
    private int powExperiencedNum = 0;

    // Leaderboard image positions, sizes, and values

    private int leaderboardNumPlayers = 0;
    private int leaderboardRows = 0;
    private int[] leaderboardStart = {0, 0};
    private int leaderboardIncX = 0;
    private int leaderboardIncY = 0;
    private int[] leaderboardHeaderPos = {0, 0};
    private int leaderboardTopBotWidth = 0;
    private int[] leaderboardFooterPos = {0, 0};
    private int leaderboardEntryWidth = 0;

    // Market image positions, sizes, and values

    private int[] marketSize = {0, 0};
    private int marketPerPage = 0;
    private int marketMaxOrders = 0;
    private int marketMaxBucks = 0;
    private int[] marketOverImgStart = {0, 0};
    private int[] marketOverBuyStart = {0, 0};
    private int[] marketOverSellStart = {0, 0};
    private int marketOverIncX = 0;
    private int marketOverIncY = 0;
    private int marketOverCols = 0;
    private int[] marketOverImgSize = {0, 0};
    private int marketOverTextWidth = 0;
    private int[] marketBSImgPos = {0, 0};
    private int[] marketBSImgSize = {0, 0};
    private int[] marketBSRarityPos = {0, 0};
    private int[] marketBSNamePos = {0, 0};
    private int marketBSNameWidth = 0;
    private int[] marketBSBuyNowLabelPos = {0, 0};
    private int[] marketBSBuyNowPos = {0, 0};
    private int[] marketBSSellNowLabelPos = {0, 0};
    private int[] marketBSSellNowPos = {0, 0};
    private int[] marketBSBuyOrdLabelPos = {0, 0};
    private int[] marketBSBuyOrdPos = {0, 0};
    private int[] marketBSSellOrdLabelPos = {0, 0};
    private int[] marketBSSellOrdPos = {0, 0};
    private int[] marketOrdImgPos = {0, 0};
    private int[] marketOrdImgSize = {0, 0};
    private int[] marketOrdNamePos = {0, 0};
    private int marketOrdNameWidth = 0;
    private int[] marketOrdListPos = {0, 0};
    private int[] marketOrdPriceLabelPos = {0, 0};
    private int[] marketOrdPricePos = {0, 0};
    private int[] marketOrdFillLabelPos = {0, 0};
    private int[] marketOrdFillPos = {0, 0};
    private int[] marketOrdClaimLabelPos = {0, 0};
    private int[] marketOrdClaimPos = {0, 0};
    private int marketOrdClaimWidth = 0;
    private int marketRange = 0;

    // Quest image positions, sizes, and values

    private int questFullAmt = 0;
    private int[] questImgSize = {0, 0};
    private int[] questDatesPos = {0, 0};
    private int[] questStrStartPos = {0, 0};
    private int questSpacingY = 0;
    private int questProgressYOffset = 0;
    private int[] questBucksOffsets = {0, 0};
    private int[] questPowAmtOffsets = {0, 0};
    private int[] questPowImgOffsets = {0, 0};
    private int questStrWidth = 0;
    private int[] questRewardImgSize = {0, 0};
    private int[] questCompletionLabelPos = {0, 0};
    private int[] questCompletionPos = {0, 0};
    private int[] questCompleteCheckPos = {0, 0};
    private int[] questCompleteStrPos = {0, 0};
}
