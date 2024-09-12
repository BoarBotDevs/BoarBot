package dev.boarbot.interactives.event;

import dev.boarbot.bot.config.prompts.IndivPromptConfig;
import dev.boarbot.events.PowerupEventHandler;
import dev.boarbot.events.PromptType;
import dev.boarbot.util.interactive.InteractiveUtil;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.internal.interactions.component.ButtonImpl;

import java.util.ArrayList;
import java.util.List;

public class PowerupEventInteractive extends EventInteractive {
    private final IndivPromptConfig promptConfig;

    private final PowerupEventHandler eventHandler;

    @Setter private FileUpload eventImage;

    private final static String CORRECT_ID = "y";
    private final static String INCORRECT_ID = "n";
    private final static int BASE_SIDE_LENGTH = 4;

    public PowerupEventInteractive(TextChannel channel, FileUpload eventImage, PowerupEventHandler eventHandler) {
        super(channel);
        this.eventImage = eventImage;
        this.eventHandler = eventHandler;
        this.promptConfig = CONFIG.getPromptConfig().get(this.eventHandler.getPromptType().toString())
            .getPrompts().get(this.eventHandler.getPromptID());
    }

    @Override
    public void execute(GenericComponentInteractionCreateEvent compEvent) {
        if (compEvent == null) {
            this.sendResponse();
            return;
        }
    }

    private void sendResponse() {
        MessageEditBuilder messageBuilder = new MessageEditBuilder().setContent("test").setFiles(this.eventImage)
            .setComponents(this.getCurComponents());
        Message interactiveMessage = this.updateInteractive(messageBuilder.build());
        PowerupEventHandler.getCurMessages().add(interactiveMessage);
    }

    @Override
    public ActionRow[] getCurComponents() {
        List<ActionRow> rows = new ArrayList<>();

        switch (this.eventHandler.getPromptType()) {
            case PromptType.EMOJI_FIND -> this.makeFindEmoji(rows);
            case PromptType.TRIVIA -> this.makeTrivia(rows);
            case PromptType.FAST -> this.makeFast(rows);
            case PromptType.CLOCK -> {}
        }

        return rows.toArray(new ActionRow[0]);
    }

    private void makeFindEmoji(List<ActionRow> rows) {
        int correctIndex = (int) (Math.random() * BASE_SIDE_LENGTH * BASE_SIDE_LENGTH);

        Emoji correctEmoji = InteractiveUtil.parseEmoji(promptConfig.getEmoji1());
        Emoji incorrectEmoji = InteractiveUtil.parseEmoji(promptConfig.getEmoji2());

        for (int i=0; i<BASE_SIDE_LENGTH; i++) {
            List<ItemComponent> rowComponents = new ArrayList<>();

            for (int j=0; j<BASE_SIDE_LENGTH; j++) {
                int curIndex = i*BASE_SIDE_LENGTH+j;

                if (curIndex == correctIndex) {
                    rowComponents.add(new ButtonImpl(CORRECT_ID, null, ButtonStyle.PRIMARY, false, correctEmoji));
                    continue;
                }

                rowComponents.add(new ButtonImpl(
                    INCORRECT_ID + curIndex, null, ButtonStyle.PRIMARY, false, incorrectEmoji
                ));
            }

            rows.add(ActionRow.of(rowComponents));
        }
    }

    private void makeTrivia(List<ActionRow> rows) {
        final int TRIVIA_SIDE_LENGTH = 2;

        int correctIndex = (int) (Math.random() * TRIVIA_SIDE_LENGTH * TRIVIA_SIDE_LENGTH);

        String[] choices = promptConfig.getChoices();
        String correctLabel = choices[0];
        String[] incorrectLabels = {choices[1], choices[2], choices[3]};

        int incorrectIndex = 0;
        for (int i=0; i<TRIVIA_SIDE_LENGTH; i++) {
            List<ItemComponent> rowComponents = new ArrayList<>();

            for (int j=0; j<TRIVIA_SIDE_LENGTH; j++) {
                int curIndex = i*TRIVIA_SIDE_LENGTH+j;

                if (curIndex == correctIndex) {
                    rowComponents.add(new ButtonImpl(CORRECT_ID, correctLabel, ButtonStyle.PRIMARY, false, null));
                    continue;
                }

                rowComponents.add(new ButtonImpl(
                    INCORRECT_ID + curIndex, incorrectLabels[incorrectIndex], ButtonStyle.PRIMARY, false, null
                ));
                incorrectIndex++;
            }

            rows.add(ActionRow.of(rowComponents));
        }
    }

    private void makeFast(List<ActionRow> rows) {
        final int FAST_MAX_LENGTH = 3;

        int correctIndex = (int) (Math.random() * promptConfig.getNumButtons());
        String label = STRS.getEmpty();

        for (int i=0; i<FAST_MAX_LENGTH; i++) {
            List<ItemComponent> rowComponents = new ArrayList<>();

            if (i*FAST_MAX_LENGTH >= promptConfig.getNumButtons()) {
                break;
            }

            for (int j=0; j<FAST_MAX_LENGTH; j++) {
                int curIndex = i*FAST_MAX_LENGTH+j;

                if (curIndex >= promptConfig.getNumButtons()) {
                    break;
                }

                if (curIndex == correctIndex) {
                    rowComponents.add(new ButtonImpl(CORRECT_ID, label, ButtonStyle.PRIMARY, false, null));
                    continue;
                }

                rowComponents.add(new ButtonImpl(INCORRECT_ID + curIndex,label , ButtonStyle.SECONDARY, false, null));
            }

            rows.add(ActionRow.of(rowComponents));
        }
    }
}
