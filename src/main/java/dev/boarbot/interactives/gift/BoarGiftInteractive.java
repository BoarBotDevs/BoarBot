package dev.boarbot.interactives.gift;

import dev.boarbot.api.util.Weighted;
import dev.boarbot.bot.config.components.IndivComponentConfig;
import dev.boarbot.bot.config.items.OutcomeConfig;
import dev.boarbot.bot.config.items.PowerupItemConfig;
import dev.boarbot.bot.config.items.SubOutcomeConfig;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.BoarUserFactory;
import dev.boarbot.entities.boaruser.Synchronizable;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.interactives.ItemInteractive;
import dev.boarbot.interactives.UserInteractive;
import dev.boarbot.util.logging.Log;
import dev.boarbot.util.quests.QuestInfo;
import dev.boarbot.util.quests.QuestUtil;
import dev.boarbot.util.boar.BoarObtainType;
import dev.boarbot.util.boar.BoarUtil;
import dev.boarbot.util.quests.QuestType;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.data.GuildDataUtil;
import dev.boarbot.util.generators.EmbedImageGenerator;
import dev.boarbot.util.generators.GiftImageGenerator;
import dev.boarbot.util.interactive.InteractiveUtil;
import dev.boarbot.util.interactive.StopType;
import dev.boarbot.util.time.TimeUtil;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class BoarGiftInteractive extends UserInteractive implements Synchronizable {
    private final PowerupItemConfig giftConfig = POWS.get("gift");

    private final FileUpload giftImage;

    private boolean isSkyblockGuild = false;

    private boolean enabled = false;
    private long giftEnabledTimestamp = 0;
    private final Map<User, Long> giftTimes = new HashMap<>();
    private final Map<User, GenericComponentInteractionCreateEvent> giftInteractions = new HashMap<>();
    private boolean givenGift = false;
    private User giftWinner;
    private long giftWinnerValue;
    private boolean hasGift = false;

    private OutcomeType outcomeType;
    private OutcomeConfig outcomeConfig;
    private SubOutcomeType subOutcomeType;
    private SubOutcomeConfig subOutcomeConfig;

    private List<String> boarIDs = new ArrayList<>();
    private int numBucks = 0;

    private final List<QuestInfo> senderQuestInfos = new ArrayList<>();
    private final List<QuestInfo> openerQuestInfos = new ArrayList<>();

    private final Map<String, IndivComponentConfig> components = CONFIG.getComponentConfig().getGift();

    public BoarGiftInteractive(
        Interaction interaction, boolean isMsg
    ) throws IOException, URISyntaxException {
        super(interaction, isMsg, NUMS.getGiftIdle(), NUMS.getGiftIdle());

        this.giftImage = new GiftImageGenerator(this.user.getName()).generate().getFileUpload();

        try (Connection connection = DataUtil.getConnection()) {
            this.isSkyblockGuild = GuildDataUtil.isSkyblockGuild(connection, this.guildID);
        } catch (SQLException exception) {
            this.stop(StopType.EXCEPTION);
            Log.error(this.user, this.getClass(), "Failed to get skyblock status", exception);
        }
    }

    @Override
    public synchronized void attemptExecute(GenericComponentInteractionCreateEvent compEvent, long startTime) {
        this.execute(compEvent);
    }

    @Override
    public void execute(GenericComponentInteractionCreateEvent compEvent) {
        if (compEvent == null) {
            this.sendResponse();

            int randWaitTime = (int) (Math.random() * (NUMS.getGiftHighWait() - NUMS.getGiftLowWait())) +
                NUMS.getGiftLowWait();

            this.enabled = true;

            CompletableFuture.runAsync(() -> this.enableGift(randWaitTime));

            return;
        }

        if (this.user.getId().equals(compEvent.getUser().getId())) {
            compEvent.deferEdit().queue(null, e -> Log.warn(
                this.user, this.getClass(), "Failed to defer edit", e
            ));

            try {
                MessageCreateBuilder msg = new MessageCreateBuilder()
                    .setFiles(new EmbedImageGenerator(STRS.getGiftSelfOpen()).generate().getFileUpload());
                compEvent.getHook().sendMessage(msg.build()).setEphemeral(true).complete();
            } catch (IOException exception) {
                this.stop(StopType.EXCEPTION);
                Log.error(this.user, this.getClass(), "Failed to generate self open response", exception);
            }

            return;
        }

        compEvent.deferEdit().complete();

        if (this.giftTimes.get(compEvent.getUser()) != null) {
            return;
        }

        long userTime = TimeUtil.getCurMilli() - this.giftEnabledTimestamp;
        this.giftTimes.put(compEvent.getUser(), userTime);
        this.giftInteractions.put(compEvent.getUser(), compEvent);
        Log.debug(this.user, this.getClass(), "Time: %,dms".formatted(userTime));

        if (userTime > NUMS.getGiftMaxHandicap()) {
            this.giveGift();
        }
    }

    private void sendResponse() {
        try {
            MessageEditBuilder editedMsg = new MessageEditBuilder()
                .setFiles(this.giftImage)
                .setComponents(this.getCurComponents());

            if (this.isStopped) {
                return;
            }

            this.updateInteractive(editedMsg.build());
        } catch (Exception ignored) {}
    }

    private void enableGift(int waitTime) {
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException exception) {
            if (!this.isStopped) {
                this.stop(StopType.EXPIRED);
            }
        }

        this.giftEnabledTimestamp = TimeUtil.getCurMilli();
        this.sendResponse();

        CompletableFuture.runAsync(this::tryClaimAtMaxHandicap);
    }

    private void tryClaimAtMaxHandicap() {
        try {
            Thread.sleep(NUMS.getGiftMaxHandicap());
        } catch (InterruptedException exception) {
            if (!this.isStopped) {
                this.stop(StopType.EXPIRED);
            }
        }

        if (!this.giftTimes.isEmpty()) {
            this.giveGift();
        }
    }

    private synchronized void giveGift() {
        if (this.givenGift) {
            return;
        }

        this.givenGift = true;

        BoarUser boarUser = BoarUserFactory.getBoarUser(this.user);
        boarUser.passSynchronizedAction(this);

        if (!this.hasGift) {
            Log.debug(this.user, this.getClass(), "No gifts");
            this.stop(StopType.EXPIRED);
            return;
        }

        this.giftWinner = this.giftTimes.keySet().toArray(new User[0])[0];

        for (User user : this.giftTimes.keySet()) {
            BoarUser openUser = BoarUserFactory.getBoarUser(user);
            openUser.passSynchronizedAction(this);
        }

        try {
            MessageCreateBuilder msg = new MessageCreateBuilder().setFiles(
                new EmbedImageGenerator(STRS.getGiftOpened().formatted(
                    this.giftTimes.get(this.giftWinner)
                )).generate().getFileUpload()
            );

            this.giftInteractions.get(this.giftWinner).getHook().sendMessage(msg.build()).setEphemeral(true).complete();
        } catch (IOException exception) {
            this.stop(StopType.EXCEPTION);
            Log.error(this.user, this.getClass(), "Failed to generate gift time message", exception);
        }

        this.stop(StopType.FINISHED);

        this.setOutcome();
        this.setSubOutcome();

        if (this.subOutcomeType == SubOutcomeType.SPECIAL_SANTA && !TimeUtil.isChristmas()) {
            this.outcomeType = OutcomeType.POWERUP;
            this.outcomeConfig = giftConfig.getOutcomes().get(this.outcomeType.toString());
            this.setSubOutcome();
        }

        if (this.subOutcomeType == SubOutcomeType.SPECIAL_SANTA && TimeUtil.isChristmas()) {
            this.setSubOutcome();
        }

        Log.debug(
            this.user, this.getClass(), "Outcome: %s | Suboutcome: %s".formatted(this.outcomeType, this.subOutcomeType)
        );

        switch (this.outcomeType) {
            case SPECIAL -> this.boarIDs.add(this.subOutcomeType.toString());
            case BUCKS -> {
                int minBucks = this.subOutcomeConfig.getMinBucks();
                int maxBucks = this.subOutcomeConfig.getMaxBucks();
                this.numBucks = (int) Math.round(Math.random() * (maxBucks - minBucks) + minBucks);
            }
            case BOAR -> this.boarIDs = BoarUtil.getRandBoarIDs(
                TimeUtil.isChristmas()
                    ? 50
                    : 0,
                this.isSkyblockGuild
            );
        }

        BoarUser openUser = BoarUserFactory.getBoarUser(this.giftWinner);
        openUser.passSynchronizedAction(this);

        BoarUser sendUser = BoarUserFactory.getBoarUser(this.user);
        sendUser.passSynchronizedAction(this);
    }

    private void setOutcome() {
        Map<String, OutcomeConfig> outcomeConfigs = this.giftConfig.getOutcomes();
        List<String> outcomeKeys = new ArrayList<>(outcomeConfigs.keySet());
        List<OutcomeConfig> outcomeArray = new ArrayList<>(outcomeConfigs.values());

        int outcomeIndex = this.getRandWeightedIndex(outcomeArray);

        this.outcomeType = OutcomeType.fromString(outcomeKeys.get(outcomeIndex));
        this.outcomeConfig = outcomeArray.get(outcomeIndex);
    }

    private void setSubOutcome() {
        Map<String, SubOutcomeConfig> subOutcomeConfigs = this.outcomeConfig.getSubOutcomes();

        if (subOutcomeConfigs == null) {
            return;
        }

        List<String> subOutcomeKeys = new ArrayList<>(subOutcomeConfigs.keySet());
        List<SubOutcomeConfig> subOutcomeArray = new ArrayList<>(subOutcomeConfigs.values());

        int subOutcomeIndex = this.getRandWeightedIndex(subOutcomeArray);

        this.subOutcomeType = SubOutcomeType.fromString(subOutcomeKeys.get(subOutcomeIndex));
        this.subOutcomeConfig = subOutcomeArray.get(subOutcomeIndex);
    }

    private int getRandWeightedIndex(List<? extends Weighted> arr) {
        List<Double> probabilities = new ArrayList<>();
        double randVal = Math.random();
        int weightTotal = 0;

        for (Weighted obj : arr) {
            weightTotal += obj.getWeight();
        }

        for (int i=0; i<arr.size(); i++) {
            double weight = arr.get(i).getWeight();
            probabilities.add(weight / weightTotal);

            if (probabilities.size() == 1) {
                continue;
            }

            probabilities.set(i, probabilities.get(i) + probabilities.get(i-1));
        }

        for (int i=0; i<probabilities.size(); i++) {
            if (randVal < probabilities.get(i)) {
                return i;
            }
        }

        return arr.size()-1;
    }

    @Override
    public void doSynchronizedAction(BoarUser boarUser) {
        try {
            if (this.outcomeType == null && this.giftWinner != null) {
                try (Connection connection = DataUtil.getConnection()) {
                    long userVal = this.giftTimes.get(boarUser.getUser()) -
                       boarUser.giftQuery().getGiftHandicap(connection);

                    Log.debug(this.user, this.getClass(), "Handicapped Value: %,d".formatted(userVal));
                    if (userVal > this.giftWinnerValue) {
                        this.giftWinner = boarUser.getUser();
                        this.giftWinnerValue = userVal;
                    }

                    boarUser.giftQuery().updateGiftHandicap(connection, this.giftTimes.get(boarUser.getUser()));
                }
            } else if (this.outcomeType == null) {
                try (Connection connection = DataUtil.getConnection()) {
                    this.hasGift = boarUser.powQuery().getPowerupAmount(connection, "gift") > 0;

                    if (this.hasGift) {
                        boarUser.powQuery().usePowerup(connection, "gift", 1);
                        this.getQuestInfos(boarUser).add(boarUser.questQuery().addProgress(
                            QuestType.SEND_GIFTS, 1, connection
                        ));
                    }
                }
            } else if (this.outcomeType == OutcomeType.SPECIAL || this.outcomeType == OutcomeType.BOAR) {
                this.giveBoar(boarUser);
            } else if (this.outcomeType == OutcomeType.BUCKS) {
                this.giveBucks(boarUser);
            } else if (this.outcomeType == OutcomeType.POWERUP) {
                this.givePowerup(boarUser);
            }

            if (this.outcomeType != null) {
                try (Connection connection = DataUtil.getConnection()) {
                    List<String> rarityKeys = new ArrayList<>();

                    for (String boarID : this.boarIDs) {
                        rarityKeys.add(BoarUtil.findRarityKey(boarID));
                    }

                    boarUser.giftQuery().openGift(
                        connection, this.numBucks, rarityKeys, this.giftWinner.getId().equals(boarUser.getUserID())
                    );

                    if (this.giftWinner.getId().equals(boarUser.getUserID())) {
                        this.openerQuestInfos.add(boarUser.questQuery().addProgress(
                            QuestType.OPEN_GIFTS, 1, connection
                        ));
                        QuestUtil.sendQuestClaimMessage(
                            this.giftInteractions.get(this.giftWinner).getHook(), this.openerQuestInfos
                        );
                    } else {
                        QuestUtil.sendQuestClaimMessage(this.hook, this.senderQuestInfos);
                    }
                }
            }
        } catch (SQLException exception) {
            this.stop(StopType.EXCEPTION);
            Log.error(this.user, this.getClass(), "Failed to fully perform gift open", exception);
        }
    }

    private void giveBoar(BoarUser boarUser) throws SQLException {
        List<Integer> bucksGotten = new ArrayList<>();
        List<Integer> editions = new ArrayList<>();

        try (Connection connection = DataUtil.getConnection()) {
            boarUser.boarQuery().addBoars(this.boarIDs, connection, BoarObtainType.GIFT, bucksGotten, editions);
            this.getQuestInfos(boarUser).add(boarUser.questQuery().addProgress(
                QuestType.COLLECT_RARITY, this.boarIDs, connection
            ));
        }

        if (boarUser.getUserID().equals(this.user.getId())) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            String title = STRS.getGiftTitle();
            ItemInteractive.sendInteractive(
                this.boarIDs,
                bucksGotten,
                editions,
                this.user,
                this.giftWinner,
                title,
                this.giftInteractions.get(this.giftWinner).getHook(),
                false
            );
            Log.debug(this.user, this.getClass(), "Sent ItemInteractive");
        });
    }

    private void giveBucks(BoarUser boarUser) throws SQLException {
        try (Connection connection = DataUtil.getConnection()) {
            boarUser.baseQuery().giveBucks(connection, this.numBucks);
            this.getQuestInfos(boarUser).add(boarUser.questQuery().addProgress(
                QuestType.COLLECT_BUCKS, this.boarIDs, connection
            ));
        }

        if (boarUser.getUserID().equals(this.user.getId())) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            String title = STRS.getGiftTitle();

            String rewardStr = this.outcomeConfig.getRewardStr().formatted(
                this.numBucks,
                this.numBucks == 1
                    ? STRS.getBucksName()
                    : STRS.getBucksPluralName()
            );

            String filePath = PATHS.getOtherAssets() + PATHS.getGiftBucks();

            ItemInteractive.sendInteractive(
                rewardStr,
                filePath,
                "bucks",
                this.user,
                this.giftWinner,
                title,
                this.giftInteractions.get(this.giftWinner).getHook(),
                false
            );
            Log.debug(this.user, this.getClass(), "Sent ItemInteractive");
        });
    }

    private void givePowerup(BoarUser boarUser) throws SQLException {
        try (Connection connection = DataUtil.getConnection()) {
            boarUser.powQuery().addPowerup(
                connection, this.subOutcomeType.toString(), this.subOutcomeConfig.getRewardAmt(), true
            );
        }

        if (boarUser.getUserID().equals(this.user.getId())) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            PowerupItemConfig powConfig = POWS.get(this.subOutcomeType.toString());
            String title = STRS.getGiftTitle();

            String rewardStr = this.outcomeConfig.getRewardStr().formatted(
                this.subOutcomeConfig.getRewardAmt(),
                this.subOutcomeConfig.getRewardAmt() == 1
                    ? powConfig.getName()
                    : powConfig.getPluralName()
            );

            String filePath = PATHS.getPowerups() + powConfig.getFile();

            ItemInteractive.sendInteractive(
                rewardStr,
                filePath,
                "powerup",
                this.user,
                this.giftWinner,
                title,
                this.giftInteractions.get(this.giftWinner).getHook(),
                false
            );
            Log.debug(this.user, this.getClass(), "Sent ItemInteractive");
        });
    }

    @Override
    public void stop(StopType type) {
        Interactive interactive = this.removeInteractive();
        this.isStopped = true;

        if (interactive == null) {
            return;
        }

        switch (type) {
            case EXCEPTION -> super.stop(type);

            case EXPIRED -> {
                this.deleteInteractive();
                Log.debug(this.user, this.getClass(), "Interactive expired");
            }

            case FINISHED -> {
                this.updateComponents();
                Log.debug(this.user, this.getClass(), "Finished Interactive");
            }
        }
    }

    @Override
    public ActionRow[] getCurComponents() {
        List<ActionRow> rows = new ArrayList<>();
        int SIDE_LENGTH = 3;
        int claimIndex = (int) (Math.random() * SIDE_LENGTH);

        for (int i=0; i<SIDE_LENGTH; i++) {
            List<ItemComponent> rowComponents = new ArrayList<>();

            for (int j=0; j<SIDE_LENGTH; j++) {
                int curIndex = i*SIDE_LENGTH+j;

                if (this.enabled && curIndex == claimIndex) {
                    rowComponents.add(
                        InteractiveUtil.makeComponents(
                            this.interactionID,
                                components.get("claimBtn")
                        ).getFirst()
                    );
                    continue;
                }

                rowComponents.add(
                    InteractiveUtil.makeComponents(
                        this.interactionID,
                        Integer.toString(curIndex),
                        components.get("fillerBtn")
                    ).getFirst()
                );
            }

            rows.add(ActionRow.of(rowComponents));
        }

        return rows.toArray(new ActionRow[0]);
    }

    private List<QuestInfo> getQuestInfos(BoarUser boarUser) {
        if (this.giftWinner != null && this.giftWinner.equals(boarUser.getUser())) {
            return this.openerQuestInfos;
        }

        return this.senderQuestInfos;
    }
}
