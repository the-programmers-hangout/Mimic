package uk.co.markg.mimic.command;

import java.time.temporal.ChronoUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import disparse.discord.jda.DiscordRequest;
import disparse.parser.dispatch.CooldownScope;
import disparse.parser.reflection.CommandHandler;
import disparse.parser.reflection.Cooldown;
import disparse.parser.reflection.Populate;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import uk.co.markg.mimic.database.UserRepository;
import uk.co.markg.mimic.markov.MarkovSender;

public class OptOut {
  private static final Logger logger = LogManager.getLogger(OptOut.class);

  private MessageReceivedEvent event;
  private UserRepository userRepo;

  /**
   * Command execution method held by Disparse
   *
   * @param request  The discord request dispatched to this command
   * @param userRepo The user repository used to communicate with the database
   */
  @Populate
  public OptOut(DiscordRequest request, UserRepository userRepo) {
    this.event = request.getEvent();
    this.userRepo = userRepo;
  }

  /**
   * Command execution method held by Disparse
   */
  @Cooldown(amount = 10, unit = ChronoUnit.SECONDS, scope = CooldownScope.USER,
      sendCooldownMessage = false)
  @CommandHandler(commandName = "opt-out", description = "Opt-out for all messages to be removed.")
  public void optOutCommand() {
    this.execute();
  }

  /**
   * Executes the command. If the user is already opted out a message is sent to discord. Otherwise
   * the user and their data is deleted from the database.
   */
  private void execute() {
    long userid = event.getAuthor().getIdLong();
    if (userRepo.isUserOptedIn(userid, event.getGuild().getIdLong())) {
      optOutUser(userid);
    } else {
      MarkovSender.alreadyOptedOut(event.getChannel());
    }
  }

  /**
   * OptOut a user by deleting them and their related data from the database.
   *
   * @param userid the user to delete
   */
  private void optOutUser(long userid) {
    userRepo.delete(userid, event.getGuild().getIdLong());
    MarkovSender.optedOut(event.getChannel());
    logger.info("Opting-out user {} in server {}.", userid, event.getGuild().getIdLong());
  }
}
