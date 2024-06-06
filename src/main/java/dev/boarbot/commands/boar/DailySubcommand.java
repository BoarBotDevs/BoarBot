package dev.boarbot.commands.boar;

import dev.boarbot.commands.Subcommand;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.BoarUserAction;
import dev.boarbot.entities.boaruser.BoarUserFactory;
import dev.boarbot.interactives.boar.DailyInteractive;
import dev.boarbot.util.boar.BoarObtainType;
import dev.boarbot.util.boar.BoarUtil;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.generators.ItemImageGenerator;
import dev.boarbot.util.generators.ItemImageGrouper;
import dev.boarbot.util.time.TimeUtil;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Log4j2
public class DailySubcommand extends Subcommand {
    private List<String> boarIDs = new ArrayList<>();
    private final List<Integer> bucksGotten = new ArrayList<>();
    private final List<Integer> boarEditions = new ArrayList<>();

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

            boarUser.addBoars(this.boarIDs, connection, BoarObtainType.DAILY, this.bucksGotten, this.boarEditions);
        } catch (SQLException exception) {
            log.error("Failed to add boar to database for user (%s)!".formatted(this.user.getName()), exception);
        }
    }

    private void sendResponse() {
        List<ItemImageGenerator> itemGens = new ArrayList<>();

        for (int i=0; i<this.boarIDs.size(); i++) {
            boolean isFirst = i == 0;
            String title = "Extra Boar!";

            if (this.boarIDs.get(i).equals("bacteria")) {
                title = "First Edition!";
            } else if (isFirst) {
                title = "Daily Boar!";
            }

            ItemImageGenerator boarItemGen = new ItemImageGenerator(
                this.event.getUser(), title, this.boarIDs.get(i), false, null, this.bucksGotten.get(i)
            );

            itemGens.add(boarItemGen);
        }

        try (FileUpload imageToSend = ItemImageGrouper.groupItems(itemGens, 0)) {
            if (itemGens.size() > 1) {
                DailyInteractive interactive = new DailyInteractive(
                    this.event, itemGens, this.boarIDs, this.boarEditions
                );

                MessageEditBuilder editedMsg = new MessageEditBuilder()
                    .setFiles(imageToSend)
                    .setComponents(interactive.getCurComponents());

                this.interaction.getHook().editOriginal(editedMsg.build()).complete();

                return;
            }

            this.interaction.getHook().sendFiles(imageToSend).complete();
        } catch (Exception exception) {
            log.error("Failed to send daily boar response!", exception);
        }
    }
}
