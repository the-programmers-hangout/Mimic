package uk.co.markg.bertrand.listener;

import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import uk.co.markg.bertrand.database.ChannelRepository;
import uk.co.markg.bertrand.database.MessageRepository;

public class DeleteMessage extends ListenerAdapter {

  private ChannelRepository channelRepo;
  private MessageRepository messageRepo;

  public DeleteMessage() {
    this.channelRepo = ChannelRepository.getRepository();
    this.messageRepo = MessageRepository.getRepository();
  }

  @Override
  public void onGuildMessageDelete(GuildMessageDeleteEvent event) {
    if (channelRepo.hasReadPermission(event.getChannel().getIdLong())) {
      messageRepo.deleteById(event.getMessageIdLong());
    }
  }

}
