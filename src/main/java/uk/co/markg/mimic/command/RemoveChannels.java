package uk.co.markg.mimic.command;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import disparse.discord.AbstractPermission;
import disparse.discord.jda.DiscordRequest;
import disparse.parser.dispatch.CooldownScope;
import disparse.parser.reflection.CommandHandler;
import disparse.parser.reflection.Cooldown;
import disparse.parser.reflection.Populate;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import uk.co.markg.mimic.database.ChannelRepository;

public class RemoveChannels {

  private MessageReceivedEvent event;
  private ChannelRepository channelRepo;
  private List<String> args;

  /**
   * Command execution method held by Disparse
   *
   * @param request     The discord request dispatched to this command
   * @param channelRepo The channel repository used to communicate with the database
   */
  @Populate
  public RemoveChannels(DiscordRequest request, ChannelRepository channelRepo) {
    this.event = request.getEvent();
    this.channelRepo = channelRepo;
    this.args = request.getArgs();
  }

  @Cooldown(amount = 5, unit = ChronoUnit.SECONDS, scope = CooldownScope.USER,
      sendCooldownMessage = false)
  /**
   * Command execution method held by Disparse
   */
  @CommandHandler(commandName = "channels.remove",
      description = "Remove channels from the database. All related messages are also removed.",
      perms = AbstractPermission.BAN_MEMBERS)
  public void executeRemove() {
    this.execute();
  }

  private void execute() {
    String response = removeChannels();
    event.getChannel().sendMessage(response).queue();
  }

  /**
   * Removes existing channels from the database and any messages saved from those channels
   *
   * @return A string indicating success or failure. Failure message includes a list of channels
   *         that could not be removed from the database
   */
  private String removeChannels() {
    var badChannels = new ArrayList<String>();
    for (String channelid : args) {
      if (channelRepo.delete(channelid) != 1) {
        badChannels.add(channelid);
      }
    }
    return badChannels.isEmpty() ? "All input channels successfully deleted"
        : "Ignored arguments: " + String.join(",", badChannels);
  }
}
