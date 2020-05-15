package uk.co.markg.bertrand.command;

import java.util.ArrayList;
import java.util.List;
import disparse.parser.reflection.CommandHandler;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import uk.co.markg.bertrand.database.ChannelRepository;
import uk.co.markg.bertrand.database.UserRepository;
import uk.co.markg.bertrand.db.tables.pojos.Users;

public class AddChannels {
  private MessageReceivedEvent event;
  private ChannelRepository channelRepo;
  private UserRepository userRepo;
  private List<String> args;

  public AddChannels(MessageReceivedEvent event, ChannelRepository channelRepo,
      UserRepository userRepo, List<String> args) {
    this.event = event;
    this.channelRepo = channelRepo;
    this.userRepo = userRepo;
    this.args = args;
  }

  @CommandHandler(commandName = "channels.add", description = "Add channels to read from",
      roles = "staff")
  public static void executeAdd(MessageReceivedEvent event, ChannelRepository channelRepo,
      UserRepository userRepo, List<String> args) {
    new AddChannels(event, channelRepo, userRepo, args).execute();
  }

  private void execute() {
    String response = addChannels();
    event.getChannel().sendMessage(response).queue();
  }

  private String addChannels() {
    var badChannels = new ArrayList<String>();
    for (String channelid : args) {
      var textChannel = event.getJDA().getTextChannelById(channelid);
      if (textChannel != null) {
        channelRepo.save(channelid);
        retrieveChannelHistory(textChannel);
      } else {
        badChannels.add(channelid);
      }
    }
    return badChannels.isEmpty() ? "All channels succesfully added"
        : "Ignored arguments: " + String.join(",", badChannels);
  }

  private void retrieveChannelHistory(TextChannel channel) {
    var users = userRepo.getAll();
    for (Users user : users) {
      OptIn.saveUserHistory(channel, user);
    }
  }
}
