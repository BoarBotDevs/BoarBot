import {BotConfig} from '../config/BotConfig';
import fs from 'fs';
import {FontLibrary} from 'skia-canvas';
import moment from 'moment/moment';
import {LogDebug} from '../../util/logging/LogDebug';

/**
 * {@link ConfigHandler ConfigHandler.ts}
 *
 * Handles loading, getting, and validating config
 * information for a bot instance.
 *
 * @copyright WeslayCodes & Contributors 2023
 */
export class ConfigHandler {
    private config = new BotConfig();

    /**
     * Loads config data from configuration file in project root
     *
     * @param firstLoad - Used to force maintenance mode for the first 10 seconds on initial load
     */
    public async loadConfig(firstLoad = false): Promise<void> {
        let parsedConfig: BotConfig;

        this.setRelativeTime();

        try {
            parsedConfig = JSON.parse(fs.readFileSync('config.json', 'utf-8'));

            if (firstLoad) {
                const maintenanceActive = parsedConfig.maintenanceMode;
                parsedConfig.maintenanceMode = true;

                setTimeout(() => {
                    parsedConfig.maintenanceMode = maintenanceActive;
                    this.config = parsedConfig as BotConfig;
                }, 10000);
            }
        } catch {
            await LogDebug.handleError(
                'Unable to parse config file. Is \'config.json\' in the project root? ' +
                'Try renaming example_config.json to config.json'
            );
            process.exit(-1);
        }

        if (!await this.validateConfig(parsedConfig)) {
            LogDebug.log('Failed to validate config file, sticking with old/default version.', this.config);
            return;
        }

        this.config = parsedConfig as BotConfig;

        LogDebug.log('Config file successfully loaded!', this.config);

        this.removeTempFiles();
        this.loadFonts();
    }

    /**
     * Validates the contents of the data in the configuration file.
     * Passes if in maintenance mode even if not valid
     *
     * @param parsedConfig - The newly parsed config file to validate
     * @return Whether the config file passed validation
     * @private
     */
    private async validateConfig(parsedConfig: BotConfig): Promise<boolean> {
        const rarities = parsedConfig.rarityConfigs;
        const boars = parsedConfig.itemConfigs.boars;
        const boarIDs = Object.keys(boars);
        const badges = parsedConfig.itemConfigs.badges;
        const badgeIDs = Object.keys(badges);
        const powerups = parsedConfig.itemConfigs.powerups;
        const powerupIDs = Object.keys(powerups);
        const foundBoars = [] as string[];

        const pathConfig = parsedConfig.pathConfig;
        const boarImages = pathConfig.boars;
        const badgeImages = pathConfig.badges;
        const powerupImages = pathConfig.powerups;
        const itemAssets = pathConfig.itemAssets;
        const collAssets = pathConfig.collAssets;
        const otherAssets = pathConfig.otherAssets;

        const globalFolders = [
            pathConfig.globalDataFolder,
            pathConfig.guildDataFolder,
            pathConfig.userDataFolder
        ];

        const globalFileNames = [
            pathConfig.itemDataFileName,
            pathConfig.leaderboardsFileName,
            pathConfig.bannedUsersFileName,
            pathConfig.powerupDataFileName
        ] as string[];

        const allPaths = [
            itemAssets + pathConfig.itemOverlay,
            itemAssets + pathConfig.itemUnderlay,
            itemAssets + pathConfig.itemBackplate,
            collAssets + pathConfig.collUnderlay,
            collAssets + pathConfig.cellNone,
            collAssets + pathConfig.cellCommon,
            collAssets + pathConfig.cellUncommon,
            collAssets + pathConfig.cellRare,
            collAssets + pathConfig.cellEpic,
            collAssets + pathConfig.cellLegendary,
            collAssets + pathConfig.cellMythic,
            collAssets + pathConfig.cellDivine,
            collAssets + pathConfig.collDetailUnderlay,
            collAssets + pathConfig.collDetailOverlay,
            collAssets + pathConfig.collPowerUnderlay,
            collAssets + pathConfig.collPowerUnderlay2,
            collAssets + pathConfig.collPowerUnderlay3,
            collAssets + pathConfig.collPowerOverlay,
            collAssets + pathConfig.collGiftUnderlay,
            collAssets + pathConfig.collEnhanceUnderlay,
            pathConfig.fontAssets + pathConfig.mainFont,
            otherAssets + pathConfig.helpGeneral1,
            otherAssets + pathConfig.helpPowerup1,
            otherAssets + pathConfig.helpPowerup2,
            otherAssets + pathConfig.helpMarket1,
            otherAssets + pathConfig.helpMarket2,
            otherAssets + pathConfig.helpBadgeBoar1,
            otherAssets + pathConfig.helpBadgeBoar2,
            otherAssets + pathConfig.helpBadgeBoar3,
            otherAssets + pathConfig.circleMask,
            pathConfig.dynamicImageScript,
            pathConfig.userOverlayScript
        ] as string[];

        let passed = true;

        for (const rarity in rarities) {
            const rarityInfo = rarities[rarity];
            for (const boar of rarityInfo.boars) {
                if (boarIDs.includes(boar) && !foundBoars.includes(boar)) {
                    foundBoars.push(boar);
                    continue;
                }

                if (!boarIDs.includes(boar)) {
                    LogDebug.log(`Boar ID '${boar}' not found in rarity '${rarity}'`, this.config);
                }

                if (foundBoars.includes(boar)) {
                    LogDebug.log(`Boar ID '${boar}' used more than once`, this.config);
                }

                passed = false;
                foundBoars.push(boar);
            }
        }

        for (const boar of boarIDs) {
            allPaths.push(boarImages + boars[boar].file);

            if (foundBoars.includes(boar)) continue;

            LogDebug.log(`Boar ID '${boar}' is unused`, this.config);
            passed = false;
        }

        for (const badge of badgeIDs) {
            allPaths.push(badgeImages + badges[badge].file);
        }

        for (const powerup of powerupIDs) {
            allPaths.push(powerupImages + powerups[powerup].file);
        }

        for (const path of allPaths) {
            if (fs.existsSync(path)) continue;

            LogDebug.log(`Path '${path}' is invalid`, this.config);
            passed = false;
        }

        for (const folder of globalFolders) {
            if (fs.existsSync(pathConfig.databaseFolder + folder)) continue;

            LogDebug.log(`Path '${pathConfig.databaseFolder + folder}' is invalid`, this.config);
            passed = false;
        }

        for (const file of globalFileNames) {
            if (file) continue;

            LogDebug.log(`Global file name '${file}' is invalid`, this.config);
            passed = false;
        }

        return passed || parsedConfig.maintenanceMode;
    }

    /**
     * Removes temp files. This allows new config changes to show
     *
     * @private
     */
    private removeTempFiles(): void {
        const tempItemFolder = this.config.pathConfig.tempItemAssets;

        if (!fs.existsSync(tempItemFolder)) {
            fs.mkdirSync(tempItemFolder);
        }

        const tempItemFiles = fs.readdirSync(tempItemFolder);

        for (const file of tempItemFiles) {
            fs.rmSync(tempItemFolder + file);
        }

        LogDebug.log('Deleted all temp files!', this.config);
    }

    /**
     * Grabs {@link BotConfig config} data the bot uses
     */
    public getConfig(): BotConfig {
        return this.config;
    }

    /**
     * Grabs the font file and loads it for Canvas if it exists
     */
    public loadFonts(): void {
        try {
            const mcFont = this.config.pathConfig.fontAssets + this.config.pathConfig.mainFont;
            FontLibrary.use(this.config.stringConfig.fontName, mcFont);
        } catch {
            LogDebug.handleError('Unable to load custom font. Verify its path in \'config.json\'.');
            return;
        }

        LogDebug.log('Fonts successfully loaded!', this.config);
    }

    /**
     * Sets relative time information like cutoffs and locales
     */
    public setRelativeTime(): void {
        moment.relativeTimeThreshold('s', 60);
        moment.relativeTimeThreshold('ss', 1);
        moment.relativeTimeThreshold('m', 60);
        moment.relativeTimeThreshold('h', 24);
        moment.relativeTimeThreshold('d', 30.437);
        moment.relativeTimeThreshold('M', 12);

        moment.updateLocale('en', {
            relativeTime : {
                future: 'in %s',
                past:   '%s ago',
                s  : '%d second',
                ss : '%d seconds',
                m:  '%d minute',
                mm: '%d minutes',
                h:  '%d hour',
                hh: '%d hours',
                d:  '%d day',
                dd: '%d days',
                M:  '%d month',
                MM: '%d months',
                y:  '%d year',
                yy: '%d years'
            }
        });

        LogDebug.log('Relative time information set!', this.config);
    }
}
