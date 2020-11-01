package uk.co.markg.mimic.listener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import uk.co.markg.mimic.database.UserRepository;

public class MemberLeave extends ListenerAdapter {

  private static final Logger logger = LogManager.getLogger(MemberLeave.class);

  private UserRepository userRepo;

  public MemberLeave() {
    this.userRepo = UserRepository.getRepository();
  }

  /**
   * Removes the user messages from the database.
   * 
   * @param event the {@link net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent}
   *              instance
   */
  @Override
  public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
    long userid = event.getUser().getIdLong();
    if (userRepo.isUserOptedIn(userid, event.getGuild().getIdLong())) {
      logger.info("User {} left the server.", userid);
      userRepo.delete(userid, event.getGuild().getIdLong());
    }
  }

}
