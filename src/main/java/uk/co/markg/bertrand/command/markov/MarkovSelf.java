package uk.co.markg.bertrand.command.markov;

import disparse.discord.jda.DiscordRequest;
import disparse.parser.reflection.CommandHandler;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import uk.co.markg.bertrand.database.ChannelRepository;
import uk.co.markg.bertrand.database.UserRepository;
import uk.co.markg.bertrand.markov.Markov;
import uk.co.markg.bertrand.markov.MarkovSender;

public class MarkovSelf {

  @CommandHandler(commandName = "self",
      description = "Generate a random number of sentences from your own messages!")
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
    if (!userRepo.isMarkovCandidate(userid)) {
      MarkovSender.notMarkovCandidate(event.getChannel());
    }
    event.getChannel().sendTyping().queue();
    MarkovSender.sendMessageWithDelay(event, Markov.load(userid).generateRandom());
  }
}
