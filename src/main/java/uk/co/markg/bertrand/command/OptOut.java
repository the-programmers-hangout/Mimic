package uk.co.markg.bertrand.command;

import disparse.parser.reflection.CommandHandler;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import uk.co.markg.bertrand.database.UserRepository;

public class OptOut {

  private MessageReceivedEvent event;
  private UserRepository userRepo;

  public OptOut(MessageReceivedEvent event, UserRepository userRepo) {
    this.event = event;
    this.userRepo = userRepo;
  }

  @CommandHandler(commandName = "opt-out", description = "Opt-out for all messages to be removed.")
  public static void execute(MessageReceivedEvent event, UserRepository userRepo) {
    new OptOut(event, userRepo).execute();
  }

  private void execute() {
    long userid = event.getAuthor().getIdLong();
    if (userRepo.isUserOptedIn(userid)) {
      optOutUser(userid);
    } else {
      event.getChannel().sendMessage("You're already out!").queue();
    }
  }

  private void optOutUser(long userid) {
    userRepo.delete(userid);
    event.getChannel().sendMessage("You've been opted out!").queue();
  }
}
