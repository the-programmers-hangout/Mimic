package uk.co.markg.bertrand.listener;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import uk.co.markg.bertrand.database.ChannelRepository;
import uk.co.markg.bertrand.markov.MarkovSender;

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
    if (!ChannelRepository.getRepository().hasWritePermission(event.getChannel().getIdLong())) {
      return;
    }
    if (event.getMessage().getMentionedMembers().contains(event.getGuild().getSelfMember())) {
      MarkovSender.sendMentionDeprecation(event.getChannel());
    }
  }

}
