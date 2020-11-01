package uk.co.markg.mimic.command;

import java.time.temporal.ChronoUnit;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import disparse.discord.jda.DiscordRequest;
import disparse.parser.dispatch.CooldownScope;
import disparse.parser.reflection.CommandHandler;
import disparse.parser.reflection.Cooldown;
import disparse.parser.reflection.Populate;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import uk.co.markg.mimic.database.ChannelRepository;
import uk.co.markg.mimic.database.ServerConfigRepository;
import uk.co.markg.mimic.database.UserRepository;
import uk.co.markg.mimic.db.tables.pojos.Channels;
import uk.co.markg.mimic.markov.MarkovSender;

public class OptIn {
  private static final Logger logger = LogManager.getLogger(OptIn.class);
  private MessageReceivedEvent event;
  private UserRepository userRepo;
  private ChannelRepository channelRepo;
  private ServerConfigRepository serverConfigRepo;

  /**
   * Command execution method held by Disparse
   *
   * @param request     The {@link disparse.discord.jda.DiscordRequest DiscordRequest} dispatched to
   *                    this command
   * @param userRepo    The {@link uk.co.markg.mimic.database.UserRepository UserRepository}
   *                    instance used to communicate with the database
   * @param channelRepo The {@link uk.co.markg.mimic.database.ChannelRepository ChannelRepository}
   *                    instance used to communicate with the database
   */
  @Populate
  public OptIn(DiscordRequest request, UserRepository userRepo, ChannelRepository channelRepo,
      ServerConfigRepository serverConfigRepo) {
    this.event = request.getEvent();
    this.userRepo = userRepo;
    this.channelRepo = channelRepo;
    this.serverConfigRepo = serverConfigRepo;
  }

  /**
   * Command execution method held by Disparse. Has a cooldown of ten seconds per user. Opts the
   * user in for their messages to be read. User must have the required opt-in role if it exists.
   */
  @Cooldown(amount = 10, unit = ChronoUnit.SECONDS, scope = CooldownScope.USER,
      sendCooldownMessage = false)
  @CommandHandler(commandName = "opt-in", description = "Opt-in for your messages to be read.")
  public void optInCommand() {
    long serverid = event.getGuild().getIdLong();
    String optInRole = serverConfigRepo.get(serverid).getOptInRole();
    if (optInRole.isEmpty() || userHasRole(optInRole)) {
      this.execute();
    } else {
      event.getChannel().sendMessage("You do not have the correct permissions to run: `opt-in`")
          .queue();
    }
  }

  /**
   * Checks if user has the required opt-in role.
   * 
   * @param optInRole The role the user has to have to be opted-in.
   * @return Whether or not the user has the role.
   */
  private boolean userHasRole(String optInRole) {
    var userRole = event.getMember().getRoles().stream()
        .filter(role -> role.getName().equals(optInRole)).findFirst();
    return userRole.isPresent();
  }

  /**
   * Executes the command. If the user is already opted in a message is returned to discord.
   * Otherwise the user is opted in.
   */
  private void execute() {
    long userid = event.getAuthor().getIdLong();
    if (userRepo.isUserOptedIn(userid, event.getGuild().getIdLong())) {
      MarkovSender.alreadyOptedIn(event.getChannel());
    } else {
      optInUser(userid);
    }
  }

  /**
   * Opts in a user by saving their id to the database and saving their history from all added
   * channels to the database.
   *
   * @param userid The discord userid of the user
   */
  private void optInUser(long userid) {
    userRepo.save(userid, event.getGuild().getIdLong());
    MarkovSender.optedIn(event.getChannel());
    var channels = channelRepo.getAll(event.getGuild().getIdLong());
    logger.info("Starting opt-in for user {} in server {}.", userid, event.getGuild().getIdLong());
    for (Channels channel : channels) {
      var textChannel = event.getJDA().getTextChannelById(channel.getChannelid());
      if (textChannel != null && channel.getReadPerm()) {
        new HistoryGrabber(textChannel, List.of(userid)).execute();
      }
    }
  }
}
