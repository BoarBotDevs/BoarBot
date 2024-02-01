import {BotConfig} from '../../bot/config/BotConfig';
import {Canvas, loadImage} from 'skia-canvas';
import {CanvasUtils} from './CanvasUtils';
import {AttachmentBuilder} from 'discord.js';
import {BoarUser} from '../boar/BoarUser';
import {DataHandlers} from '../data/DataHandlers';
import {QuestData} from '../../bot/data/global/QuestData';

/**
 * {@link QuestsImageGenerator QuestsImageGenerator.ts}
 *
 * Creates an image showing a user's progress on weekly boar quests
 *
 * @copyright WeslayCodes & Contributors 2023
 */

export class QuestsImageGenerator {
    /**
     * Creates a dynamic generic embed
     *
     * @param boarUser
     * @param config - Used to get position and other config info
     */
    public static async makeImage(boarUser: BoarUser, config: BotConfig): Promise<AttachmentBuilder> {
        const strConfig = config.stringConfig;
        const nums = config.numberConfig;
        const colorConfig = config.colorConfig;
        const pathConfig = config.pathConfig;
        const questConfigs = config.questConfigs;
        const powConfigs = config.itemConfigs.powerups;

        const questsUnderlay = pathConfig.otherAssets + pathConfig.questsUnderlay;
        const checkImgPath = pathConfig.otherAssets + pathConfig.check;

        const fontBig = `${nums.fontBig}px ${strConfig.fontName}`;
        const fontMedium = `${nums.fontMedium}px ${strConfig.fontName}`;
        const fontSmallest = `${nums.fontSmallest}px ${strConfig.fontName}`;

        const questData = DataHandlers.getGlobalData(DataHandlers.GlobalFile.Quest) as QuestData;
        const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'June', 'July', 'Aug', 'Sept', 'Oct', 'Nov', 'Dec'];

        const startDay = new Date(questData.questsStartTimestamp);
        const endDay = new Date(questData.questsStartTimestamp + nums.oneDay * 6);

        const startDayStr = months[startDay.getUTCMonth()] + ' ' + startDay.getUTCDate();
        const endDayStr = months[endDay.getUTCMonth()] + ' ' + endDay.getUTCDate();

        const fullCompleteRewardLeft = boarUser.stats.quests
            ? nums.questFullAmt - boarUser.stats.quests.claimed[boarUser.stats.quests.claimed.length-1]
            : nums.questFullAmt;

        const canvas = new Canvas(...nums.questImgSize);
        const ctx = canvas.getContext('2d');

        ctx.drawImage(await loadImage(questsUnderlay), ...nums.originPos);

        await CanvasUtils.drawText(
            ctx, `${startDayStr} - ${endDayStr}`, nums.questDatesPos, fontMedium, 'center', colorConfig.font
        );

        const startPos = nums.questStrStartPos;
        let index = 0;
        let fullComplete = true;
        for (const id of questData.curQuestIDs) {
            const questConfig = questConfigs[id];
            const valIndex = Math.floor(index / 2);
            let isAltStr = false;
            let numToComplete = 1;
            let dynamicPart = '';

            const questStrPos = [
                startPos[0],
                startPos[1] + index * nums.questSpacingY
            ] as [number, number];
            const progressStrPos = [
                startPos[0],
                startPos[1] + index * nums.questSpacingY + nums.questProgressYOffset
            ] as [number, number];
            const bucksRewardPos = [
                startPos[0] + nums.questBucksOffsets[0],
                startPos[1] + index * nums.questSpacingY + nums.questBucksOffsets[1]
            ] as [number, number];
            const powRewardAmtPos = [
                startPos[0] + nums.questPowAmtOffsets[0],
                startPos[1] + index * nums.questSpacingY + nums.questPowAmtOffsets[1]
            ] as [number, number];
            const powRewardImgPos = [
                startPos[0] + nums.questPowImgOffsets[0],
                startPos[1] + index * nums.questSpacingY - nums.questPowImgOffsets[1]
            ] as [number, number];

            const questProgress = boarUser.stats.quests
                ? boarUser.stats.quests.progress[index]
                : 0;
            const questRewardLeft = boarUser.stats.quests
                ? questConfig.questVals[valIndex][1] - boarUser.stats.quests.claimed[index]
                : questConfig.questVals[valIndex][1];

            switch(questConfig.valType) {
                case 'number': {
                    isAltStr = questConfig.questVals[valIndex][0] > 1;
                    numToComplete = questConfig.questVals[valIndex][0];
                    dynamicPart = (id.toLowerCase().includes('bucks')
                        ? '$'
                        : '') + questConfig.questVals[valIndex][0].toLocaleString();
                    break;
                }

                case 'rarity': {
                    isAltStr = valIndex === 1 || valIndex === 3;
                    numToComplete = 1;
                    dynamicPart = config.rarityConfigs[questConfig.questVals[valIndex][0] - 1].name + ' Boar';
                    break;
                }

                case 'time': {
                    numToComplete = 1;
                    dynamicPart = '<' + questConfig.questVals[valIndex][0].toLocaleString() + 'ms';
                    break;
                }
            }

            await CanvasUtils.drawText(
                ctx,
                isAltStr
                    ? questConfig.descriptionAlt
                    : questConfig.description,
                questStrPos,
                fontMedium,
                'left',
                colorConfig.font,
                nums.questStrWidth,
                false,
                [dynamicPart],
                [colorConfig.green]
            );

            await CanvasUtils.drawText(
                ctx,
                questProgress + '/' + numToComplete,
                progressStrPos,
                fontMedium,
                'left',
                questProgress >= numToComplete
                    ? colorConfig.green
                    : colorConfig.silver
            );

            if (questRewardLeft > 0 && valIndex < 2 && questConfig.lowerReward === 'bucks') {
                await CanvasUtils.drawText(
                    ctx,
                    '+%@',
                    bucksRewardPos,
                    fontMedium,
                    'right',
                    colorConfig.font,
                    undefined,
                    undefined,
                    ['$' + questRewardLeft],
                    [colorConfig.bucks]
                );
                fullComplete = false;
            } else if (questRewardLeft > 0 && valIndex < 2) {
                const powRewardImgPath = pathConfig.powerups + powConfigs[questConfig.lowerReward].file;

                await CanvasUtils.drawText(
                    ctx,
                    '+%@',
                    powRewardAmtPos,
                    fontSmallest,
                    'right',
                    colorConfig.font,
                    undefined,
                    undefined,
                    [questRewardLeft.toLocaleString()],
                    [colorConfig.powerup]
                );
                ctx.drawImage(await loadImage(powRewardImgPath), ...powRewardImgPos, ...nums.questRewardImgSize);
                fullComplete = false;
            } else if (questRewardLeft > 0) {
                const powRewardImgPath = pathConfig.powerups + powConfigs[questConfig.higherReward].file;

                await CanvasUtils.drawText(
                    ctx,
                    '+%@',
                    powRewardAmtPos,
                    fontSmallest,
                    'right',
                    colorConfig.font,
                    undefined,
                    undefined,
                    [questRewardLeft.toLocaleString()],
                    [colorConfig.powerup]
                );
                ctx.drawImage(await loadImage(powRewardImgPath), ...powRewardImgPos, ...nums.questRewardImgSize);
                fullComplete = false;
            } else {
                ctx.drawImage(await loadImage(checkImgPath), ...powRewardImgPos, ...nums.questRewardImgSize);
            }

            index++;
        }

        if (fullCompleteRewardLeft > 0) {
            await CanvasUtils.drawText(
                ctx,
                strConfig.questCompletionBonus,
                nums.questCompletionLabelPos,
                fontMedium,
                'center',
                fullComplete
                    ? colorConfig.green
                    : colorConfig.font
            );

            await CanvasUtils.drawText(
                ctx,
                fullCompleteRewardLeft + ' ' + config.itemConfigs.powerups.enhancer.pluralName,
                nums.questCompletionPos,
                fontMedium,
                'center',
                colorConfig.powerup
            );
        } else {
            ctx.drawImage(
                await loadImage(checkImgPath), ...nums.questCompleteCheckPos, ...nums.questRewardImgSize
            );
            await CanvasUtils.drawText(
                ctx, strConfig.questFullyComplete, nums.questCompleteStrPos, fontBig, 'center', colorConfig.green
            );
        }

        return new AttachmentBuilder(await canvas.png, { name: `${strConfig.defaultImageName}.png` });
    }
}
