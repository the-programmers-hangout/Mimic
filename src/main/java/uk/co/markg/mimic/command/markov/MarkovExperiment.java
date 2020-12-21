package uk.co.markg.mimic.command.markov;

import java.time.temporal.ChronoUnit;
import disparse.discord.jda.DiscordRequest;
import disparse.parser.dispatch.CooldownScope;
import disparse.parser.reflection.CommandHandler;
import disparse.parser.reflection.Cooldown;
import disparse.parser.reflection.MessageStrategy;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import uk.co.markg.mimic.database.ChannelRepository;
import uk.co.markg.mimic.database.UsageRepository;
import uk.co.markg.mimic.database.UserRepository;
import uk.co.markg.mimic.markov.MarkovLoader;
import uk.co.markg.mimic.markov.MarkovSender;
import uk.co.markg.mimic.markov.Trigram;

public class MarkovExperiment {

  @Cooldown(amount = 5, unit = ChronoUnit.SECONDS, scope = CooldownScope.USER,
      messageStrategy = MessageStrategy.REACT)
  @CommandHandler(commandName = "experimental",
      description = "Trigram experiment. [this command may change, break, or disappear over time]")
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
    UsageRepository.getRepository().save(MarkovExperiment.class, event);
    event.getChannel().sendTyping().queue();
    var markov = MarkovLoader.of(Trigram.class).loadServer(event.getGuild().getIdLong());
    MarkovSender.sendMessage(event, markov.generate());
  }

}
