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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

@Slf4j
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
            log.error("Failed to update Powerup Event messages in database.", exception);
        }
    }

    private void setPriorMessages() {
        priorMessages.clear();

        if (!curMessages.isEmpty()) {
            priorMessages.addAll(curMessages);
            curMessages.clear();
            return;
        }

        try (Connection connection = DataUtil.getConnection()) {
            priorMessages.addAll(GuildDataUtil.getPowerupMessages(connection));
        } catch (SQLException exception) {
            log.error("Failed to get prior Powerup Event messages to remove.", exception);
        }
    }

    private void removePriorEvent() {
        for (Message msg : priorMessages) {
            try {
                msg.delete().queue();
            } catch (ErrorResponseException ignored) {}
        }
    }

    @Override
    protected void handleResults() {
        String fastestStr = null;
        String avgStr = null;

        List<Map.Entry<String, Long>> userEntries = new ArrayList<>(this.userTimes.entrySet());
        userEntries.sort(Map.Entry.comparingByValue());

        for (Map.Entry<String, Long> entry : userEntries) {
            this.sortedUsers.add(entry.getKey());
        }

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
                boarUser.decRefs();
            }
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
            log.error("Failed update user after Powerup Event", exception);
        }
    }

    private void updateEventEnd(String fastestStr, String avgStr) {
        try {
            this.currentImage = ((PowerupEventImageGenerator) this.imageGenerator).setEndStrings(fastestStr, avgStr)
                .generate().getFileUpload();

            MessageEditBuilder editedMsg = new MessageEditBuilder()
                .setComponents().setFiles(this.currentImage);

            for (Message msg : curMessages) {
                msg.editMessage(editedMsg.build()).queue();
            }
        } catch (Exception exception) {
            log.error("Failed to get end image Powerup Event.", exception);
        }
    }
}
