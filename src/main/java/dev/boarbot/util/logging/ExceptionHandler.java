package dev.boarbot.util.logging;

import dev.boarbot.interactives.Interactive;
import dev.boarbot.util.interaction.SpecialReply;
import dev.boarbot.util.interactive.StopType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.exceptions.InteractionFailureException;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.requests.ErrorResponse;

public class ExceptionHandler {
    private final static String exceptStr = "A problem occurred while handling an exception";

    public static void replyHandle(SlashCommandInteraction interaction, Object obj, Throwable exception) {
        try {
            stopInteractive(obj);
            handle(interaction.getUser(), obj.getClass(), exception);
            SpecialReply.sendErrorMessage(interaction, obj);
        } catch (RuntimeException exception1) {
            Log.error(interaction.getUser(), obj.getClass(), exceptStr, exception1);
        }
    }

    public static void replyHandle(IReplyCallback interaction, Object obj, Throwable exception) {
        try {
            stopInteractive(obj);
            handle(interaction.getUser(), obj.getClass(), exception);
            SpecialReply.sendErrorMessage(interaction, obj);
        } catch (RuntimeException exception1) {
            Log.error(interaction.getUser(), obj.getClass(), exceptStr, exception1);
        }
    }

    public static void replyHandle(InteractionHook hook, Object obj, Throwable exception) {
        try {
            stopInteractive(obj);
            handle(hook.getInteraction().getUser(), obj.getClass(), exception);
            SpecialReply.sendErrorMessage(hook, obj);
        } catch (RuntimeException exception1) {
            Log.error(hook.getInteraction().getUser(), obj.getClass(), exceptStr, exception1);
        }
    }

    public static void messageHandle(Message message, Object obj, Throwable exception) {
        try {
            stopInteractive(obj);
            handle(message.getAuthor(), obj.getClass(), exception);
            SpecialReply.sendErrorMessage(message, obj);
        } catch (RuntimeException exception1) {
            Log.error(obj.getClass(), exceptStr, exception1);
        }
    }

    private static void stopInteractive(Object obj) {
        if (obj instanceof Interactive) {
            ((Interactive) obj).stop(StopType.EXCEPTION);
        }
    }

    public static void deferHandle(Interaction interaction, Object obj, Throwable exception) {
        handle(interaction.getUser(), obj.getClass(), exception);
    }

    public static void deferHandle(Class<?> clazz, Throwable exception) {
        handle(null, clazz, exception);
    }

    public static void handle(Class<?> clazz, Throwable exception) {
        handle(null, clazz, exception);
    }

    public static void handle(User user, Class<?> clazz, Throwable exception) {
        try {
            if (exception instanceof InteractionFailureException) {
                if (user != null) {
                    Log.warn(user, clazz, "Interaction failed", exception);
                    return;
                }

                Log.warn(clazz, "Interaction failed", exception);
                return;
            }

            if (exception instanceof ErrorResponseException) {
                ErrorResponse response = ((ErrorResponseException) exception).getErrorResponse();

                if (response == ErrorResponse.UNKNOWN_INTERACTION) {
                    if (user != null) {
                        Log.warn(user, clazz, "Interaction has expired", exception);
                        return;
                    }

                    Log.warn(clazz, "Interaction has expired", exception);
                    return;
                }

                if (user != null) {
                    Log.warn(user, clazz, "Discord threw an exception: " + response, exception);
                    return;
                }

                Log.warn(clazz, "Discord threw an exception: " + response, exception);
                return;
            }

            if (exception instanceof InsufficientPermissionException) {
                if (user != null) {
                    Log.warn(user, clazz, "Insufficient permissions to send message", exception);
                    return;
                }

                Log.warn(clazz, "Insufficient permissions to send message", exception);
                return;
            }

            if (user != null) {
                Log.error(user, clazz, "Runtime exception: " + exception.getMessage(), exception);
                return;
            }

            Log.error(clazz, "Runtime exception: " + exception.getMessage(), exception);
        } catch (RuntimeException exception1) {
            Log.error(clazz, exceptStr, exception1);
        }
    }
}
