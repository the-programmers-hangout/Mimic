package uk.co.markg.mimic.command.markov;

import java.io.File;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import disparse.discord.jda.DiscordRequest;
import disparse.parser.dispatch.CooldownScope;
import disparse.parser.reflection.CommandHandler;
import disparse.parser.reflection.Cooldown;
import disparse.parser.reflection.MessageStrategy;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import uk.co.markg.mimic.database.ChannelRepository;
import uk.co.markg.mimic.database.UsageRepository;
import uk.co.markg.mimic.database.UserRepository;
import uk.co.markg.mimic.markov.Markov;
import uk.co.markg.mimic.markov.MarkovSender;

public class MarkovAll {
  private static final Logger logger = LogManager.getLogger(MarkovAll.class);

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
      messageStrategy = MessageStrategy.REACT)
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
    var markov = file.exists() ? loadFromFile(file) : loadChain(event);
    markov.ifPresent(m -> MarkovSender.sendMessage(event, m.generateRandom()));
  }

  /**
   * Loads Markov chain from file.
   * 
   * @param file The Markov file to be loaded
   */
  private Optional<Markov> loadFromFile(File file) {
    logger.info("Loading chain from file");
    try {
      return Optional.of(Markov.load(file));
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }
    return Optional.empty();
  }

  /**
   * Loads chain from database.
   * 
   * @param event The {@link net.dv8tion.jda.api.events.message.MessageReceivedEvent
   *              MessageReceivedEvent} instance
   */
  private Optional<Markov> loadChain(MessageReceivedEvent event) {
    logger.info("Loading chain from database");
    var repo = UserRepository.getRepository();
    var serverid = event.getGuild().getIdLong();
    return Optional.of(Markov.load(repo.getAllMarkovCandidateIds(serverid), serverid));
  }


}
