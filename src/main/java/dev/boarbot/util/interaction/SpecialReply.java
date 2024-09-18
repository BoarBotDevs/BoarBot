package dev.boarbot.util.interaction;

import dev.boarbot.api.util.Configured;
import dev.boarbot.util.generators.EmbedImageGenerator;
import dev.boarbot.util.logging.Log;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;

import java.io.IOException;

public class SpecialReply implements Configured {
    public static void sendMaintenanceEmbed(SlashCommandInteraction interaction) {
        MessageCreateBuilder msg = new MessageCreateBuilder()
            .setFiles(getMaintenanceEmbed());

        try {
            interaction.reply(msg.build()).queue();
        } catch (ErrorResponseException exception) {
            Log.warn(EmbedImageGenerator.class, "Failed to send maintenance embed", exception);
        }
    }

    public static void sendErrorEmbed(SlashCommandInteraction interaction) {
        MessageEditBuilder editedMsg = new MessageEditBuilder()
            .setFiles(getErrorEmbed())
            .setComponents();

        try {
            if (interaction.isAcknowledged()) {
                interaction.getHook().editOriginal(editedMsg.build()).queue();
                return;
            }
            interaction.reply(MessageCreateData.fromEditData(editedMsg.build())).queue();
        } catch (ErrorResponseException exception) {
            Log.warn(EmbedImageGenerator.class, "Failed to send error embed", exception);
        }
    }

    public static void sendErrorEmbed(InteractionHook hook) {
        MessageCreateBuilder msg = new MessageCreateBuilder()
            .setFiles(getErrorEmbed());

        try {
            hook.sendMessage(msg.build()).queue();
        } catch (ErrorResponseException exception) {
            Log.warn(EmbedImageGenerator.class, "Failed to send error embed", exception);
        }
    }

    public static FileUpload getErrorEmbed() {
        try {
            return new EmbedImageGenerator(STRS.getError(), COLORS.get("error")).generate().getFileUpload();
        } catch (IOException exception) {
            Log.error(EmbedImageGenerator.class, "Failed to generate error embed", exception);
            return FileUpload.fromData(new byte[0], "error.png");
        }
    }

    private static FileUpload getMaintenanceEmbed() {
        try {
            return new EmbedImageGenerator(STRS.getMaintenance(), COLORS.get("maintenance")).generate().getFileUpload();
        } catch (IOException exception) {
            Log.error(EmbedImageGenerator.class, "Failed to generate maintenance embed", exception);
            return FileUpload.fromData(new byte[0], "maintenance.png");
        }
    }
}
