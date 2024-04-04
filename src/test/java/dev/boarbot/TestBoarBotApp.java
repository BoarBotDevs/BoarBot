package dev.boarbot;

import dev.boarbot.api.bot.Bot;
import dev.boarbot.util.test.TestUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestBoarBotApp {
    @Test
    public void testMain() {
        Bot boarBot = TestUtil.getBot();
        assertNotNull(boarBot, "Bot not created");
    }
}
