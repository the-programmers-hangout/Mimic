package uk.co.markg.bertrand.command.markov;

import java.util.concurrent.ThreadLocalRandom;
import disparse.parser.reflection.CommandHandler;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import uk.co.markg.bertrand.database.ChannelRepository;
import uk.co.markg.bertrand.database.UserRepository;
import uk.co.markg.bertrand.markov.Markov;

public class MarkovSelf {

  @CommandHandler(commandName = "self",
      description = "Generate a random number of sentences from your own messages!")
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
    if (!userRepo.isMarkovCandidate(userid)) {
      event.getChannel().sendMessage("I don't know enough about you!").queue();
    }
    int rand = ThreadLocalRandom.current().nextInt(5) + 1;
    event.getChannel().sendMessage(Markov.load(userid).generate(rand)).queue();
  }
}
