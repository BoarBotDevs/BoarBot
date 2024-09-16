package dev.boarbot.interactives.boar.daily;

import dev.boarbot.bot.config.components.IndivComponentConfig;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.BoarUserFactory;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.interactives.UserInteractive;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.generators.EmbedImageGenerator;
import dev.boarbot.util.interactive.InteractiveUtil;
import dev.boarbot.util.interactive.StopType;
import dev.boarbot.util.logging.Log;
import dev.boarbot.util.time.TimeUtil;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class DailyNotifyInteractive extends UserInteractive {
    private boolean hasButton = true;

    private final Map<String, IndivComponentConfig> COMPONENTS = CONFIG.getComponentConfig().getDaily();

    public DailyNotifyInteractive(Interaction initEvent) {
        super(initEvent);
    }

    @Override
    public void execute(GenericComponentInteractionCreateEvent compEvent) {
        if (compEvent == null) {
            this.sendResponse();
            return;
        }

        Log.debug(this.user, this.getClass(), "Attempting to enable notifications");
        compEvent.deferEdit().queue();

        this.hasButton = true;

        try {
            compEvent.getUser().openPrivateChannel().complete().sendMessage(STRS.getNotificationSuccess()).complete();
        } catch (ErrorResponseException exception) {
            EmbedImageGenerator embedGen = new EmbedImageGenerator(STRS.getNotificationFailed());

            try {
                MessageCreateBuilder msg = new MessageCreateBuilder().setFiles(embedGen.generate().getFileUpload());
                compEvent.getHook().sendMessage(msg.build()).setEphemeral(true).complete();
            } catch (IOException exception1) {
                Log.error(this.user, this.getClass(), "Failed to generate notification fail message", exception1);
            }

            return;
        }

        try (Connection connection = DataUtil.getConnection()) {
            BoarUser boarUser = BoarUserFactory.getBoarUser(compEvent.getUser());
            boarUser.baseQuery().setNotifications(connection, compEvent.getChannelId());
            boarUser.decRefs();
        } catch (SQLException exception) {
            Log.error(this.user, this.getClass(), "Failed to enable notifications", exception);
            return;
        }

        this.sendResponse();
    }

    private void sendResponse() {
        String dailyResetDistance = TimeUtil.getTimeDistance(TimeUtil.getNextDailyResetMilli(), false);
        dailyResetDistance = dailyResetDistance.substring(dailyResetDistance.indexOf(' ')+1);
        String replyStr = STRS.getDailyUsed().formatted(dailyResetDistance);

        if (this.hasButton) {
            replyStr += " " + STRS.getDailyUsedNotify();
        }

        try {
            FileUpload fileUpload = new EmbedImageGenerator(replyStr).generate().getFileUpload();
            MessageEditBuilder editedMsg = new MessageEditBuilder().setFiles(fileUpload).setComponents();

            if (this.hasButton) {
                editedMsg.setComponents(this.getCurComponents());
            }

            if (this.isStopped) {
                return;
            }

            this.updateInteractive(editedMsg.build());
        } catch (IOException exception) {
            Log.error(this.user, this.getClass(), "Failed to generate daily used message", exception);
        }
    }

    @Override
    public void stop(StopType type) {
        Interactive interactive = this.removeInteractive();
        this.isStopped = true;

        if (interactive == null) {
            return;
        }

        this.deleteInteractive();
    }

    @Override
    public ActionRow[] getCurComponents() {
        List<ItemComponent> notifyBtn = InteractiveUtil.makeComponents(
            this.interactionID, this.COMPONENTS.get("notifyBtn")
        );

        return new ActionRow[] {ActionRow.of(notifyBtn)};
    }
}
