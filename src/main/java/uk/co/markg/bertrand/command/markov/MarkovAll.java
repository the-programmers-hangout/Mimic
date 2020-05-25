package uk.co.markg.bertrand.command.markov;

import disparse.parser.reflection.CommandHandler;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import uk.co.markg.bertrand.database.ChannelRepository;
import uk.co.markg.bertrand.database.UserRepository;
import uk.co.markg.bertrand.markov.Markov;
import uk.co.markg.bertrand.markov.MarkovSender;

public class MarkovAll {

  @CommandHandler(commandName = "all",
      description = "Generate a random number of sentences from all opted in user messages!")
  public static void execute(MessageReceivedEvent event, ChannelRepository channelRepo,
      UserRepository userRepo) {
    if (!channelRepo.hasWritePermission(event.getChannel().getIdLong())) {
      return;
    }
    long userid = event.getAuthor().getIdLong();
    if (!userRepo.isUserOptedIn(userid)) {
      MarkovSender.notOptedIn(event.getChannel());
      return;
    }
    event.getChannel().sendTyping().queue();
    var users = userRepo.getAllMarkovCandidateIds();
    MarkovSender.sendMessage(event, Markov.load(users).generateRandom());
  }
}
