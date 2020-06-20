package uk.co.markg.bertrand.command.markov;

import java.util.concurrent.ThreadLocalRandom;

import disparse.discord.jda.DiscordRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import disparse.parser.reflection.CommandHandler;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import uk.co.markg.bertrand.database.ChannelRepository;
import uk.co.markg.bertrand.database.UserRepository;
import uk.co.markg.bertrand.markov.Markov;
import uk.co.markg.bertrand.markov.MarkovSender;

public class MarkovRand {
  private static final Logger logger = LogManager.getLogger(MarkovRand.class);

  @CommandHandler(commandName = "rand",
      description = "Generate a random number of sentences from random user's messages!")
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
    logger.info("Generating random chain");
    event.getChannel().sendTyping().queue();
    var users = userRepo.getAllMarkovCandidateIds();
    var sb = new StringBuilder();
    int noOfSentences = ThreadLocalRandom.current().nextInt(5) + 1;
    for (int i = 0; i < noOfSentences; i++) {
      long targetUser = users.get(ThreadLocalRandom.current().nextInt(users.size()));
      sb.append(Markov.load(targetUser).generate()).append(" ");
    }
    MarkovSender.sendMessageWithDelay(event, sb.toString());
  }

}
