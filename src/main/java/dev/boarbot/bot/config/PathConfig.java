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

    private String collUnderlay = "";
    private String profUnderlay = "";
    private String compUnderlay = "";
    private String rarityBorder = "";

    // Miscellaneous image/asset file names

    private String mainFont = "";
    private String circleMask = "";

    // Python scripts

    private String dynamicImageScript = "";
    private String userOverlayScript = "";
}
