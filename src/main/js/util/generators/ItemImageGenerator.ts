import Canvas from 'canvas';
import {BotConfig} from '../../bot/config/BotConfig';
import {BoarUtils} from '../boar/BoarUtils';
import {PythonShell} from 'python-shell';
import {LogDebug} from '../logging/LogDebug';
import {CanvasUtils} from './CanvasUtils';
import {AttachmentBuilder, User} from 'discord.js';
import fs from 'fs';

/**
 * {@link ItemImageGenerator ItemImageGenerator.ts}
 *
 * Creates the dynamic item image.
 *
 * @copyright WeslayCodes & Contributors 2023
 */
export class ItemImageGenerator {
    private readonly user = {} as User;
    private readonly config = {} as BotConfig;
    private readonly id = '' as string;
    private readonly title = '' as string;
    private buffer = {} as Buffer;
    private tempPath = '';
    private colorKey = '';
    private imageFilePath = '';
    private userAvatar = '';
    private userTag = '';
    private giftingUserAvatar?: string;
    private giftingUserTag?: string;
    private itemName = '';
    private itemNameColored = '';
    private itemFile = '';

    /**
     * Creates a new item image generator
     *
     * @param user - The user that got the item
     * @param id - The ID of the item
     * @param title - The title to put on the item attachment
     * @param config - Used to get path, string, and other information
     */
    constructor(user: User, id: string, title: string, config: BotConfig) {
        this.user = user;
        this.config = config;
        this.id = id;
        this.title = title;
    }

    /**
     * Creates the image to be sent on boar/badge add
     *
     * @param isBadge - Whether the item is a badge
     * @param giftingUser - The user that's gifting the item (if there is one)
     * @param coloredText - The portion of text that's colored (if there is one)
     * @param manualInput - A way to input name, file, and color information outside of ID
     * @param score - Score gained from the item
     * @return AttachmentBuilder object containing image
     * @private
     */
    public async handleImageCreate(
        isBadge = false,
        giftingUser?: User,
        coloredText?: string,
        manualInput?: { name: string, file: string, colorKey: string },
        score?: number
    ): Promise<AttachmentBuilder> {
        const strConfig = this.config.stringConfig;
        const pathConfig = this.config.pathConfig;

        let folderPath: string | undefined;

        if (isBadge && manualInput === undefined) {
            const badgeInfo = this.config.itemConfigs.badges[this.id];
            this.itemName = badgeInfo.name;
            this.itemFile = badgeInfo.file;
            folderPath = pathConfig.badges;
            this.colorKey = 'badge';
        } else if (manualInput === undefined) {
            const boarInfo = this.config.itemConfigs.boars[this.id];
            this.itemName = boarInfo.name;
            this.itemFile = boarInfo.file;
            folderPath = pathConfig.boars;
            this.colorKey = 'rarity' + BoarUtils.findRarity(this.id, this.config)[0];
        } else {
            this.itemName = manualInput.name;
            this.itemFile = manualInput.file;
            this.colorKey = manualInput.colorKey;
        }

        this.itemNameColored = coloredText ? coloredText : this.itemName;
        this.itemName = this.itemName.replace(this.itemNameColored, '%@');

        this.imageFilePath = folderPath
            ? folderPath + this.itemFile
            : this.itemFile;
        const imageExtension = this.imageFilePath.split('.')[1];
        const isAnimated = imageExtension === 'gif';

        const usernameLength = this.config.numberConfig.maxUsernameLength;

        this.tempPath = pathConfig.tempItemAssets + this.id + this.colorKey +
            this.title.toLowerCase().substring(0, 4) + '.' + imageExtension;

        this.userAvatar = this.user.displayAvatarURL({ extension: 'png' });
        this.userTag = this.user.username.substring(0, usernameLength);

        this.giftingUserTag = giftingUser?.username.substring(0, usernameLength);
        this.giftingUserAvatar = giftingUser?.displayAvatarURL({ extension: 'png' });

        // Creates base response attachment depending on the boar's image type
        if (!fs.existsSync(this.tempPath)) {
            if (isAnimated) {
                await this.makeAnimated();
            } else {
                await this.makeStatic();
            }
            fs.writeFileSync(this.tempPath, this.buffer);
        } else {
            this.buffer = fs.readFileSync(this.tempPath);
        }

        if (isAnimated) {
            await this.addAnimatedProfile(score);
        } else {
            await this.addStaticProfile(score);
        }

        return new AttachmentBuilder(this.buffer, { name:`${strConfig.defaultImageName}.${imageExtension}` });
    }

    /**
     * Creates the base of an item image if it's animated
     *
     * @private
     */
    private async makeAnimated(): Promise<void> {
        const script = this.config.pathConfig.dynamicImageScript;
        await this.makeStatic(false);

        const tempAnimBasePath = this.config.pathConfig.tempItemAssets + this.id + this.colorKey + Date.now() +
            'animbase.png';
        fs.writeFileSync(tempAnimBasePath, this.buffer);

        const scriptOptions = {
            args: [
                JSON.stringify(this.config.pathConfig),
                JSON.stringify(this.config.numberConfig),
                this.imageFilePath,
                tempAnimBasePath
            ]
        };

        // Sends python all dynamic image data and receives final animated image
        let data: string[];
        try {
            data = await PythonShell.run(script, scriptOptions);
        } catch (err: unknown) {
            LogDebug.handleError(err);
            return;
        }

        this.buffer = Buffer.from(data[0], 'base64');

        fs.rmSync(tempAnimBasePath);
    }

    /**
     * Creates the base of an item image if it's static
     *
     * @private
     */
    private async makeStatic(makeWithBoar = true): Promise<void> {
        const strConfig = this.config.stringConfig;
        const nums = this.config.numberConfig;
        const pathConfig = this.config.pathConfig;
        const colorConfig = this.config.colorConfig;

        const itemAssetsFolder = pathConfig.itemAssets;
        const underlayPath = itemAssetsFolder + pathConfig.itemUnderlay;
        const backplatePath = itemAssetsFolder + pathConfig.itemBackplate;
        const overlay = itemAssetsFolder + pathConfig.itemOverlay;

        const origin = nums.originPos as [number, number];
        const imageSize = nums.itemImageSize as [number, number];

        const mainPos = nums.itemPos as [number, number];
        const mainSize = nums.itemSize as [number, number];

        const fontName = strConfig.fontName;
        const mediumFont = `${nums.fontMedium}px ${fontName}`;

        const canvas = Canvas.createCanvas(imageSize[0], imageSize[1]);
        const ctx = canvas.getContext('2d');

        CanvasUtils.drawRect(ctx, origin, imageSize, colorConfig[this.colorKey]);
        ctx.globalCompositeOperation = 'destination-in';
        ctx.drawImage(await Canvas.loadImage(underlayPath), ...origin, ...imageSize);
        ctx.globalCompositeOperation = 'source-over';

        ctx.drawImage(await Canvas.loadImage(backplatePath), ...origin);
        if (makeWithBoar) {
            ctx.drawImage(await Canvas.loadImage(this.imageFilePath), ...mainPos, ...mainSize);
        }
        ctx.drawImage(await Canvas.loadImage(overlay), ...origin);

        await CanvasUtils.drawText(ctx, this.title, nums.itemTitlePos, mediumFont, 'center', colorConfig.font);
        await CanvasUtils.drawText(
            ctx,
            this.itemName,
            nums.itemNamePos,
            mediumFont,
            'center',
            colorConfig.font,
            undefined,
            false,
            [this.itemNameColored],
            [colorConfig[this.colorKey]]
        );

        this.buffer = canvas.toBuffer('image/png');
    }

    /**
     * Adds user and other information on top of animated image
     *
     * @param score - The score that was gained
     * @private
     */
    private async addAnimatedProfile(score?: number): Promise<void> {
        const script = this.config.pathConfig.userOverlayScript;

        const scriptOptions = {
            args: [
                JSON.stringify(this.config.pathConfig),
                JSON.stringify(this.config.colorConfig),
                JSON.stringify(this.config.numberConfig),
                this.tempPath,
                this.userAvatar,
                this.userTag,
                score === undefined ? '' : score.toLocaleString(),
                this.giftingUserAvatar === undefined ? '' : this.giftingUserAvatar,
                this.giftingUserTag === undefined ? '' : this.giftingUserTag
            ]
        };

        // Sends python all dynamic image data and receives final animated image
        let data: string[];
        try {
            data = await PythonShell.run(script, scriptOptions);
        } catch (err: unknown) {
            LogDebug.handleError(err);
            return;
        }

        this.buffer = Buffer.from(data[0], 'base64');
    }

    /**
     * Adds user and other information on top of static image
     *
     * @param score - The score that was gained
     * @private
     */
    private async addStaticProfile(score?: number): Promise<void> {
        const nums = this.config.numberConfig;
        const colorConfig = this.config.colorConfig;

        const smallMediumFont = `${nums.fontSmallMedium}px ${this.config.stringConfig.fontName}`;

        const origin = nums.originPos as [number, number];
        const imageSize = nums.itemImageSize as [number, number];

        let userBoxY = nums.itemBoxOneY;

        const canvas = Canvas.createCanvas(imageSize[0], imageSize[1]);
        const ctx = canvas.getContext('2d');

        ctx.drawImage(await Canvas.loadImage(this.tempPath), ...origin, ...imageSize);

        ctx.font = smallMediumFont;

        if (this.giftingUserTag !== undefined && this.giftingUserAvatar !== undefined) {
            userBoxY = nums.itemBoxTwoY;

            ctx.beginPath();
            ctx.roundRect(
                nums.itemBoxX, nums.itemBoxOneY,
                ctx.measureText('To').width + nums.itemTextBoxExtra, nums.itemBoxHeight, nums.border
            );
            ctx.fillStyle = this.config.colorConfig.dark;
            ctx.fill();

            await CanvasUtils.drawText(
                ctx,
                'To',
                [nums.itemTextX, nums.itemBoxOneY + nums.itemTextYOffset],
                smallMediumFont,
                'left',
                colorConfig.font
            );

            ctx.beginPath();
            ctx.roundRect(
                nums.itemBoxX,
                nums.itemBoxThreeY,
                ctx.measureText('From').width + nums.itemTextBoxExtra,
                nums.itemBoxHeight,
                nums.border
            );
            ctx.fillStyle = this.config.colorConfig.dark;
            ctx.fill();

            await CanvasUtils.drawText(
                ctx,
                'From',
                [nums.itemTextX, nums.itemBoxThreeY + nums.itemTextYOffset],
                smallMediumFont,
                'left',
                colorConfig.font
            );

            ctx.beginPath();
            ctx.roundRect(
                nums.itemBoxX,
                nums.itemBoxFourY,
                ctx.measureText(this.giftingUserTag).width + nums.itemUserBoxExtra,
                nums.itemBoxHeight,
                nums.border
            );
            ctx.fillStyle = this.config.colorConfig.dark;
            ctx.fill();

            await CanvasUtils.drawText(
                ctx,
                this.giftingUserTag,
                [nums.itemUserTagX, nums.itemBoxFourY + nums.itemTextYOffset],
                smallMediumFont,
                'left',
                colorConfig.font
            );

            CanvasUtils.drawCircleImage(
                ctx,
                await Canvas.loadImage(this.giftingUserAvatar),
                [nums.itemUserAvatarX, nums.itemBoxFourY + nums.itemUserAvatarYOffset],
                nums.itemUserAvatarWidth
            );
        }

        ctx.beginPath();
        ctx.roundRect(
            nums.itemBoxX,
            userBoxY,
            ctx.measureText(this.userTag).width + nums.itemUserBoxExtra,
            nums.itemBoxHeight,
            nums.border
        );
        ctx.fillStyle = this.config.colorConfig.dark;
        ctx.fill();

        await CanvasUtils.drawText(
            ctx,
            this.userTag,
            [nums.itemUserTagX, userBoxY + nums.itemTextYOffset],
            smallMediumFont,
            'left',
            colorConfig.font
        );

        let userAvatar;

        try {
            userAvatar = await Canvas.loadImage(this.userAvatar);
        } catch {
            userAvatar = await Canvas.loadImage(this.config.pathConfig.otherAssets + this.config.pathConfig.noAvatar);
        }

        CanvasUtils.drawCircleImage(
            ctx, userAvatar, [nums.itemUserAvatarX, userBoxY + nums.itemUserAvatarYOffset], nums.itemUserAvatarWidth
        );

        if (score && this.giftingUserTag === undefined) {
            ctx.beginPath();
            ctx.roundRect(
                nums.itemBoxX,
                nums.itemBoxTwoY,
                ctx.measureText('+$' + score).width + nums.itemTextBoxExtra,
                nums.itemBoxHeight,
                nums.border
            );
            ctx.fillStyle = this.config.colorConfig.dark;
            ctx.fill();

            await CanvasUtils.drawText(
                ctx,
                '+%@',
                [nums.itemTextX, nums.itemBoxTwoY + nums.itemTextYOffset],
                smallMediumFont,
                'left',
                colorConfig.font,
                undefined,
                false,
                ['$' + score.toLocaleString()],
                [colorConfig.bucks]
            );
        }

        this.buffer = canvas.toBuffer('image/png');
    }
}
