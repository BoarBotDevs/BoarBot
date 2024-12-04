package dev.boarbot.util.graphics;

import java.awt.*;

public record ImageCacheEntry(Image image, long lastAccessTimestamp) {}