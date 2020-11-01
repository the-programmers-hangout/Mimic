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
import disparse.parser.reflection.Populate;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import uk.co.markg.mimic.command.HistoryGrabber;
import uk.co.markg.mimic.command.admin.AddChannels.ChannelRequest;
import uk.co.markg.mimic.database.ChannelRepository;
import uk.co.markg.mimic.database.MessageRepository;
import uk.co.markg.mimic.database.UserRepository;

public class EditChannels {
  private static final Logger logger = LogManager.getLogger(EditChannels.class);

  private MessageReceivedEvent event;
  private ChannelRequest req;
  private ChannelRepository channelRepo;
  private UserRepository userRepo;
  private MessageRepository messageRepo;
  private List<String> args;

  /**
   * Command execution method held by Disparse
   * 
   * @param request     The {@link disparse.discord.jda.DiscordRequest DiscordRequest} dispatched to
   *                    this command
   * @param req         The parsed {@link disparse.parser.reflection.Flag Flags} passed with the
   *                    command
   * @param channelRepo The {@link uk.co.markg.mimic.database.ChannelRepository ChannelRepository}
   *                    instance used to communicate with the database
   * @param userRepo    The {@link uk.co.markg.mimic.database.UserRepository UserRepository}
   *                    instance
   * @param messageRepo The {@link uk.co.markg.mimic.database.MessageRepository MessageRepository}
   *                    instance
   */
  @Populate
  public EditChannels(DiscordRequest request, ChannelRequest req, ChannelRepository channelRepo,
      UserRepository userRepo, MessageRepository messageRepo) {
    this.event = request.getEvent();
    this.req = req;
    this.channelRepo = channelRepo;
    this.userRepo = userRepo;
    this.messageRepo = messageRepo;
    this.args = request.getArgs();
  }

  /*
   * Method held by Disparse to begin command execution. Has a cooldown of five seconds per user.
   */
  @Cooldown(amount = 5, unit = ChronoUnit.SECONDS, scope = CooldownScope.USER,
      sendCooldownMessage = false)
  @CommandHandler(commandName = "channels.edit",
      description = "Edits added channel permissions. Collects user message history if read permission granted and deletes it if revoked.",
      perms = AbstractPermission.BAN_MEMBERS)
  public void executeEdit() {
    this.execute();
  }

  /**
   * Executes the command. Edits any valid channels and updates database. Sends a confirmation
   * message to discord.
   */
  private void execute() {
    String response = editChannels();
    event.getChannel().sendMessage(response).queue();
  }

  /**
   * Takes all channelids passed in and overwrites permissions in the database. Collects user
   * message history of all users for each channel if read permission granted and deletes it if
   * revoked.
   *
   * @return The response message to send back to the channel
   */

  private String editChannels() {
    var badChannels = new ArrayList<String>();
    for (String channelid : args) {
      var textChannel = event.getJDA().getTextChannelById(channelid);
      var channelidLong = Long.parseLong(channelid);
      var serverid = textChannel.getGuild().getIdLong();
      boolean channelExists = channelRepo.isChannelAdded(channelidLong);

      if (textChannel != null && channelExists) {
        var currentRead = channelRepo.hasReadPermission(channelidLong);
        channelRepo.updatePermissions(channelidLong, req.read, req.write);

        if (channelRepo.hasReadPermission(channelidLong) && !currentRead) {
          var userids = userRepo.getAllUserids(serverid);
          new HistoryGrabber(textChannel, userids).execute();
        }
        if (!channelRepo.hasReadPermission(channelidLong) && currentRead) {
          messageRepo.deleteByChannelId(channelidLong);
        }
        logger.info("Updated channel {} permissions in server {} to READ-{} and WRITE-{}.",
            channelid, serverid, req.read, req.write);
      } else {
        badChannels.add(channelid);
      }
    }

    return badChannels.isEmpty() ? "All input channels succesfully edited"
        : "Ignored arguments: " + String.join(",", badChannels);
  }

}
