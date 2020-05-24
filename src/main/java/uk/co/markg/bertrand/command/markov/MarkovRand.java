package uk.co.markg.bertrand.command.markov;

import java.util.concurrent.ThreadLocalRandom;
import disparse.parser.reflection.CommandHandler;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import uk.co.markg.bertrand.database.ChannelRepository;
import uk.co.markg.bertrand.database.UserRepository;
import uk.co.markg.bertrand.markov.Markov;

public class MarkovRand {

  @CommandHandler(commandName = "rand",
      description = "Generate a random number of sentences from random user's messages!")
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
    var users = userRepo.getAllMarkovCandidateIds();
    var sb = new StringBuilder();
    int noOfSentences = ThreadLocalRandom.current().nextInt(5) + 1;
    for (int i = 0; i < noOfSentences; i++) {
      long targetUser = users.get(ThreadLocalRandom.current().nextInt(users.size()));
      sb.append(Markov.load(targetUser).generate()).append(" ");
    }
    String response = sb.toString();
    event.getChannel().sendMessage(response).queue();
  }

}
