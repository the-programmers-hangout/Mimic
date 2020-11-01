package uk.co.markg.mimic.command.markov;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.ThreadLocalRandom;
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

public class MarkovRand {

  /**
   * Method held by Disparse to begin command execution. Has a cooldown of five seconds per user.
   * 
   * Executes the command. Generates a random number of sentences from random users'
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
  @CommandHandler(commandName = "rand",
      description = "Generate a random number of sentences from random user's messages!")
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
    UsageRepository.getRepository().save(MarkovRand.class, event);
    event.getChannel().sendTyping().queue();
    var users = userRepo.getAllMarkovCandidateIds(event.getGuild().getIdLong());
    var sb = new StringBuilder();
    int noOfSentences = ThreadLocalRandom.current().nextInt(5) + 1;
    for (int i = 0; i < noOfSentences; i++) {
      long targetUser = users.get(ThreadLocalRandom.current().nextInt(users.size()));
      sb.append(Markov.load(targetUser, event.getGuild().getIdLong()).generate()).append(" ");
    }
    MarkovSender.sendMessage(event, sb.toString());
  }

}
