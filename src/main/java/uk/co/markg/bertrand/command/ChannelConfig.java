package uk.co.markg.bertrand.command;

import static uk.co.markg.bertrand.db.tables.Channels.CHANNELS;
import java.util.List;
import org.jooq.DSLContext;
import disparse.parser.reflection.CommandHandler;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ChannelConfig {

  @CommandHandler(commandName = "channel.add", description = "Add channels to read from")
  public void executeAdd(MessageReceivedEvent event, DSLContext dsl, List<String> args) {
    int addedChannels = 0;
    for (String channelid : args) {
      if (channelidIsValid(channelid)) {
        addedChannels += addChannel(dsl, channelid);
      }
    }
    event.getChannel().sendMessage("Added " + addedChannels + " from " + args.size() + " inputs")
        .queue();
  }

  private int addChannel(DSLContext dsl, String channelid) {
    return dsl.insertInto(CHANNELS).values(Long.parseLong(channelid)).execute();
  }

  @CommandHandler(commandName = "channel.remove", description = "Remove channels to read from")
  public void executeRemove(MessageReceivedEvent event, DSLContext dsl, List<String> args) {
    int removedChannels = 0;
    for (String channelid : args) {
      if (channelidIsValid(channelid)) {
        removedChannels += deleteChannel(dsl, channelid);
      }
    }
    event.getChannel()
        .sendMessage("Removed " + removedChannels + " from " + args.size() + " inputs").queue();
  }

  private int deleteChannel(DSLContext dsl, String channelid) {
    return dsl.deleteFrom(CHANNELS).where(CHANNELS.CHANNELID.eq(Long.parseLong(channelid)))
        .execute();
  }

  private boolean channelidIsValid(String channelid) {
    return channelid.matches("[0-9]+");
  }

}
