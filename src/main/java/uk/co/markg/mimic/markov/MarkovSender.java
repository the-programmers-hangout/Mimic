package uk.co.markg.mimic.markov;

import java.awt.Color;
import java.util.EnumSet;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message.MentionType;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class MarkovSender {

  private static final EnumSet<MentionType> ALLOWED_MENTIONS =
      EnumSet.of(MentionType.CHANNEL, MentionType.EMOTE);

  /**
   * Sends a discord message with specific text.
   * 
   * @param event The {@link net.dv8tion.jda.api.events.message.MessageReceivedEven
   *              MessageReceivedEvent} instance
   * @param text  The content of the message
   */
  public static void sendMessage(MessageReceivedEvent event, String text) {
    event.getChannel().sendMessage(text).allowedMentions(ALLOWED_MENTIONS)
        .queue(message -> message.suppressEmbeds(true).queue());
  }

  /**
   * Notifies the user they're not opted in.
   * 
   * @param channel The target channel.
   */
  public static void notOptedIn(MessageChannel channel) {
    channel.sendMessage(buildErrorEmbed("You are not opted in! Use `mimic!opt-in`")).queue();
  }

  /**
   * Notifies the user they're not a valid Markov candidate.
   * 
   * @param channel The target channel.
   */
  public static void notMarkovCandidate(MessageChannel channel) {
    channel.sendMessage(buildErrorEmbed(
        "I don't know enough about you yet! Chat more in the channels I can read! Find out more by using `mimic!channels`"))
        .queue();
  }

  /**
   * Notifies the user they've opted in successfully.
   * 
   * @param channel The target channel.
   */
  public static void optedIn(MessageChannel channel) {
    channel.sendMessage(buildSuccessEmbed(
        "You have been opted-in. I'll start saving your messages. It might take me a few minutes!"))
        .queue();
  }

  /**
   * Notifies the user they're already opted in.
   * 
   * @param channel The target channel.
   */
  public static void alreadyOptedIn(MessageChannel channel) {
    channel.sendMessage(buildErrorEmbed("You're already in!")).queue();
  }

  /**
   * Notifies the user they've been opted out successfully.
   * 
   * @param channel The target channel.
   */
  public static void optedOut(MessageChannel channel) {
    channel.sendMessage(buildSuccessEmbed("You've been opted out!")).queue();
  }

  /**
   * Notifies the user they're already opted out.
   * 
   * @param channel The target channel.
   */
  public static void alreadyOptedOut(MessageChannel channel) {
    channel.sendMessage(buildErrorEmbed("You're already out!")).queue();
  }

  /**
   * Builds a {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed} instance containing the
   * specified error.
   * 
   * @param error The error message
   * @return The discord {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed} containing
   *         the error message
   */
  private static MessageEmbed buildErrorEmbed(String error) {
    return buildEmbed("Uh oh!", error);
  }

  /**
   * Builds a {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed} instance informing the
   * command was executed successfully.
   * 
   * @param message The success message
   * @return The discord {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed} containing
   *         the message
   */
  private static MessageEmbed buildSuccessEmbed(String message) {
    return buildEmbed("Success!", message);
  }

  /**
   * Builds a {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed} with a custom title and
   * description.
   * 
   * @param title       The title of the embed
   * @param description The description of the embed
   * @return The discord {@link net.dv8tion.jda.api.entities.MessageEmbed MessageEmbed}
   */
  private static MessageEmbed buildEmbed(String title, String description) {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle(title);
    eb.setDescription(description);
    eb.setColor(Color.decode("#eb7701"));
    return eb.build();
  }

}
