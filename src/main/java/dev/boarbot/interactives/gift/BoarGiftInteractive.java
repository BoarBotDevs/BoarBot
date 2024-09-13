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
import dev.boarbot.util.boar.BoarObtainType;
import dev.boarbot.util.boar.BoarUtil;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.data.GuildDataUtil;
import dev.boarbot.util.generators.EmbedImageGenerator;
import dev.boarbot.util.generators.GiftImageGenerator;
import dev.boarbot.util.interactive.InteractiveUtil;
import dev.boarbot.util.interactive.StopType;
import dev.boarbot.util.time.TimeUtil;
import lombok.extern.slf4j.Slf4j;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class BoarGiftInteractive extends UserInteractive implements Synchronizable {
    private final PowerupItemConfig giftConfig = POWS.get("gift");

    private FileUpload giftImage;

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

    private final Map<String, IndivComponentConfig> components = CONFIG.getComponentConfig().getGift();

    public BoarGiftInteractive(Interaction interaction, boolean isMsg) {
        super(interaction, isMsg, NUMS.getGiftIdle(), NUMS.getGiftIdle());

        try {
            this.giftImage = new GiftImageGenerator(this.user.getName()).generate().getFileUpload();
        } catch (IOException | URISyntaxException exception) {
            log.error("Failed to generate gift image", exception);
        }

        try (Connection connection = DataUtil.getConnection()) {
            this.isSkyblockGuild = GuildDataUtil.isSkyblockGuild(connection, this.guildID);
        } catch (SQLException exception) {
            log.error("Failed to get guild data", exception);
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

            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(() -> this.enableGift(randWaitTime));
            executor.shutdown();

            return;
        }

        if (this.user.getId().equals(compEvent.getUser().getId())) {
            compEvent.deferEdit().queue();

            try {
                MessageCreateBuilder msg = new MessageCreateBuilder()
                    .setFiles(new EmbedImageGenerator(STRS.getGiftSelfOpen()).generate().getFileUpload());
                compEvent.getHook().sendMessage(msg.build()).setEphemeral(true).complete();
            } catch (IOException exception) {
                log.error("An error occurred while sending self gift open message.", exception);
            }

            return;
        }

        compEvent.deferEdit().queue();

        if (this.giftTimes.get(compEvent.getUser()) != null) {
            return;
        }

        long userTime = TimeUtil.getCurMilli() - this.giftEnabledTimestamp;
        this.giftTimes.put(compEvent.getUser(), userTime);
        this.giftInteractions.put(compEvent.getUser(), compEvent);

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

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
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
        });
        executor.shutdown();
    }

    private synchronized void giveGift() {
        if (this.givenGift) {
            return;
        }

        this.givenGift = true;

        try {
            BoarUser boarUser = BoarUserFactory.getBoarUser(this.user);
            boarUser.passSynchronizedAction(this);
            boarUser.decRefs();
        } catch (SQLException exception) {
            log.error("Failed to get sender powerups");
        }

        if (!this.hasGift) {
            this.stop(StopType.EXPIRED);
            return;
        }

        this.giftWinner = this.giftTimes.keySet().toArray(new User[0])[0];

        for (User user : this.giftTimes.keySet()) {
            try {
                BoarUser openUser = BoarUserFactory.getBoarUser(user);
                openUser.passSynchronizedAction(this);
                openUser.decRefs();
            } catch (SQLException exception) {
                log.error("Failed to get user data", exception);
            }
        }

        try {
            MessageCreateBuilder msg = new MessageCreateBuilder().setFiles(
                new EmbedImageGenerator(STRS.getGiftOpened().formatted(
                    this.giftTimes.get(this.giftWinner)
                )).generate().getFileUpload()
            );

            this.giftInteractions.get(this.giftWinner).getHook().sendMessage(msg.build()).setEphemeral(true).complete();
        } catch (IOException exception) {
            log.error("Failed to send gift time message", exception);
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

        try {
            BoarUser openUser = BoarUserFactory.getBoarUser(this.giftWinner);
            openUser.passSynchronizedAction(this);
            openUser.decRefs();
        } catch (SQLException exception) {
            log.error("Failed to get user data", exception);
        }

        try {
            BoarUser sendUser = BoarUserFactory.getBoarUser(this.user);
            sendUser.passSynchronizedAction(this);
            sendUser.decRefs();
        } catch (SQLException exception) {
            log.error("Failed to get user data", exception);
        }
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
                    long userVal = this.giftTimes.get(boarUser.getUser()) - boarUser.getGiftHandicap(connection);

                    if (userVal > this.giftWinnerValue) {
                        this.giftWinner = boarUser.getUser();
                        this.giftWinnerValue = userVal;
                    }

                    boarUser.updateGiftHandicap(connection, this.giftTimes.get(boarUser.getUser()));
                }
            } else if (this.outcomeType == null) {
                try (Connection connection = DataUtil.getConnection()) {
                    this.hasGift = boarUser.getPowerupAmount(connection, "gift") > 0;

                    if (this.hasGift) {
                        boarUser.usePowerup(connection, "gift", 1);
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

                    boarUser.openGift(
                        connection, this.numBucks, rarityKeys, this.giftWinner.getId().equals(boarUser.getUserID())
                    );
                }
            }
        } catch (SQLException exception) {
            log.error("Failed to get user data", exception);
        }
    }

    private void giveBoar(BoarUser boarUser) throws SQLException {
        List<Integer> bucksGotten = new ArrayList<>();
        List<Integer> editions = new ArrayList<>();

        try (Connection connection = DataUtil.getConnection()) {
            boarUser.addBoars(this.boarIDs, connection, BoarObtainType.GIFT, bucksGotten, editions);
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
        });
    }

    private void giveBucks(BoarUser boarUser) throws SQLException {
        try (Connection connection = DataUtil.getConnection()) {
            boarUser.giveBucks(connection, this.numBucks);
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
        });
    }

    private void givePowerup(BoarUser boarUser) throws SQLException {
        try (Connection connection = DataUtil.getConnection()) {
            boarUser.addPowerup(connection, this.subOutcomeType.toString(), this.subOutcomeConfig.getRewardAmt());
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
        });
    }

    @Override
    public void stop(StopType stopType) {
        Interactive interactive = this.removeInteractive();
        this.isStopped = true;

        if (interactive == null) {
            return;
        }

        switch (stopType) {
            case EXPIRED -> this.deleteInteractive();
            case FINISHED -> this.updateComponents();
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
}
