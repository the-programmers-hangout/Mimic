package uk.co.markg.bertrand.markov;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Message.MentionType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class MarkovSender {


  public static void sendMessage(MessageReceivedEvent event, String text) {
    Message msg =
        new MessageBuilder().append(text).stripMentions(event.getJDA(), MentionType.USER).build();
    event.getChannel().sendMessage(msg).queue(message -> message.suppressEmbeds(true).queue());
  }

  public static void notOptedIn(MessageChannel channel) {
    channel.sendMessage("You are not opted in! Use `mimic!opt-in`").queue();
  }
}
