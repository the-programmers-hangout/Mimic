package uk.co.markg.bertrand.listener;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MarkovResponse extends ListenerAdapter {

  /**
   * Listener method triggered by the discord bot receiving a message
   * 
   * @param event the discord event
   */
  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    if (event.getAuthor().isBot()) {
      return;
    }
    if (event.isFromType(ChannelType.PRIVATE)) {
      return;
    }
    event.getChannel().sendMessage(
        "Deprecated! Use `mimic!self`, `mimic!rand`, or `mimic!all`. Make sure you are opted in with `mimic!opt-in`. For more info see `mimic!help`.")
        .queue();
  }

}
