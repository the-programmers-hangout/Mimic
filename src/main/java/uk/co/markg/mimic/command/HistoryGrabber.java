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

  public HistoryGrabber(TextChannel channel, List<Long> userids) {
    this.channel = channel;
    this.userids = userids;
  }

  public void execute() {
    logger.info("run");
    getUserHistory(this::saveMessages);
  }

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
   * Prepare and save messages into the database
   * 
   * @param messages
   */
  private void saveMessages(List<Message> messages) {
    var messageRecords = buildMessageList(messages);
    var messageRepository = MessageRepository.getRepository();
    messageRepository.batchInsert(messageRecords);
  }

  /**
   * Builds a list of MessagesRecords from found valid discord messages ready to be saved into the
   * database.
   * 
   * @param validHistoryMessages
   * @return
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
