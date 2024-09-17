package dev.boarbot.commands.boar;

import dev.boarbot.commands.Subcommand;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.BoarUserFactory;
import dev.boarbot.entities.boaruser.Synchronizable;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.interactives.InteractiveFactory;
import dev.boarbot.interactives.ItemInteractive;
import dev.boarbot.interactives.boar.daily.DailyNotifyInteractive;
import dev.boarbot.util.logging.Log;
import dev.boarbot.util.quests.QuestInfo;
import dev.boarbot.util.quests.QuestUtil;
import dev.boarbot.util.boar.BoarObtainType;
import dev.boarbot.util.boar.BoarUtil;
import dev.boarbot.util.quests.QuestType;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.data.GuildDataUtil;
import dev.boarbot.util.generators.EmbedImageGenerator;
import dev.boarbot.util.time.TimeUtil;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DailySubcommand extends Subcommand implements Synchronizable {
    private List<String> boarIDs = new ArrayList<>();
    private final List<Integer> bucksGotten = new ArrayList<>();
    private final List<Integer> boarEditions = new ArrayList<>();

    private boolean canDaily = true;
    private boolean notificationsOn = false;
    private boolean isFirstDaily = false;
    private boolean hasDonePowerup = false;

    private final List<QuestInfo> questInfos = new ArrayList<>();

    public DailySubcommand(SlashCommandInteractionEvent event) {
        super(event);
    }

    @Override
    public void execute() {
        if (!this.canInteract()) {
            return;
        }

        BoarUser boarUser = BoarUserFactory.getBoarUser(this.user);
        boarUser.passSynchronizedAction(this);

        if (!this.canDaily) {
            this.interaction.deferReply().setEphemeral(true).queue();

            if (!this.notificationsOn) {
                Interactive interactive = InteractiveFactory.constructInteractive(
                    this.event, DailyNotifyInteractive.class
                );
                interactive.execute(null);
                Log.debug(this.user, this.getClass(), "Sent DailyNotifyInteractive");
                return;
            }

            String dailyResetDistance = TimeUtil.getTimeDistance(TimeUtil.getNextDailyResetMilli(), false);
            dailyResetDistance = dailyResetDistance.substring(dailyResetDistance.indexOf(' ')+1);
            String replyStr = STRS.getDailyUsed().formatted(dailyResetDistance);

            try {
                FileUpload fileUpload = new EmbedImageGenerator(replyStr).generate().getFileUpload();
                MessageEditBuilder editedMsg = new MessageEditBuilder().setFiles(fileUpload).setComponents();
                this.interaction.getHook().editOriginal(editedMsg.build()).queue();
            } catch (IOException exception) {
                Log.error(this.user, this.getClass(), "Failed to generate daily used message", exception);
            }
        }

        if (!this.boarIDs.isEmpty()) {
            this.sendResponse();
        }
    }

    @Override
    public void doSynchronizedAction(BoarUser boarUser) {
        try (Connection connection = DataUtil.getConnection()) {
            if (!boarUser.boarQuery().canUseDaily(connection) && !CONFIG.getMainConfig().isUnlimitedBoars()) {
                Log.debug(this.user, this.getClass(), "Daily not available");
                this.canDaily = false;
                this.notificationsOn = boarUser.baseQuery().getNotificationStatus(connection);
                return;
            }
        } catch (SQLException exception) {
            Log.error(this.user, this.getClass(), "Failed to get daily or notification status", exception);
        }

        try (Connection connection = DataUtil.getConnection()) {
            if (!this.hasDonePowerup && this.event.getOption("powerup") != null) {
                Log.debug(this.user, this.getClass(), "Attempting to use powerup");
                this.hasDonePowerup = true;
                this.sendPowResponse();
                return;
            }

            if (!this.hasDonePowerup) {
                Log.debug(this.user, this.getClass(), "Doing daily without powerup");
                this.interaction.deferReply().complete();
            }

            this.isFirstDaily = boarUser.isFirstDaily();

            long blessings = boarUser.baseQuery().getBlessings(connection);
            Log.debug(this.user, this.getClass(), "Blessings: %,d".formatted(blessings));

            boolean isSkyblockGuild = GuildDataUtil.isSkyblockGuild(
                connection, Objects.requireNonNull(this.interaction.getGuild()).getId()
            );
            this.boarIDs = BoarUtil.getRandBoarIDs(blessings, isSkyblockGuild);

            boarUser.boarQuery().addBoars(
                this.boarIDs, connection, BoarObtainType.DAILY, this.bucksGotten, this.boarEditions
            );
            boarUser.powQuery().useActiveMiracles(this.boarIDs, this.bucksGotten, connection);

            this.questInfos.add(boarUser.questQuery().addProgress(QuestType.DAILY, 1, connection));
            this.questInfos.add(boarUser.questQuery().addProgress(
                QuestType.COLLECT_RARITY, this.boarIDs, connection
            ));
            this.questInfos.add(boarUser.questQuery().addProgress(
                QuestType.COLLECT_BUCKS, this.bucksGotten.stream().reduce(0, Integer::sum), connection
            ));
        } catch (SQLException exception) {
            Log.error(this.user, this.getClass(), "Failed to fully complete daily update logic", exception);
        }
    }

    private void sendResponse() {
        String title = STRS.getDailyTitle();

        ItemInteractive.sendInteractive(
            this.boarIDs, this.bucksGotten, this.boarEditions, null, this.user, title, this.interaction.getHook(), false
        );
        Log.debug(this.user, this.getClass(), "Sent ItemInteractive");

        if (this.isFirstDaily) {
            try {
                EmbedImageGenerator embedGen = new EmbedImageGenerator(STRS.getDailyFirstTime());
                this.interaction.getHook().sendFiles(embedGen.generate().getFileUpload()).setEphemeral(true).complete();
            } catch (IOException exception) {
                Log.error(this.user, this.getClass(), "Failed to generate first daily reward message", exception);
            }
        }

        QuestUtil.sendQuestClaimMessage(this.interaction.getHook(), this.questInfos);
    }

    private void sendPowResponse() {
        this.interaction.deferReply().complete();

        Interactive interactive = InteractiveFactory.constructDailyPowerupInteractive(this.event, this);
        interactive.execute(null);
        Log.debug(this.user, this.getClass(), "Sent DailyPowerupInteractive");
    }
}
