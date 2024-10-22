package dev.boarbot.interactives.event;

import dev.boarbot.bot.config.prompts.IndivPromptConfig;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.BoarUserFactory;
import dev.boarbot.entities.boaruser.Synchronizable;
import dev.boarbot.events.PowerupEventHandler;
import dev.boarbot.events.PromptType;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.util.interaction.SpecialReply;
import dev.boarbot.util.logging.ExceptionHandler;
import dev.boarbot.util.logging.Log;
import dev.boarbot.util.quests.QuestUtil;
import dev.boarbot.util.quests.QuestType;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.generators.EmbedImageGenerator;
import dev.boarbot.util.interactive.InteractiveUtil;
import dev.boarbot.util.interactive.StopType;
import dev.boarbot.util.time.TimeUtil;
import lombok.Setter;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import net.dv8tion.jda.internal.interactions.component.ButtonImpl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PowerupEventInteractive extends EventInteractive implements Synchronizable {
    private final IndivPromptConfig promptConfig;

    private final PowerupEventHandler eventHandler;
    private final String powerupID;
    private final Map<String, Long> userTimes;
    private final Map<String, Boolean> failUsers;
    private final Map<String, InteractionHook> userHooks = new ConcurrentHashMap<>();
    private long sentTimestamp;

    private final Set<String> failedSynchronized = new HashSet<>();

    @Setter private FileUpload eventImage;

    private final String CORRECT_ID = this.interactiveID + ",y";
    private final String INCORRECT_ID = this.interactiveID + ",n";
    private final static int BASE_SIDE_LENGTH = 4;

    private final static Button tabulatingBtn = new ButtonImpl("x", "Tabulating...", ButtonStyle.SECONDARY, true, null);

    public PowerupEventInteractive(TextChannel channel, FileUpload eventImage, PowerupEventHandler eventHandler) {
        super(channel, NUMS.getPowDurationMillis(), NUMS.getPowDurationMillis());
        this.eventImage = eventImage;
        this.eventHandler = eventHandler;
        this.powerupID = eventHandler.getPowerupID();
        this.userTimes = eventHandler.getUserTimes();
        this.failUsers = eventHandler.getFailUsers();
        this.promptConfig = CONFIG.getPromptConfig().get(this.eventHandler.getPromptType().toString())
            .getPrompts().get(this.eventHandler.getPromptID());
    }

    @Override
    public void execute(GenericComponentInteractionCreateEvent compEvent) {
        if (compEvent == null) {
            this.sendResponse();
            this.sentTimestamp = TimeUtil.getCurMilli();
            return;
        }

        compEvent.deferEdit().queue(null, e -> ExceptionHandler.deferHandle(compEvent, this, e));

        String userID = compEvent.getUser().getId();
        EmbedImageGenerator embedGen = new EmbedImageGenerator(STRS.getPowEventAttempted(), COLORS.get("error"));

        this.userHooks.put(userID, compEvent.getHook());

        try {
            boolean hasAttempted = this.userTimes.containsKey(userID) ||
                this.failUsers.containsKey(userID) && this.failUsers.get(userID);

            if (hasAttempted) {
                compEvent.getHook().sendFiles(embedGen.generate().getFileUpload()).setEphemeral(true)
                    .queue(null, e -> ExceptionHandler.replyHandle(compEvent.getHook(), this, e));
                return;
            }

            if (compEvent.getComponentId().endsWith("y")) {
                this.userTimes.put(userID, TimeUtil.getCurMilli() - this.sentTimestamp);

                BoarUser boarUser = BoarUserFactory.getBoarUser(compEvent.getUser());
                boarUser.passSynchronizedAction(this);

                if (this.failedSynchronized.contains(boarUser.getUserID())) {
                    return;
                }

                int eventAmt = POWS.get(this.powerupID).getEventAmt();
                String powStr = eventAmt == 1
                    ? POWS.get(this.powerupID).getName()
                    : POWS.get(this.powerupID).getPluralName();
                embedGen.setStr(STRS.getPowEventSuccess().formatted(this.userTimes.get(userID), eventAmt, powStr))
                    .setColor(COLORS.get("font"));

                compEvent.getHook().sendFiles(embedGen.generate().getFileUpload()).setEphemeral(true)
                    .queue(null, e -> ExceptionHandler.replyHandle(compEvent.getHook(), this, e));
                Log.debug(compEvent.getUser(), this.getClass(), "Gave powerup win rewards");
                return;
            }

            if (this.failUsers.containsKey(userID)) {
                this.failUsers.put(userID, true);

                BoarUser boarUser = BoarUserFactory.getBoarUser(compEvent.getUser());
                boarUser.passSynchronizedAction(this);

                embedGen.setStr(STRS.getPowEventFail()).setColor(COLORS.get("font"));
                compEvent.getHook().sendFiles(embedGen.generate().getFileUpload()).setEphemeral(true)
                    .queue(null, e -> ExceptionHandler.replyHandle(compEvent.getHook(), this, e));
                Log.debug(compEvent.getUser(), this.getClass(), "Updated powerup fail status");
                return;
            }

            this.failUsers.put(userID, false);
            embedGen.setStr(STRS.getPowEventIncorrect()).setColor(COLORS.get("font"));
            compEvent.getHook().sendFiles(embedGen.generate().getFileUpload()).setEphemeral(true)
                .queue(null, e -> ExceptionHandler.replyHandle(compEvent.getHook(), this, e));
            Log.debug(compEvent.getUser(), this.getClass(), "Guessed incorrectly");
        } catch (SQLException exception) {
            SpecialReply.sendErrorMessage(compEvent.getHook(), this);
            Log.error(compEvent.getUser(), this.getClass(), "Failed to update data", exception);
        } catch (IOException exception) {
            SpecialReply.sendErrorMessage(compEvent.getHook(), this);
            Log.error(compEvent.getUser(), this.getClass(), "Failed to generate response", exception);
        }
    }

    private void sendResponse() {
        MessageEditBuilder messageBuilder = new MessageEditBuilder()
            .setFiles(this.eventImage)
            .setComponents(this.getCurComponents());
        this.updateInteractive(false, messageBuilder.build());
    }

    @Override
    public void updateInteractive(boolean stopping, MessageEditData editedMsg) {
        if (this.isStopped && !stopping) {
            return;
        }

        semaphore.acquireUninterruptibly();

        if (this.msg == null) {
            try {
                this.channel.sendMessage(MessageCreateData.fromEditData(editedMsg)).queue(
                    msg -> {
                        semaphore.release();
                        this.msg = msg;
                        PowerupEventHandler.getCurMessages().add(this.msg);
                        this.eventHandler.decNumPotential();
                        this.eventHandler.incNumActive();
                    },
                    e -> {
                        semaphore.release();
                        this.stop(StopType.EXCEPTION);

                        if (e instanceof InsufficientPermissionException) {
                            this.eventHandler.getFailedGuilds().add(this.channel.getGuild().getId());
                        }

                        this.eventHandler.decNumPotential();
                        ExceptionHandler.handle(this.getClass(), e);
                    }
                );
            } catch (InsufficientPermissionException exception) {
                semaphore.release();
                this.stop(StopType.EXCEPTION);
                this.eventHandler.decNumPotential();
                this.eventHandler.getFailedGuilds().add(this.channel.getGuild().getId());
                ExceptionHandler.handle(this.getClass(), exception);
            }

            return;
        }

        this.msg.editMessage(editedMsg).queue(
            m -> semaphore.release(),
            e -> {
                semaphore.release();
                ExceptionHandler.messageHandle(this.msg, this, e);
            }
        );
    }

    public void doSynchronizedAction(BoarUser boarUser) {
        String userID = boarUser.getUserID();

        if (this.userTimes.containsKey(userID)) {
            try (Connection connection = DataUtil.getConnection()) {
                boarUser.eventQuery().applyPowEventWin(
                    connection, this.powerupID, POWS.get(this.powerupID).getEventAmt(), this.userTimes.get(userID)
                );
                QuestUtil.sendQuestClaimMessage(
                    this.userHooks.get(boarUser.getUserID()),
                    boarUser.questQuery().addProgress(QuestType.POW_WIN, 1, connection),
                    boarUser.questQuery().addProgress(QuestType.POW_FAST, this.userTimes.get(userID), connection)
                );
            } catch (SQLException exception) {
                this.failedSynchronized.add(boarUser.getUserID());
                SpecialReply.sendErrorMessage(this.userHooks.get(boarUser.getUserID()), this);
                Log.error(boarUser.getUser(), this.getClass(), "Failed to give Powerup Event win", exception);
            }
        } else if (this.failUsers.containsKey(userID) && this.failUsers.get(userID)) {
            try (Connection connection = DataUtil.getConnection()) {
                boarUser.eventQuery().applyPowEventFail(connection);
            } catch (SQLException exception) {
                this.failedSynchronized.add(boarUser.getUserID());
                SpecialReply.sendErrorMessage(this.userHooks.get(boarUser.getUserID()), this);
                Log.error(boarUser.getUser(), this.getClass(), "Failed to give Powerup Event fail", exception);
            }
        }
    }

    @Override
    public void stop(StopType type) {
        Interactive interactive = this.removeInteractive();
        this.isStopped = true;

        if (interactive == null || this.msg == null) {
            return;
        }

        if (type.equals(StopType.EXCEPTION)) {
            super.stop(type);
            this.eventHandler.decNumActive();
            return;
        }

        try {
            this.updateComponents(true, ActionRow.of(tabulatingBtn));
            this.eventHandler.decNumActive();
        } catch (ErrorResponseException ignored) {}
    }

    @Override
    public ActionRow[] getCurComponents() {
        List<ActionRow> rows = new ArrayList<>();

        switch (this.eventHandler.getPromptType()) {
            case PromptType.EMOJI_FIND -> this.makeFindEmoji(rows);
            case PromptType.TRIVIA -> this.makeTrivia(rows);
            case PromptType.FAST -> this.makeFast(rows);
            case PromptType.CLOCK -> this.makeClock(rows);
        }

        return rows.toArray(new ActionRow[0]);
    }

    private void makeFindEmoji(List<ActionRow> rows) {
        int correctIndex = (int) (Math.random() * BASE_SIDE_LENGTH * BASE_SIDE_LENGTH);

        Emoji correctEmoji = InteractiveUtil.parseEmoji(promptConfig.getEmoji1());
        Emoji incorrectEmoji = InteractiveUtil.parseEmoji(promptConfig.getEmoji2());

        for (int i=0; i<BASE_SIDE_LENGTH; i++) {
            List<ItemComponent> rowComponents = new ArrayList<>();

            for (int j=0; j<BASE_SIDE_LENGTH; j++) {
                int curIndex = i*BASE_SIDE_LENGTH+j;

                if (curIndex == correctIndex) {
                    rowComponents.add(new ButtonImpl(CORRECT_ID, null, ButtonStyle.PRIMARY, false, correctEmoji));
                    continue;
                }

                rowComponents.add(new ButtonImpl(
                    INCORRECT_ID + curIndex, null, ButtonStyle.PRIMARY, false, incorrectEmoji
                ));
            }

            rows.add(ActionRow.of(rowComponents));
        }
    }

    private void makeTrivia(List<ActionRow> rows) {
        final int TRIVIA_SIDE_LENGTH = 2;

        int correctIndex = (int) (Math.random() * TRIVIA_SIDE_LENGTH * TRIVIA_SIDE_LENGTH);

        String[] choices = promptConfig.getChoices();
        String correctLabel = choices[0];
        String[] incorrectLabels = {choices[1], choices[2], choices[3]};

        int incorrectIndex = 0;
        for (int i=0; i<TRIVIA_SIDE_LENGTH; i++) {
            List<ItemComponent> rowComponents = new ArrayList<>();

            for (int j=0; j<TRIVIA_SIDE_LENGTH; j++) {
                int curIndex = i*TRIVIA_SIDE_LENGTH+j;

                if (curIndex == correctIndex) {
                    rowComponents.add(new ButtonImpl(CORRECT_ID, correctLabel, ButtonStyle.PRIMARY, false, null));
                    continue;
                }

                rowComponents.add(new ButtonImpl(
                    INCORRECT_ID + curIndex, incorrectLabels[incorrectIndex], ButtonStyle.PRIMARY, false, null
                ));
                incorrectIndex++;
            }

            rows.add(ActionRow.of(rowComponents));
        }
    }

    private void makeFast(List<ActionRow> rows) {
        final int FAST_MAX_LENGTH = 3;

        int correctIndex = (int) (Math.random() * promptConfig.getNumButtons());
        String label = STRS.getEmpty();

        for (int i=0; i<FAST_MAX_LENGTH; i++) {
            List<ItemComponent> rowComponents = new ArrayList<>();

            if (i*FAST_MAX_LENGTH >= promptConfig.getNumButtons()) {
                break;
            }

            for (int j=0; j<FAST_MAX_LENGTH; j++) {
                int curIndex = i*FAST_MAX_LENGTH+j;

                if (curIndex >= promptConfig.getNumButtons()) {
                    break;
                }

                if (curIndex == correctIndex) {
                    rowComponents.add(new ButtonImpl(CORRECT_ID, label, ButtonStyle.PRIMARY, false, null));
                    continue;
                }

                rowComponents.add(new ButtonImpl(INCORRECT_ID + curIndex,label , ButtonStyle.SECONDARY, false, null));
            }

            rows.add(ActionRow.of(rowComponents));
        }
    }

    private void makeClock(List<ActionRow> rows) {
        int correctIndex = (int) (Math.random() * BASE_SIDE_LENGTH * BASE_SIDE_LENGTH);

        Emoji correctEmoji = InteractiveUtil.parseEmoji(promptConfig.getRightClock());
        List<Emoji> incorrectEmojis = new ArrayList<>();

        Collection<IndivPromptConfig> prompts = CONFIG.getPromptConfig()
            .get(this.eventHandler.getPromptType().toString()).getPrompts().values();

        for (IndivPromptConfig prompt : prompts) {
            if (!prompt.getName().equals(promptConfig.getName())) {
                incorrectEmojis.add(InteractiveUtil.parseEmoji(prompt.getRightClock()));
            }
        }

        for (int i=0; i<BASE_SIDE_LENGTH; i++) {
            List<ItemComponent> rowComponents = new ArrayList<>();

            for (int j=0; j<BASE_SIDE_LENGTH; j++) {
                int curIndex = i*BASE_SIDE_LENGTH+j;

                if (curIndex == correctIndex) {
                    rowComponents.add(new ButtonImpl(CORRECT_ID, null, ButtonStyle.PRIMARY, false, correctEmoji));
                    continue;
                }

                Emoji randEmoji = incorrectEmojis.remove((int) (Math.random() * incorrectEmojis.size()));
                rowComponents.add(new ButtonImpl(INCORRECT_ID + curIndex, null, ButtonStyle.PRIMARY, false, randEmoji));
            }

            rows.add(ActionRow.of(rowComponents));
        }
    }
}
