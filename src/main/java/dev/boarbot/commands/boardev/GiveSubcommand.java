package dev.boarbot.commands.boardev;

import dev.boarbot.bot.config.items.BadgeItemConfig;
import dev.boarbot.bot.config.items.PowerupItemConfig;
import dev.boarbot.commands.Subcommand;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.BoarUserFactory;
import dev.boarbot.interactives.ItemInteractive;
import dev.boarbot.util.boar.BoarTag;
import dev.boarbot.util.boar.ItemType;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.generators.EmbedImageGenerator;
import dev.boarbot.util.interaction.SpecialReply;
import dev.boarbot.util.logging.ExceptionHandler;
import dev.boarbot.util.logging.Log;
import dev.boarbot.util.resource.ResourceUtil;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class GiveSubcommand extends Subcommand {
    private final User giftedUser;
    private final ItemType itemType;
    private String itemID;
    private int tier = 0;
    private int amount = 1;

    private final List<String> boarIDs = new ArrayList<>();
    private final List<Integer> bucks = new ArrayList<>();
    private final List<Integer> editions = new ArrayList<>();
    private final Set<String> firstBoarIDs = new HashSet<>();

    private boolean failedSynchronized = false;

    public GiveSubcommand(SlashCommandInteractionEvent event) {
        super(event);

        this.giftedUser = Objects.requireNonNull(event.getOption("user")).getAsUser();
        this.itemType = ItemType.values()[Objects.requireNonNull(event.getOption("type")).getAsInt()];

        if (event.getOption("id") != null) {
            this.itemID = Objects.requireNonNull(event.getOption("id")).getAsString();
        }

        if (event.getOption("tier") != null) {
            this.tier = Math.max(Objects.requireNonNull(event.getOption("tier")).getAsInt()-1, 0);
        }

        if (event.getOption("amount") != null) {
            this.amount = Math.max(Objects.requireNonNull(event.getOption("amount")).getAsInt(), 1);
        }
    }

    @Override
    public void execute() {
        if (!Arrays.asList(CONFIG.getMainConfig().getDevs()).contains(this.user.getId())) {
            sendBadReply(STRS.getNoPermission(), "Failed to generate no permission message");
            return;
        }

        if (this.itemType != ItemType.BUCKS && this.itemID == null) {
            sendBadReply(STRS.getGiveNoID(), "Failed to generate no id message");
            return;
        }

        if (this.itemType == ItemType.BOAR) {
            this.giveBoar();
            return;
        }

        if (this.itemType == ItemType.BADGE) {
            this.giveBadge();
            return;
        }

        if (this.itemType == ItemType.POWERUP) {
            this.givePowerup();
            return;
        }

        if (this.itemType == ItemType.BUCKS) {
            this.giveBucks();
        }
    }

    private void giveBoar() {
        if (!BOARS.containsKey(this.itemID)) {
            sendBadReply(STRS.getGiveBadID().formatted(this.itemID), "Failed to generate bad boar id message");
            return;
        }

        this.interaction.deferReply().queue(null, e -> ExceptionHandler.deferHandle(this.interaction, this, e));

        for (int i=0; i<this.amount; i++) {
            this.boarIDs.add(this.itemID);
        }

        try {
            BoarUser boarUser = BoarUserFactory.getBoarUser(this.giftedUser);
            boarUser.passSynchronizedAction(() -> this.processBoars(boarUser));
        } catch (SQLException exception) {
            SpecialReply.sendErrorMessage(this.interaction, this);
            Log.error(this.giftedUser, this.getClass(), "Failed to update data", exception);
            return;
        }

        if (this.failedSynchronized) {
            return;
        }

        ItemInteractive.sendInteractive(
            this.boarIDs,
            this.bucks,
            this.editions,
            this.firstBoarIDs,
            null,
            this.giftedUser,
            STRS.getGiveTitle(),
            this.interaction.getHook(),
            false
        );
    }

    private void giveBadge() {
        if (!BADGES.containsKey(this.itemID)) {
            sendBadReply(STRS.getGiveBadID().formatted(this.itemID), "Failed to generate bad badge id message");
            return;
        }

        this.interaction.deferReply().queue(null, e -> ExceptionHandler.deferHandle(this.interaction, this, e));

        BadgeItemConfig badge = BADGES.get(this.itemID);
        this.tier = Math.min(badge.getNames().length-1, this.tier);

        try {
            BoarUser boarUser = BoarUserFactory.getBoarUser(this.giftedUser);
            boarUser.passSynchronizedAction(() -> this.processBadge(boarUser));
        } catch (SQLException exception) {
            SpecialReply.sendErrorMessage(this.interaction, this);
            Log.error(this.giftedUser, this.getClass(), "Failed to update data", exception);
            return;
        }

        if (this.failedSynchronized) {
            return;
        }

        ItemInteractive.sendInteractive(
            badge.getNames()[this.tier],
            ResourceUtil.badgeAssetsPath + badge.getFiles()[this.tier],
            "badge",
            null,
            this.giftedUser,
            STRS.getGiveTitle(),
            false,
            this.interaction.getHook(),
            false
        );
    }

    private void givePowerup() {
        if (!POWS.containsKey(this.itemID)) {
            sendBadReply(STRS.getGiveBadID().formatted(this.itemID), "Failed to generate bad powerup id message");
            return;
        }

        this.interaction.deferReply().queue(null, e -> ExceptionHandler.deferHandle(this.interaction, this, e));

        try {
            BoarUser boarUser = BoarUserFactory.getBoarUser(this.giftedUser);
            boarUser.passSynchronizedAction(() -> this.processPowerups(boarUser));
        } catch (SQLException exception) {
            SpecialReply.sendErrorMessage(this.interaction, this);
            Log.error(this.giftedUser, this.getClass(), "Failed to update data", exception);
            return;
        }

        if (this.failedSynchronized) {
            return;
        }

        PowerupItemConfig powerup = POWS.get(this.itemID);
        String powerupName = POWS.get("gift").getOutcomes().get("powerup").getRewardStr().formatted(
            this.amount, this.amount == 1 ? powerup.getName() : powerup.getPluralName()
        );

        ItemInteractive.sendInteractive(
            powerupName,
            ResourceUtil.powerupAssetsPath + powerup.getFile(),
            "powerup",
            null,
            this.giftedUser,
            STRS.getGiveTitle(),
            false,
            this.interaction.getHook(),
            false
        );
    }

    private void giveBucks() {
        this.interaction.deferReply().queue(null, e -> ExceptionHandler.deferHandle(this.interaction, this, e));

        try {
            BoarUser boarUser = BoarUserFactory.getBoarUser(this.giftedUser);
            boarUser.passSynchronizedAction(() -> this.processBucks(boarUser));
        } catch (SQLException exception) {
            SpecialReply.sendErrorMessage(this.interaction, this);
            Log.error(this.giftedUser, this.getClass(), "Failed to update data", exception);
            return;
        }

        if (this.failedSynchronized) {
            return;
        }

        String bucksName = POWS.get("gift").getOutcomes().get("bucks").getRewardStr().formatted(
            this.amount, this.amount == 1 ? STRS.getBucksName() : STRS.getBucksPluralName()
        );

        ItemInteractive.sendInteractive(
            bucksName,
            ResourceUtil.bucksGiftPath,
            "bucks",
            null,
            this.giftedUser,
            STRS.getGiveTitle(),
            false,
            this.interaction.getHook(),
            false
        );
    }

    public void processBoars(BoarUser boarUser) {
        try (Connection connection = DataUtil.getConnection()) {
            boarUser.boarQuery().addBoars(
                this.boarIDs,
                connection,
                BoarTag.GIVEN.toString(),
                this.bucks,
                this.editions,
                this.firstBoarIDs
            );
        } catch (SQLException exception) {
            this.failedSynchronized = true;
            SpecialReply.sendErrorMessage(this.interaction, this);
            Log.error(this.user, this.getClass(), "Failed to add boars", exception);
        }
    }

    public void processBadge(BoarUser boarUser) {
        try (Connection connection = DataUtil.getConnection()) {
            boarUser.baseQuery().giveBadge(connection, this.itemID, this.tier);
        } catch (SQLException exception) {
            this.failedSynchronized = true;
            SpecialReply.sendErrorMessage(this.interaction, this);
            Log.error(this.user, this.getClass(), "Failed to add badge", exception);
        }
    }

    public void processPowerups(BoarUser boarUser) {
        try (Connection connection = DataUtil.getConnection()) {
            boarUser.powQuery().addPowerup(connection, this.itemID, this.amount, true);
        } catch (SQLException exception) {
            this.failedSynchronized = true;
            SpecialReply.sendErrorMessage(this.interaction, this);
            Log.error(this.user, this.getClass(), "Failed to add powerup", exception);
        }
    }

    public void processBucks(BoarUser boarUser) {
        try (Connection connection = DataUtil.getConnection()) {
            boarUser.baseQuery().giveBucks(connection, this.amount);
        } catch (SQLException exception) {
            this.failedSynchronized = true;
            SpecialReply.sendErrorMessage(this.interaction, this);
            Log.error(this.user, this.getClass(), "Failed to add bucks", exception);
        }
    }

    private void sendBadReply(String replyMsg, String errorMsg) {
        try {
            FileUpload fileUpload = new EmbedImageGenerator(replyMsg, COLORS.get("error")).generate()
                .getFileUpload();
            this.interaction.replyFiles(fileUpload).setEphemeral(true)
                .queue(null, e -> ExceptionHandler.replyHandle(this.interaction, this, e));
        } catch (IOException exception) {
            SpecialReply.sendErrorMessage(this.interaction, this);
            Log.error(this.user, this.getClass(), errorMsg, exception);
        }
    }
}
