import {Canvas, loadImage} from 'skia-canvas';
import {BotConfig} from '../../bot/config/BotConfig';
import {CanvasUtils} from './CanvasUtils';
import {
    AttachmentBuilder
} from 'discord.js';
import {Queue} from '../interactions/Queue';
import {DataHandlers} from '../data/DataHandlers';
import {ChoicesConfig} from '../../bot/config/commands/ChoicesConfig';

enum Board {
    Bucks = 'bucks',
    Total = 'total',
    Uniques = 'uniques',
    UniquesSB = 'uniquesSB',
    Streak = 'streak',
    Attempts = 'attempts',
    TopAttempts = 'topAttempts',
    GiftsUsed = 'giftsUsed',
    Multiplier = 'multiplier',
    Fastest = 'fastest'
}

/**
 * {@link LeaderboardImageGenerator LeaderboardImageGenerator.ts}
 *
 * Creates the dynamic leaderboard image.
 *
 * @copyright WeslayCodes & Contributors 2023
 */
export class LeaderboardImageGenerator {
    private config = {} as BotConfig;
    private curBoard = Board.Bucks;
    private boardData = [] as [string, [string, number]][];

    /**
     * Creates a new leaderboard image generator
     *
     * @param boardData
     * @param board
     * @param config - Used to get strings, paths, and other information
     */
    constructor(boardData: [string, [string, number]][], board: Board, config: BotConfig) {
        this.curBoard = board;
        this.boardData = boardData;
        this.config = config;
    }

    /**
     * Used when leaderboard boar type has changed
     *
     * @param boardData
     * @param board
     * @param config - Used to get strings, paths, and other information
     */
    public async updateInfo(boardData: [string, [string, number]][], board: Board, config: BotConfig): Promise<void> {
        this.curBoard = board;
        this.boardData = boardData;
        this.config = config;
    }

    public async makeLeaderboardImage(page: number): Promise<AttachmentBuilder> {
        const strConfig = this.config.stringConfig;
        const nums = this.config.numberConfig;
        const topChoices = this.config.commandConfigs.boar.top.args[0].choices as ChoicesConfig[];
        const colorConfig = this.config.colorConfig;

        const underlay = this.config.pathConfig.otherAssets + this.config.pathConfig.leaderboardUnderlay;

        const curShowing = this.boardData.slice(
            page * nums.leaderboardNumPlayers, (page+1) * nums.leaderboardNumPlayers
        );
        let leaderboardTypeStr = '';

        switch(this.curBoard) {
            case (Board.Bucks): {
                leaderboardTypeStr = topChoices[0].name;
                break;
            }

            case (Board.Total): {
                leaderboardTypeStr = topChoices[1].name;
                break;
            }

            case (Board.Uniques): {
                leaderboardTypeStr = topChoices[2].name;
                break;
            }

            case (Board.UniquesSB): {
                leaderboardTypeStr = topChoices[3].name;
                break;
            }

            case (Board.Streak): {
                leaderboardTypeStr = topChoices[4].name;
                break;
            }

            case (Board.Attempts): {
                leaderboardTypeStr = topChoices[5].name;
                break;
            }

            case (Board.TopAttempts): {
                leaderboardTypeStr = topChoices[6].name;
                break;
            }

            case (Board.GiftsUsed): {
                leaderboardTypeStr = topChoices[7].name;
                break;
            }

            case (Board.Multiplier): {
                leaderboardTypeStr = topChoices[8].name;
                break;
            }

            case (Board.Fastest): {
                leaderboardTypeStr = topChoices[9].name;
                break;
            }
        }

        const numUsers = this.boardData.length;
        const maxPages = Math.ceil(this.boardData.length / nums.leaderboardNumPlayers) - 1;

        const bigFont = `${nums.fontBig}px ${strConfig.fontName}`;
        const mediumFont = `${nums.fontMedium}px ${strConfig.fontName}`;

        const canvas = new Canvas(...nums.collImageSize);
        const ctx = canvas.getContext('2d');

        ctx.drawImage(await loadImage(underlay), ...nums.originPos);

        await CanvasUtils.drawText(
            ctx,
            strConfig.boardHeader.replace('%@', leaderboardTypeStr.toUpperCase()),
            nums.leaderboardHeaderPos,
            bigFont,
            'left',
            colorConfig.font,
            nums.leaderboardTopBotWidth
        );

        await CanvasUtils.drawText(
            ctx,
            strConfig.boardFooter
                .replace('%@', numUsers.toLocaleString())
                .replace('%@', (page+1).toLocaleString())
                .replace('%@', (maxPages+1).toLocaleString())
                .replace('%@', (page * nums.leaderboardNumPlayers + 1).toLocaleString())
                .replace('%@', Math.min(((page+1) * nums.leaderboardNumPlayers), numUsers).toLocaleString()),
            nums.leaderboardFooterPos,
            mediumFont,
            'center',
            colorConfig.font,
            nums.leaderboardTopBotWidth
        );

        for (let i=0; i<curShowing.length; i++) {
            const userPos = [
                nums.leaderboardStart[0] + Math.floor(i / nums.leaderboardRows) * nums.leaderboardIncX,
                nums.leaderboardStart[1] + i % nums.leaderboardRows * nums.leaderboardIncY
            ] as [number, number];
            const userID = curShowing[i][0];
            const userVal = curShowing[i][1][1].toLocaleString() + (this.curBoard === Board.Fastest
                ? 'ms'
                : '');
            let username = curShowing[i][1][0];
            const position = (page * nums.leaderboardNumPlayers) + 1 + i;
            const bannedUserIDs = Object.keys(
                DataHandlers.getGlobalData(DataHandlers.GlobalFile.BannedUsers) as Record<string, number>
            );
            let positionColor: string;

            if (bannedUserIDs.includes(userID)) {
                username = strConfig.deletedUsername;
                await Queue.addQueue(async () => {
                    await DataHandlers.removeLeaderboardUser(userID);
                }, 'top_rem_banned' + userID + 'global').catch((err: unknown) => {
                    throw err;
                });
            }

            switch (position) {
                case 1: {
                    positionColor = colorConfig.gold;
                    break;
                }

                case 2: {
                    positionColor = colorConfig.silver;
                    break;
                }

                case 3: {
                    positionColor = colorConfig.bronze;
                    break;
                }

                default: {
                    positionColor = colorConfig.font;
                }
            }

            await CanvasUtils.drawText(
                ctx,
                '%@ ' + username + ' - ' + userVal,
                userPos,
                mediumFont,
                'center',
                colorConfig.font,
                nums.leaderboardEntryWidth,
                false,
                ['#' + position],
                [positionColor]
            );
        }

        return new AttachmentBuilder(await canvas.png, { name: `${strConfig.defaultImageName}.png` })
    }
}
