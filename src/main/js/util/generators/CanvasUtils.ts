import {Canvas, Image, loadImage, CanvasRenderingContext2D} from 'skia-canvas';

/**
 * {@link CanvasUtils CanvasUtils.ts}
 *
 * A collection of functions to make canvas
 * editing easier/cleaner.
 *
 * @copyright WeslayCodes & Contributors 2023
 */
export class CanvasUtils {
    /**
     * Draws text onto CanvasRenderingContext
     *
     * @param ctx - CanvasRenderingContext
     * @param text - Text to draw
     * @param pos - Position to place text
     * @param font - Font to use for text
     * @param align - Alignment of text
     * @param color - Base color of text
     * @param coloredContents - Texts to use secondary colors on
     * @param secondaryColors - Secondary colors
     * @param wrap - Whether to wrap the text
     * @param width - Width to wrap/shrink at
     */
    public static async drawText(
        ctx: CanvasRenderingContext2D,
        text: string,
        pos: [number, number],
        font: string,
        align: CanvasTextAlign,
        color: string,
        width?: number,
        wrap = false,
        coloredContents = [''],
        secondaryColors = [color]
    ): Promise<number> {
        ctx.font = font;
        ctx.textAlign = align;
        ctx.textBaseline = 'alphabetic';
        ctx.fillStyle = color;

        const replaceIndexes = [] as number[];

        for (let i=0; i<coloredContents.length; i++) {
            replaceIndexes.push(text.indexOf('%@', i === 0
                ? 0
                : replaceIndexes[i-1]
            ));
            text = text.replace('%@', coloredContents[i]);
        }

        let heightDiff = 0;

        if (width != undefined && wrap) {
            const words = text.split(' ');
            const lineHeight = (ctx.measureText('Sp').actualBoundingBoxAscent +
                ctx.measureText('Sp').actualBoundingBoxDescent) * 1.1;
            let newHeight = pos[1];
            const lines = [] as string[];
            let curLine = '';

            let totalChars = 0;
            for (let i=0; i<words.length; i++) {
                const word = words[i];

                if (ctx.measureText(curLine + word).width < width) {
                    curLine += word + ' ';
                } else {
                    lines.push(curLine.trim());
                    totalChars--;
                    for (let i=0; i<replaceIndexes.length; i++) {
                        if (replaceIndexes[i] >= totalChars) {
                            replaceIndexes[i]--;
                        }
                    }
                    curLine = word + ' ';
                }
                totalChars += (word + ' ').length;
            }

            if (curLine.trim() !== '') {
                lines.push(curLine.trim());
            }

            newHeight -= lineHeight * (lines.length-1) / 2;

            let charIndex = 0;

            for (const line of lines) {
                const prevCharIndex = charIndex;
                charIndex += line.length;

                let firstIndex = -1;
                let lastIndex = -1;

                for (let i=0; i<replaceIndexes.length; i++) {
                    if (replaceIndexes[i] >= charIndex) {
                        break;
                    }

                    if (replaceIndexes[i] >= prevCharIndex) {
                        firstIndex = firstIndex >= 0 ? firstIndex : i;
                        lastIndex = i;
                    }
                }

                const relReplaceIndexes = lastIndex >= 0
                    ? replaceIndexes.slice(firstIndex, lastIndex+1).map((val: number) => {
                        return val - prevCharIndex;
                    })
                    : [-1];
                const contentToReplace = lastIndex >= 0
                    ? coloredContents.slice(firstIndex, lastIndex+1)
                    : [''];
                const colorsToUse = lastIndex >= 0
                    ? secondaryColors.slice(firstIndex, lastIndex+1)
                    : [color];

                if (lastIndex >= 0 && replaceIndexes[lastIndex] + coloredContents[lastIndex].length >= charIndex) {
                    const numCharsToColor = charIndex - replaceIndexes[lastIndex] + 1;

                    contentToReplace[contentToReplace.length-1] = contentToReplace[contentToReplace.length-1]
                        .substring(0, numCharsToColor).trimEnd();
                    replaceIndexes[lastIndex] = charIndex;
                    coloredContents[lastIndex] = coloredContents[lastIndex].substring(numCharsToColor).trimEnd();
                }

                await this.drawColoredText(
                    ctx,
                    line,
                    'center',
                    [pos[0], newHeight],
                    relReplaceIndexes,
                    contentToReplace,
                    color,
                    colorsToUse
                );

                newHeight += lineHeight;
                heightDiff += lineHeight;
            }
        } else if (width != undefined) {
            ctx.textBaseline = 'middle';
            while (ctx.measureText(text).width > width) {
                font = (parseInt(font)-1) + font.substring(font.indexOf('px'));
                ctx.font = font;
            }
            await this.drawColoredText(ctx, text, align, pos, replaceIndexes, coloredContents, color, secondaryColors);
        } else {
            await this.drawColoredText(ctx, text, align, pos, replaceIndexes, coloredContents, color, secondaryColors);
        }

        return heightDiff;
    }

    /**
     * Draws the secondary colored text
     *
     * @param ctx - CanvasRenderingContext
     * @param text - Text to draw
     * @param align - Alignment of text
     * @param pos - Position to place text
     * @param replaceIndexes - The indexes to start replacing with colored text
     * @param coloredContents - The actual texts that are colored
     * @param color - Base color of text
     * @param secondaryColors - Secondary colors
     * @private
     */
    private static async drawColoredText(
        ctx: CanvasRenderingContext2D,
        text: string,
        align: string,
        pos: [number, number],
        replaceIndexes: number[],
        coloredContents: string[],
        color: string,
        secondaryColors: string[]
    ): Promise<void> {
        const priorNormText = [] as string[];
        let textEnd = '';

        for (let i=0; i<replaceIndexes.length; i++) {
            if (i === 0) {
                priorNormText.push(text.substring(0, replaceIndexes[i]));
            } else {
                priorNormText.push(text.substring(replaceIndexes[i-1] + coloredContents[i-1].length, replaceIndexes[i]));
            }

            if (i === replaceIndexes.length - 1) {
                textEnd = text.substring(replaceIndexes[i] + coloredContents[i].length);
            }
        }

        if (align === 'center') {
            for (let i=0; i<replaceIndexes.length; i++) {
                await this.applyTextGradient(
                    ctx, priorNormText[i], [
                        pos[0] - ctx.measureText(text.substring(replaceIndexes[i])).width / 2 + (i > 0
                            ? ctx.measureText(text.substring(0, replaceIndexes[i-1]) + coloredContents[i-1]).width / 2
                            : 0),
                        pos[1]
                    ], color
                );

                if (replaceIndexes[i] === -1) break;

                await this.applyTextGradient(
                    ctx, coloredContents[i], [
                        pos[0] + ctx.measureText(text.substring(0, replaceIndexes[i])).width / 2 - ctx.measureText(
                            text.substring(replaceIndexes[i] + coloredContents[i].length)
                        ).width / 2,
                        pos[1]
                    ], secondaryColors[i]
                );
            }

            await this.applyTextGradient(
                ctx, textEnd, [
                    pos[0] + ctx.measureText(
                        text.substring(
                            0, replaceIndexes[replaceIndexes.length-1] +
                            coloredContents[coloredContents.length-1].length
                        )
                    ).width / 2,
                    pos[1]
                ], color
            );
        } else if (align === 'left') {
            for (let i=0; i<replaceIndexes.length; i++) {
                await this.applyTextGradient(ctx, priorNormText[i], pos, color);

                if (replaceIndexes[i] === -1) break;

                await this.applyTextGradient(
                    ctx, coloredContents[i], [
                        pos[0] + ctx.measureText(text.substring(0, replaceIndexes[i])).width, pos[1]
                    ], secondaryColors[i]
                );
            }

            await this.applyTextGradient(
                ctx, textEnd, [
                    pos[0] + ctx.measureText(
                        text.substring(
                            0, replaceIndexes[replaceIndexes.length-1] +
                            coloredContents[coloredContents.length-1].length
                        )
                    ).width,
                    pos[1]
                ], color
            );
        } else {
            for (let i=0; i<replaceIndexes.length; i++) {
                await this.applyTextGradient(ctx, priorNormText[i], [
                    pos[0] - ctx.measureText(text).width, pos[1]
                ], color);

                if (replaceIndexes[i] === -1) break;

                await this.applyTextGradient(
                    ctx, coloredContents[i], [
                        pos[0] - ctx.measureText(text.substring(0, replaceIndexes[i])).width, pos[1]
                    ], secondaryColors[i]
                );
            }

            await this.applyTextGradient(
                ctx, textEnd, [
                    pos[0] - ctx.measureText(
                        text.substring(
                            0, replaceIndexes[replaceIndexes.length-1] +
                            coloredContents[coloredContents.length-1].length
                        )
                    ).width,
                    pos[1]
                ], color
            );
        }
    }

    private static async applyTextGradient(
        ctx: CanvasRenderingContext2D, text: string, pos: [number, number], color: string
    ): Promise<void> {
        if (text.length === 0) return;

        if (!color.includes(',')) {
            ctx.fillStyle = color;
            ctx.fillText(text, ...pos);
            return;
        }

        const canvas = new Canvas(ctx.canvas.width, ctx.canvas.height);
        const newCtx = canvas.getContext('2d');

        newCtx.font = ctx.font;
        newCtx.textAlign = ctx.textAlign;
        newCtx.textBaseline = ctx.textBaseline;

        newCtx.fillText(text, ...pos);

        newCtx.globalCompositeOperation = 'source-in';

        const gradStartPos = [
            newCtx.textAlign === 'center'
                ? pos[0] - newCtx.measureText(text).width / 2
                : newCtx.textAlign === 'left'
                    ? pos[0]
                    : pos[0] - newCtx.measureText(text).width,
            newCtx.textBaseline === 'middle'
                ? pos[1] - (newCtx.measureText('Sp').actualBoundingBoxAscent +
                    newCtx.measureText('Sp').actualBoundingBoxDescent) / 2
                : pos[1] - (newCtx.measureText('Sp').actualBoundingBoxAscent)
        ] as [number, number];
        const gradEndPos = [
            gradStartPos[0] + newCtx.measureText(text).width,
            gradStartPos[1] + (newCtx.measureText('Sp').actualBoundingBoxAscent +
                newCtx.measureText('Sp').actualBoundingBoxDescent)
        ] as [number, number];
        const gradColors = color.split(',');

        const gradient = newCtx.createLinearGradient(...gradStartPos, ...gradEndPos);
        gradColors.forEach((gradColor, index) => {
            gradient.addColorStop(index / Math.max(gradColors.length-1, 1), gradColor);
        });

        newCtx.fillStyle = gradient;
        newCtx.fillRect(
            gradStartPos[0], gradStartPos[1], ctx.measureText(text).width,
            (ctx.measureText('Sp').actualBoundingBoxAscent + ctx.measureText('Sp').actualBoundingBoxDescent)
        );

        const buffer = await canvas.png;
        const img = await loadImage(buffer);
        ctx.drawImage(img, 0, 0);
    }

    /**
     * Draws circle onto CanvasRenderingContext
     *
     * @param ctx - CanvasRenderingContext
     * @param img - Image to convert into circle
     * @param pos - Position to place circle
     * @param diameter - Diameter of circle
     */
    public static drawCircleImage(
        ctx: CanvasRenderingContext2D,
        img: Image,
        pos: number[],
        diameter: number
    ): void {
        const radius = diameter / 2;

        ctx.beginPath();
        ctx.arc(pos[0] + radius, pos[1] + radius, radius, 0, Math.PI * 2);
        ctx.closePath();
        ctx.save();
        ctx.clip();
        ctx.drawImage(img, pos[0], pos[1], diameter, diameter);
        ctx.restore();
    }

    /**
     * Draws a line
     *
     * @param ctx - CanvasRenderingContext
     * @param pos1 - Position the line starts at
     * @param pos2 - Position the line ends at
     * @param width - Width of the line
     * @param color - Color of the line
     */
    public static drawLine(
        ctx: CanvasRenderingContext2D,
        pos1: [number, number],
        pos2: [number, number],
        width: number,
        color: string
    ): void {
        ctx.lineWidth = width;

        ctx.beginPath();

        const gradColors = color.split(',');
        const gradient = ctx.createLinearGradient(...pos1, ...pos2);
        gradColors.forEach((gradColor, index) => {
            gradient.addColorStop(index / Math.max(gradColors.length-1, 1), gradColor);
        });

        ctx.strokeStyle = gradient;

        ctx.moveTo(pos1[0], pos1[1]);
        ctx.lineTo(pos2[0], pos2[1]);

        ctx.stroke();
    }

    /**
     * Draws a rectangle
     *
     * @param ctx - CanvasRenderingContext
     * @param pos - Position of rectangle
     * @param size - Dimensions of rectangle
     * @param color - Color of rectangle
     */
    public static drawRect(
        ctx: CanvasRenderingContext2D,
        pos: [number, number],
        size: [number, number],
        color: string
    ): void {
        const gradColors = color.split(',');
        const gradient = ctx.createLinearGradient(...pos, pos[0] + size[0], pos[1] + size[1]);
        gradColors.forEach((gradColor, index) => {
            gradient.addColorStop(index / Math.max(gradColors.length-1, 1), gradColor);
        });

        ctx.fillStyle = gradient;
        ctx.fillRect(...pos, ...size);
    }
}
