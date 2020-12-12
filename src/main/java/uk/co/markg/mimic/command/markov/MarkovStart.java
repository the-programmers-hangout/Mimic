package uk.co.markg.mimic.command.markov;

import java.io.File;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import disparse.discord.jda.DiscordRequest;
import disparse.parser.dispatch.CooldownScope;
import disparse.parser.reflection.CommandHandler;
import disparse.parser.reflection.Cooldown;
import disparse.parser.reflection.MessageStrategy;
import disparse.parser.reflection.Usage;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import uk.co.markg.mimic.database.ChannelRepository;
import uk.co.markg.mimic.database.UsageRepository;
import uk.co.markg.mimic.database.UserRepository;
import uk.co.markg.mimic.markov.Markov;
import uk.co.markg.mimic.markov.MarkovSender;

public class MarkovStart {
  private static final Logger logger = LogManager.getLogger(MarkovStart.class);

  /**
   * Method held by Disparse to begin command execution. Has a cooldown of five seconds per user.
   * 
   * Executes the command. Finishes sentence the user provided as an argument. The sentence is
   * generated from all opted in user {@link net.dv8tion.jda.api.entities.Message Messages}. To
   * execute, user must be opt-ed in and command must be sent in a channel with write permission
   * enabled.
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
  @Usage(usage = "\"I'm really\"",
      description = "Tells mimic to start generating a sentence with \"I'm really\"")
  @Cooldown(amount = 5, unit = ChronoUnit.SECONDS, scope = CooldownScope.USER,
      messageStrategy = MessageStrategy.REACT)
  @CommandHandler(commandName = "start",
      description = "Provide the start of a sentence and let mimic finish it! Use quotations around your sentence!")
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
    UsageRepository.getRepository().save(MarkovStart.class, event);
    event.getChannel().sendTyping().queue();

    File file = new File("markov/servers/" + event.getGuild().getIdLong() + ".markov");
    var markov = file.exists() ? loadFromFile(file) : loadChain(event);
    String lastWord = getLastWord(request.getArgs());
    markov.ifPresent(m -> {
      String sentence = buildMessageStart(request.getArgs()) + m.generate(lastWord);
      MarkovSender.sendMessage(event, sentence);
    });
  }

  /**
   * Gets the last word in the sentence provided by the user.
   * 
   * @param args The parsed sentence passed as an argument
   * @return The last word in the argument
   */
  private static String getLastWord(List<String> args) {
    if (args.isEmpty()) {
      return "";
    }
    if (args.size() == 1) {
      String[] words = args.get(0).split(" ");
      return words[words.length - 1];
    } else {
      return args.get(args.size() - 1);
    }
  }

  /**
   * Constructs the start of the generated MarkovStart sentence.
   * 
   * @param args The parsed sentence passed as an argument
   * @return The start of the MarkovStart sentence
   */
  private static String buildMessageStart(List<String> args) {
    if (args.isEmpty()) {
      return "";
    }
    if (args.size() == 1) {
      String[] words = args.get(0).split(" ");
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < words.length - 1; i++) {
        sb.append(words[i]).append(" ");
      }
      return sb.toString();
    } else {
      args.remove(args.size() - 1);
      return String.join(" ", args) + " ";
    }
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
