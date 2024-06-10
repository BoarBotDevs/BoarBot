package dev.boarbot.interactives.boar.daily;

import dev.boarbot.bot.config.components.IndivComponentConfig;
import dev.boarbot.entities.boaruser.BoarUser;
import dev.boarbot.entities.boaruser.BoarUserFactory;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.generators.EmbedGenerator;
import dev.boarbot.util.interactive.InteractiveUtil;
import dev.boarbot.util.interactive.StopType;
import dev.boarbot.util.time.TimeUtil;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Log4j2
public class DailyNotifyInteractive extends Interactive {
    private ActionRow[] curComponents = new ActionRow[0];

    private final Map<String, IndivComponentConfig> COMPONENTS = this.config.getComponentConfig().getDaily();

    public DailyNotifyInteractive(SlashCommandInteractionEvent initEvent) {
        super(initEvent);
    }

    @Override
    public void execute(GenericComponentInteractionCreateEvent compEvent) {
        compEvent.deferEdit().queue();

        if (!this.initEvent.getUser().getId().equals(compEvent.getUser().getId())) {
            return;
        }

        String dailyResetDistance = TimeUtil.getTimeDistance(TimeUtil.getNextDailyResetMilli());
        dailyResetDistance = dailyResetDistance.substring(dailyResetDistance.indexOf(' ')+1);
        String replyStr = this.config.getStringConfig().getDailyUsed().formatted(dailyResetDistance);

        try {
            EmbedGenerator embedGen = new EmbedGenerator(replyStr);

            MessageEditBuilder editedMsg = new MessageEditBuilder()
                .setFiles(embedGen.generate())
                .setComponents();

            compEvent.getUser().openPrivateChannel().complete().sendMessage(
                this.config.getStringConfig().getNotificationSuccess()
            ).complete();

            if (!this.isStopped) {
                this.interaction.getHook().editOriginal(editedMsg.build()).queue();
            }

            try (Connection connection = DataUtil.getConnection()) {
                BoarUser boarUser = BoarUserFactory.getBoarUser(compEvent.getUser());
                boarUser.setNotifications(connection, compEvent.getChannelId());
                boarUser.decRefs();
            }
        } catch (IOException exception) {
            log.error(
                "An error occurred while attempting to update message after enabling notifications.", exception
            );
        } catch (ErrorResponseException exception) {
            EmbedGenerator embedGen = new EmbedGenerator(
                this.config.getStringConfig().getNotificationFailed(), this.config.getColorConfig().get("error")
            );

            try {
                this.interaction.getHook().sendFiles(embedGen.generate()).setEphemeral(true).queue();
            } catch (IOException exception1) {
                log.error(
                    "An error occurred while attempting to update message after enabling notifications.", exception1
                );
            }
        } catch (SQLException exception) {
            log.error("An error occurred while attempting to enable notifications.", exception);
        }
    }

    @Override
    public void stop(StopType type) {
        Interactive interactive = this.removeInteractive();
        this.isStopped = true;

        if (interactive == null) {
            return;
        }

        this.interaction.getHook().deleteOriginal().queue();
    }

    @Override
    public ActionRow[] getCurComponents() {
        if (this.curComponents.length == 0) {
            this.curComponents = this.getComponents();
        }

        return this.curComponents;
    }

    private ActionRow[] getComponents() {
        List<ItemComponent> notifyBtn = InteractiveUtil.makeComponents(
            this.interaction.getId(),
            this.COMPONENTS.get("notifyBtn")
        );

        return new ActionRow[] {
            ActionRow.of(notifyBtn)
        };
    }
}
