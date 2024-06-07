package dev.boarbot.interactives.boarmanage;

import dev.boarbot.bot.config.StringConfig;
import dev.boarbot.bot.config.components.IndivComponentConfig;
import dev.boarbot.interactives.Interactive;
import dev.boarbot.util.data.DataUtil;
import dev.boarbot.util.interactive.StopType;
import dev.boarbot.util.generators.EmbedGenerator;
import dev.boarbot.util.interactive.InteractiveUtil;
import lombok.extern.log4j.Log4j2;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log4j2
public class SetupInteractive extends Interactive {
    private int page = 0;
    private final EmbedGenerator embedGen = new EmbedGenerator("");
    private final MessageEditBuilder editedMsg = new MessageEditBuilder();
    private ActionRow[] curComponents = new ActionRow[0];

    private List<GuildChannel> chosenChannels = new ArrayList<>();
    private boolean isSb = false;

    private final Map<String, IndivComponentConfig> COMPONENTS = this.config.getComponentConfig().getSetup();

    public SetupInteractive(SlashCommandInteractionEvent initEvent) {
        super(initEvent);
    }

    @Override
    public void execute(GenericComponentInteractionCreateEvent compEvent) {
        compEvent.deferEdit().queue();

        if (!this.initEvent.getUser().getId().equals(compEvent.getUser().getId())) {
            return;
        }

        String compID = compEvent.getComponentId().split(",")[1];

        try {
            switch (compID) {
                case "CHANNEL_SELECT" -> this.doChannels(compEvent);
                case "SB_YES", "SB_NO" -> this.doSb(compID);
                case "NEXT" -> this.doNext();
                case "INFO" -> this.doInfo();
                case "CANCEL" -> this.stop(StopType.CANCELLED);
            }
        } catch (Exception exception){
            log.error("Failed to create file from image data!", exception);
        }
    }

    public void doChannels(GenericComponentInteractionCreateEvent compEvent) {
        Button nextBtn = ((Button) this.curComponents[1].getComponents().get(2)).withDisabled(false);

        this.chosenChannels = ((EntitySelectInteractionEvent) compEvent).getMentions().getChannels();

        this.curComponents[1].getComponents().set(2, nextBtn);
        this.editedMsg.setComponents(this.curComponents);
        this.interaction.getHook().editOriginal(this.editedMsg.build()).complete();
    }

    public void doSb(String compID) throws IOException {
        StringConfig strConfig = this.config.getStringConfig();
        Map<String, String> colorConfig = this.config.getColorConfig();

        this.isSb = compID.equals("SB_YES");

        Button nextBtn = ((Button) this.curComponents[1].getComponents().get(2)).withDisabled(false);
        this.curComponents[1].getComponents().set(2, nextBtn);

        this.embedGen.setStr(strConfig.getSetupFinished2() + (this.isSb ? "//green//Yes" : "//error//No"));
        this.embedGen.setColor(colorConfig.get("font"));

        this.editedMsg.setFiles(this.embedGen.generate()).setComponents(this.curComponents);
        this.interaction.getHook().editOriginal(this.editedMsg.build()).complete();
    }

    public void doNext() throws IOException {
        StringConfig strConfig = this.config.getStringConfig();
        Map<String, String> colorConfig = this.config.getColorConfig();

        if (this.page == 0) {
            this.page = 1;

            this.getCurComponents();

            Button nextBtn = ((Button) this.curComponents[1].getComponents().get(2)).asDisabled()
                .withStyle(ButtonStyle.SUCCESS)
                .withLabel("Finish");

            this.curComponents[1].getComponents().set(2, nextBtn);

            this.embedGen.setStr(strConfig.getSetupUnfinished2());
            this.embedGen.setColor(colorConfig.get("font"));

            this.editedMsg.setFiles(this.embedGen.generate()).setComponents(this.curComponents);

            if (this.isStopped) {
                return;
            }

            this.interaction.getHook().editOriginal(this.editedMsg.build()).complete();

            return;
        }

        this.stop(StopType.FINISHED);
    }

    public void doInfo() throws IOException {
        StringConfig strConfig = this.config.getStringConfig();
        Map<String, String> colorConfig = this.config.getColorConfig();

        if (this.page == 0) {
            this.embedGen.setStr(strConfig.getSetupInfoResponse1());
        } else {
            this.embedGen.setStr(strConfig.getSetupInfoResponse2());
        }

        this.embedGen.setColor(colorConfig.get("font"));

        MessageCreateBuilder infoMsg = new MessageCreateBuilder();
        infoMsg.setFiles(this.embedGen.generate());
        this.interaction.getHook().sendMessage(infoMsg.build()).setEphemeral(true).complete();
    }

    @Override
    public void stop(StopType type) throws IOException {
        StringConfig strConfig = this.config.getStringConfig();
        Map<String, String> colorConfig = this.config.getColorConfig();
        Interactive interactive = this.removeInteractive();
        this.isStopped = true;

        if (interactive == null) {
            return;
        }

        switch (type) {
            case StopType.CANCELLED -> {
                this.embedGen.setStr(strConfig.getSetupCancelled());
                this.embedGen.setColor(colorConfig.get("error"));
            }

            case StopType.EXPIRED -> {
                this.embedGen.setStr(strConfig.getSetupExpired());
                this.embedGen.setColor(colorConfig.get("error"));
            }

            case StopType.FINISHED -> {
                String query = """
                    REPLACE INTO guilds (guild_id, is_skyblock_community, channel_one, channel_two, channel_three)
                    VALUES (?, ?, ?, ?, ?);
                """;

                try (
                    Connection connection = DataUtil.getConnection();
                    PreparedStatement statement = connection.prepareStatement(query)
                ) {
                    statement.setString(1, this.interaction.getGuild().getId());
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

                this.embedGen.setStr(strConfig.getSetupFinishedAll());
                this.embedGen.setColor(colorConfig.get("green"));
            }
        }

        this.editedMsg.setFiles(this.embedGen.generate()).setComponents();
        this.interaction.getHook().editOriginal(this.editedMsg.build()).complete();
    }

    @Override
    public ActionRow[] getCurComponents() {
        if (this.page == 0) {
            this.curComponents = getFirstComponents();
        } else {
            this.curComponents = getSecondComponents();
        }

        return this.curComponents;
    }

    private ActionRow[] getFirstComponents() {
        ActionRow navRow = this.getNavRow();

        List<ItemComponent> channelSelect1 = InteractiveUtil.makeComponents(
            this.interaction.getId(), this.COMPONENTS.get("channelSelect")
        );

        return new ActionRow[] {
            ActionRow.of(channelSelect1),
            navRow
        };
    }

    private ActionRow[] getSecondComponents() {
        ActionRow navRow = this.getNavRow();

        List<ItemComponent> sbChoice = InteractiveUtil.makeComponents(
            this.interaction.getId(), this.COMPONENTS.get("sbYesBtn"), this.COMPONENTS.get("sbNoBtn")
        );

        return new ActionRow[] {
            ActionRow.of(sbChoice),
            navRow
        };
    }

    private ActionRow getNavRow() {
        List<ItemComponent> components = InteractiveUtil.makeComponents(
            this.interaction.getId(),
            this.COMPONENTS.get("cancelBtn"),
            this.COMPONENTS.get("infoBtn"),
            this.COMPONENTS.get("nextBtn")
        );

        return ActionRow.of(components);
    }
}
