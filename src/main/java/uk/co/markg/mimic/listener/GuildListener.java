package uk.co.markg.mimic.listener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import uk.co.markg.mimic.database.ChannelRepository;
import uk.co.markg.mimic.database.UsageRepository;
import uk.co.markg.mimic.database.UserRepository;

public class GuildListener extends ListenerAdapter {

  private static final Logger logger = LogManager.getLogger(GuildListener.class);

  private ChannelRepository channelRepo;
  private UsageRepository usageRepo;
  private UserRepository userRepo;

  public GuildListener() {
    this.channelRepo = ChannelRepository.getRepository();
    this.usageRepo = UsageRepository.getRepository();
    this.userRepo = UserRepository.getRepository();
  }

  @Override
  public void onGuildJoin(GuildJoinEvent event) {
    logger.info("Bot has joined guild: {}, id: {}", event.getGuild().getName(),
        event.getGuild().getId());
  }

  @Override
  public void onGuildLeave(GuildLeaveEvent event) {
    long serverid = event.getGuild().getIdLong();
    logger.info("Bot has left guild: {}, id: {}", event.getGuild().getName(), serverid);
    channelRepo.deleteByServerId(serverid);
    usageRepo.deleteByServerId(serverid);
    userRepo.deleteByServerId(serverid);

  }
}
