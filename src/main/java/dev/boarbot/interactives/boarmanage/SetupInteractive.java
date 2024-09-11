package dev.boarbot.interactives.boarmanage;

import dev.boarbot.bot.config.components.IndivComponentConfig;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.interactives.UserInteractive;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.interactive.StopType;
import dev.boarbot.util.generators.EmbedImageGenerator;
import dev.boarbot.util.interactive.InteractiveUtil;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class SetupInteractive extends UserInteractive {
    private int page = 0;
    private FileUpload currentImageUpload;

    private boolean selected = false;

    private List<GuildChannel> chosenChannels = new ArrayList<>();
    private boolean isSb = false;

    private final Map<String, IndivComponentConfig> COMPONENTS = config.getComponentConfig().getSetup();

    public SetupInteractive(Interaction initEvent) {
        super(initEvent);
    }

    @Override
    public void execute(GenericComponentInteractionCreateEvent compEvent) {
        if (compEvent != null) {
            compEvent.deferEdit().queue();

            if (!this.user.getId().equals(compEvent.getUser().getId())) {
                return;
            }

            String compID = compEvent.getComponentId().split(",")[1];

            try {
                switch (compID) {
                    case "CHANNEL_SELECT" -> this.doChannels(compEvent);
                    case "SB_YES", "SB_NO" -> this.doSb(compID);
                    case "NEXT" -> this.doNext();
                    case "INFO" -> this.doInfo(compEvent);
                    case "CANCEL" -> this.stop(StopType.CANCELLED);
                }
            } catch (Exception exception){
                log.error("Failed to create file from image data!", exception);
            }
        }

        this.sendResponse();
    }

    private void sendResponse() {
        try {
            if (this.page == 0) {
                this.currentImageUpload = new EmbedImageGenerator(strConfig.getSetupUnfinished1()).generate()
                    .getFileUpload();
            }

            MessageEditBuilder editedMsg = new MessageEditBuilder()
                .setFiles(this.currentImageUpload)
                .setComponents(this.getCurComponents());

            if (this.isStopped) {
                return;
            }

            this.updateInteractive(editedMsg.build());
        } catch (IOException exception) {
            log.error("Failed to generate powerup use image.", exception);
        }
    }

    public void doChannels(GenericComponentInteractionCreateEvent compEvent) {
        this.selected = true;
        this.chosenChannels = ((EntitySelectInteractionEvent) compEvent).getMentions().getChannels();
    }

    public void doSb(String compID) throws IOException {
        this.selected = true;
        this.isSb = compID.equals("SB_YES");

        this.currentImageUpload = new EmbedImageGenerator(
            strConfig.getSetupFinished2() + (this.isSb ? "<>green<>Yes" : "<>error<>No")
        ).generate().getFileUpload();
    }

    public void doNext() throws IOException {
        if (this.page != 0) {
            this.stop(StopType.FINISHED);
            return;
        }

        this.selected = false;
        this.page = 1;

        this.currentImageUpload = new EmbedImageGenerator(strConfig.getSetupUnfinished2()).generate().getFileUpload();
    }

    public void doInfo(GenericComponentInteractionCreateEvent compEvent) throws IOException {
        FileUpload imageUpload;

        if (this.page == 0) {
            imageUpload = new EmbedImageGenerator(
                strConfig.getSetupInfoResponse1()
            ).generate().getFileUpload();
        } else {
            imageUpload = new EmbedImageGenerator(
                strConfig.getSetupInfoResponse2()
            ).generate().getFileUpload();
        }

        MessageCreateBuilder infoMsg = new MessageCreateBuilder();
        infoMsg.setFiles(imageUpload);
        compEvent.getHook().sendMessage(infoMsg.build()).setEphemeral(true).complete();
    }

    @Override
    public void stop(StopType type) throws IOException {
        Map<String, String> colorConfig = config.getColorConfig();
        Interactive interactive = this.removeInteractive();
        this.isStopped = true;

        if (interactive == null) {
            return;
        }

        switch (type) {
            case StopType.CANCELLED -> this.currentImageUpload = new EmbedImageGenerator(
                strConfig.getSetupCancelled(),
                colorConfig.get("error")
            ).generate().getFileUpload();

            case StopType.EXPIRED -> this.currentImageUpload = new EmbedImageGenerator(
                strConfig.getSetupExpired(),
                colorConfig.get("error")
            ).generate().getFileUpload();

            case StopType.FINISHED -> {
                String query = """
                    REPLACE INTO guilds (guild_id, is_skyblock_community, channel_one, channel_two, channel_three)
                    VALUES (?, ?, ?, ?, ?);
                """;

                try (
                    Connection connection = DataUtil.getConnection();
                    PreparedStatement statement = connection.prepareStatement(query)
                ) {
                    statement.setString(1, this.guildID);
                    statement.setBoolean(2, this.isSb);
                    statement.setString(3, this.chosenChannels.get(0).getId());
                    statement.setString(
                        4, this.chosenChannels.size() < 2 ? null : this.chosenChannels.get(1).getId()
                    );
                    statement.setString(
                        5, this.chosenChannels.size() < 3 ? null : this.chosenChannels.get(2).getId()
                    );

                    statement.executeUpdate();
                } catch (SQLException exception) {
                    log.error("Failed to add guild to database!", exception);
                }

                this.currentImageUpload = new EmbedImageGenerator(
                    strConfig.getSetupFinishedAll(),
                    colorConfig.get("green")
                ).generate().getFileUpload();
            }
        }

        MessageEditBuilder editedMsg = new MessageEditBuilder()
            .setFiles(this.currentImageUpload)
            .setComponents();
        this.updateInteractive(editedMsg.build());
    }

    @Override
    public ActionRow[] getCurComponents() {
        if (this.page == 0) {
            return getFirstComponents();
        }

        return getSecondComponents();
    }

    private ActionRow[] getFirstComponents() {
        ActionRow navRow = this.getNavRow();

        List<ItemComponent> channelSelect1 = InteractiveUtil.makeComponents(
            this.interactionID, this.COMPONENTS.get("channelSelect")
        );

        return new ActionRow[] {
            ActionRow.of(channelSelect1),
            navRow
        };
    }

    private ActionRow[] getSecondComponents() {
        ActionRow navRow = this.getNavRow();

        List<ItemComponent> sbChoice = InteractiveUtil.makeComponents(
            this.interactionID, this.COMPONENTS.get("sbYesBtn"), this.COMPONENTS.get("sbNoBtn")
        );

        return new ActionRow[] {
            ActionRow.of(sbChoice),
            navRow
        };
    }

    private ActionRow getNavRow() {
        List<ItemComponent> components = InteractiveUtil.makeComponents(
            this.interactionID,
            this.COMPONENTS.get("cancelBtn"),
            this.COMPONENTS.get("infoBtn"),
            this.COMPONENTS.get("nextBtn")
        );

        if (this.selected) {
            Button nextBtn = ((Button) components.get(2)).withDisabled(false);
            components.set(2, nextBtn);
        }

        if (this.page == 1) {
            Button nextBtn = ((Button) components.get(2))
                .withStyle(ButtonStyle.SUCCESS)
                .withLabel("Finish");
            components.set(2, nextBtn);
        }

        return ActionRow.of(components);
    }
}
