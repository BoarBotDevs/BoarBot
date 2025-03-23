package dev.boarbot.commands.boar;

import dev.boarbot.bot.ConfigUpdater;
import dev.boarbot.commands.Subcommand;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.BoarUserFactory;
import dev.boarbot.entities.boaruser.Synchronizable;
import dev.boarbot.interactives.ItemInteractive;
import dev.boarbot.util.boar.BoarObtainType;
import dev.boarbot.util.boar.BoarUtil;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.data.UserDataUtil;
import dev.boarbot.util.generators.EmbedImageGenerator;
import dev.boarbot.util.interaction.InteractionUtil;
import dev.boarbot.util.interaction.SpecialReply;
import dev.boarbot.util.logging.ExceptionHandler;
import dev.boarbot.util.logging.Log;
import dev.boarbot.util.time.TimeUtil;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class GuessSubcommand extends Subcommand implements Synchronizable {
    private static final List<String> superSecretGuesses = Arrays.asList(STRS.getGuessStrs());
    private static final List<String> superSecretReplies = Arrays.asList(STRS.getGuessReplyStrs());

    private static final List<String> spookGuesses = Arrays.asList(STRS.getSpookGuessStrs());
    private static final List<String> spookReplies = Arrays.asList(STRS.getSpookGuessReplyStrs());
    private static final List<String> halloweenBoarIDs = new ArrayList<>();
    private static final List<String> halloweenBoarStrs = new ArrayList<>();
    private static final String spookyBoarStr = "<>%s<>%s"
        .formatted(BoarUtil.findRarityKey("spooky"), BOARS.get("spooky").getName());

    private boolean isTrophyGuess = false;
    private int spookIndex = -1;
    private boolean isEasterGuess = false;
    private boolean failedSynchronized = false;

    private final EmbedImageGenerator embedImageGenerator = new EmbedImageGenerator(
        STRS.getIncorrectGuess(), COLORS.get("error")
    );

    static {
        String[] curHalloweenBoarIDs = RARITIES.get("halloween").getBoars();
        for (int i=curHalloweenBoarIDs.length-spookGuesses.size(); i<curHalloweenBoarIDs.length; i++) {
            halloweenBoarStrs.add("<>halloween<>" + BOARS.get(curHalloweenBoarIDs[i]).getName());
            halloweenBoarIDs.add(curHalloweenBoarIDs[i]);
        }
    }

    public GuessSubcommand(SlashCommandInteractionEvent event) {
        super(event);
    }

    @Override
    public void execute() {
        if (!this.canInteract()) {
            return;
        }

        this.interaction.deferReply(true).queue(null, e -> ExceptionHandler.deferHandle(this.interaction, this, e));

        String input = getFixedInput(Objects.requireNonNull(this.event.getOption("value")).getAsString());

        Log.info(this.user, this.getClass(), "Guessed: " + input);

        if (superSecretGuesses.contains(input)) {
            int guessIndex = superSecretGuesses.indexOf(input);
            this.embedImageGenerator.setStr(superSecretReplies.get(guessIndex));
            this.embedImageGenerator.setColor(COLORS.get("font"));
        }

        if (TimeUtil.isHalloween() && spookGuesses.contains(input)) {
            this.spookIndex = spookGuesses.indexOf(input);
        }

        if (TimeUtil.isEaster() && input.equals(STRS.getEasterGuessStr())) {
            this.isEasterGuess = true;
        }

        if (input.equals(STRS.getTrophyGuessStr())) {
            this.isTrophyGuess = true;
        }

        if (this.spookIndex != -1 || this.isEasterGuess || this.isTrophyGuess) {
            try {
                BoarUser boarUser = BoarUserFactory.getBoarUser(this.user);
                boarUser.passSynchronizedAction(this);
            } catch (SQLException exception) {
                SpecialReply.sendErrorMessage(this.interaction, this);
                Log.error(this.user, this.getClass(), "Failed to update data", exception);
                return;
            }
        }

        if (this.failedSynchronized) {
            return;
        }

        MessageEditBuilder messageBuilder = new MessageEditBuilder();

        try {
            messageBuilder.setFiles(this.embedImageGenerator.generate().getFileUpload());
        } catch (IOException exception) {
            Log.error(this.event.getUser(), this.getClass(), "Failed to generate guess reply embed", exception);
            messageBuilder.setContent(STRS.getMaintenance());
        }

        this.interaction.getHook().editOriginal(messageBuilder.build())
            .queue(null, e -> ExceptionHandler.replyHandle(this.interaction, this, e));
    }

    @Override
    public void doSynchronizedAction(BoarUser boarUser) {
        List<String> boarIDs = new ArrayList<>();
        List<Integer> bucksGotten = new ArrayList<>();
        List<Integer> editions = new ArrayList<>();
        Set<String> firstBoarIDs = new HashSet<>();
        String obtainType = null;

        try (Connection connection = DataUtil.getConnection()) {
            if (this.isTrophyGuess && isTrophyAvailable()) {
                obtainType = BoarObtainType.OTHER.toString();
                this.embedImageGenerator.setStr(STRS.getTrophyGuessReplyStr());
                ConfigUpdater.clearTrophyGuessStr();
                boarIDs.add("trophy");
            } else if (this.spookIndex != -1) {
                obtainType = "SPOOK_%d_%d".formatted(this.spookIndex+1, TimeUtil.getYear());

                if (!boarUser.boarQuery().hasBoarWithTag(connection, obtainType)) {
                    String spookReply = spookReplies.get(this.spookIndex);

                    boolean userHasSpooky = boarUser.boarQuery().hasYearlySpooky(connection);
                    boolean canGiveSpooky = !userHasSpooky &&
                        UserDataUtil.isSpookyAvailable(connection, obtainType);

                    if (canGiveSpooky) {
                        boarIDs.add("spooky");
                        boarUser.powQuery().addPowerup(connection, "transmute", 1);
                        spookReply += " " + STRS.getSpookFirstExtraStr().formatted(
                            spookyBoarStr, halloweenBoarStrs.get(this.spookIndex), POWS.get("transmute").getName()
                        );
                    } else if (userHasSpooky && UserDataUtil.isSpookyAvailable(connection, obtainType)) {
                        boarUser.powQuery().addPowerup(connection, "transmute", 3);
                        spookReply += " " + STRS.getSpookHasExtraStr()
                            .formatted(halloweenBoarStrs.get(this.spookIndex), POWS.get("transmute").getPluralName());
                    } else {
                        boarUser.powQuery().addPowerup(connection, "transmute", 1);
                        spookReply += " " + STRS.getSpookExtraStr()
                            .formatted(halloweenBoarStrs.get(this.spookIndex), POWS.get("transmute").getName());
                    }

                    boarIDs.add(halloweenBoarIDs.get(this.spookIndex));

                    this.embedImageGenerator.setStr(spookReply);
                }
            } else if (this.isEasterGuess) {
                obtainType = "EASTER_%d".formatted(TimeUtil.getYear());

                if (!boarUser.boarQuery().hasBoarWithTag(connection, obtainType)) {
                    boarIDs.add("egg");
                    boarUser.powQuery().addPowerup(connection, "transmute", 1);
                    this.embedImageGenerator.setStr(STRS.getEasterGuessReplyStr());
                }
            }

            if (!boarIDs.isEmpty()) {
                this.embedImageGenerator.setColor(COLORS.get("font"));

                boarUser.boarQuery().addBoars(boarIDs, connection, obtainType, bucksGotten, editions, firstBoarIDs);

                CompletableFuture.runAsync(() -> {
                    try {
                        String title = this.isTrophyGuess
                            ? STRS.getTrophyTitle()
                            : STRS.getSpookTitle();

                        InteractionUtil.runWhenEdited(
                            this.interaction,
                            () -> ItemInteractive.sendInteractive(
                                boarIDs,
                                bucksGotten,
                                editions,
                                firstBoarIDs,
                                null,
                                this.user,
                                title,
                                this.interaction.getHook(),
                                true
                            ),
                            15000
                        );

                        Log.debug(this.user, this.getClass(), "Sent ItemInteractive");
                    } catch (RuntimeException exception) {
                        SpecialReply.sendErrorMessage(this.interaction.getHook(), this);
                        Log.error(
                            this.user,
                            this.getClass(),
                            "A problem occurred when sending clone item interactive",
                            exception
                        );
                    }
                });
            }
        } catch (SQLException | IOException exception) {
            this.failedSynchronized = true;
            SpecialReply.sendErrorMessage(this.interaction, this);
            Log.error(this.user, this.getClass(), "Failed to give hunt rewards", exception);
        }
    }

    private static boolean isTrophyAvailable() {
        return STRS.getTrophyGuessStr() != null;
    }

    private static String getFixedInput(String input) {
        return input.replaceAll("[\\s`]+", "").toLowerCase();
    }
}
