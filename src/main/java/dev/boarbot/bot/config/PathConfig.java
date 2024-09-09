package dev.boarbot.bot.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * {@link PathConfig PathConfig.java}
 *
 * Stores path configurations for a bot instance.
 *
 * @copyright WeslayCodes & Contributors 2023
 */
@Getter
@Setter
@ToString
public class PathConfig {
    // Folder storing logs

    private String logsFolder = "";

    // Base paths for images/assets

    private String boars = "";
    private String badges = "";
    private String powerups = "";
    private String itemAssets = "";
    private String megaMenuAssets = "";
    private String otherAssets = "";
    private String fontAssets = "";

    // Image/asset file names for item attachments (boars and badges)

    private String itemUnderlay = "";
    private String itemBackplate = "";
    private String itemShadowLeft = "";
    private String itemShadowRight = "";

    // Image/asset file names for mega menu images

    private String megaMenuBase = "";
    private String collUnderlay = "";
    private String profUnderlay = "";
    private String compUnderlay = "";
    private String powAnomUnderlay = "";
    private String powCellNone = "";
    private String powCellCommon = "";
    private String powCellUncommon = "";
    private String powCellRare = "";
    private String powCellEpic = "";
    private String powCellLegendary = "";
    private String powCellMythic = "";
    private String powCellDivine = "";
    private String powCellEntropic = "";
    private String rarityBorder = "";
    private String favorite = "";

    // Miscellaneous image/asset file names

    private String mainFont = "";
    private String circleMask = "";
    private String giftBucks = "";
    private String giftImage = "";

    // Python scripts

    private String makeImageScript = "";
    private String overlayScript = "";
    private String groupScript = "";
    private String applyScript = "";
}
