package uk.co.markg.bertrand.command;

import java.util.ArrayList;
import java.util.List;
import disparse.parser.reflection.CommandHandler;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import uk.co.markg.bertrand.database.ChannelRepository;

public class RemoveChannels {

  private MessageReceivedEvent event;
  private ChannelRepository channelRepo;
  private List<String> args;

  public RemoveChannels(MessageReceivedEvent event, ChannelRepository channelRepo,
      List<String> args) {
    this.event = event;
    this.channelRepo = channelRepo;
    this.args = args;
  }

  /**
   * Command execution method held by Disparse
   * 
   * @param event The message event from discord that triggered the command
   * @param repo  The channel repository used to communicate with the database
   */
  @CommandHandler(commandName = "channels.remove", description = "Remove channels to read from",
      roles = "staff")
  public static void executeRemove(MessageReceivedEvent event, ChannelRepository repo,
      List<String> args) {
    new RemoveChannels(event, repo, args).execute();
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
