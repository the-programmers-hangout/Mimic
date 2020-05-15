package uk.co.markg.bertrand.command;

import java.util.ArrayList;
import java.util.List;
import disparse.parser.reflection.CommandHandler;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import uk.co.markg.bertrand.database.ChannelRepository;
import uk.co.markg.bertrand.database.UserRepository;
import uk.co.markg.bertrand.db.tables.pojos.Channels;
import uk.co.markg.bertrand.db.tables.pojos.Users;

public class ChannelConfig {

  @CommandHandler(commandName = "channels", description = "Lists all channels registered",
      roles = "staff")
  public void executeList(MessageReceivedEvent event, ChannelRepository repo) {
    var channels = repo.getAll();
    String message = buildListOfChannels(channels);
    event.getChannel().sendMessage(message).queue();
  }

  @CommandHandler(commandName = "channels.add", description = "Add channels to read from",
      roles = "staff")
  public void executeAdd(MessageReceivedEvent event, ChannelRepository repo,
      UserRepository userRepo, List<String> args) {
    String response = addChannels(event, repo, userRepo, args);
    event.getChannel().sendMessage(response).queue();
  }

  @CommandHandler(commandName = "channels.remove", description = "Remove channels to read from",
      roles = "staff")
  public void executeRemove(MessageReceivedEvent event, ChannelRepository repo, List<String> args) {
    String response = removeChannels(repo, args);
    event.getChannel().sendMessage(response).queue();
  }

  private String addChannels(MessageReceivedEvent event, ChannelRepository repo,
      UserRepository userRepo, List<String> args) {
    var badChannels = new ArrayList<String>();
    for (String channelid : args) {
      var textChannel = event.getJDA().getTextChannelById(channelid);
      if (textChannel != null) {
        repo.save(channelid);
        retrieveChannelHistory(textChannel, userRepo);
      } else {
        badChannels.add(channelid);
      }
    }
    return badChannels.isEmpty() ? "All channels succesfully added"
        : "Ignored arguments: " + String.join(",", badChannels);
  }

  private String removeChannels(ChannelRepository repo, List<String> args) {
    var badChannels = new ArrayList<String>();
    for (String channelid : args) {
      if (repo.delete(channelid) != 1) {
        badChannels.add(channelid);
      }
    }
    return badChannels.isEmpty() ? "All channels successfully deleted"
        : "Ignored arguments: " + String.join(",", badChannels);
  }

  private void retrieveChannelHistory(TextChannel channel, UserRepository userRepo) {
    var users = userRepo.getAll();
    for (Users user : users) {
      OptIn.saveUserHistory(channel, user);
    }
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
