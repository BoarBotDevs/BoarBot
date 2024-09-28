package dev.boarbot.util.interaction;

import dev.boarbot.api.util.Configured;
import dev.boarbot.util.generators.EmbedImageGenerator;
import dev.boarbot.util.logging.ExceptionHandler;
import dev.boarbot.util.logging.Log;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.io.IOException;

public class SpecialReply implements Configured {
    public static void sendErrorMessage(IReplyCallback interaction, Object obj) {
        MessageCreateData msgData = getErrorMsgData();
        interaction.reply(msgData).setEphemeral(true).queue(null, e ->
            interaction.getHook().editOriginal(MessageEditData.fromCreateData(msgData)).queue(null, e1 ->
                ExceptionHandler.handle(interaction.getUser(), obj.getClass(), e1)
            )
        );
    }

    public static void sendErrorMessage(InteractionHook hook, Object obj) {
        hook.sendMessage(getErrorMsgData()).setEphemeral(true)
            .queue(null, e -> ExceptionHandler.handle(hook.getInteraction().getUser(), obj.getClass(), e));
    }

    public static void sendErrorMessage(Message message, Object obj) {
        message.editMessage(MessageEditData.fromCreateData(getErrorMsgData()))
            .queue(null, e -> ExceptionHandler.handle(obj.getClass(), e));
    }

    public static MessageCreateData getErrorMsgData() {
        MessageCreateBuilder msg = new MessageCreateBuilder();

        try {
            msg.setFiles(new EmbedImageGenerator(STRS.getError(), COLORS.get("error")).generate().getFileUpload());
        } catch (IOException exception) {
            Log.error(SpecialReply.class, "Failed to generate error embed", exception);
            msg.setContent(STRS.getError());
        }

        return msg.build();
    }
}
