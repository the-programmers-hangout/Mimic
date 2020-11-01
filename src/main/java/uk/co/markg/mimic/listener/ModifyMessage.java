package uk.co.markg.mimic.listener;

import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import uk.co.markg.mimic.database.ChannelRepository;
import uk.co.markg.mimic.database.MessageRepository;

/**
 * Really this should be two classes but without using reflection, adding new Listener classes to
 * the JDABuilder becomes kinda nasty
 */
public class ModifyMessage extends ListenerAdapter {

  private ChannelRepository channelRepo;
  private MessageRepository messageRepo;

  public ModifyMessage() {
    this.channelRepo = ChannelRepository.getRepository();
    this.messageRepo = MessageRepository.getRepository();
  }

  /**
   * Removes any deleted discord messages from the database.
   * 
   * @param event The {@link net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent
   *              GuildMessageDeleteEvent} event.
   */
  @Override
  public void onGuildMessageDelete(GuildMessageDeleteEvent event) {
    if (channelRepo.hasReadPermission(event.getChannel().getIdLong())) {
      messageRepo.deleteById(event.getMessageIdLong());
    }
  }

  /**
   * Updates any edited discord messages in the database.
   * 
   * @param event The {@link net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent
   *              GuildMessageDeleteEvent} event.
   */
  @Override
  public void onGuildMessageUpdate(GuildMessageUpdateEvent event) {
    if (channelRepo.hasReadPermission(event.getChannel().getIdLong())
        && MessageReader.messageIsValid(event.getMessage())) {
      messageRepo.edit(event.getMessageIdLong(), event.getMessage());
    }
  }
}
