package uk.co.markg.bertrand.command.markov;

import java.util.concurrent.ThreadLocalRandom;
import disparse.parser.reflection.CommandHandler;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import uk.co.markg.bertrand.database.ChannelRepository;
import uk.co.markg.bertrand.database.UserRepository;
import uk.co.markg.bertrand.markov.Markov;

public class MarkovAll {

  @CommandHandler(commandName = "all",
      description = "Generate a markov chain from all user messages!")
  public static void execute(MessageReceivedEvent event, ChannelRepository channelRepo,
      UserRepository userRepo) {
    if (!channelRepo.hasWritePermission(event.getChannel().getIdLong())) {
      return;
    }
    long userid = event.getAuthor().getIdLong();
    if (!userRepo.isUserOptedIn(userid)) {
      event.getChannel().sendMessage("You are not opted in! Use `mimic!opt-in`").queue();
      return;
    }
    int rand = ThreadLocalRandom.current().nextInt(5) + 1;
    var users = userRepo.getAllMarkovCandidateIds();
    event.getChannel().sendMessage(Markov.load(users).generate(rand)).queue();
  }
}
