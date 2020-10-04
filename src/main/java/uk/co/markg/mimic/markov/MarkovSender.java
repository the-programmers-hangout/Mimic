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

  public static void sendMessage(MessageReceivedEvent event, String text) {
    event.getChannel().sendMessage(text).allowedMentions(ALLOWED_MENTIONS)
        .queue(message -> message.suppressEmbeds(true).queue());
  }

  public static void notOptedIn(MessageChannel channel) {
    channel.sendMessage(buildErrorEmbed("You are not opted in! Use `mimic!opt-in`")).queue();
  }

  public static void notMarkovCandidate(MessageChannel channel) {
    channel.sendMessage(buildErrorEmbed(
        "I don't know enough about you yet! Chat more in the channels I can read! Find out more by using `mimic!channels`"))
        .queue();
  }

  public static void optedIn(MessageChannel channel) {
    channel.sendMessage(buildSuccessEmbed(
        "You have been opted-in. I'll start saving your messages. It might take me a few minutes!"))
        .queue();
  }

  public static void alreadyOptedIn(MessageChannel channel) {
    channel.sendMessage(buildErrorEmbed("You're already in!")).queue();
  }

  public static void optedOut(MessageChannel channel) {
    channel.sendMessage(buildSuccessEmbed("You've been opted out!")).queue();
  }

  public static void alreadyOptedOut(MessageChannel channel) {
    channel.sendMessage(buildErrorEmbed("You're already out!")).queue();
  }

  private static MessageEmbed buildErrorEmbed(String error) {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Uh oh!");
    eb.setDescription(error);
    eb.setColor(Color.decode("#eb7701"));
    return buildEmbed("Uh oh!", error);
  }

  private static MessageEmbed buildSuccessEmbed(String message) {
    return buildEmbed("Success!", message);
  }

  private static MessageEmbed buildEmbed(String title, String description) {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle(title);
    eb.setDescription(description);
    eb.setColor(Color.decode("#eb7701"));
    return eb.build();
  }

}
