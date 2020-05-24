package uk.co.markg.bertrand.command.markov;

import java.util.concurrent.ThreadLocalRandom;
import disparse.parser.reflection.CommandHandler;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import uk.co.markg.bertrand.database.ChannelRepository;
import uk.co.markg.bertrand.database.UserRepository;
import uk.co.markg.bertrand.markov.Markov;

public class MarkovAll {

  private MessageReceivedEvent event;
  private ChannelRepository channelRepo;
  private UserRepository userRepo;

  public MarkovAll(MessageReceivedEvent event, ChannelRepository channelRepo,
      UserRepository userRepo) {
    this.event = event;
    this.channelRepo = channelRepo;
    this.userRepo = userRepo;
  }

  @CommandHandler(commandName = "all",
      description = "Generate a markov chain from all user messages!")
  public static void execute(MessageReceivedEvent event, ChannelRepository channelRepo,
      UserRepository userRepo) {
    new MarkovAll(event, channelRepo, userRepo).execute();
  }

  private void execute() {
    if (!hasWritePermission(event)) {
      return;
    }
    long userid = event.getAuthor().getIdLong();
    if (!isUserOptedIn(userid)) {
      event.getChannel().sendMessage("You are not opted in! Use `mimic!opt-in`").queue();
      return;
    }
    int rand = ThreadLocalRandom.current().nextInt(5) + 1;
    var users = userRepo.getAllMarkovCandidateIds();
    event.getChannel().sendMessage(Markov.load(users).generate(rand)).queue();
  }


  private boolean isUserOptedIn(long userid) {
    return userRepo.isUserOptedIn(userid);
  }

  /**
   * Returns whether the bot has write permissions for a channel to send a markov chain response
   * 
   * @param event the discord event
   * @return true if the bot has write permissions for the channel
   */
  private boolean hasWritePermission(MessageReceivedEvent event) {
    return channelRepo.hasWritePermission(event.getChannel().getIdLong());
  }


}
