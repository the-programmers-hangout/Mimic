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
import uk.co.markg.mimic.database.ChannelRepository;

public class RemoveChannels {
  private static final Logger logger = LogManager.getLogger(RemoveChannels.class);

  private MessageReceivedEvent event;
  private ChannelRepository channelRepo;
  private List<String> args;

  /**
   * Command execution method held by Disparse
   *
   * @param request     The {@link disparse.discord.jda.DiscordRequest DiscordRequest} dispatched to
   *                    this command
   * @param channelRepo The {@link uk.co.markg.mimic.database.ChannelRepository ChannelRepository}
   *                    instance used to communicate with the database
   */
  @Populate
  public RemoveChannels(DiscordRequest request, ChannelRepository channelRepo) {
    this.event = request.getEvent();
    this.channelRepo = channelRepo;
    this.args = request.getArgs();
  }

  /*
   * Method held by Disparse to begin command execution. Has a cooldown of five seconds per user.
   */
  @Cooldown(amount = 5, unit = ChronoUnit.SECONDS, scope = CooldownScope.USER,
      sendCooldownMessage = false)
  @CommandHandler(commandName = "channels.remove",
      description = "Remove channels from the database. All related messages are also removed.",
      perms = AbstractPermission.BAN_MEMBERS)
  public void executeRemove() {
    this.execute();
  }

  /**
   * Executes the command. Removes any valid channels and updates database. Sends a confirmation
   * message to discord.
   */
  private void execute() {
    String response = removeChannels();
    event.getChannel().sendMessage(response).queue();
  }

  /**
   * Removes existing channels from the database and any {@link net.dv8tion.jda.api.entities.Message
   * Messages} saved from those channels
   *
   * @return A string indicating success or failure. Failure message includes a list of channels
   *         that could not be removed from the database
   */
  private String removeChannels() {
    var badChannels = new ArrayList<String>();
    for (String channelid : args) {
      if (channelRepo.delete(channelid) != 1) {
        badChannels.add(channelid);
      } else {
        logger.info("Removed channel {} from server {}.", channelid, event.getGuild().getId());
      }
    }
    return badChannels.isEmpty() ? "All input channels successfully deleted"
        : "Ignored arguments: " + String.join(",", badChannels);
  }
}
