package dev.boarbot.events;

import dev.boarbot.BoarBotApp;
import dev.boarbot.api.util.Configured;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.BoarUserFactory;
import dev.boarbot.interactives.event.PowerupEventInteractive;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.data.GuildDataUtil;
import dev.boarbot.util.generators.PowerupEventImageGenerator;
import dev.boarbot.util.interaction.SpecialReply;
import dev.boarbot.util.logging.ExceptionHandler;
import dev.boarbot.util.logging.Log;
import dev.boarbot.util.time.TimeUtil;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

public class PowerupEventHandler extends EventHandler implements Configured {
    @Getter protected static final List<Message> curMessages = new ArrayList<>();
    private static final Set<Message> priorMessages = new HashSet<>();

    @Getter private final Map<String, Long> userTimes = new ConcurrentHashMap<>();
    private final List<String> sortedUsers = new ArrayList<>();
    @Getter private final Map<String, Boolean> failUsers = new ConcurrentHashMap<>();

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

        this.powerupID = TimeUtil.isChristmas() && Math.random() < 0.8
            ? "gift"
            : validPowerupIDs.get((int) (Math.random() * validPowerupIDs.size()));

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
    protected void sendInteractive(TextChannel channel) {
        PowerupEventInteractive interactive = new PowerupEventInteractive(channel, this.currentImage, this);
        interactive.execute(null);
    }

    @Override
    protected void handleAfterSend() {
        try (Connection connection = DataUtil.getConnection()) {
            GuildDataUtil.updatePowerupMessages(connection, curMessages);
            GuildDataUtil.setEventNotify(this.failedGuilds, connection);
        } catch (SQLException exception) {
            Log.error(this.getClass(), "Failed to update Powerup Event messages in database", exception);
        } catch (RuntimeException exception) {
            Log.error(this.getClass(), "Something went wrong when updating Powerup Event messages", exception);
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
        Semaphore semaphore = new Semaphore(1);

        for (Message msg : priorMessages) {
            semaphore.acquireUninterruptibly();
            msg.delete().queue(
                m -> semaphore.release(),
                e -> {
                    semaphore.release();
                    ExceptionHandler.handle(this.getClass(), e);
                }
            );
        }

        Log.debug(this.getClass(), "Removed prior Powerup Event messages");
    }

    @Override
    protected void handleResults() {
        Log.debug(this.getClass(), "All Powerup Events ended, handling results");

        try {
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
                    try {
                        BoarUser boarUser = BoarUserFactory.getBoarUser(userID);
                        boarUser.passSynchronizedAction(() -> this.updatePowerupStats(boarUser));
                    } catch (SQLException exception) {
                        Log.error(this.getClass(), "Failed to update user data", exception);
                    }
                }
                Log.debug(this.getClass(), "Updated participant stats");
            }
        } catch (RuntimeException exception) {
            Log.error(this.getClass(), "Failed to handle Powerup Event results", exception);
        }
    }

    public void updatePowerupStats(BoarUser boarUser) {
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
        MessageCreateData msgData = SpecialReply.getErrorMsgData();

        try {
            this.currentImage = ((PowerupEventImageGenerator) this.imageGenerator).setEndStrings(fastestStr, avgStr)
                .generate().getFileUpload();
            msgData = new MessageCreateBuilder().setComponents().setFiles(this.currentImage).build();
        } catch (IOException | URISyntaxException exception) {
            Log.error(this.getClass(), "Failed to generate Powerup Event end message", exception);
        }

        Semaphore semaphore = new Semaphore(1);

        for (Message msg : curMessages) {
            semaphore.acquireUninterruptibly();
            msg.editMessage(MessageEditData.fromCreateData(msgData)).queue(
                m -> semaphore.release(),
                e -> {
                    semaphore.release();
                    ExceptionHandler.handle(this.getClass(), e);
                }
            );
        }

        Log.debug(this.getClass(), "Edited all Powerup Event messages");
    }
}
