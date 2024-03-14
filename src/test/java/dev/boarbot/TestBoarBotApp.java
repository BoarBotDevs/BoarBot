package dev.boarbot;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class TestBoarBotApp {
    @Test
    public void testMainNoArgs() {
        BoarBotApp.main();
        assertNotNull(BoarBotApp.getBot());
    }

    @Test
    public void testMainDeployProd() {
        BoarBotApp.main("deploy-production");
        assertNotNull(BoarBotApp.getBot());
    }

    @Test
    public void testMainDeployCommands() {
        BoarBotApp.main("deploy-commands");
        assertNotNull(BoarBotApp.getBot());
    }

    @Test
    public void testMainInvalidArg() {
        BoarBotApp.main("foobar");
        assertNotNull(BoarBotApp.getBot());
    }
}
