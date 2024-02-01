import {PromptTypeConfig} from '../../bot/config/prompts/PromptTypeConfig';
import {PromptConfig} from '../../bot/config/prompts/PromptConfig';
import {BotConfig} from '../../bot/config/BotConfig';
import {Canvas, loadImage, CanvasRenderingContext2D} from 'skia-canvas';
import {CanvasUtils} from './CanvasUtils';
import {AttachmentBuilder} from 'discord.js';
import {BoarBotApp} from '../../BoarBotApp';

/**
 * {@link PowerupImageGenerator PowerupImageGenerator.ts}
 *
 * Create images for powerup spawns
 *
 * @copyright WeslayCodes & Contributors 2023
 */

export class PowerupImageGenerator {
    /**
     * Creates the image for when a powerup spawns
     *
     * @param powerupTypeID - Used to get the reward for the powerup
     * @param promptType - Used to get the prompt description
     * @param prompt - Used to get the prompt description (if the prompt has one)
     * @param config - Used for getting config info
     */
    public static async makePowerupSpawnImage(
        powerupTypeID: string,
        promptType: PromptTypeConfig,
        prompt: PromptConfig,
        config: BotConfig
    ): Promise<AttachmentBuilder> {
        const strConfig = config.stringConfig;
        const nums = config.numberConfig;
        const colorConfig = config.colorConfig;

        const font = `${nums.fontBig}px ${strConfig.fontName}`;

        const promptDescription = promptType.description ? promptType.description : prompt.description;
        const coloredContent = [] as string[];

        if (prompt.rightClock) {
            coloredContent.push(prompt.name);
        }

        const canvas = new Canvas(...nums.eventSpawnSize);
        const ctx = canvas.getContext('2d');

        await this.makeBaseHeaderFooter(ctx, powerupTypeID, strConfig.eventTitle.replace('%@', 'POWERUP'), config);

        await CanvasUtils.drawText(
            ctx,
            promptDescription,
            nums.powSpawnDescriptionPos,
            font,
            'center',
            colorConfig.font,
            nums.powSpawnDescriptionWidth,
            true,
            coloredContent,
            [config.colorConfig.silver]
        );

        return new AttachmentBuilder(
            await canvas.png, { name: `${config.stringConfig.defaultImageName}.png` }
        );
    }

    /**
     * Used to replace the spawn image after the powerup is done
     *
     * @param powerupTypeID - Used to get powerup reward name
     * @param topClaimer - The top user and their time
     * @param avgTime - The average time for the powerup
     * @param numUsers - Number of users that claimed the powerup
     * @param promptID - The ID of the prompt
     * @param promptTypeID - The ID of the prompt type
     * @param config - Used for config info
     */
    public static async makePowerupEndImage(
        powerupTypeID: string,
        topClaimer: [string, number] | undefined,
        avgTime: number | undefined,
        numUsers: number,
        promptID: string,
        promptTypeID: string,
        config: BotConfig
    ): Promise<AttachmentBuilder> {
        const strConfig = config.stringConfig;
        const nums = config.numberConfig;
        const colorConfig = config.colorConfig;

        const font = `${nums.fontBig}px ${strConfig.fontName}`;

        const canvas = new Canvas(...nums.eventSpawnSize);
        const ctx = canvas.getContext('2d');

        await this.makeBaseHeaderFooter(
            ctx, powerupTypeID, strConfig.eventEndedTitle.replace('%@', 'POWERUP'), config
        );

        if (numUsers > 0 && topClaimer && avgTime) {
            let topClaimerUsername = strConfig.deletedUsername;

            try {
                const user = await BoarBotApp.getBot().getClient().users.fetch(topClaimer[0] as string);
                topClaimerUsername = user.username.substring(0, nums.maxUsernameLength);
            } catch {}

            await CanvasUtils.drawText(ctx, strConfig.powTop, nums.powTopLabelPos, font, 'center', colorConfig.font);
            await CanvasUtils.drawText(
                ctx,
                strConfig.powTopResult
                    .replace('%@', topClaimer[1].toLocaleString())
                    .replace('%@', topClaimerUsername),
                nums.powTopPos,
                font,
                'center',
                colorConfig.silver,
                nums.powDataWidth
            );

            await CanvasUtils.drawText(ctx, strConfig.powAvg, nums.powAvgLabelPos, font, 'center', colorConfig.font);
            await CanvasUtils.drawText(
                ctx,
                numUsers > 1
                    ? strConfig.powAvgResultPlural
                        .replace('%@', avgTime.toLocaleString())
                        .replace('%@', numUsers.toLocaleString())
                    : strConfig.powAvgResult
                        .replace('%@', avgTime.toLocaleString())
                        .replace('%@', numUsers.toLocaleString()),
                nums.powAvgPos,
                font,
                'center',
                colorConfig.silver,
                nums.powDataWidth
            );

            await CanvasUtils.drawText(ctx, strConfig.powPrompt, nums.powPromptLabelPos, font, 'center', colorConfig.font);
            await CanvasUtils.drawText(
                ctx,
                config.promptConfigs.types[promptTypeID].name + ' - ' +
                    (config.promptConfigs.types[promptTypeID][promptID] as PromptConfig).name,
                nums.powPromptPos,
                font,
                'center',
                colorConfig.silver,
                nums.powDataWidth
            );
        } else {
            await CanvasUtils.drawText(
                ctx,
                strConfig.eventNobody,
                nums.powSpawnDescriptionPos,
                font,
                'center',
                colorConfig.font,
                nums.powSpawnDescriptionWidth,
                true
            );
        }

        return new AttachmentBuilder(
            await canvas.png, { name: `${config.stringConfig.defaultImageName}.png` }
        );
    }

    private static async makeBaseHeaderFooter(
        ctx: CanvasRenderingContext2D, powerupTypeID: string, title: string, config: BotConfig
    ): Promise<void> {
        const strConfig = config.stringConfig;
        const pathConfig = config.pathConfig;
        const nums = config.numberConfig;
        const colorConfig = config.colorConfig;

        const fontTitle = `${nums.fontHuge}px ${strConfig.fontName}`;
        const font = `${nums.fontBig}px ${strConfig.fontName}`;

        const powerupType = config.itemConfigs.powerups[powerupTypeID];
        const powRewardStr = powerupType.rewardAmt + ' ' + (powerupType.rewardAmt as number > 1
            ? powerupType.pluralName
            : powerupType.name);

        ctx.drawImage(await loadImage(pathConfig.otherAssets + pathConfig.eventUnderlay), ...nums.originPos);

        await CanvasUtils.drawText(
            ctx, title, nums.eventTitlePos, fontTitle, 'center', colorConfig.powerup, nums.eventTitleWidth, true
        );

        ctx.drawImage(
            await loadImage(pathConfig.otherAssets + pathConfig.powerup),
            ...nums.eventCornerImgPos1, ...nums.eventCornerImgSize
        );

        ctx.save();
        ctx.scale(-1, 1);

        ctx.drawImage(
            await loadImage(pathConfig.otherAssets + pathConfig.powerup),
            ...nums.eventCornerImgPos2, ...nums.eventCornerImgSize
        );

        ctx.scale(1, 1);
        ctx.restore();

        await CanvasUtils.drawText(
            ctx,
            strConfig.powReward,
            nums.powSpawnRewardPos,
            font,
            'center',
            colorConfig.font,
            nums.powSpawnDescriptionWidth,
            false,
            [powRewardStr],
            [colorConfig.powerup]
        );
    }
}
