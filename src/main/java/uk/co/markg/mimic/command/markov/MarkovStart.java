package uk.co.markg.mimic.command.markov;

import java.io.File;
import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import disparse.discord.jda.DiscordRequest;
import disparse.parser.dispatch.CooldownScope;
import disparse.parser.reflection.CommandHandler;
import disparse.parser.reflection.Cooldown;
import disparse.parser.reflection.Usage;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import uk.co.markg.mimic.database.ChannelRepository;
import uk.co.markg.mimic.database.UsageRepository;
import uk.co.markg.mimic.database.UserRepository;
import uk.co.markg.mimic.markov.Markov;
import uk.co.markg.mimic.markov.MarkovSender;

public class MarkovStart {
  private static final Logger logger = LogManager.getLogger(MarkovStart.class);

  @Usage(usage = "\"I'm really\"",
      description = "Tells mimic to start generating a sentence with \"I'm really\"")
  @Cooldown(amount = 5, unit = ChronoUnit.SECONDS, scope = CooldownScope.USER,
      sendCooldownMessage = false)
  @CommandHandler(commandName = "start",
      description = "Provide the start of a sentence and let mimic finish it! Use quotations around your sentence!")
  public static void execute(DiscordRequest request, ChannelRepository channelRepo,
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
    try {
      Markov markov = Markov.load(new File(event.getGuild().getIdLong() + ".markov"));
      String lastWord = getLastWord(request.getArgs());
      MarkovSender.sendMessage(event,
          buildMessageStart(request.getArgs()) + markov.generate(lastWord));
    } catch (IOException e) {
      logger.error(e.getMessage(), e);
    }
  }

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

  private static String buildMessageStart(List<String> args) {
    args.remove(args.size() - 1);
    String message = String.join(" ", args);
    return message + " ";
  }

}
