package uk.co.markg.bertrand.command.markov;

import disparse.parser.reflection.CommandHandler;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import uk.co.markg.bertrand.database.ChannelRepository;
import uk.co.markg.bertrand.database.UserRepository;
import uk.co.markg.bertrand.markov.Markov;
import uk.co.markg.bertrand.markov.MarkovSender;

public class MarkovAll {

  private static Markov markov;
  private static long loadTime;

  @CommandHandler(commandName = "all",
      description = "Generate a random number of sentences from all opted in user messages!")
  public static void execute(MessageReceivedEvent event, ChannelRepository channelRepo,
      UserRepository userRepo) {
    if (!channelRepo.hasWritePermission(event.getChannel().getIdLong())) {
      return;
    }
    long userid = event.getAuthor().getIdLong();
    if (!userRepo.isUserOptedIn(userid)) {
      MarkovSender.notOptedIn(event.getChannel());
      return;
    }
    event.getChannel().sendTyping().queue();
    if (markov == null || cacheExpired()) {
      System.out.println("Loading markov");
      markov = getMarkovChain();
      loadTime = System.currentTimeMillis();
    }
    MarkovSender.sendMessage(event, markov.generateRandom());
  }

  private static boolean cacheExpired() {
    return System.currentTimeMillis() - loadTime > 7_200_000;
  }

  public static Markov getMarkovChain() {
    var repo = UserRepository.getRepository();
    return Markov.load(repo.getAllMarkovCandidateIds());
  }
}
