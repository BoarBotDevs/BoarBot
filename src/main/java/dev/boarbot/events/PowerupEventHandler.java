package dev.boarbot.events;

import dev.boarbot.BoarBotApp;
import dev.boarbot.api.util.Configured;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.BoarUserFactory;
import dev.boarbot.entities.boaruser.Synchronizable;
import dev.boarbot.interactives.event.PowerupEventInteractive;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.data.GuildDataUtil;
import dev.boarbot.util.generators.PowerupEventImageGenerator;
import dev.boarbot.util.logging.Log;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class PowerupEventHandler extends EventHandler implements Synchronizable, Configured {
    @Getter private static final List<Message> curMessages = new ArrayList<>();
    private static final Set<Message> priorMessages = new HashSet<>();

    @Getter private final Map<String, Long> userTimes = new HashMap<>();
    private final List<String> sortedUsers = new ArrayList<>();
    @Getter private final Map<String, Boolean> failUsers = new HashMap<>();

    @Getter private final PromptType promptType;
    @Getter private final String promptID;
    @Getter private final String powerupID;

    public PowerupEventHandler() {
        PromptType[] promptTypes = PromptType.values();
        this.promptType = promptTypes[(int) (Math.random() * promptTypes.length)];

        String[] promptIDs = CONFIG.getPromptConfig().get(this.promptType.toString()).getPrompts().keySet()
            .toArray(new String[0]);
        this.promptID = promptIDs[(int) (Math.random() * promptIDs.length)];

        List<String> validPowerupIDs = new ArrayList<>();

        for (String powerupID : POWS.keySet()) {
            if (POWS.get(powerupID).getEventAmt() != 0) {
                validPowerupIDs.add(powerupID);
            }
        }

        this.powerupID = validPowerupIDs.get((int) (Math.random() * validPowerupIDs.size()));
        this.imageGenerator = new PowerupEventImageGenerator(this.promptType, this.promptID, this.powerupID);
    }

    @Override
    public void sendEvent() {
        Log.debug(this.getClass(), "Preparing Powerup Event");

        this.setPriorMessages();
        super.sendEvent();
        this.removePriorEvent();
    }

    @Override
    protected void sendInteractive(TextChannel channel) throws InsufficientPermissionException {
        PowerupEventInteractive interactive = new PowerupEventInteractive(channel, this.currentImage, this);
        interactive.execute(null);
    }

    @Override
    protected void handleAfterSend() {
        try (Connection connection = DataUtil.getConnection()) {
            GuildDataUtil.updatePowerupMessages(connection, curMessages);
        } catch (SQLException exception) {
            Log.error(this.getClass(), "Failed to update Powerup Event messages in database", exception);
        }

        Log.debug(this.getClass(), "Updated prior Powerup Event messages in database");
    }

    private void setPriorMessages() {
        priorMessages.clear();

        if (!curMessages.isEmpty()) {
            priorMessages.addAll(curMessages);
            curMessages.clear();
            Log.debug(this.getClass(), "Retrieved prior Powerup Event messages from memory");
            return;
        }

        try (Connection connection = DataUtil.getConnection()) {
            priorMessages.addAll(GuildDataUtil.getPowerupMessages(connection));
        } catch (SQLException exception) {
            Log.error(this.getClass(), "Failed to get prior Powerup Event messages", exception);
        }

        Log.debug(this.getClass(), "Retrieved prior Powerup Event messages from database");
    }

    private void removePriorEvent() {
        for (Message msg : priorMessages) {
            try {
                msg.delete().queue();
            } catch (ErrorResponseException ignored) {}
        }

        Log.debug(this.getClass(), "Removed prior Powerup Event messages");
    }

    @Override
    protected void handleResults() {
        Log.debug(this.getClass(), "All Powerup Events ended, handling results");

        String fastestStr = null;
        String avgStr = null;

        List<Map.Entry<String, Long>> userEntries = new ArrayList<>(this.userTimes.entrySet());
        userEntries.sort(Map.Entry.comparingByValue());

        for (Map.Entry<String, Long> entry : userEntries) {
            this.sortedUsers.add(entry.getKey());
        }

        Log.debug(this.getClass(), "Sorted users based on time");

        if (!this.userTimes.isEmpty()) {
            User user = BoarBotApp.getBot().getJDA().getUserById(this.sortedUsers.getFirst());
            String fastestUsername = user != null ? user.getName() : STRS.getUnavailable();
            long fastestTime = this.userTimes.get(this.sortedUsers.getFirst());

            long totalTime = this.userTimes.values().stream().reduce(0L, Long::sum);
            int numUsers = this.userTimes.size();

            fastestStr = STRS.getPowEventFastVal().formatted(fastestTime, fastestUsername);
            avgStr = numUsers == 1
                ? STRS.getPowEventAvgVal().formatted(totalTime / numUsers, numUsers)
                : STRS.getPowEventAvgPluralVal().formatted(totalTime / numUsers, numUsers);
        }

        this.updateEventEnd(fastestStr, avgStr);

        if (!this.userTimes.isEmpty()) {
            for (String userID : this.userTimes.keySet()) {
                BoarUser boarUser = BoarUserFactory.getBoarUser(userID);
                boarUser.passSynchronizedAction(this);
            }
            Log.debug(this.getClass(), "Updated participant stats");
        }
    }

    @Override
    public void doSynchronizedAction(BoarUser boarUser) {
        try (Connection connection = DataUtil.getConnection()) {
            if (boarUser.getUserID().equals(this.sortedUsers.getFirst())) {
                boarUser.eventQuery().addPerfectPowerup(connection);
            }

            double placement = this.sortedUsers.size() > 1
                ? (double) this.sortedUsers.indexOf(boarUser.getUserID()) / (this.sortedUsers.size()-1)
                : 0;
            boarUser.eventQuery().addPrompt(connection, this.promptID, placement);
        } catch (SQLException exception) {
            Log.error(boarUser.getUser(), this.getClass(), "Failed to update powerup stats", exception);
        }
    }

    private void updateEventEnd(String fastestStr, String avgStr) {
        try {
            this.currentImage = ((PowerupEventImageGenerator) this.imageGenerator).setEndStrings(fastestStr, avgStr)
                .generate().getFileUpload();

            MessageEditBuilder editedMsg = new MessageEditBuilder().setComponents().setFiles(this.currentImage);

            for (Message msg : curMessages) {
                try {
                    msg.editMessage(editedMsg.build()).queue();
                } catch (ErrorResponseException ignored) {}
            }

            Log.debug(this.getClass(), "Edited all Powerup Event messages");
        } catch (IOException | URISyntaxException exception) {
            Log.error(this.getClass(), "Failed to generate Powerup Event end message", exception);
        }
    }
}
