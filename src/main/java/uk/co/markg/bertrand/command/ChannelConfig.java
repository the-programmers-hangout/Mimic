package uk.co.markg.bertrand.command;

import static uk.co.markg.bertrand.db.tables.Messages.MESSAGES;
import static uk.co.markg.bertrand.db.tables.Channels.CHANNELS;
import static uk.co.markg.bertrand.db.tables.Users.USERS;
import java.util.List;
import org.jooq.DSLContext;
import disparse.parser.reflection.CommandHandler;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import uk.co.markg.bertrand.db.tables.pojos.Channels;
import uk.co.markg.bertrand.db.tables.pojos.Users;

public class ChannelConfig {

  @CommandHandler(commandName = "channels", description = "Lists all channels registered")
  public void executeList(MessageReceivedEvent event, DSLContext dsl) {
    var channels = dsl.selectFrom(CHANNELS).fetchInto(Channels.class);
    StringBuilder message = new StringBuilder();
    for (Channels channel : channels) {
      message.append("<#").append(channel.getChannelid()).append(">")
          .append(System.lineSeparator());
    }
    event.getChannel().sendMessage(message.toString()).queue();
  }

  @CommandHandler(commandName = "channels.add", description = "Add channels to read from")
  public void executeAdd(MessageReceivedEvent event, DSLContext dsl, List<String> args) {
    int addedChannels = 0;
    for (String channelid : args) {
      if (channelidIsValid(channelid)) {
        addedChannels += addChannel(dsl, channelid);
        retrieveChannelHistory(event, dsl, channelid);
      }
    }
    event.getChannel().sendMessage("Added " + addedChannels + " from " + args.size() + " inputs")
        .queue();
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

  private int addChannel(DSLContext dsl, String channelid) {
    return dsl.insertInto(CHANNELS).values(Long.parseLong(channelid)).execute();
  }

  @CommandHandler(commandName = "channels.remove", description = "Remove channels to read from")
  public void executeRemove(MessageReceivedEvent event, DSLContext dsl, List<String> args) {
    int removedChannels = 0;
    for (String channelid : args) {
      if (channelidIsValid(channelid)) {
        removedChannels += deleteChannel(dsl, channelid);
        deleteMessagesInChannel(dsl, channelid);
      }
    }
    event.getChannel()
        .sendMessage("Removed " + removedChannels + " from " + args.size() + " inputs").queue();
  }

  private void deleteMessagesInChannel(DSLContext dsl, String channelid) {
    dsl.deleteFrom(MESSAGES).where(MESSAGES.CHANNELID.eq(Long.parseLong(channelid))).execute();
  }

  private int deleteChannel(DSLContext dsl, String channelid) {
    return dsl.deleteFrom(CHANNELS).where(CHANNELS.CHANNELID.eq(Long.parseLong(channelid)))
        .execute();
  }

  private boolean channelidIsValid(String channelid) {
    return channelid.matches("[0-9]+");
  }

}
