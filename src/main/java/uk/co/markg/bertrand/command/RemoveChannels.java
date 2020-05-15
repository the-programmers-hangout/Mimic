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

  @CommandHandler(commandName = "channels.remove", description = "Remove channels to read from",
      roles = "staff")
  public static void executeRemove(MessageReceivedEvent event, ChannelRepository repo,
      List<String> args) {
    new RemoveChannels(event, repo, args).execute();
  }

  public void execute() {
    String response = removeChannels();
    event.getChannel().sendMessage(response).queue();
  }

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
