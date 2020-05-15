package uk.co.markg.bertrand.command;

import java.util.List;
import disparse.parser.reflection.CommandHandler;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import uk.co.markg.bertrand.database.ChannelRepository;
import uk.co.markg.bertrand.db.tables.pojos.Channels;

public class ListChannels {
  private MessageReceivedEvent event;
  private ChannelRepository channelRepo;

  public ListChannels(MessageReceivedEvent event, ChannelRepository channelRepo) {
    this.event = event;
    this.channelRepo = channelRepo;
  }

  @CommandHandler(commandName = "channels", description = "Lists all channels registered",
      roles = "staff")
  public static void executeList(MessageReceivedEvent event, ChannelRepository repo) {
    new ListChannels(event, repo).execute();
  }

  private void execute() {
    var channels = channelRepo.getAll();
    String message = buildListOfChannels(channels);
    event.getChannel().sendMessage(message).queue();
  }

  private String buildListOfChannels(List<Channels> channels) {
    StringBuilder message = new StringBuilder();
    for (Channels channel : channels) {
      message.append("<#").append(channel.getChannelid());
      message.append(">").append(System.lineSeparator());
    }
    return message.toString();
  }
}
