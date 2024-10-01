package dev.boarbot.interactives.boar.help;

import dev.boarbot.bot.config.components.IndivComponentConfig;
import dev.boarbot.bot.config.components.SelectOptionConfig;
import dev.boarbot.bot.config.modals.ModalConfig;
import dev.boarbot.interactives.ModalInteractive;
import dev.boarbot.modals.ModalHandler;
import dev.boarbot.util.generators.HelpImageGenerator;
import dev.boarbot.util.interactive.InteractiveUtil;
import dev.boarbot.util.interactive.StopType;
import dev.boarbot.util.logging.ExceptionHandler;
import dev.boarbot.util.logging.Log;
import dev.boarbot.util.modal.ModalUtil;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.internal.interactions.component.StringSelectMenuImpl;
import net.dv8tion.jda.internal.interactions.modal.ModalImpl;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

public class HelpInteractive extends ModalInteractive {
    private HelpView curView;
    private int page = 0;
    private int maxPage = 0;

    private final static Map<String, IndivComponentConfig> COMPONENTS = CONFIG.getComponentConfig().getHelp();
    private final static Map<String, ModalConfig> MODALS = CONFIG.getModalConfig();
    private List<SelectOption> navOptions = new ArrayList<>();

    public HelpInteractive(SlashCommandInteractionEvent event) {
        super(event);

        this.curView = event.getOption("menu") != null
            ? HelpView.fromString(Objects.requireNonNull(event.getOption("menu")).getAsString())
            : HelpView.GENERAL;
        this.setPage(
            event.getOption("page") != null
                ? Objects.requireNonNull(event.getOption("page")).getAsInt() - 1
                : 0
        );

        this.makeSelectOptions(COMPONENTS.get("menuSelect"));
    }

    @Override
    public void execute(GenericComponentInteractionCreateEvent compEvent) {
        if (compEvent == null) {
            this.sendResponse();
            return;
        }

        if (this.getModalHandler() != null) {
            this.getModalHandler().stop();
        }

        String compID = compEvent.getComponentId().split(",")[1];

        if (!compID.equals("PAGE")) {
            compEvent.deferEdit().queue(null, e -> ExceptionHandler.deferHandle(compEvent, this, e));
        }

        Log.debug(
            this.user,
            this.getClass(),
            "Page: %d | Menu: %s | Component: %s".formatted(this.page, this.curView.toString(), compID)
        );

        switch (compID) {
            case "MENU_SELECT" -> {
                this.curView = HelpView.fromString(((StringSelectInteractionEvent) compEvent).getValues().getFirst());
                this.setPage(0);
                Log.debug(this.user, this.getClass(), "Selected menu: " + this.curView);
            }

            case "LEFT" -> this.setPage(this.page - 1);

            case "PAGE" -> {
                Modal modal = new ModalImpl(
                    ModalUtil.makeModalID(MODALS.get("pageInput").getId(), compEvent),
                    MODALS.get("pageInput").getTitle(),
                    ModalUtil.makeModalComponents(MODALS.get("pageInput").getComponents())
                );

                this.setModalHandler(new ModalHandler(compEvent, this));
                compEvent.replyModal(modal).queue(null, e -> ExceptionHandler.replyHandle(compEvent, this, e));

                Log.debug(this.user, this.getClass(), "Sent page input modal");
            }

            case "RIGHT" -> this.setPage(this.page + 1);
        }

        this.sendResponse();
    }

    @Override
    public void modalExecute(ModalInteractionEvent modalEvent) {
        modalEvent.deferEdit().queue(null, e -> ExceptionHandler.deferHandle(modalEvent, this, e));

        Log.debug(
            this.user, this.getClass(), "Page input: " + modalEvent.getValues().getFirst().getAsString()
        );

        try {
            String pageInput = modalEvent.getValues().getFirst().getAsString().replaceAll("[^0-9]+", "");
            this.setPage(Integer.parseInt(pageInput)-1);
        } catch (NumberFormatException exception) {
            Log.debug(this.user, this.getClass(), "Invalid modal input");
        }

        this.execute(null);
    }

    private void sendResponse() {
        try {
            FileUpload fileUpload = new HelpImageGenerator(this.curView, this.page, this.maxPage).generate()
                .getFileUpload();
            MessageEditBuilder editedMsg = new MessageEditBuilder()
                .setFiles(fileUpload).setComponents(this.getCurComponents());

            this.updateInteractive(false, editedMsg.build());
        } catch (IOException | URISyntaxException exception) {
            this.stop(StopType.EXCEPTION);
            Log.error(this.user, this.getClass(), "Failed to generate help image", exception);
        }
    }

    private int getMaxPage() {
        return switch (this.curView) {
            case GENERAL, BOARS -> 1;
            case BADGES -> 0;
            case POWERUPS -> 2;
        };
    }

    private void setPage(int page) {
        this.maxPage = getMaxPage();
        this.page = Math.max(Math.min(page, this.maxPage), 0);
    }

    @Override
    public ActionRow[] getCurComponents() {
        List<ItemComponent> viewSelect = InteractiveUtil.makeComponents(
            this.getInteractionID(), COMPONENTS.get("menuSelect")
        );

        List<ItemComponent> navBtns = InteractiveUtil.makeComponents(
            this.getInteractionID(), COMPONENTS.get("leftBtn"), COMPONENTS.get("pageBtn"), COMPONENTS.get("rightBtn")
        );

        for (int i=0; i<this.navOptions.size(); i++) {
            SelectOption navOption = this.navOptions.get(i);

            if (navOption.getValue().equals(this.curView.toString())) {
                this.navOptions.set(i, navOption.withDefault(true));
                continue;
            }

            this.navOptions.set(i, navOption.withDefault(false));
        }

        Button leftBtn = ((Button) navBtns.getFirst()).asDisabled();
        Button pageBtn = ((Button) navBtns.get(1)).asDisabled();
        Button rightBtn = ((Button) navBtns.get(2)).asDisabled();

        if (this.page > 0) {
            leftBtn = leftBtn.withDisabled(false);
        }

        if (this.maxPage > 0) {
            pageBtn = pageBtn.withDisabled(false);
        }

        if (this.page < this.maxPage) {
            rightBtn = rightBtn.withDisabled(false);
        }

        navBtns.set(0, leftBtn);
        navBtns.set(1, pageBtn);
        navBtns.set(2, rightBtn);

        StringSelectMenu viewSelectMenu = (StringSelectMenu) viewSelect.getFirst();
        viewSelect.set(0, new StringSelectMenuImpl(
            viewSelectMenu.getId(),
            viewSelectMenu.getPlaceholder(),
            viewSelectMenu.getMinValues(),
            viewSelectMenu.getMaxValues(),
            viewSelectMenu.isDisabled(),
            this.navOptions
        ));

        return new ActionRow[] {
            ActionRow.of(viewSelect), ActionRow.of(navBtns)
        };
    }

    private void makeSelectOptions(IndivComponentConfig selectMenu) {
        List<SelectOption> options = new ArrayList<>();

        for (SelectOptionConfig selectOption : selectMenu.getOptions()) {
            SelectOption option = SelectOption.of(selectOption.getLabel(), selectOption.getValue())
                .withEmoji(InteractiveUtil.parseEmoji(selectOption.getEmoji()))
                .withDescription(selectOption.getDescription());
            options.add(option);
        }

        this.navOptions = options;
    }
}
