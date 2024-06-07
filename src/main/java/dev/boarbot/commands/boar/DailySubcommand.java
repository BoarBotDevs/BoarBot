package dev.boarbot.commands.boar;

import dev.boarbot.bot.config.StringConfig;
import dev.boarbot.commands.Subcommand;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.BoarUserAction;
import dev.boarbot.entities.boaruser.BoarUserFactory;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.interactives.InteractiveFactory;
import dev.boarbot.interactives.boar.daily.DailyNotifyInteractive;
import dev.boarbot.util.boar.BoarObtainType;
import dev.boarbot.util.boar.BoarUtil;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.generators.EmbedGenerator;
import dev.boarbot.util.generators.ItemImageGenerator;
import dev.boarbot.util.generators.ItemImageGrouper;
import dev.boarbot.util.time.TimeUtil;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Log4j2
public class DailySubcommand extends Subcommand {
    private List<String> boarIDs = new ArrayList<>();
    private final List<Integer> bucksGotten = new ArrayList<>();
    private final List<Integer> boarEditions = new ArrayList<>();

    private boolean notificationsOn = false;
    private boolean isFirstDaily = false;

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

        if (this.boarIDs.isEmpty()) {
            String dailyResetDistance = TimeUtil.getTimeDistance(TimeUtil.getNextDailyResetMilli());
            dailyResetDistance = dailyResetDistance.substring(dailyResetDistance.indexOf(' ')+1);

            String replyStr = this.config.getStringConfig().getDailyUsed().formatted(dailyResetDistance);

            MessageEditBuilder editedMsg = new MessageEditBuilder();

            if (!this.notificationsOn) {
                Interactive interactive = InteractiveFactory.constructInteractive(
                    this.event, DailyNotifyInteractive.class
                );
                editedMsg.setComponents(interactive.getCurComponents());

                replyStr += " " + this.config.getStringConfig().getDailyUsedNotify();
            }

            try {
                EmbedGenerator embedGen = new EmbedGenerator(replyStr);
                editedMsg.setFiles(embedGen.generate());

                this.interaction.getHook().editOriginal(editedMsg.build()).queue();
            } catch (IOException exception) {
                log.error("Failed to generate next daily reset image.", exception);
            }

            return;
        }

        this.sendResponse();
    }

    public void doDaily(BoarUser boarUser) {
        try (Connection connection = DataUtil.getConnection()) {
            if (!this.config.isUnlimitedBoars() && !boarUser.canUseDaily(connection)) {
                this.notificationsOn = boarUser.getNotificationStatus(connection);
                return;
            }

            this.isFirstDaily = boarUser.isFirstDaily();

            long multiplier = boarUser.getMultiplier(connection);
            this.boarIDs = BoarUtil.getRandBoarIDs(multiplier, this.interaction.getGuild().getId(), connection);

            boarUser.addBoars(this.boarIDs, connection, BoarObtainType.DAILY, this.bucksGotten, this.boarEditions);
        } catch (SQLException exception) {
            log.error("Failed to add boar to database for user (%s)!".formatted(this.user.getName()), exception);
        }
    }

    private void sendResponse() {
        StringConfig strConfig = this.config.getStringConfig();

        List<ItemImageGenerator> itemGens = new ArrayList<>();

        for (int i=0; i<this.boarIDs.size(); i++) {
            String title = strConfig.getDailyTitle();

            if (this.boarIDs.get(i).equals(strConfig.getFirstBoarID())) {
                title = strConfig.getFirstTitle();
            }

            ItemImageGenerator boarItemGen = new ItemImageGenerator(
                this.event.getUser(), title, this.boarIDs.get(i), false, null, this.bucksGotten.get(i)
            );

            itemGens.add(boarItemGen);
        }

        try (FileUpload imageToSend = ItemImageGrouper.groupItems(itemGens, 0)) {
            if (itemGens.size() > 1) {
                Interactive interactive = InteractiveFactory.constructDailyInteractive(
                    this.event, itemGens, this.boarIDs, this.boarEditions
                );

                MessageEditBuilder editedMsg = new MessageEditBuilder()
                    .setFiles(imageToSend)
                    .setComponents(interactive.getCurComponents());

                if (interactive.isStopped()) {
                    return;
                }

                this.interaction.getHook().editOriginal(editedMsg.build()).complete();
            } else {
                this.interaction.getHook().editOriginalAttachments(imageToSend).complete();
            }
        } catch (Exception exception) {
            log.error("Failed to send daily boar response!", exception);
        }

        if (this.isFirstDaily) {
            try {
                EmbedGenerator embedGen = new EmbedGenerator(this.config.getStringConfig().getDailyFirstTime());

                this.interaction.getHook().sendFiles(embedGen.generate()).complete();
            } catch (IOException exception) {
                log.error("Failed to generate first daily reward image.", exception);
            }
        }
    }
}
