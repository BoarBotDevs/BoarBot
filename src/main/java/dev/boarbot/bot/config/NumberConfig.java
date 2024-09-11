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
    // Font sizes

    private int fontHuge = 0;
    private int fontBig = 0;
    private int fontMedium = 0;
    private int fontSmallMedium = 0;
    private int fontSmallest = 0;

    // Important constants

    private int maxUsernameLength = 0;
    private int maxTransmute = 0;
    private int maxStreakBless = 0;
    private int maxQuestBless = 0;
    private int maxUniqueBless = 0;
    private int maxOtherBless = 0;
    private int rarityIncreaseConst = 0;
    private int miracleIncreaseMax = 0;

    // General default values

    private int border = 0;

    // Time constants

    private int interactiveIdle = 0;
    private int interactiveHardStop = 0;
    private int giftIdle = 0;
    private int openDelay = 0;
    private int giftLowWait = 0;
    private int giftHighWait = 0;
    private int giftMaxHandicap = 0;

    // Boar sizes

    private int[] smallBoarSize = {0, 0};
    private int[] mediumBoarSize = {0, 0};
    private int[] mediumBigBoarSize = {0, 0};
    private int[] bigBoarSize = {0, 0};
    private int[] largeBoarSize = {0, 0};

    // Powerup values

    private int emojiRows = 0;
    private int emojiCols = 0;
    private int triviaRows = 0;
    private int triviaCols = 0;
    private int fastCols = 0;
    private int powPlusMinusMins = 0;
    private int powIntervalHours = 0;
    private int powDurationMillis = 0;
    private int powNumStopHelp = 0;

    // Quest values

    private int questFullAmt = 0;
}
