package uk.co.markg.mimic.command.markov;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import disparse.discord.jda.DiscordRequest;
import disparse.parser.reflection.CommandHandler;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import uk.co.markg.mimic.database.ChannelRepository;
import uk.co.markg.mimic.database.UsageRepository;
import uk.co.markg.mimic.database.UserRepository;
import uk.co.markg.mimic.markov.Markov;
import uk.co.markg.mimic.markov.MarkovSender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MarkovAll {
  private static final Logger logger = LogManager.getLogger(MarkovAll.class);
  private static final int TWO_HOURS_MILLIS = 7_200_000;
  private static Map<Long, Long> cacheTimes = new HashMap<>();

  @CommandHandler(commandName = "all",
      description = "Generate a random number of sentences from all opted in user messages!")
  public void execute(DiscordRequest request, ChannelRepository channelRepo,
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
    UsageRepository.getRepository().save(MarkovAll.class, event);
    event.getChannel().sendTyping().queue();
    long guildid = event.getGuild().getIdLong();
    File file = new File(guildid + ".markov");
    if (file.exists() && !cacheExpired(guildid)) {
      loadFromFileAndSend(event, file);
    } else {
      loadChainAndSave(event, file);
    }
  }

  private void loadFromFileAndSend(MessageReceivedEvent event, File file) {
    logger.info("Loading chain from file");
    try {
      Markov markov = Markov.load(file);
      MarkovSender.sendMessage(event, markov.generateRandom());
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }
  }

  private void loadChainAndSave(MessageReceivedEvent event, File file) {
    logger.info("Loading chain from database");
    Markov markov = updateMarkovChain(event.getGuild().getIdLong());
    MarkovSender.sendMessage(event, markov.generateRandom());
    try {
      markov.save(file);
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }
  }

  private Markov updateMarkovChain(long guildid) {
    logger.info("Updated cache for {}", guildid);
    cacheTimes.put(guildid, System.currentTimeMillis());
    return getMarkovChain();
  }

  private boolean cacheExpired(long guildid) {
    long cacheTime = cacheTimes.getOrDefault(guildid, 0L);
    return System.currentTimeMillis() - cacheTime > TWO_HOURS_MILLIS;
  }

  public Markov getMarkovChain() {
    var repo = UserRepository.getRepository();
    return Markov.load(repo.getAllMarkovCandidateIds());
  }
}
