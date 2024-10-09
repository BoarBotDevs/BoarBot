package dev.boarbot.interactives.boar;

import dev.boarbot.api.util.Configured;
import dev.boarbot.bot.config.components.IndivComponentConfig;
import dev.boarbot.bot.config.components.SelectOptionConfig;
import dev.boarbot.bot.config.modals.ModalConfig;
import dev.boarbot.interactives.ModalInteractive;
import dev.boarbot.modals.ModalHandler;
import dev.boarbot.util.data.top.TopData;
import dev.boarbot.util.data.top.TopType;
import dev.boarbot.util.generators.TopImageGenerator;
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

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TopInteractive extends ModalInteractive implements Configured {
    private static final int ENTRIES_PER_PAGE = 40;

    private int page;
    private String username;
    private Integer usernameIndex;
    private int maxPage;
    private TopType boardType;

    public final static Map<TopType, Map<String, TopData>> cachedBoards = new ConcurrentHashMap<>();
    public final static Map<TopType, List<String>> indexedBoards = new ConcurrentHashMap<>();

    private final static Map<String, IndivComponentConfig> COMPONENTS = CONFIG.getComponentConfig().getLeaderboard();
    private final static Map<String, ModalConfig> MODALS = CONFIG.getModalConfig();
    private List<SelectOption> navOptions = new ArrayList<>();

    public TopInteractive(SlashCommandInteractionEvent event) {
        super(event);

        this.boardType = event.getOption("board") != null
            ? TopType.fromString(Objects.requireNonNull(event.getOption("board")).getAsString())
            : TopType.TOTAL_BUCKS;

        this.setPage(0);

        if (event.getOption("user") != null) {
            String usernameInput = Objects.requireNonNull(event.getOption("user")).getAsUser().getName();

            boolean boardHasUser = cachedBoards.get(this.boardType).containsKey(usernameInput);
            if (boardHasUser) {
                this.username = usernameInput;
                this.usernameIndex = cachedBoards.get(this.boardType).get(usernameInput).index();
                this.setPage(this.usernameIndex / ENTRIES_PER_PAGE);
            }
        } else if (event.getOption("page") != null) {
            this.setPage(Objects.requireNonNull(event.getOption("page")).getAsInt() - 1);
        }

        if (this.usernameIndex == null && cachedBoards.get(this.boardType).containsKey(this.user.getName())) {
            this.username = this.user.getName();
            this.usernameIndex = cachedBoards.get(this.boardType).get(this.user.getName()).index();
        }

        this.makeSelectOptions(COMPONENTS.get("boardSelect"));
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
            "Page: %d | Board: %s | Component: %s".formatted(this.page, this.boardType, compID)
        );

        switch (compID) {
            case "BOARD_SELECT" -> {
                this.page = 0;
                this.boardType = TopType.fromString(((StringSelectInteractionEvent) compEvent).getValues().getFirst());
                Log.debug(this.user, this.getClass(), "Selected board: " + this.boardType);
            }

            case "LEFT" -> this.setPage(this.page - 1);

            case "PAGE" -> {
                Modal modal = ModalUtil.getModal(MODALS.get("pageBoardInput"), compEvent);
                this.setModalHandler(new ModalHandler(compEvent, this, NUMS.getInteractiveIdle()));
                compEvent.replyModal(modal).queue(null, e -> ExceptionHandler.replyHandle(compEvent, this, e));
                Log.debug(this.user, this.getClass(), "Sent page/username input modal");
            }

            case "RIGHT" -> this.setPage(this.page + 1);
        }

        if (cachedBoards.get(this.boardType).containsKey(this.username)) {
            this.usernameIndex = cachedBoards.get(this.boardType).get(this.username).index();
        }

        this.sendResponse();
    }

    private void setMaxPage() {
        this.maxPage = Math.max((indexedBoards.get(this.boardType).size()-1) / ENTRIES_PER_PAGE, 0);
    }

    private void setPage(int page) {
        this.setMaxPage();
        this.page = Math.min(Math.max(page, 0), this.maxPage);
    }

    @Override
    public void modalExecute(ModalInteractionEvent modalEvent) {
        modalEvent.deferEdit().queue(null, e -> ExceptionHandler.deferHandle(modalEvent, this, e));

        String pageInput = modalEvent.getValues().getFirst().getAsString().replaceAll("[^0-9]+", "");
        String usernameInput = modalEvent.getValues().get(1).getAsString();

        Log.debug(
            this.user, this.getClass(), "Page input: %s | Username input: %s".formatted(pageInput, usernameInput)
        );

        try {
            boolean boardHasUser = cachedBoards.get(this.boardType).containsKey(usernameInput);
            if (boardHasUser) {
                this.username = usernameInput;
                this.usernameIndex = cachedBoards.get(this.boardType).get(usernameInput).index();
                this.setPage(this.usernameIndex / ENTRIES_PER_PAGE);

                this.execute(null);
                return;
            }

            this.setPage(Integer.parseInt(pageInput)-1);
        } catch (NumberFormatException exception) {
            Log.debug(this.user, this.getClass(), "Invalid modal input");
        }

        this.setPage(this.page);
        this.execute(null);
    }

    private void sendResponse() {
        try {
            FileUpload fileUpload = new TopImageGenerator(this.page, this.boardType, this.usernameIndex).generate()
                .getFileUpload();
            MessageEditBuilder editedMsg = new MessageEditBuilder()
                .setFiles(fileUpload).setComponents(this.getCurComponents());

            this.updateInteractive(false, editedMsg.build());
        } catch (IOException | URISyntaxException exception) {
            this.stop(StopType.EXCEPTION);
            Log.error(this.user, this.getClass(), "Failed to generate leaderboard image", exception);
        }
    }

    @Override
    public ActionRow[] getCurComponents() {
        List<ItemComponent> boardSelect = InteractiveUtil.makeComponents(
            this.getInteractionID(),
            COMPONENTS.get("boardSelect")
        );

        List<ItemComponent> navBtns = InteractiveUtil.makeComponents(
            this.getInteractionID(),
            COMPONENTS.get("leftBtn"),
            COMPONENTS.get("pageBtn"),
            COMPONENTS.get("rightBtn")
        );

        for (int i=0; i<this.navOptions.size(); i++) {
            SelectOption navOption = this.navOptions.get(i);

            if (navOption.getValue().equals(this.boardType.toString())) {
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

        StringSelectMenu boardSelectMenu = (StringSelectMenu) boardSelect.getFirst();
        boardSelect.set(0, new StringSelectMenuImpl(
            boardSelectMenu.getId(),
            boardSelectMenu.getPlaceholder(),
            boardSelectMenu.getMinValues(),
            boardSelectMenu.getMaxValues(),
            boardSelectMenu.isDisabled(),
            this.navOptions
        ));

        return new ActionRow[] {
            ActionRow.of(boardSelect), ActionRow.of(navBtns)
        };
    }

    private void makeSelectOptions(IndivComponentConfig selectMenu) {
        List<SelectOption> options = new ArrayList<>();

        for (SelectOptionConfig selectOption : selectMenu.getOptions()) {
            SelectOption option = SelectOption.of(selectOption.getLabel(), selectOption.getValue())
                .withDescription(selectOption.getDescription());
            options.add(option);
        }

        this.navOptions = options;
    }
}
