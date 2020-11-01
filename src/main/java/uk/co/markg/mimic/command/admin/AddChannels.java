package uk.co.markg.mimic.command.admin;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import disparse.discord.AbstractPermission;
import disparse.discord.jda.DiscordRequest;
import disparse.parser.dispatch.CooldownScope;
import disparse.parser.reflection.CommandHandler;
import disparse.parser.reflection.Cooldown;
import disparse.parser.reflection.Flag;
import disparse.parser.reflection.ParsedEntity;
import disparse.parser.reflection.Populate;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import uk.co.markg.mimic.command.HistoryGrabber;
import uk.co.markg.mimic.database.ChannelRepository;
import uk.co.markg.mimic.database.UserRepository;

public class AddChannels {
  private static final Logger logger = LogManager.getLogger(AddChannels.class);

  private MessageReceivedEvent event;
  private ChannelRequest req;
  private ChannelRepository channelRepo;
  private UserRepository userRepo;
  private List<String> args;

  /**
   * Creates two new {@link disparse.parser.reflection.Flag Flags} for the channel's read and write
   * permissions. Both default to false.
   * 
   * Note- Read permission refer to the bot's ability to read the message history in the channel and
   * add the opted-in users {@link net.dv8tion.jda.api.entities.Message Messages} to the database.
   * Write permission refer to the bot's ability to execute any Markov command in the channel.
   */
  @ParsedEntity
  static class ChannelRequest {
    @Flag(shortName = 'r', longName = "read",
        description = "Whether the bot should read from the channel. Defaults to false")
    Boolean read = Boolean.FALSE;

    @Flag(shortName = 'w', longName = "write",
        description = "Whether the bot can write to the channel. Defaults to false")
    Boolean write = Boolean.FALSE;
  }

  /**
   * Constructs a new channel command
   *
   * @param request     The {@link disparse.discord.jda.DiscordRequest DiscordRequest} dispatched to
   *                    this command
   * @param req         The parsed {@link disparse.parser.reflection.Flag Flags} passed with the
   *                    command
   * @param channelRepo The {@link uk.co.markg.mimic.database.ChannelRepository ChannelRepository}
   *                    instance used to communicate with the database
   * @param userRepo    The {@link uk.co.markg.mimic.database.UserRepository UserRepository}
   *                    instance
   */
  @Populate
  public AddChannels(DiscordRequest request, ChannelRequest req, ChannelRepository channelRepo,
      UserRepository userRepo) {
    this.event = request.getEvent();
    this.req = req;
    this.channelRepo = channelRepo;
    this.userRepo = userRepo;
    this.args = request.getArgs();
  }

  /**
   * Method held by Disparse to begin command execution. Has a cooldown of five seconds per user.
   */
  @Cooldown(amount = 5, unit = ChronoUnit.SECONDS, scope = CooldownScope.USER,
      sendCooldownMessage = false)
  @CommandHandler(commandName = "channels.add",
      description = "Add channels. Allows configurable read and write permissions.",
      perms = AbstractPermission.BAN_MEMBERS)
  public void executeAdd() {
    this.execute();
  }

  /**
   * Executes the command. Adds any valid channels to the database. Sends a confirmation message to
   * discord.
   */
  private void execute() {
    String response = addChannels();
    event.getChannel().sendMessage(response).queue();
  }

  /**
   * Takes all channelids passed in and adds them to the database. Collects user message history of
   * all users for each channel
   *
   * @return The response message to send back to the channel
   */
  private String addChannels() {
    var badChannels = new ArrayList<String>();
    for (String channelid : args) {
      var textChannel = event.getJDA().getTextChannelById(channelid);
      var channelidLong = Long.parseLong(channelid);
      if (textChannel != null && !channelRepo.isChannelAdded(channelidLong)) {
        channelRepo.save(channelid, req.read, req.write, event.getGuild().getIdLong());
        logger.info("Added channel {} in server {} with permissions READ-{} and WRITE-{}.",
            channelid, event.getGuild().getId(), req.read, req.write);
        if (req.read) {
          logger.info("Reading from channel {}", channelid);
          retrieveChannelHistory(textChannel);
        }
      } else {
        badChannels.add(channelid);
      }
    }
    return badChannels.isEmpty() ? "All input channels succesfully added"
        : "Ignored arguments: " + String.join(",", badChannels);
  }

  /**
   * Collect channel history for the specificed {@link net.dv8tion.jda.api.entities.TextChannel
   * TextChannel} for all users in the database
   *
   * @param channel The target channel
   */
  private void retrieveChannelHistory(TextChannel channel) {
    var serverid = channel.getGuild().getIdLong();
    var userids = userRepo.getAllUserids(serverid);
    logger.info("Found {} users", userids.size());
    new HistoryGrabber(channel, userids).execute();
  }
}
