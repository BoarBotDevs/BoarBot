import {BotConfig} from '../../bot/config/BotConfig';
import {Canvas} from 'skia-canvas';
import {CanvasUtils} from './CanvasUtils';
import {AttachmentBuilder} from 'discord.js';

/**
 * {@link CustomEmbedGenerator CustomEmbedGenerator.ts}
 *
 * Creates an embed that dynamically shrinks and grows
 *
 * @copyright WeslayCodes & Contributors 2023
 */

export class CustomEmbedGenerator {
    /**
     * Creates a dynamic generic embed
     *
     * @param str - The string to put in the embed
     * @param color - Color of the string
     * @param config - Used to get position and other config info
     * @param coloredContents - A portion of text to color differently
     * @param secondaryColors - The secondary colors
     */
    public static async makeEmbed(
        str: string, color: string, config: BotConfig, coloredContents?: string[], secondaryColors?: string[]
    ) {
        const strConfig = config.stringConfig;
        const nums = config.numberConfig;
        const colorConfig = config.colorConfig;

        const font = `${nums.fontBig}px ${strConfig.fontName}`;

        const canvas = new Canvas(1, nums.embedMinHeight);
        const ctx = canvas.getContext('2d');

        let fullString = str;

        if (coloredContents) {
            for (const content of coloredContents) {
                fullString = fullString.replace('%@', content);
            }
        }

        ctx.font = font;
        canvas.width = Math.min(ctx.measureText(fullString).width + nums.border * 4, nums.embedMaxWidth);

        canvas.height += await CanvasUtils.drawText(
            ctx, fullString, nums.originPos, font, 'center', colorConfig.font, canvas.width === nums.embedMaxWidth
                ? nums.embedMaxWidth - nums.border * 4
                : undefined,
            true
        );
        ctx.clearRect(nums.originPos[0], nums.originPos[1], canvas.width, canvas.height);

        ctx.beginPath();
        ctx.fillStyle = colorConfig.dark;
        ctx.roundRect(0, 0, canvas.width, canvas.height, canvas.width/nums.embedMaxWidth * nums.border);
        ctx.fill();

        ctx.beginPath();
        ctx.fillStyle = colorConfig.mid;
        ctx.fillRect(
            canvas.width / nums.embedMaxWidth * nums.border,
            canvas.width / nums.embedMaxWidth * nums.border,
            canvas.width - canvas.width / nums.embedMaxWidth * nums.border * 2,
            canvas.height - canvas.width / nums.embedMaxWidth * nums.border * 2,
        );
        ctx.fill();

        await CanvasUtils.drawText(
            ctx,
            str,
            [canvas.width / 2, canvas.height / 2 + nums.fontSmallMedium / 2 + 7],
            font,
            'center',
            color,
            canvas.width === nums.embedMaxWidth
                ? nums.embedMaxWidth - nums.border * 4
                : undefined,
            true,
            coloredContents,
            secondaryColors
        );

        return new AttachmentBuilder(await canvas.png, { name: `${strConfig.defaultImageName}.png` });
    }
}
