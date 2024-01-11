import {
    AttachmentBuilder, ButtonInteraction,
    ChatInputCommandInteraction,
    TextChannel
} from 'discord.js';
import {BoarUser} from '../../util/boar/BoarUser';
import {BoarBotApp} from '../../BoarBotApp';
import moment from 'moment/moment';
import {Subcommand} from '../../api/commands/Subcommand';
import {Queue} from '../../util/interactions/Queue';
import {InteractionUtils} from '../../util/interactions/InteractionUtils';
import {LogDebug} from '../../util/logging/LogDebug';
import {Replies} from '../../util/interactions/Replies';
import {BoarUtils} from '../../util/boar/BoarUtils';
import {ItemImageGenerator} from '../../util/generators/ItemImageGenerator';
import {CollectorUtils} from '../../util/discord/CollectorUtils';
import {CustomEmbedGenerator} from '../../util/generators/CustomEmbedGenerator';
import {ComponentUtils} from '../../util/discord/ComponentUtils';
import {FormatStrings} from '../../util/discord/FormatStrings';
import {GuildData} from '../../bot/data/global/GuildData';

/**
 * {@link DailySubcommand DailySubcommand.ts}
 *
 * Used to give users their daily boar.
 *
 * @copyright WeslayCodes & Contributors 2023
 */
export default class DailySubcommand implements Subcommand {
    private config = BoarBotApp.getBot().getConfig();
    private subcommandInfo = this.config.commandConfigs.boar.daily;
    private guildData?: GuildData;
    private interaction = {} as ChatInputCommandInteraction;
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
        this.interaction = interaction;

        await this.doDaily();
    }

    /**
     * Checks if user can use their daily boar, and if they can,
     * get it, display it, and place it in user data
     *
     * @private
     */
    private async doDaily(): Promise<void> {
        const strConfig = this.config.stringConfig;
        const colorConfig = this.config.colorConfig;
        const powItemConfigs = this.config.itemConfigs.powerups;

        let boarUser = {} as BoarUser;
        let boarIDs = ['']; // Stores IDs of boars gotten

        let firstDaily = false; // Stores if this is the first daily a user has done
        let powTried = false; // Stores if user attempted to use a powerup
        let powCanUse = false; // Stores if user attempted to and can use a powerup

        await Queue.addQueue(async () => {
            try {
                boarUser = new BoarUser(this.interaction.user, true);

                const canUseDaily = await this.canUseDaily(boarUser);
                if (!canUseDaily) return;

                // Gets powerup usage input from user

                const powInput = this.interaction.options.getString(this.subcommandInfo.args[0].name);
                powTried = powInput !== null;
                powCanUse = powTried && boarUser.itemCollection.powerups.miracle.numTotal > 0;

                // Map of rarity index keys and weight values
                let rarityWeights = BoarUtils.getBaseRarityWeights(this.config);

                let userMultiplier = boarUser.stats.general.multiplier + 1;

                // Uses powerup if user attempted to and they can do so
                if (powCanUse) {
                    (boarUser.itemCollection.powerups.miracle.numActive as number) +=
                        boarUser.itemCollection.powerups.miracle.numTotal;
                    boarUser.itemCollection.powerups.miracle.numTotal = 0;
                }

                // Gets modified multiplier value after applying active miracle charms
                for (let i=0; i<(boarUser.itemCollection.powerups.miracle.numActive as number); i++) {
                    userMultiplier += Math.min(
                        Math.ceil(userMultiplier * 0.1), this.config.numberConfig.miracleIncreaseMax
                    );
                }

                // Adjusts weights in accordance to multiplier
                rarityWeights = BoarUtils.applyMultiplier(userMultiplier, rarityWeights, this.config);

                const hasAllTruths = boarUser.stats.general.truths &&
                    !boarUser.stats.general.truths.includes(false);

                if (hasAllTruths && boarUser.itemCollection.powerups.miracle.numActive as number > 0) {
                    const totalWeight = [...rarityWeights.values()].reduce((sum: number, val: number) => {
                        return sum + val;
                    });

                    // Increases chance of truth boars for each blessing
                    // ~.01% at 1k | ~.1% at 10k | ~1% at 100k | ~10% at 1m | ~50% at 10m
                    rarityWeights.set(9, totalWeight / (10000000 / userMultiplier));
                }

                // Probability of rolling extra boars
                const extraVals = [
                    Math.min(userMultiplier / 10, 100),
                    Math.min(userMultiplier / 100, 100),
                    Math.min(userMultiplier / 1000, 100)
                ];

                // Gets the boars gotten based on multiplier
                boarIDs = BoarUtils.getRandBoars(this.guildData, rarityWeights, this.config, extraVals);

                if (boarIDs.includes('')) {
                    await LogDebug.handleError(this.config.stringConfig.dailyNoBoarFound, this.interaction);
                    return;
                }

                // Adjusts miracle charms stats if there were some active
                if (boarUser.itemCollection.powerups.miracle.numActive as number > 0) {
                    LogDebug.log(
                        `Used ${boarUser.itemCollection.powerups.miracle.numActive} Miracle Charm(s)`,
                        this.config,
                        this.interaction,
                        true
                    );

                    const numActive = (boarUser.itemCollection.powerups.miracle.numActive as number);

                    boarUser.itemCollection.powerups.miracle.numUsed += numActive;
                    boarUser.itemCollection.powerups.miracle.numActive = 0;
                }

                boarUser.stats.general.highestMulti = Math.max(userMultiplier-1, boarUser.stats.general.highestMulti);

                boarUser.stats.general.boarStreak++;
                boarUser.stats.general.lastDaily = Date.now();
                boarUser.stats.general.numDailies++;

                if (boarUser.stats.general.firstDaily === 0) {
                    firstDaily = true;
                    boarUser.stats.general.firstDaily = Date.now();
                    boarUser.itemCollection.powerups.miracle.numTotal += 5; // Bonus for new users
                }

                boarUser.updateUserData();
            } catch (err: unknown) {
                await LogDebug.handleError(err, this.interaction);
            }
        }, 'daily_main' + this.interaction.id + this.interaction.user.id).catch((err: unknown) => {
            throw err;
        });

        if (boarIDs.includes('')) return;

        const randScores = [] as number[];
        const attachments = [] as AttachmentBuilder[];

        // Gets slightly deviated scores for each boar
        for (let i=0; i<boarIDs.length; i++) {
            randScores.push(
                Math.round(
                    this.config.rarityConfigs[BoarUtils.findRarity(boarIDs[i], this.config)[0]-1].baseScore *
                        (Math.random() * (1.1 - .9) + .9)
                )
            );
        }

        // Adds boars to collection and gathers the editions for each boar
        const bacteriaEditions = await boarUser.addBoars(boarIDs, this.interaction, this.config, randScores);

        // Gets item images for each boar
        for (let i=0; i<boarIDs.length; i++) {
            attachments.push(
                await new ItemImageGenerator(
                    boarUser.user, boarIDs[i], i === 0 ? strConfig.dailyTitle : strConfig.extraTitle, this.config
                ).handleImageCreate(false, undefined, undefined, undefined, randScores[i])
            );
        }

        // Sends all images of boars gotten
        for (let i=0; i<attachments.length; i++) {
            if (i === 0) {
                await this.interaction.editReply({ files: [attachments[i]] });
            } else {
                await this.interaction.followUp({ files: [attachments[i]] });
            }
        }

        // Tells user if they got a new user bonus and what the bonus is
        if (firstDaily) {
            await Replies.handleReply(
                this.interaction,
                strConfig.dailyFirstTime,
                colorConfig.font,
                [strConfig.dailyBonus, '/boar help'],
                [colorConfig.powerup, colorConfig.silver],
                true,
                true
            );
        }

        // Sends bacteria boar images if edition one gotten
        for (let i=0; i<bacteriaEditions.length; i++) {
            await this.interaction.followUp({
                files: [
                    await new ItemImageGenerator(
                        this.interaction.user, 'bacteria', strConfig.giveTitle, this.config
                    ).handleImageCreate()
                ]
            });
        }

        if (powCanUse) {
            // Tells user they used their powerup
            await Replies.handleReply(
                this.interaction,
                strConfig.dailyPowUsed,
                colorConfig.font,
                [powItemConfigs.miracle.pluralName],
                [colorConfig.powerup],
                true
            );
        } else if (powTried) {
            // Tells user their attempt to use their powerup failed
            await Replies.handleReply(
                this.interaction,
                strConfig.dailyPowFailed,
                colorConfig.error,
                [powItemConfigs.miracle.pluralName, 'Powerups', '/boar collection'],
                [colorConfig.powerup, colorConfig.powerup, colorConfig.silver],
                true
            );
        }
    }

    /**
     * Returns whether the user can use their daily boar and
     * takes in user notification choice
     *
     * @param boarUser - User's boar information
     * @return Whether user can use their daily boar
     * @private
     */
    private async canUseDaily(boarUser: BoarUser): Promise<boolean> {
        // Midnight of next day (UTC)
        const nextBoarTime = new Date().setUTCHours(24,0,0,0);

        const strConfig = this.config.stringConfig;
        const nums = this.config.numberConfig;
        const colorConfig = this.config.colorConfig;

        if (boarUser.stats.general.lastDaily < nextBoarTime - nums.oneDay || this.config.unlimitedBoars) {
            return true;
        }

        // Tells user they already used /boar daily without asking to enable notifications
        if (boarUser.stats.general.notificationsOn) {
            const msg = await this.interaction.editReply({
                files: [
                    await CustomEmbedGenerator.makeEmbed(
                        this.config.stringConfig.dailyUsed,
                        this.config.colorConfig.font,
                        this.config,
                        [moment(nextBoarTime).fromNow().substring(3)],
                        [this.config.colorConfig.silver]
                    )
                ]
            });

            setTimeout(async () => {
                try {
                    await msg.delete();
                } catch (err: unknown) {
                    LogDebug.handleError(err);
                }
            }, this.config.numberConfig.notificationButtonDelay);

            return false;
        }

        const dailyRows = this.config.commandConfigs.boar.daily.componentFields[0];
        const dailyComponentRows = ComponentUtils.makeRows(dailyRows);

        ComponentUtils.addToIDs(dailyRows, dailyComponentRows, this.interaction.id, this.interaction.user.id);

        const collector = await CollectorUtils.createCollector(
            this.interaction.channel as TextChannel, this.interaction.id, nums, false, nums.notificationButtonDelay
        );

        // Tells user they already used /boar daily and asks if they want to enable notifications
        const msg = await this.interaction.editReply({
            files: [
                await CustomEmbedGenerator.makeEmbed(
                    strConfig.dailyUsedNotify,
                    colorConfig.font,
                    this.config,
                    [moment(nextBoarTime).fromNow().substring(3)],
                    [colorConfig.silver]
                )
            ],
            components: dailyComponentRows
        });

        // Handles user clicking button to enable notifications
        collector.on('collect', async (inter: ButtonInteraction) => {
            await Queue.addQueue(async () => {
                try {
                    // Tells user they've successfully enabled notifications in DMs
                    await this.interaction.user.send(
                        strConfig.notificationSuccess + '\n# ' +
                        FormatStrings.toBasicChannel(this.interaction.channel?.id) +
                        strConfig.notificationStopStr
                    );

                    // Tells user they've successfully enabled notifications in channel they used /boar daily in
                    await Replies.handleReply(inter, strConfig.notificationSuccessReply, colorConfig.green);

                    // Edits the message, removing the part asking about notifications
                    await msg.edit({
                        files: [
                            await CustomEmbedGenerator.makeEmbed(
                                strConfig.dailyUsed,
                                colorConfig.font,
                                this.config,
                                [moment(nextBoarTime).fromNow().substring(3)],
                                [colorConfig.silver]
                            )
                        ],
                        components: []
                    });

                    boarUser.refreshUserData();
                    boarUser.stats.general.notificationsOn = true;
                    boarUser.stats.general.notificationChannel = this.interaction.channel
                        ? this.interaction.channel.id
                        : '0';
                    boarUser.updateUserData();

                    LogDebug.log(
                        `${inter.user.username} (${inter.user.id}) turned ON notifications`,
                        this.config,
                        undefined,
                        true
                    );
                } catch {
                    try {
                        // Tells user the bot is unable to DM them
                        await Replies.handleReply(inter, strConfig.notificationFailed, colorConfig.error);
                    } catch (err: unknown) {
                        await LogDebug.handleError(err, this.interaction);
                    }
                }
            }, 'daily_notify' + this.interaction + this.interaction.id).catch((err: unknown) => {
                LogDebug.handleError(err, this.interaction);
            });
        });

        // Deletes /boar daily fail message after 30 seconds
        collector.once('end', async () => {
            try {
                await msg.delete();
            } catch (err: unknown) {
                await LogDebug.handleError(err, this.interaction);
            }
        });

        return false;
    }
}
