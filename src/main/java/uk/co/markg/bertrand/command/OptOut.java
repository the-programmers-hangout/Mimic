package uk.co.markg.bertrand.command;

import disparse.parser.reflection.CommandHandler;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import uk.co.markg.bertrand.database.UserRepository;
import uk.co.markg.bertrand.markov.MarkovSender;

public class OptOut {

  private MessageReceivedEvent event;
  private UserRepository userRepo;

  public OptOut(MessageReceivedEvent event, UserRepository userRepo) {
    this.event = event;
    this.userRepo = userRepo;
  }

  /**
   * Command execution method held by Disparse
   * 
   * @param event    The message event from discord that triggered the command
   * @param userRepo The user repository used to communicate with the database
   */
  @CommandHandler(commandName = "opt-out", description = "Opt-out for all messages to be removed.")
  public static void execute(MessageReceivedEvent event, UserRepository userRepo) {
    new OptOut(event, userRepo).execute();
  }

  /**
   * Executes the command. If the user is already opted out a message is sent to discord. Otherwise
   * the user and their data is deleted from the database.
   */
  private void execute() {
    long userid = event.getAuthor().getIdLong();
    if (userRepo.isUserOptedIn(userid)) {
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
    userRepo.delete(userid);
    MarkovSender.optedOut(event.getChannel());
  }
}
