package uk.co.markg.bertrand.command;

import static uk.co.markg.bertrand.db.tables.Messages.MESSAGES;
import static uk.co.markg.bertrand.db.tables.Channels.CHANNELS;
import static uk.co.markg.bertrand.db.tables.Users.USERS;
import java.util.ArrayList;
import java.util.List;
import org.jooq.DSLContext;
import disparse.parser.reflection.CommandHandler;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import uk.co.markg.bertrand.db.tables.pojos.Channels;
import uk.co.markg.bertrand.db.tables.pojos.Users;

public class ChannelConfig {

  @CommandHandler(commandName = "channels", description = "Lists all channels registered",
      roles = "staff")
  public void executeList(MessageReceivedEvent event, DSLContext dsl) {
    var channels = getAllChannels(dsl);
    String message = buildListOfChannels(channels);
    event.getChannel().sendMessage(message).queue();
  }

  @CommandHandler(commandName = "channels.add", description = "Add channels to read from",
      roles = "staff")
  public void executeAdd(MessageReceivedEvent event, DSLContext dsl, List<String> args) {
    String response = addChannels(event, dsl, args);
    event.getChannel().sendMessage(response).queue();
  }

  @CommandHandler(commandName = "channels.remove", description = "Remove channels to read from",
      roles = "staff")
  public void executeRemove(MessageReceivedEvent event, DSLContext dsl, List<String> args) {
    String response = removeChannels(dsl, args);
    event.getChannel().sendMessage(response).queue();
  }

  private String addChannels(MessageReceivedEvent event, DSLContext dsl, List<String> args) {
    var badChannels = new ArrayList<String>();
    for (String channelid : args) {
      if (channelidIsValid(channelid)) {
        saveChannel(dsl, channelid);
        retrieveChannelHistory(event, dsl, channelid);
      } else {
        badChannels.add(channelid);
      }
    }
    return badChannels.isEmpty() ? "All channels succesfully added"
        : "Ignored arguments: " + String.join(",", badChannels);
  }

  private String removeChannels(DSLContext dsl, List<String> args) {
    var badChannels = new ArrayList<String>();
    for (String channelid : args) {
      if (channelidIsValid(channelid)) {
        deleteChannel(dsl, channelid);
        deleteMessagesInChannel(dsl, channelid);
      } else {
        badChannels.add(channelid);
      }
    }
    return badChannels.isEmpty() ? "All channels successfully deleted"
        : "Ignored arguments: " + String.join(",", badChannels);
  }

  private void retrieveChannelHistory(MessageReceivedEvent event, DSLContext dsl,
      String channelid) {
    var users = dsl.selectFrom(USERS).fetchInto(Users.class);
    var textChannel = event.getJDA().getTextChannelById(channelid);
    if (textChannel != null) {
      for (Users user : users) {
        OptIn.saveUserHistory(textChannel, dsl, user);
      }
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

  private boolean channelidIsValid(String channelid) {
    return channelid.matches("[0-9]+");
  }

  private int saveChannel(DSLContext dsl, String channelid) {
    return dsl.insertInto(CHANNELS).values(Long.parseLong(channelid)).execute();
  }

  private List<Channels> getAllChannels(DSLContext dsl) {
    return dsl.selectFrom(CHANNELS).fetchInto(Channels.class);
  }

  private void deleteMessagesInChannel(DSLContext dsl, String channelid) {
    dsl.deleteFrom(MESSAGES).where(MESSAGES.CHANNELID.eq(Long.parseLong(channelid))).execute();
  }

  private int deleteChannel(DSLContext dsl, String channelid) {
    return dsl.deleteFrom(CHANNELS).where(CHANNELS.CHANNELID.eq(Long.parseLong(channelid)))
        .execute();
  }
}
