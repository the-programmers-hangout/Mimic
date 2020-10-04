package uk.co.markg.mimic.command;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import disparse.discord.AbstractPermission;
import disparse.discord.jda.DiscordRequest;
import disparse.parser.reflection.CommandHandler;
import disparse.parser.reflection.Populate;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import uk.co.markg.mimic.command.AddChannels.ChannelRequest;
import uk.co.markg.mimic.database.ChannelRepository;
import uk.co.markg.mimic.database.UserRepository;
import uk.co.markg.mimic.db.tables.pojos.Users;

public class EditChannels {

  private MessageReceivedEvent event;
  private ChannelRequest req;
  private ChannelRepository channelRepo;
  private UserRepository userRepo;
  private List<String> args;

  /**
   * Command execution method held by Disparse
   *
   */
  @Populate
  public EditChannels(DiscordRequest request, ChannelRequest req, ChannelRepository channelRepo,
      UserRepository userRepo) {
    this.event = request.getEvent();
    this.req = req;
    this.channelRepo = channelRepo;
    this.userRepo = userRepo;
    this.args = request.getArgs();
  }

  /**
   * Command execution method held by Disparse
   */
  @CommandHandler(commandName = "channels.edit", description = "Edit channels on database.",
      perms = AbstractPermission.BAN_MEMBERS)

  public void executeEdit() {
    this.execute();
  }

  /**
   * Executes the command. Adds any valid channels to the database. Sends a confirmation message to
   * discord.
   */
  private void execute() {
    String response = editChannels();
    event.getChannel().sendMessage(response).queue();
  }


  private String editChannels() {
    var badChannels = new ArrayList<String>();
    for (String channelid : args) {
      var textChannel = event.getJDA().getTextChannelById(channelid);
      var channelidLong = Long.parseLong(channelid);
      boolean channelExists = channelRepo.isChannelAdded(channelidLong);

      if (textChannel != null && channelExists) {
        channelRepo.updatePermissions(channelidLong, req.read, req.write);

        if (channelRepo.hasReadPermission(channelidLong)) {
          var userids =
              userRepo.getAll().stream().map(Users::getUserid).collect(Collectors.toList());
          new HistoryGrabber(textChannel, userids).execute();
        } else {
          channelRepo.delete(channelid);
        }
      } else {
        badChannels.add(channelid);
      }
    }

    return badChannels.isEmpty() ? "All input channels succesfully edited"
        : "Ignored arguments: " + String.join(",", badChannels);
  }

}
