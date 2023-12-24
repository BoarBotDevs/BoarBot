import {
    ActionRowBuilder, AttachmentBuilder,
    ButtonBuilder,
    ButtonInteraction,
    ChatInputCommandInteraction, Client, ColorResolvable, EmbedBuilder,
    Events,
    Interaction, InteractionCollector,
    MessageComponentInteraction,
    ModalBuilder,
    StringSelectMenuBuilder, StringSelectMenuInteraction,
    TextChannel
} from 'discord.js';
import {BoarUser} from '../../util/boar/BoarUser';
import {BoarBotApp} from '../../BoarBotApp';
import {Subcommand} from '../../api/commands/Subcommand';
import {Queue} from '../../util/interactions/Queue';
import {InteractionUtils} from '../../util/interactions/InteractionUtils';
import {LogDebug} from '../../util/logging/LogDebug';
import {CollectorUtils} from '../../util/discord/CollectorUtils';
import {ComponentUtils} from '../../util/discord/ComponentUtils';
import {BoarUtils} from '../../util/boar/BoarUtils';
import {CollectionImageGenerator} from '../../util/generators/CollectionImageGenerator';
import {Replies} from '../../util/interactions/Replies';
import {FormatStrings} from '../../util/discord/FormatStrings';
import {RarityConfig} from '../../bot/config/items/RarityConfig';
import {BoarGift} from '../../feat/BoarGift';
import {ItemImageGenerator} from '../../util/generators/ItemImageGenerator';
import {DataHandlers} from '../../util/data/DataHandlers';
import {GuildData} from '../../bot/data/global/GuildData';
import {BoardData} from '../../bot/data/global/BoardData';
import {QuestData} from '../../bot/data/global/QuestData';

enum View {
    Normal,
    Detailed,
    Powerups
}

/**
 * {@link CollectionSubcommand CollectionSubcommand.ts}
 *
 * Used to see a collection of boars, powerups,
 * and other information pertaining to a user.
 *
 * @copyright WeslayCodes & Contributors 2023
 */
export default class CollectionSubcommand implements Subcommand {
    private config = BoarBotApp.getBot().getConfig();
    private subcommandInfo = this.config.commandConfigs.boar.collection;
    private guildData?: GuildData;
    private firstInter = {} as ChatInputCommandInteraction;
    private compInter = {} as ButtonInteraction;
    private collectionImage = {} as CollectionImageGenerator;
    private allBoars = [] as {
        id: string,
        name: string,
        file: string,
        staticFile?: string,
        num: number,
        editions: number[],
        editionDates: number[],
        firstObtained: number,
        lastObtained: number,
        rarity: [number, RarityConfig],
        color: string,
        description: string,
        isSB: boolean
    }[];
    private allBoarsSearchArr = [] as [name: string, index: number][];
    private boarUser = {} as BoarUser;
    private baseRows = [] as ActionRowBuilder<ButtonBuilder | StringSelectMenuBuilder>[];
    private optionalButtons = new ActionRowBuilder<ButtonBuilder | StringSelectMenuBuilder>();
    private curView = View.Normal;
    private curPage = 0;
    private maxPageNormal = 0;
    private enhanceStage = 0;
    private giftStage = 0;
    private miracleStage = 0;
    private cloneStage = 0;
    private timerVars = { timeUntilNextCollect: 0, updateTime: setTimeout(() => {}) };
    private curModalListener?: (submittedModal: Interaction) => Promise<void>;
    private modalShowing = {} as ModalBuilder;
    private collector = {} as InteractionCollector<ButtonInteraction | StringSelectMenuInteraction>;
    private hasStopped = false;
    public readonly data = { name: this.subcommandInfo.name, path: __filename };

    /**
     * Handles the functionality for this subcommand
     *
     * @param interaction - The interaction that called the subcommand
     */
    public async execute(interaction: ChatInputCommandInteraction): Promise<void> {
        this.guildData = await InteractionUtils.handleStart(interaction, this.config);
        if (!this.guildData) return;

        await interaction.deferReply();
        this.firstInter = interaction;

        // Gets user to interact with
        const userInput = interaction.options.getUser(this.subcommandInfo.args[0].name) ?? interaction.user;

        // Gets view to start out in
        const viewInput = interaction.options.getInteger(this.subcommandInfo.args[1].name) ?? View.Normal;

        // Gets page to start out on
        const pageInput = interaction.options.getString(this.subcommandInfo.args[2].name) ?? '1';

        LogDebug.log(
            `User: ${userInput}, View: ${viewInput}, Page: ${pageInput}`, this.config, this.firstInter
        );

        // Removes athlete badge from user if they no longer qualify for it
        await Queue.addQueue(async () => {
            try {
                this.boarUser = new BoarUser(userInput);

                const hasAthlete = this.boarUser.itemCollection.badges.athlete &&
                    this.boarUser.itemCollection.badges.athlete.possession;
                if (!hasAthlete) return;

                const leaderboardData = DataHandlers.getGlobalData(
                    DataHandlers.GlobalFile.Leaderboards
                ) as Record<string, BoardData>;

                let removeAthlete = true;

                // Gets if user is a top user
                for (const boardID of Object.keys(leaderboardData)) {
                    if (leaderboardData[boardID].topUser !== userInput.id) continue;
                    removeAthlete = false;
                }

                if (removeAthlete) {
                    await this.boarUser.removeBadge('athlete', interaction, true);
                }

                this.boarUser.updateUserData();
            } catch (err: unknown) {
                LogDebug.handleError(err, this.firstInter);
            }
        }, 'coll_athlete_remove' + interaction.id + userInput.id).catch((err: unknown) => {
            throw err;
        });

        await this.getUserInfo();

        this.maxPageNormal = Math.ceil(
            Object.keys(this.allBoars).length / this.config.numberConfig.collBoarsPerPage
        ) - 1;

        // Only allow views to be entered if there's something to show
        const shouldShowView = viewInput === View.Detailed && this.allBoars.length > 0 ||
            viewInput === View.Powerups && Object.keys(this.boarUser.itemCollection.powerups).length > 0;

        if (shouldShowView) {
            this.curView = viewInput;
        }

        // Convert page input into actual page

        let pageVal = 1;
        if (!Number.isNaN(parseInt(pageInput))) {
            pageVal = parseInt(pageInput);
        } else if (this.curView === View.Normal) {
            // Maps boar search value to its normal view page index
            const normalSearchArr = this.allBoarsSearchArr.map((val: [string, number]) => {
                return [val[0], Math.ceil(val[1] / this.config.numberConfig.collBoarsPerPage)] as [string, number];
            });

            pageVal = BoarUtils.getClosestName(pageInput.toLowerCase().replace(/\s+/g, ''), normalSearchArr);
        } else if (this.curView === View.Detailed) {
            pageVal = BoarUtils.getClosestName(
                pageInput.toLowerCase().replace(/\s+/g, ''), this.allBoarsSearchArr
            );
        }

        this.setPage(pageVal);

        // Stop prior collector that user may have open still to reduce number of listeners
        if (CollectorUtils.collectionCollectors[interaction.user.id]) {
            const oldCollector = CollectorUtils.collectionCollectors[interaction.user.id];

            setTimeout(() => {
                oldCollector.stop(CollectorUtils.Reasons.Overridden);
            }, 1000);
        }

        this.collector = await CollectorUtils.createCollector(
            interaction.channel as TextChannel, interaction.id, this.config.numberConfig
        );
        CollectorUtils.collectionCollectors[interaction.user.id] = this.collector;

        this.collector.on('collect', async (inter: ButtonInteraction) => {
            await this.handleCollect(inter);
        });

        this.collector.once('end', async (_, reason: string) => {
            await this.handleEndCollect(reason);
        });

        this.collectionImage = new CollectionImageGenerator(this.boarUser, this.allBoars, this.config);
        await this.showCollection(true);
    }

    /**
     * Handles collecting button interactions
     *
     * @param inter - The button interaction
     * @private
     */
    private async handleCollect(inter: ButtonInteraction): Promise<void> {
        try {
            const canInteract = await CollectorUtils.canInteract(this.timerVars, Date.now(), inter);
            if (!canInteract) return;

            if (!inter.isMessageComponent()) return;

            if (!inter.customId.includes(this.firstInter.id)) {
                this.collector.stop(CollectorUtils.Reasons.Error);
            }

            this.compInter = inter;

            LogDebug.log(
                `${inter.customId.split('|')[0]} on page ${this.curPage} in view ${this.curView}`,
                this.config, this.firstInter
            );

            const collRowConfig = this.config.commandConfigs.boar.collection.componentFields;
            const collComponents = {
                leftPage: collRowConfig[0][0].components[0],
                inputPage: collRowConfig[0][0].components[1],
                rightPage: collRowConfig[0][0].components[2],
                refresh: collRowConfig[0][0].components[3],
                normalView: collRowConfig[0][1].components[0],
                detailedView: collRowConfig[0][1].components[1],
                powerupView: collRowConfig[0][1].components[2],
                favorite: collRowConfig[1][0].components[0],
                gift: collRowConfig[1][0].components[1],
                editions: collRowConfig[1][0].components[2],
                enhance: collRowConfig[1][0].components[3],
                miracle: collRowConfig[1][0].components[4],
                clone: collRowConfig[1][0].components[5]
            };

            // User wants to input a page manually
            if (inter.customId.startsWith(collComponents.inputPage.customId)) {
                await this.modalHandle(inter);

                this.enhanceStage--;
                this.giftStage--;
                this.miracleStage--;
                this.cloneStage--;

                clearInterval(this.timerVars.updateTime);
                return;
            }

            await inter.deferUpdate();

            switch (inter.customId.split('|')[0]) {
                // User wants to go to previous page
                case collComponents.leftPage.customId: {
                    this.curPage--;
                    break;
                }

                // User wants to go to the next page
                case collComponents.rightPage.customId: {
                    this.curPage++;
                    break;
                }

                // User wants to view normal view
                case collComponents.normalView.customId: {
                    this.curView = View.Normal;
                    this.curPage = 0;
                    break;
                }

                // User wants to refresh data
                case collComponents.refresh.customId: {
                    await this.getUserInfo();
                    await this.collectionImage.updateInfo(this.boarUser, this.allBoars, this.config);
                    break;
                }

                // User wants to view detailed view
                case collComponents.detailedView.customId: {
                    this.curView = View.Detailed;
                    this.curPage = 0;
                    break;
                }

                // User wants to view powerup view
                case collComponents.powerupView.customId: {
                    this.curView = View.Powerups;
                    this.curPage = 0;
                    break;
                }

                // User wants to favorite a boar in detailed view
                case collComponents.favorite.customId: {
                    await Queue.addQueue(async () => {
                        try {
                            this.boarUser.refreshUserData();
                            this.boarUser.stats.general.favoriteBoar = this.allBoars[this.curPage].id;
                            this.boarUser.updateUserData();
                        } catch (err: unknown) {
                            await LogDebug.handleError(err, inter);
                        }
                    }, 'coll_fav' + inter.id + this.boarUser.user.id).catch((err: unknown) => {
                        throw err;
                    });

                    break;
                }

                // User wants to view editions of a special in detailed view
                case collComponents.editions.customId: {
                    await this.doEditions();
                    break;
                }

                // User wants to enhance a boar in detailed view
                case collComponents.enhance.customId: {
                    await this.doEnhance();
                    break;
                }

                // User wants to send a gift in powerup view
                case collComponents.gift.customId: {
                    const isBanned = await InteractionUtils.handleBanned(interaction, this.config);
                    if (isBanned) return;
    
                    await interaction.deferReply({ ephemeral: true });
                    this.interaction = interaction;
                    
                    await this.doGift();
                    break;
                }

                // User wants to activate miracles in powerup view
                case collComponents.miracle.customId: {
                    await this.doMiracles();
                    break;
                }

                // User wants to clone a boar in detailed view
                case collComponents.clone.customId: {
                    await this.doClone();
                    break;
                }
            }

            this.enhanceStage--;
            this.giftStage--;
            this.miracleStage--;
            this.cloneStage--;

            await this.showCollection();
        } catch (err: unknown) {
            const canStop = await LogDebug.handleError(err, this.firstInter);
            if (canStop) {
                this.collector.stop(CollectorUtils.Reasons.Error);
            }
        }

        clearInterval(this.timerVars.updateTime);
    }

    /**
     * Gets the editions of the current boar and send them and their dates
     *
     * @private
     */
    private async doEditions(): Promise<void> {
        const strConfig = this.config.stringConfig;

        let replyString = '';

        // Grabs each edition and the associated timestamp to put in embed
        for (let i=0; i<this.allBoars[this.curPage].editions.length; i++) {
            const edition = this.allBoars[this.curPage].editions[i];
            const editionDate = Math.floor(this.allBoars[this.curPage].editionDates[i] / 1000);

            replyString += strConfig.collEditionLine
                .replace('%@', edition.toString())
                .replace('%@', FormatStrings.toShortDateTime(editionDate));
        }

        replyString = replyString.substring(0, replyString.length-1).substring(0, 4096);

        // Sends users the edition numbers and dates they have
        await this.compInter.followUp({
            embeds: [
                new EmbedBuilder()
                    .setTitle(strConfig.collEditionTitle.replace('%@', this.allBoars[this.curPage].name))
                    .setDescription(replyString)
                    .setColor(this.config.colorConfig.green as ColorResolvable)
            ],
            ephemeral: true
        });
    }

    /**
     * Enhances a boar by removing it, giving a random boar of the next rarity, and giving some boar bucks
     *
     * @private
     */
    private async doEnhance(): Promise<void> {
        const enhancersNeeded = this.allBoars[this.curPage].rarity[1].enhancersNeeded;

        // User clicks transmute for the first time, prompting confirmation
        if (this.enhanceStage !== 1) {
            this.enhanceStage = 2;

            await this.compInter.followUp({
                files: [await this.collectionImage.finalizeEnhanceConfirm(this.curPage)],
                ephemeral: true
            });

            return;
        }

        this.enhanceStage--;

        const questData = DataHandlers.getGlobalData(DataHandlers.GlobalFile.Quest) as QuestData;

        // The boar user will get if transmutation is successful
        const enhancedBoar = BoarUtils.findValid(this.allBoars[this.curPage].rarity[0], this.guildData, this.config);

        // Disallow 3rd highest rarity and higher from being enhanced if bypassed
        if (enhancedBoar === '' || this.allBoars[this.curPage].rarity[0] >= this.config.rarityConfigs.length - 2) {
            await LogDebug.handleError(this.config.stringConfig.dailyNoBoarFound, this.firstInter);
            return;
        }

        LogDebug.log(
            `Attempting to enhance '${this.allBoars[this.curPage].id}' to '${enhancedBoar}'`,
            this.config,
            this.firstInter,
            true
        );

        let dataChanged = false;
        let noMoney = false;

        // Attempts to edit user's collection if they have the items/boars/bucks
        await Queue.addQueue(async () => {
            try {
                const spendBucksIndex = questData.curQuestIDs.indexOf('spendBucks');
                this.boarUser.refreshUserData();

                dataChanged = this.boarUser.itemCollection.powerups.enhancer.numTotal - enhancersNeeded < 0 ||
                    this.boarUser.itemCollection.boars[this.allBoars[this.curPage].id].num === 0;

                if (dataChanged) return;

                noMoney = this.boarUser.stats.general.boarScore - enhancersNeeded * 5 < 0;

                if (noMoney) return;

                this.boarUser.itemCollection.boars[this.allBoars[this.curPage].id].num--;
                this.boarUser.itemCollection.boars[this.allBoars[this.curPage].id].editions.pop();
                this.boarUser.itemCollection.boars[this.allBoars[this.curPage].id].editionDates.pop();
                this.boarUser.stats.general.boarScore -= enhancersNeeded * 5;
                this.boarUser.stats.general.totalBoars--;
                this.boarUser.itemCollection.powerups.enhancer.numTotal = 0;
                (this.boarUser.itemCollection.powerups.enhancer.raritiesUsed as number[])[
                    this.allBoars[this.curPage].rarity[0]-3
                ]++;
                this.boarUser.stats.quests.progress[spendBucksIndex] += enhancersNeeded * 5;

                this.boarUser.updateUserData();
            } catch (err: unknown) {
                await LogDebug.handleError(err, this.compInter);
            }
        }, 'coll_enhance' + this.compInter.id + this.boarUser.user.id).catch((err: unknown) => {
            throw err;
        });

        // Cancels transmutation if proper requirements not met
        if (dataChanged || noMoney) {
            LogDebug.log(
                `Failed to enhance '${this.allBoars[this.curPage].id}' to '${enhancedBoar}'`,
                this.config, this.firstInter, true
            );

            // Tells user transmutation was unable to proceed
            await Replies.handleReply(
                this.compInter,
                dataChanged
                    ? this.config.stringConfig.collDataChange
                    : this.config.stringConfig.collEnhanceNoBucks,
                this.config.colorConfig.error,
                undefined,
                undefined,
                true
            );

            await this.getUserInfo();
            await this.collectionImage.updateInfo(this.boarUser, this.allBoars, this.config);

            return;
        }

        // Adds transmuted boar to collection and gets the edition of that boar
        const bacteriaEditions = await this.boarUser.addBoars([enhancedBoar], this.firstInter, this.config);

        await this.getUserInfo();

        this.curPage = BoarUtils.getClosestName(
            this.config.itemConfigs.boars[enhancedBoar].name.toLowerCase().replace(/\s+/g, ''), this.allBoarsSearchArr
        ) - 1;

        // Sends transmuted boar image
        await this.compInter.followUp({
            files: [
                await new ItemImageGenerator(
                    this.compInter.user, enhancedBoar, this.config.stringConfig.enhanceTitle, this.config
                ).handleImageCreate()
            ]
        });

        // Sends bacteria boar image if edition 1 gotten
        for (let i=0; i<bacteriaEditions.length; i++) {
            await this.compInter.followUp({
                files: [
                    await new ItemImageGenerator(
                        this.compInter.user, 'bacteria', this.config.stringConfig.giveTitle, this.config
                    ).handleImageCreate()
                ]
            });
        }

        await this.getUserInfo();
        await this.collectionImage.updateInfo(this.boarUser, this.allBoars, this.config);
    }

    /**
     * Handles Boar Gift sending and confirmation
     *
     * @private
     */
    private async doGift(): Promise<void> {
        const strConfig = this.config.stringConfig;
        const colorConfig = this.config.colorConfig;

        // User clicks gift button for the first time, prompting confirmation
        if (this.giftStage !== 1) {
            this.boarUser.refreshUserData();

            // Tells user they have no gifts
            if (this.boarUser.itemCollection.powerups.gift.numTotal <= 0) {
                await Replies.handleReply(
                    this.compInter, strConfig.giftNone, colorConfig.error, undefined, undefined, true
                );
                return;
            }

            this.giftStage = 2;

            // Sends confirmation image
            await Replies.handleReply(
                this.compInter,
                strConfig.giftConfirm,
                colorConfig.font,
                [this.config.itemConfigs.powerups.gift.name],
                [colorConfig.powerup],
                true
            );
            return;
        }

        this.giftStage--;

        let shouldGift = true;

        // Edits a user's collection and sends out the gift message
        await Queue.addQueue(async () => {
            try {
                this.boarUser.refreshUserData();

                // Tells user they don't have any gifts
                if (this.boarUser.itemCollection.powerups.gift.numTotal <= 0) {
                    await Replies.handleReply(
                        this.compInter, strConfig.giftNone, colorConfig.error, undefined, undefined, true
                    );
                    shouldGift = false;
                    return;
                }

                const curOutVal = this.boarUser.itemCollection.powerups.gift.curOut;

                // Tells user they currently have a gift sent out
                if (curOutVal && curOutVal + 30000 >= Date.now()) {
                    await Replies.handleReply(
                        this.compInter, strConfig.giftOut, colorConfig.error, undefined, undefined, true
                    );
                    shouldGift = false;
                    return;
                }

                this.boarUser.itemCollection.powerups.gift.curOut = Date.now();
                this.boarUser.updateUserData();
            } catch (err: unknown) {
                LogDebug.handleError(err, this.compInter);
            }
        }, 'coll_gift' + this.compInter.id + this.compInter.user.id).catch((err: unknown) => {
            throw err;
        });

        if (!shouldGift) return;

        await new BoarGift(this.boarUser, this.config).sendMessage(this.compInter);
    }

    /**
     * Handles Miracle Charm activating and confirmation
     *
     * @private
     */
    private async doMiracles(): Promise<void> {
        const strConfig = this.config.stringConfig;
        const colorConfig = this.config.colorConfig;
        const powerupConfigs = this.config.itemConfigs.powerups;

        // User clicks active miracles button for the first time, prompting confirmation
        if (this.miracleStage !== 1) {
            this.miracleStage = 2;

            const miraclesActivated = this.boarUser.itemCollection.powerups.miracle.numTotal +
                (this.boarUser.itemCollection.powerups.miracle.numActive as number);
            let multiplier = this.boarUser.stats.general.multiplier + 1;

            // Gets blessing value after using miracle charms
            for (let i=0; i<miraclesActivated; i++) {
                multiplier += Math.min(Math.ceil(multiplier * 0.1), this.config.numberConfig.miracleIncreaseMax);
            }
            multiplier--;

            // Sends confirmation message
            await Replies.handleReply(
                this.compInter,
                strConfig.miracleConfirm,
                colorConfig.font,
                [
                    powerupConfigs.miracle.pluralName,
                    multiplier.toLocaleString() + '\u2738 Boar Blessings',
                    '/boar daily'
                ],
                [colorConfig.powerup, colorConfig.powerup, colorConfig.silver],
                true
            );

            return;
        }

        this.miracleStage--;

        LogDebug.log(
            `Activating ${this.boarUser.itemCollection.powerups.miracle.numTotal} miracles`,
            this.config, this.firstInter, true
        );

        // Edits a user's collection, activating miracle charms
        await Queue.addQueue(async () => {
            try {
                this.boarUser.refreshUserData();
                (this.boarUser.itemCollection.powerups.miracle.numActive as number) +=
                    this.boarUser.itemCollection.powerups.miracle.numTotal;
                this.boarUser.itemCollection.powerups.miracle.numTotal = 0;
                this.boarUser.updateUserData();
            } catch (err: unknown) {
                LogDebug.handleError(err, this.compInter);
            }
        }, 'coll_miracle' + this.compInter.id + this.compInter.user.id).catch((err: unknown) => {
            throw err;
        });

        // Tells the user they successfully used their miracle charms
        await Replies.handleReply(
            this.compInter,
            strConfig.miracleSuccess,
            colorConfig.font,
            [powerupConfigs.miracle.pluralName],
            [colorConfig.powerup],
            true
        );

        await this.getUserInfo();
        await this.collectionImage.updateInfo(this.boarUser, this.allBoars, this.config);
    }

    /**
     * Handles Cloning Serum using and confirmation
     *
     * @private
     */
    private async doClone(): Promise<void> {
        const strConfig = this.config.stringConfig;
        const colorConfig = this.config.colorConfig;

        // User clicks clone button for the first time, prompting confirmation
        if (this.cloneStage !== 1) {
            this.cloneStage = 2;

            const rarityInfo = this.allBoars[this.curPage].rarity;

            // Sends confirmation message to user
            await Replies.handleReply(
                this.compInter,
                strConfig.cloneConfirm,
                colorConfig.font,
                [
                    this.config.itemConfigs.powerups.clone.name,
                    this.allBoars[this.curPage].name,
                    (1 / rarityInfo[1].avgClones * 100).toLocaleString() + '%',
                ],
                [colorConfig.powerup, colorConfig['rarity' + rarityInfo[0]], colorConfig.silver],
                true
            );

            return;
        }

        this.cloneStage--;

        const randVal = Math.random(); // Value used to determine clone success
        const cloneSuccess = randVal < 1 / this.allBoars[this.curPage].rarity[1].avgClones;

        let dataChanged = false;

        // Edits a user's collection as long as they have the right items/boars
        await Queue.addQueue(async () => {
            try {
                this.boarUser.refreshUserData();

                dataChanged = this.boarUser.itemCollection.powerups.clone.numTotal === 0 ||
                    this.boarUser.itemCollection.boars[this.allBoars[this.curPage].id].num === 0;

                if (dataChanged) return;

                this.boarUser.itemCollection.powerups.clone.numTotal--;
                this.boarUser.itemCollection.powerups.clone.numUsed++;

                if (cloneSuccess) {
                    (this.boarUser.itemCollection.powerups.clone.numSuccess as number)++;
                    (this.boarUser.itemCollection.powerups.clone.raritiesUsed as number[])[
                        this.allBoars[this.curPage].rarity[0]-1
                    ]++;
                }

                this.boarUser.updateUserData();
            } catch (err: unknown) {
                LogDebug.handleError(err, this.compInter);
            }
        }, 'coll_clone' + this.compInter.id + this.compInter.user.id).catch((err: unknown) => {
            throw err;
        });

        // User's data changed in a way that made cloning no longer possible
        if (dataChanged) {
            LogDebug.log(
                `Failed cloning of '${this.allBoars[this.curPage].id} due to data changing'`,
                this.config, this.firstInter
            );

            // Tells user their data changed
            await Replies.handleReply(
                this.compInter, strConfig.collDataChange, this.config.colorConfig.error, undefined, undefined, true
            );

            await this.getUserInfo();
            await this.collectionImage.updateInfo(this.boarUser, this.allBoars, this.config);

            return;
        }

        // Handles if cloning attempt was successful or not
        if (cloneSuccess) {
            // Adds the boar to user's collection. Checking for edition 1 not needed since it's guaranteed that
            // the boar has been gotten before
            await this.boarUser.addBoars([this.allBoars[this.curPage].id], this.compInter, this.config);

            // Sends cloned boar image
            await this.compInter.followUp({
                files: [
                    await new ItemImageGenerator(
                        this.compInter.user, this.allBoars[this.curPage].id, strConfig.cloneTitle, this.config
                    ).handleImageCreate()
                ]
            });
        } else {
            LogDebug.log(
                `Failed cloning of '${this.allBoars[this.curPage].id}'`, this.config, this.firstInter
            );

            // Tells user their cloning attempt failed
            await Replies.handleReply(
                this.compInter,
                strConfig.cloneFail,
                colorConfig.font,
                [this.allBoars[this.curPage].name],
                [colorConfig['rarity' + this.allBoars[this.curPage].rarity[0]]],
                true
            );
        }

        await this.getUserInfo();
        await this.collectionImage.updateInfo(this.boarUser, this.allBoars, this.config);
    }

    /**
     * Sends the modal that gets page input
     *
     * @param inter - Used to show the modal and create/remove listener
     * @private
     */
    private async modalHandle(inter: MessageComponentInteraction): Promise<void> {
        const modals = this.config.commandConfigs.boar.collection.modals;

        this.modalShowing = new ModalBuilder(modals[0]);
        this.modalShowing.setCustomId(modals[0].customId + '|' + inter.id);
        await inter.showModal(this.modalShowing);

        this.curModalListener = this.modalListener;

        inter.client.on(Events.InteractionCreate, this.curModalListener);
    }

    /**
     * Handles page input that was input in modal
     *
     * @param submittedModal - The interaction to respond to
     * @private
     */
    private modalListener = async (submittedModal: Interaction): Promise<void> => {
        try  {
            if (submittedModal.user.id !== this.firstInter.user.id) return;

            const isUserComponentInter = submittedModal.isMessageComponent() &&
                submittedModal.customId.endsWith(this.firstInter.id + '|' + this.firstInter.user.id);
            const maintenanceBlock = this.config.maintenanceMode && !this.config.devs.includes(this.compInter.user.id);

            if (isUserComponentInter || maintenanceBlock) {
                this.endModalListener(submittedModal.client);
                return;
            }

            const canInteract = await CollectorUtils.canInteract(this.timerVars, Date.now());
            if (!canInteract) {
                this.endModalListener(submittedModal.client);
                return;
            }

            const invalidSubmittedModal = !submittedModal.isModalSubmit() || this.collector.ended ||
                !submittedModal.guild || submittedModal.customId !== this.modalShowing.data.custom_id;

            if (invalidSubmittedModal) {
                this.endModalListener(submittedModal.client);
                return;
            }

            await submittedModal.deferUpdate();

            const submittedPage = submittedModal.fields.getTextInputValue(
                this.modalShowing.components[0].components[0].data.custom_id as string
            ).toLowerCase().replace(/\s+/g, '');

            LogDebug.log(
                `${submittedModal.customId.split('|')[0]} input value: ` + submittedPage, this.config, this.firstInter
            );

            // Convert page input into actual page

            let pageVal = 1;
            if (!Number.isNaN(parseInt(submittedPage))) {
                pageVal = parseInt(submittedPage);
            } else if (this.curView === View.Normal) {
                // Maps boar search value to its normal view page index
                const normalSearchArr = this.allBoarsSearchArr.map((val: [string, number]) => {
                    return [val[0], Math.ceil(val[1] / this.config.numberConfig.collBoarsPerPage)] as [string, number];
                });

                pageVal = BoarUtils.getClosestName(submittedPage.toLowerCase().replace(/\s+/g, ''), normalSearchArr);
            } else if (this.curView === View.Detailed) {
                pageVal = BoarUtils.getClosestName(
                    submittedPage.toLowerCase().replace(/\s+/g, ''), this.allBoarsSearchArr
                )
            }

            this.setPage(pageVal);

            await this.showCollection();
        } catch (err: unknown) {
            const canStop = await LogDebug.handleError(err, this.firstInter);
            if (canStop) {
                this.collector.stop(CollectorUtils.Reasons.Error);
            }
        }

        this.endModalListener(submittedModal.client);
    };

    /**
     * Ends the current modal listener that's active
     *
     * @param client - Used to remove the listener
     * @private
     */
    private endModalListener(client: Client): void {
        clearInterval(this.timerVars.updateTime);
        if (this.curModalListener) {
            client.removeListener(Events.InteractionCreate, this.curModalListener);
            this.curModalListener = undefined;
        }
    }

    /**
     * Handles when the collection for navigating through collection is finished
     *
     * @param reason - Why the collection ended
     * @private
     */
    private async handleEndCollect(reason: string): Promise<void> {
        try {
            this.hasStopped = true;

            if (reason !== CollectorUtils.Reasons.Overridden) {
                delete CollectorUtils.collectionCollectors[this.firstInter.user.id];
            }

            LogDebug.log('Ended collection with reason: ' + reason, this.config, this.firstInter);

            clearInterval(this.timerVars.updateTime);
            this.endModalListener(this.firstInter.client);

            if (reason === CollectorUtils.Reasons.Error) {
                await Replies.handleReply(
                    this.firstInter, this.config.stringConfig.setupError, this.config.colorConfig.error
                );
            }

            // Clears components from interaction
            await this.firstInter.editReply({
                components: []
            });
        } catch (err: unknown) {
            await LogDebug.handleError(err, this.firstInter);
        }
    }

    /**
     * Gets information from the user's file
     *
     * @private
     */
    private async getUserInfo() {
        if (!this.firstInter.guild || !this.firstInter.channel) return;

        this.boarUser.refreshUserData();
        this.allBoars = [];
        this.allBoarsSearchArr = [];

        // Adds information about each boar in user's boar collection to an array
        for (const boarID of Object.keys(this.boarUser.itemCollection.boars)) {
            const boarData = this.boarUser.itemCollection.boars[boarID];

            if (boarData.num === 0) continue;

            const rarity = BoarUtils.findRarity(boarID, this.config) as [number, RarityConfig];

            // Boar has no rarity
            if (rarity[0] === 0) continue;

            const boarConfig = this.config.itemConfigs.boars[boarID];

            this.allBoars.push({
                id: boarID,
                name: boarConfig.name,
                file: boarConfig.file,
                staticFile: boarConfig.staticFile,
                num: boarData.num,
                editions: boarData.editions,
                editionDates: boarData.editionDates,
                firstObtained: boarData.firstObtained,
                lastObtained: boarData.lastObtained,
                rarity: rarity,
                color: this.config.colorConfig['rarity' + rarity[0]],
                description: boarConfig.description,
                isSB: boarConfig.isSB
            });

            this.allBoarsSearchArr.push([boarConfig.name.toLowerCase().replace(/\s+/g, ''), this.allBoars.length]);
        }
    }

    /**
     * Displays the collection image and modifies button states
     *
     * @private
     */
    private async showCollection(firstRun = false): Promise<void> {
        try {
            const optionalRow = new ActionRowBuilder<ButtonBuilder | StringSelectMenuBuilder>();

            this.disableButtons();

            if (firstRun) {
                this.initButtons();
            }

            // Creates base for normal view for faster navigation
            if (this.curView == View.Normal && !this.collectionImage.normalBaseMade()) {
                await this.collectionImage.createNormalBase();
            }

            // Creates base for detailed view for faster navigation
            if (this.curView == View.Detailed && !this.collectionImage.detailedBaseMade()) {
                await this.collectionImage.createDetailedBase();
            }

            // Creates bases for powerups view for faster navigation
            if (this.curView == View.Powerups) {
                await this.collectionImage.createPowerupsBase(this.curPage);
            }

            // The complete image to send (base + dynamic data)
            let finalImage: AttachmentBuilder;

            if (this.curView == View.Normal) {
                finalImage = await this.collectionImage.finalizeNormalImage(this.curPage);
            } else if (this.curView == View.Detailed) {
                finalImage = await this.collectionImage.finalizeDetailedImage(this.curPage);

                // Enable favorite button if user is accessing own collection
                optionalRow.addComponents(this.optionalButtons.components[0].setDisabled(
                    this.firstInter.user.id !== this.boarUser.user.id
                ));
            } else {
                finalImage = await this.collectionImage.finalizePowerupsImage();
            }

            // Enables next button if there's more than one page
            this.baseRows[0].components[2].setDisabled(
                this.curView === View.Normal && this.maxPageNormal <= this.curPage ||
                this.curView === View.Detailed && this.allBoars.length <= this.curPage + 1 ||
                this.curView === View.Powerups && this.config.numberConfig.maxPowPages <= this.curPage + 1
            );

            // Enables previous button if on a page other than the first
            this.baseRows[0].components[0].setDisabled(this.curPage <= 0);

            // Enables manual input button if there's more than one page
            this.baseRows[0].components[1].setDisabled(
                this.curView === View.Normal && this.maxPageNormal <= 0 ||
                this.curView === View.Detailed && this.allBoars.length <= 1
            );

            // Enables refresh button
            this.baseRows[0].components[3].setDisabled(false);

            // Allows pressing Normal view if not currently on it
            this.baseRows[1].components[0].setDisabled(this.curView === View.Normal);

            // Allows pressing Detailed view if not currently on it and if there's boars to view
            this.baseRows[1].components[1].setDisabled(this.curView === View.Detailed || this.allBoars.length === 0);

            // Allows pressing Powerup view if not currently on it
            this.baseRows[1].components[2].setDisabled(
                this.curView === View.Powerups || Object.keys(this.boarUser.itemCollection.powerups).length === 0
            );

            // Enables edition viewing on special boars
            if (this.curView === View.Detailed && this.allBoars[this.curPage].rarity[1].name === 'Special') {
                optionalRow.addComponents(this.optionalButtons.components[2].setDisabled(false));
            }

            // Enables enhance button for daily boars
            if (this.curView === View.Detailed && this.allBoars[this.curPage].rarity[1].enhancersNeeded > 0) {
                optionalRow.addComponents((this.optionalButtons.components[3] as ButtonBuilder)
                    .setDisabled(
                        this.boarUser.itemCollection.powerups.enhancer.numTotal <
                        this.allBoars[this.curPage].rarity[1].enhancersNeeded ||
                        this.firstInter.user.id !== this.boarUser.user.id
                    )
                    .setStyle(this.enhanceStage === 1 ? 4 : 3)
                );
            }

            // Enables clone button for viable boars
            if (this.curView === View.Detailed && this.allBoars[this.curPage].rarity[1].avgClones > 0) {
                optionalRow.addComponents((this.optionalButtons.components[5] as ButtonBuilder)
                    .setDisabled(
                        this.boarUser.itemCollection.powerups.clone.numTotal === 0 ||
                        this.firstInter.user.id !== this.boarUser.user.id
                    )
                    .setStyle(this.cloneStage === 1 ? 4 : 3)
                );
            }

            // Gift & Miracle Activation button enabling
            if (this.curView === View.Powerups) {
                optionalRow.addComponents((this.optionalButtons.components[1] as ButtonBuilder)
                    .setDisabled(
                        this.boarUser.itemCollection.powerups.gift.numTotal === 0 ||
                        this.firstInter.user.id !== this.boarUser.user.id
                    )
                    .setStyle(this.giftStage === 1 ? 4 : 3)
                );

                optionalRow.addComponents((this.optionalButtons.components[4] as ButtonBuilder)
                    .setDisabled(
                        this.boarUser.itemCollection.powerups.miracle.numTotal === 0 ||
                        this.firstInter.user.id !== this.boarUser.user.id
                    )
                    .setStyle(this.miracleStage === 1 ? 4 : 3)
                );
            }

            if (this.hasStopped) return;

            if (optionalRow.components.length > 0) {
                // Sends finalized image with relevant components
                await this.firstInter.editReply({ files: [finalImage], components: [...this.baseRows, optionalRow] });
            } else {
                // Sends finalized image with just the base components (navigation)
                await this.firstInter.editReply({ files: [finalImage], components: this.baseRows });
            }
        } catch (err: unknown) {
            const canStop = await LogDebug.handleError(err, this.firstInter);
            if (canStop) {
                this.collector.stop(CollectorUtils.Reasons.Error);
            }
        }
    }

    /**
     * Creates the buttons and rows used for collection by adding information to IDs
     *
     * @private
     */
    private initButtons(): void {
        const collFieldConfigs = this.config.commandConfigs.boar.collection.componentFields;

        for (let i=0; i<collFieldConfigs.length; i++) {
            const newRows = ComponentUtils.makeRows(collFieldConfigs[i]);

            ComponentUtils.addToIDs(collFieldConfigs[i], newRows, this.firstInter.id, this.firstInter.user.id);

            if (i === 0) {
                this.baseRows = newRows
            }

            if (i === 1) {
                this.optionalButtons = newRows[0];
            }
        }
    }

    /**
     * Disables all buttons
     *
     * @private
     */
    private disableButtons(): void {
        for (const row of this.baseRows) {
            for (const component of row.components) {
                component.setDisabled(true);
            }
        }

        for (const component of this.optionalButtons.components) {
            component.setDisabled(true);
        }
    }

    /**
     * Sets the page by min maxing the input value to be within bounds
     *
     * @param pageVal - The page integer to min max
     * @private
     */
    private setPage(pageVal: number): void {
        if (this.curView === View.Normal) {
            this.curPage = Math.max(Math.min(pageVal-1, this.maxPageNormal), 0);
        } else if (this.curView === View.Detailed) {
            this.curPage = Math.max(Math.min(pageVal-1, this.allBoars.length-1), 0);
        } else {
            this.curPage = Math.max(Math.min(pageVal-1, this.config.numberConfig.maxPowPages-1), 0);
        }
    }
}
