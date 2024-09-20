package dev.boarbot.util.resource;

import dev.boarbot.util.logging.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ResourceUtil {
    public static final Path resourcepackDir = Paths.get("resourcepack/");

    private static final String assetsPath = "assets/";
    private static final String imagesPath = assetsPath + "images/";

    public static final String fontPath = assetsPath + "font/font.ttf";

    public static final String itemAssetsPath = imagesPath + "items/";
    public static final String boarAssetsPath = itemAssetsPath + "boars/";
    public static final String badgeAssetsPath = itemAssetsPath + "badges/";
    public static final String powerupAssetsPath = itemAssetsPath + "powerups/";

    public static final String itemUnderlayPath = itemAssetsPath + "ItemUnderlay.png";
    public static final String itemBackplatePath = itemAssetsPath + "ItemBackplate.png";
    public static final String itemShadowPathL = itemAssetsPath + "ShadowLeft.png";
    public static final String itemShadowPathR = itemAssetsPath + "ShadowRight.png";

    public static final String megaMenuAssetsPath = imagesPath + "megamenu/";
    public static final String megaMenuBasePath = megaMenuAssetsPath + "MegaMenuBase.png";
    public static final String collUnderlayPath = megaMenuAssetsPath + "CollectionUnderlay.png";
    public static final String profUnderlayPath = megaMenuAssetsPath + "ProfileUnderlay.png";
    public static final String compUnderlayPath = megaMenuAssetsPath + "CompendiumUnderlay.png";
    public static final String powAnomUnderlayPath = megaMenuAssetsPath + "PowerupsAnomalous.png";
    public static final String questUnderlayPath = megaMenuAssetsPath + "QuestsUnderlay.png";
    public static final String badgeUnderlayPath = megaMenuAssetsPath + "BadgesUnderlay.png";

    public static final String powCellNonePath = megaMenuAssetsPath + "CellNoCharge.png";
    public static final String powCellCommonPath = megaMenuAssetsPath + "CellCommon.png";
    public static final String powCellUncommonPath = megaMenuAssetsPath + "CellUncommon.png";
    public static final String powCellRarePath = megaMenuAssetsPath + "CellRare.png";
    public static final String powCellEpicPath = megaMenuAssetsPath + "CellEpic.png";
    public static final String powCellLegendaryPath = megaMenuAssetsPath + "CellLegendary.png";
    public static final String powCellMythicPath = megaMenuAssetsPath + "CellMythic.png";
    public static final String powCellDivinePath = megaMenuAssetsPath + "CellDivine.png";
    public static final String rarityBorderPath = megaMenuAssetsPath + "RarityBorder.png";
    public static final String favoritePath = megaMenuAssetsPath + "Favorite.png";

    public static final String otherAssetsPath = imagesPath + "other/";
    public static final String bucksGiftPath = otherAssetsPath + "BucksGift.png";
    public static final String giftImagePath = otherAssetsPath + "GiftImage.png";
    public static final String eventUnderlayPath = otherAssetsPath + "EventUnderlay.png";
    public static final String powIconPath = otherAssetsPath + "PowerupIcon.png";
    public static final String checkmarkPath = otherAssetsPath + "Checkmark.png";

    private static final String scriptsPath = "scripts/";
    public static final String animItemScript = scriptsPath + "make_animated_image.py";
    public static final String userItemScript = scriptsPath + "user_animated_overlay.py";
    public static final String itemGroupScript = scriptsPath + "animated_item_grouper.py";
    public static final String animOverlayScript = scriptsPath + "apply_animated.py";

    public static URL getResource(String pathStr) {
        Path path = resourcepackDir.resolve(pathStr);

        if (Files.exists(path)) {
            try {
                return path.toUri().toURL();
            } catch (MalformedURLException exception) {
                Log.error(ResourceUtil.class, "Failed to get URL from resource: " + pathStr, exception);
            }
        }

        return ResourceUtil.class.getClassLoader().getResource(pathStr);
    }

    public static InputStream getResourceStream(String pathStr) {
        Path path = resourcepackDir.resolve(pathStr);

        if (Files.exists(path)) {
            try {
                return new FileInputStream(path.toFile());
            } catch (FileNotFoundException exception) {
                Log.error(ResourceUtil.class, "Failed to open resource: " + pathStr, exception);
            }
        }

        return ResourceUtil.class.getClassLoader().getResourceAsStream(pathStr);
    }
}
