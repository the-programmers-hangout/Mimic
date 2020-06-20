package uk.co.markg.bertrand.command.markov;

import disparse.discord.jda.DiscordRequest;
import disparse.parser.reflection.CommandHandler;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import uk.co.markg.bertrand.database.ChannelRepository;
import uk.co.markg.bertrand.database.UserRepository;
import uk.co.markg.bertrand.markov.Markov;
import uk.co.markg.bertrand.markov.MarkovSender;

public class MarkovAll {
  private static final int TWO_HOURS_MILLIS = 7_200_000;

  private static Markov markov;
  private static long loadTime;

  @CommandHandler(commandName = "all",
      description = "Generate a random number of sentences from all opted in user messages!")
  public static void execute(DiscordRequest request, ChannelRepository channelRepo,
                             UserRepository userRepo) {
    MessageReceivedEvent event = request.getEvent();
    if (!channelRepo.hasWritePermission(event.getChannel().getIdLong())) {
      return;
    }
    long userid = event.getAuthor().getIdLong();
    if (!userRepo.isUserOptedIn(userid)) {
      MarkovSender.notOptedIn(event.getChannel());
      return;
    }
    event.getChannel().sendTyping().queue();
    if (markov == null) {
      updateMarkovChain();
    }
    MarkovSender.sendMessageWithDelay(event, markov.generateRandom());
    if (cacheExpired()) {
      updateMarkovChain();
    }
  }

  private static void updateMarkovChain() {
    markov = getMarkovChain();
    loadTime = System.currentTimeMillis();
  }

  private static boolean cacheExpired() {
    return System.currentTimeMillis() - loadTime > TWO_HOURS_MILLIS;
  }

  public static Markov getMarkovChain() {
    var repo = UserRepository.getRepository();
    return Markov.load(repo.getAllMarkovCandidateIds());
  }
}
