package uk.co.markg.mimic.command;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import uk.co.markg.mimic.database.MessageRepository;
import uk.co.markg.mimic.db.tables.records.MessagesRecord;
import uk.co.markg.mimic.listener.MessageReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HistoryGrabber {

  private static final Logger logger = LogManager.getLogger(HistoryGrabber.class);
  private static final int HISTORY_LIMIT = 20_000;
  private TextChannel channel;
  private List<Long> userids;

  /**
   * @param channel The discord channel id
   * @param userids The list of opted-in userids
   */
  public HistoryGrabber(TextChannel channel, List<Long> userids) {
    this.channel = channel;
    this.userids = userids;
  }

  /**
   * Executes the command. Grabs the user's message history and saves it to the database.
   */
  public void execute() {
    logger.info("run");
    getUserHistory(this::saveMessages);
  }

  /**
   * Adds message history to a list and passes list into the callback method. Method grabs the last
   * HISTORY_LIMIT number of messages.
   * 
   * @param callback The method referenced. Takes a list of
   *                 {@link net.dv8tion.jda.api.entities.Message Messages} as an argument.
   */
  private void getUserHistory(Consumer<List<Message>> callback) {
    List<Message> messages = new ArrayList<>(HISTORY_LIMIT);
    AtomicInteger historyLimit = new AtomicInteger(HISTORY_LIMIT);
    channel.getIterableHistory().cache(false).forEachAsync(message -> {
      if (userids.contains(message.getAuthor().getIdLong())
          && MessageReader.messageIsValid(message)) {
        messages.add(message);
      }
      return historyLimit.decrementAndGet() != 0;
    }).thenRun(() -> callback.accept(messages));
  }

  /**
   * Prepare and save {@link net.dv8tion.jda.api.entities.Message messages} into the database.
   * 
   * @param messages The list of all messages to be saved
   */
  private void saveMessages(List<Message> messages) {
    var messageRecords = buildMessageList(messages);
    var messageRepository = MessageRepository.getRepository();
    messageRepository.batchInsert(messageRecords);
  }

  /**
   * Builds a list of {@link uk.co.markg.mimic.db.tables.records.MessagesRecord MessagesRecord} from
   * found valid discord {@link net.dv8tion.jda.api.entities.Message messages} ready to be saved
   * into the database.
   * 
   * @param validHistoryMessages The list of valid discord messages
   * @return A list of {@link uk.co.markg.mimic.db.tables.records.MessagesRecord MessagesRecord}
   *         built from the discord {@link net.dv8tion.jda.api.entities.Message messages}.
   */
  private List<MessagesRecord> buildMessageList(List<Message> validHistoryMessages) {
    return validHistoryMessages.stream().map(this::buildMessage).collect(Collectors.toList());
  }

  /**
   * Builds a {@link uk.co.markg.mimic.db.tables.records.MessagesRecord MessagesRecord} from the
   * userid and {@link net.dv8tion.jda.api.entities.Message Message} content
   *
   * @param message the discord message content
   * @param userid  the sender of the message
   * @return the message as a {@link uk.co.markg.mimic.db.tables.records.MessagesRecord}
   */
  private MessagesRecord buildMessage(Message message) {
    return new MessagesRecord(message.getIdLong(), message.getAuthor().getIdLong(),
        message.getContentRaw(), message.getChannel().getIdLong(), message.getGuild().getIdLong());
  }
}
