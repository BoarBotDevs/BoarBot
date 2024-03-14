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
public class NumberConfig {
    // First pixel of an image location
    private int[] originPos = new int[]{0, 0};

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

    private int[] enhanceDetailsPos = new int[]{0, 0};
    private int enhanceDetailsWidth = 0;
    private int[] enhanceImageSize = new int[]{0, 0};
    private int[] enhanceCellPos = new int[]{0, 0};
    private int[] enhanceCellSize = new int[]{0, 0};
    private int[] enhanceBoarPos = new int[]{0, 0};
    private int[] enhanceRarityPos = new int[]{0, 0};
    private int[] giftImageSize = new int[]{0, 0};
    private int[] giftFromPos = new int[]{0, 0};
    private int giftFromWidth = 0;

    // Item image positions, sizes, and values

    private int[] itemImageSize = new int[]{0, 0};
    private int[] itemPos = new int[]{0, 0};
    private int[] smallBoarSize = new int[]{0, 0};
    private int[] mediumBoarSize = new int[]{0, 0};
    private int[] bigBoarSize = new int[]{0, 0};
    private int[] largeBoarSize = new int[]{0, 0};
    private int[] itemTitlePos = new int[]{0, 0};
    private int[] itemNamePos = new int[]{0, 0};
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
    private int[] collImageSize = new int[]{0, 0};
    private int[] collUserAvatarPos = new int[]{0, 0};
    private int collUserAvatarWidth = 0;
    private int[] collUserTagPos = new int[]{0, 0};
    private int[] collDateLabelPos = new int[]{0, 0};
    private int[] collDatePos = new int[]{0, 0};
    private int[] collNoBadgePos = new int[]{0, 0};
    private int collBadgeStart = 0;
    private int collBadgeSpacing = 0;
    private int collBadgeY = 0;
    private int[] collBadgeSize = new int[]{0, 0};
    private int[] collScoreLabelPos = new int[]{0, 0};
    private int[] collScorePos = new int[]{0, 0};
    private int[] collTotalLabelPos = new int[]{0, 0};
    private int[] collTotalPos = new int[]{0, 0};
    private int[] collUniquesLabelPos = new int[]{0, 0};
    private int[] collUniquePos = new int[]{0, 0};
    private int[] collDailiesLabelPos = new int[]{0, 0};
    private int[] collDailiesPos = new int[]{0, 0};
    private int[] collStreakLabelPos = new int[]{0, 0};
    private int[] collStreakPos = new int[]{0, 0};
    private int[] collLastDailyLabelPos = new int[]{0, 0};
    private int[] collLastDailyPos = new int[]{0, 0};
    private int[] collIndivRarityPos = new int[]{0, 0};
    private int collIndivFavHeight = 0;
    private int[] collIndivFavSize = new int[]{0, 0};
    private int[] collBoarNamePos = new int[]{0, 0};
    private int collBoarNameWidth = 0;
    private int[] collIndivTotalLabelPos = new int[]{0, 0};
    private int[] collIndivTotalPos = new int[]{0, 0};
    private int[] collFirstObtainedLabelPos = new int[]{0, 0};
    private int[] collFirstObtainedPos = new int[]{0, 0};
    private int[] collLastObtainedLabelPos = new int[]{0, 0};
    private int[] collLastObtainedPos = new int[]{0, 0};
    private int[] collDescriptionLabelPos = new int[]{0, 0};
    private int[] collDescriptionPos = new int[]{0, 0};
    private int collDescriptionWidth = 0;
    private int[] collAttemptsLabelPos = new int[]{0, 0};
    private int[] collAttemptsPos = new int[]{0, 0};
    private int[] collAttemptsTopLabelPos = new int[]{0, 0};
    private int[] collAttemptsTopPos = new int[]{0, 0};
    private int[] collFastestTimeLabelPos = new int[]{0, 0};
    private int[] collFastestTimePos = new int[]{0, 0};
    private int[] collBestPromptLabelPos = new int[]{0, 0};
    private int[] collBestPromptPos = new int[]{0, 0};
    private int[] collBlessLabelPos = new int[]{0, 0};
    private int[] collBlessPos = new int[]{0, 0};
    private int[] collMiraclesLabelPos = new int[]{0, 0};
    private int[] collMiraclesPos = new int[]{0, 0};
    private int[] collGiftsLabelPos = new int[]{0, 0};
    private int[] collGiftsPos = new int[]{0, 0};
    private int[] collClonesLabelPos = new int[]{0, 0};
    private int[] collClonesPos = new int[]{0, 0};
    private int[] collCellLabelPos = new int[]{0, 0};
    private int[] collCellPos = new int[]{0, 0};
    private int[] collCellSize = new int[]{0, 0};
    private int[] collChargePos = new int[]{0, 0};
    private int[] collLifetimeMiraclesLabelPos = new int[]{0, 0};
    private int[] collLifetimeMiraclesPos = new int[]{0, 0};
    private int[] collMiraclesUsedLabelPos = new int[]{0, 0};
    private int[] collMiraclesUsedPos = new int[]{0, 0};
    private int[] collMostMiraclesLabelPos = new int[]{0, 0};
    private int[] collMostMiraclesPos = new int[]{0, 0};
    private int[] collHighestMultiLabelPos = new int[]{0, 0};
    private int[] collHighestMultiPos = new int[]{0, 0};
    private int[] collGiftsClaimedLabelPos = new int[]{0, 0};
    private int[] collGiftsClaimedPos = new int[]{0, 0};
    private int[] collGiftsUsedLabelPos = new int[]{0, 0};
    private int[] collGiftsUsedPos = new int[]{0, 0};
    private int[] collGiftsOpenedLabelPos = new int[]{0, 0};
    private int[] collGiftsOpenedPos = new int[]{0, 0};
    private int[] collMostGiftsLabelPos = new int[]{0, 0};
    private int[] collMostGiftsPos = new int[]{0, 0};
    private int[] collClonesClaimedLabelPos = new int[]{0, 0};
    private int[] collClonesClaimedPos = new int[]{0, 0};
    private int[] collClonesUsedLabelPos = new int[]{0, 0};
    private int[] collClonesUsedPos = new int[]{0, 0};
    private int[] collClonesSuccLabelPos = new int[]{0, 0};
    private int[] collClonesSuccPos = new int[]{0, 0};
    private int[] collMostClonesLabelPos = new int[]{0, 0};
    private int[] collMostClonesPos = new int[]{0, 0};
    private int[] collEnhancersClaimedLabelPos = new int[]{0, 0};
    private int[] collEnhancersClaimedPos = new int[]{0, 0};
    private int[][] collEnhancedLabelPositions = new int[0][];
    private int[][] collEnhancedPositions = new int[0][];
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
    private int[] collLastBoarPos = new int[]{0, 0};
    private int[] collRecentLabelPos = new int[]{0, 0};
    private int[] collFavBoarPos = new int[]{0, 0};
    private int[] collFavLabelPos = new int[]{0, 0};
    private int[] collIndivBoarPos = new int[]{0, 0};

    // Event image positions, sizes, and values

    private int[] eventSpawnSize = new int[]{0, 0};
    private int[] eventTitlePos = new int[]{0, 0};
    private int eventTitleWidth = 0;
    private int[] eventCornerImgSize = new int[]{0, 0};
    private int[] eventCornerImgPos1 = new int[]{0, 0};
    private int[] eventCornerImgPos2 = new int[]{0, 0};

    // Powerup image positions, sizes, and values

    private int[] powSpawnDescriptionPos = new int[]{0, 0};
    private int powSpawnDescriptionWidth = 0;
    private int[] powSpawnRewardPos = new int[]{0, 0};
    private int[] powTopLabelPos = new int[]{0, 0};
    private int[] powTopPos = new int[]{0, 0};
    private int[] powAvgLabelPos = new int[]{0, 0};
    private int[] powAvgPos = new int[]{0, 0};
    private int[] powPromptLabelPos = new int[]{0, 0};
    private int[] powPromptPos = new int[]{0, 0};
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
    private int[] leaderboardStart = new int[]{0, 0};
    private int leaderboardIncX = 0;
    private int leaderboardIncY = 0;
    private int[] leaderboardHeaderPos = new int[]{0, 0};
    private int leaderboardTopBotWidth = 0;
    private int[] leaderboardFooterPos = new int[]{0, 0};
    private int leaderboardEntryWidth = 0;

    // Market image positions, sizes, and values

    private int[] marketSize = new int[]{0, 0};
    private int marketPerPage = 0;
    private int marketMaxOrders = 0;
    private int marketMaxBucks = 0;
    private int[] marketOverImgStart = new int[]{0, 0};
    private int[] marketOverBuyStart = new int[]{0, 0};
    private int[] marketOverSellStart = new int[]{0, 0};
    private int marketOverIncX = 0;
    private int marketOverIncY = 0;
    private int marketOverCols = 0;
    private int[] marketOverImgSize = new int[]{0, 0};
    private int marketOverTextWidth = 0;
    private int[] marketBSImgPos = new int[]{0, 0};
    private int[] marketBSImgSize = new int[]{0, 0};
    private int[] marketBSRarityPos = new int[]{0, 0};
    private int[] marketBSNamePos = new int[]{0, 0};
    private int marketBSNameWidth = 0;
    private int[] marketBSBuyNowLabelPos = new int[]{0, 0};
    private int[] marketBSBuyNowPos = new int[]{0, 0};
    private int[] marketBSSellNowLabelPos = new int[]{0, 0};
    private int[] marketBSSellNowPos = new int[]{0, 0};
    private int[] marketBSBuyOrdLabelPos = new int[]{0, 0};
    private int[] marketBSBuyOrdPos = new int[]{0, 0};
    private int[] marketBSSellOrdLabelPos = new int[]{0, 0};
    private int[] marketBSSellOrdPos = new int[]{0, 0};
    private int[] marketOrdImgPos = new int[]{0, 0};
    private int[] marketOrdImgSize = new int[]{0, 0};
    private int[] marketOrdNamePos = new int[]{0, 0};
    private int marketOrdNameWidth = 0;
    private int[] marketOrdListPos = new int[]{0, 0};
    private int[] marketOrdPriceLabelPos = new int[]{0, 0};
    private int[] marketOrdPricePos = new int[]{0, 0};
    private int[] marketOrdFillLabelPos = new int[]{0, 0};
    private int[] marketOrdFillPos = new int[]{0, 0};
    private int[] marketOrdClaimLabelPos = new int[]{0, 0};
    private int[] marketOrdClaimPos = new int[]{0, 0};
    private int marketOrdClaimWidth = 0;
    private int marketRange = 0;

    // Quest image positions, sizes, and values

    private int questFullAmt = 0;
    private int[] questImgSize = new int[]{0, 0};
    private int[] questDatesPos = new int[]{0, 0};
    private int[] questStrStartPos = new int[]{0, 0};
    private int questSpacingY = 0;
    private int questProgressYOffset = 0;
    private int[] questBucksOffsets = new int[]{0, 0};
    private int[] questPowAmtOffsets = new int[]{0, 0};
    private int[] questPowImgOffsets = new int[]{0, 0};
    private int questStrWidth = 0;
    private int[] questRewardImgSize = new int[]{0, 0};
    private int[] questCompletionLabelPos = new int[]{0, 0};
    private int[] questCompletionPos = new int[]{0, 0};
    private int[] questCompleteCheckPos = new int[]{0, 0};
    private int[] questCompleteStrPos = new int[]{0, 0};
}
