package uk.co.markg.bertrand.command;

import java.util.ArrayList;
import java.util.List;
import disparse.parser.reflection.CommandHandler;
import disparse.parser.reflection.Flag;
import disparse.parser.reflection.ParsedEntity;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import uk.co.markg.bertrand.database.ChannelRepository;
import uk.co.markg.bertrand.database.UserRepository;
import uk.co.markg.bertrand.db.tables.pojos.Users;

public class AddChannels {
  private MessageReceivedEvent event;
  private ChannelRequest req;
  private ChannelRepository channelRepo;
  private UserRepository userRepo;
  private List<String> args;

  @ParsedEntity
  static class ChannelRequest {
    @Flag(shortName = 'r', longName = "read",
        description = "Whether the bot should read from the channel. Defaults to true")
    Boolean read = Boolean.FALSE;

    @Flag(shortName = 'w', longName = "write",
        description = "Whether the bot can write to the channel. Defaults to false")
    Boolean write = Boolean.FALSE;
  }

  /**
   * Constructs a new channel command
   * 
   * @param event       The discord message event that triggered the command
   * @param req         The parsed flags passed with the command
   * @param channelRepo The channel repository
   * @param userRepo    The user repository
   * @param args        The arguments passed with the command. Should be a list of channel ids
   */
  public AddChannels(MessageReceivedEvent event, ChannelRequest req, ChannelRepository channelRepo,
      UserRepository userRepo, List<String> args) {
    this.event = event;
    this.req = req;
    this.channelRepo = channelRepo;
    this.userRepo = userRepo;
    this.args = args;
  }

  /**
   * Method held by Disparse to begin command execution
   * 
   * @param event       The message event from discord that triggered the command
   * @param req         The command request flags
   * @param channelRepo The {@link uk.co.markg.bertrand.database.ChannelRepository
   *                    ChannelRepository} instance
   * @param userRepo    The {@link uk.co.markg.bertrand.database.UserRepository UserRepository}
   *                    instance
   * @param args        A list of parsed command arguments. Should be a list of channel ids
   */
  @CommandHandler(commandName = "channels.add",
      description = "Add channels. Defaults to read access only.", roles = "staff")
  public static void executeAdd(MessageReceivedEvent event, ChannelRequest req,
      ChannelRepository channelRepo, UserRepository userRepo, List<String> args) {
    new AddChannels(event, req, channelRepo, userRepo, args).execute();
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
      if (textChannel != null) {
        channelRepo.save(channelid, req.read, req.write);
        retrieveChannelHistory(textChannel);
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
    var users = userRepo.getAll();
    for (Users user : users) {
      OptIn.saveUserHistory(channel, user);
    }
  }
}
