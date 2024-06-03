package dev.boarbot.commands.boar;

import dev.boarbot.commands.Subcommand;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.BoarUserAction;
import dev.boarbot.entities.boaruser.BoarUserFactory;
import dev.boarbot.util.boar.BoarObtainType;
import dev.boarbot.util.boar.BoarUtil;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.generators.ItemImageGenerator;
import dev.boarbot.util.time.TimeUtil;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Log4j2
public class DailySubcommand extends Subcommand {
    private List<String> boarIDs = new ArrayList<>();
    private final List<Integer> bucksGotten = new ArrayList<>();
    private final List<Boolean> bacteriaGotten = new ArrayList<>();

    public DailySubcommand(SlashCommandInteractionEvent event) {
        super(event);
    }

    @Override
    public void execute() throws InterruptedException {
        if (!this.canInteract()) {
            return;
        }

        this.interaction.deferReply().queue();

        BoarUser boarUser = BoarUserFactory.getBoarUser(this.user);
        boarUser.doSynchronizedAction(BoarUserAction.DAILY, this);
        boarUser.decRefs();

        if (this.bucksGotten.isEmpty()) {
            long nextDailyResetSecs = TimeUtil.getNextDailyResetMilli() / 1000;
            this.interaction.getHook().editOriginal("Daily next available <t:%d:R>".formatted(nextDailyResetSecs)).queue();
            return;
        }

        this.sendResponse();
    }

    public void doDaily(BoarUser boarUser) {
        try (Connection connection = DataUtil.getConnection()) {
            if (!this.config.isUnlimitedBoars() && !boarUser.canUseDaily(connection)) {
                return;
            }

            long multiplier = boarUser.getMultiplier(connection);
            this.boarIDs = BoarUtil.getRandBoarIDs(multiplier, this.interaction.getGuild().getId(), connection);

            boarUser.addBoars(this.boarIDs, connection, BoarObtainType.DAILY, this.bucksGotten, this.bacteriaGotten);
        } catch (SQLException exception) {
            log.error("Failed to add boar to database for user (%s)!".formatted(this.user.getName()), exception);
        }
    }

    private void sendResponse() {
        for (int i=0; i<this.boarIDs.size(); i++) {
            boolean isFirst = i == 0;
            String title = isFirst ? "Daily Boar!" : "Extra Boar!";

            ItemImageGenerator boarItemGen = new ItemImageGenerator(
                this.event.getUser(), title, this.boarIDs.get(i), false, null, this.bucksGotten.get(i)
            );

            try {
                Message boarMessage;

                if (isFirst) {
                    boarMessage = this.interaction.getHook().editOriginalAttachments(boarItemGen.generate()).complete();
                } else {
                    boarMessage = this.interaction.getHook().sendFiles(boarItemGen.generate()).complete();
                }

                if (this.bacteriaGotten.get(i)) {
                    ItemImageGenerator bacteriaItemGen = new ItemImageGenerator(
                        this.event.getUser(), "First Edition!", "bacteria"
                    );

                    boarMessage.replyFiles(bacteriaItemGen.generate()).queue();
                }
            } catch (Exception exception) {
                log.error("Failed to create file from image data!", exception);
                return;
            }
        }
    }
}
