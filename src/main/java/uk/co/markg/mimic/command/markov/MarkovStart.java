package uk.co.markg.mimic.command.markov;

import java.time.temporal.ChronoUnit;
import java.util.List;
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

public class MarkovStart {

  @Cooldown(amount = 5, unit = ChronoUnit.SECONDS, scope = CooldownScope.USER,
      sendCooldownMessage = false)
  @CommandHandler(commandName = "start",
      description = "Provide the start of a sentence and let mimic finish it!")
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
    if (!userRepo.isMarkovCandidate(userid, event.getGuild().getIdLong())) {
      MarkovSender.notMarkovCandidate(event.getChannel());
      return;
    }

    UsageRepository.getRepository().save(MarkovStart.class, event);
    event.getChannel().sendTyping().queue();
    Markov markov = Markov.load(userid, event.getGuild().getIdLong());
    var args = request.getArgs();
    String lastWord = args.get(args.size() - 1);

    MarkovSender.sendMessage(event, buildMessageStart(args) + markov.generate(lastWord));
  }

  private static String buildMessageStart(List<String> args) {
    args.remove(args.size() - 1);
    String message = String.join(" ", args);
    return message + " ";
  }

}
