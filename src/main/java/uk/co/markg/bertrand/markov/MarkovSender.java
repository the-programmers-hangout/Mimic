package uk.co.markg.bertrand.markov;

import java.awt.Color;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.MentionType;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class MarkovSender {


  public static void sendMessage(MessageReceivedEvent event, String text) {
    Message msg =
        new MessageBuilder().append(text).stripMentions(event.getJDA(), MentionType.USER).build();
    event.getChannel().sendMessage(msg).queue(message -> message.suppressEmbeds(true).queue());
  }

  public static void sendMessageWithDelay(MessageReceivedEvent event, String text) {
    Message message =
        new MessageBuilder().append(text).stripMentions(event.getJDA(), MentionType.USER).build();
    event.getChannel().sendMessage(message).queueAfter(2, TimeUnit.SECONDS,
        msg -> msg.suppressEmbeds(true).queue());
  }

  public static void notOptedIn(MessageChannel channel) {
    channel.sendMessage(buildErrorEmbed("You are not opted in! Use `mimic!opt-in`")).queue();
  }

  public static void notMarkovCandidate(MessageChannel channel) {
    channel.sendMessage(buildErrorEmbed("I don't know enough about you yet!")).queue();
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

  public static void sendMentionDeprecation(MessageChannel channel) {
    channel.sendMessage(buildErrorEmbed(
        "Deprecated! Use `mimic!rand`. Make sure you are opted in with `mimic!opt-in`. For more info see `mimic!help`."))
        .queue();
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
