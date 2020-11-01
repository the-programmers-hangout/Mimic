package uk.co.markg.mimic.command.markov;

import java.io.File;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import disparse.discord.jda.DiscordRequest;
import disparse.parser.dispatch.CooldownScope;
import disparse.parser.reflection.CommandHandler;
import disparse.parser.reflection.Cooldown;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import uk.co.markg.mimic.database.ChannelRepository;
import uk.co.markg.mimic.database.UsageRepository;
import uk.co.markg.mimic.database.UserRepository;
import uk.co.markg.mimic.markov.Markov;
import uk.co.markg.mimic.markov.MarkovSender;

public class MarkovAll {
  private static final Logger logger = LogManager.getLogger(MarkovAll.class);
  private static final int TWO_HOURS_MILLIS = 7_200_000;
  private static Map<Long, Long> cacheTimes = new HashMap<>();

  /**
   * Method held by Disparse to begin command execution. Has a cooldown of five seconds per user.
   * 
   * Executes the command. Generates a random number of sentences from all opted in user
   * {@link net.dv8tion.jda.api.entities.Message Messages}. To execute, user must be opt-ed in and
   * command must be sent in a channel with write permission enabled.
   * 
   * Saves command usage in the UsageRepository database.
   * 
   * @param request     The {@link disparse.discord.jda.DiscordRequest DiscordRequest} dispatched to
   *                    this command
   * @param channelRepo The {@link uk.co.markg.mimic.database.ChannelRepository ChannelRepository}
   *                    instance used to communicate with the database
   * @param userRepo    The {@link uk.co.markg.mimic.database.UserRepository UserRepository}
   *                    instance
   */
  @Cooldown(amount = 5, unit = ChronoUnit.SECONDS, scope = CooldownScope.USER,
      sendCooldownMessage = false)
  @CommandHandler(commandName = "all",
      description = "Generate a random number of sentences from all opted in user messages!")
  public void execute(DiscordRequest request, ChannelRepository channelRepo,
      UserRepository userRepo) {
    MessageReceivedEvent event = request.getEvent();
    if (!channelRepo.hasWritePermission(event.getChannel().getIdLong())) {
      return;
    }
    long userid = event.getAuthor().getIdLong();
    if (!userRepo.isUserOptedIn(userid, event.getGuild().getIdLong())) {
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

  /**
   * Loads Markov chain from file and sends message.
   * 
   * @param event The {@link net.dv8tion.jda.api.events.message.MessageReceivedEvent
   *              MessageReceivedEvent} instance
   * @param file  The Markov file to be loaded
   */
  private void loadFromFileAndSend(MessageReceivedEvent event, File file) {
    logger.info("Loading chain from file");
    try {
      Markov markov = Markov.load(file);
      MarkovSender.sendMessage(event, markov.generateRandom());
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }
  }

  /**
   * Loads chain from database and sends message. Saves chain in the Markov file.
   * 
   * @param event The {@link net.dv8tion.jda.api.events.message.MessageReceivedEvent
   *              MessageReceivedEvent} instance
   * @param file  The Markov file to be loaded
   */
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

  /**
   * Updates the cache for the server and gets the Markov chain.
   * 
   * @param serverid The target server
   * @return The loaded Markov chain for the server
   */
  private Markov updateMarkovChain(long serverid) {
    logger.info("Updated cache for {}", serverid);
    cacheTimes.put(serverid, System.currentTimeMillis());
    return getMarkovChain(serverid);
  }

  /**
   * Checks whether the cache time for a specific server is expired.
   * 
   * @param serverid The target server
   * @return True if cache is expired
   */
  private boolean cacheExpired(long serverid) {
    long cacheTime = cacheTimes.getOrDefault(serverid, 0L);
    return System.currentTimeMillis() - cacheTime > TWO_HOURS_MILLIS;
  }

  /**
   * Gets the Markov chain generated from all candidate users in the server.
   * 
   * @param serverid The target server
   * @return The generated Markov chain
   */
  public Markov getMarkovChain(long serverid) {
    var repo = UserRepository.getRepository();
    return Markov.load(repo.getAllMarkovCandidateIds(serverid), serverid);
  }
}
