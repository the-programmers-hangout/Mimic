package uk.co.markg.bertrand.database;

import static uk.co.markg.bertrand.db.tables.Messages.MESSAGES;
import java.util.List;
import org.jooq.DSLContext;
import disparse.parser.reflection.Injectable;
import net.dv8tion.jda.api.entities.Message;
import uk.co.markg.bertrand.db.tables.pojos.Messages;
import uk.co.markg.bertrand.db.tables.records.MessagesRecord;

public class MessageRepository {

  private DSLContext dsl;

  /**
   * {@link disparse.parser.reflection.Injectable Injectable} method used by disparse upon command
   * invocation.
   * 
   * @return a new message repository instance
   */
  @Injectable
  public static MessageRepository getRepository() {
    return new MessageRepository();
  }

  private MessageRepository() {
    dsl = JooqConnection.getJooqContext();
  }

  /**
   * Create a {@link uk.co.markg.bertrand.db.tables.pojos.Messages Messages} instance from a userid
   * and a discord message and saves it to the database
   * 
   * @param userid  the userid of the message author
   * @param message the discord message object
   * @return the number of rows inserted
   */
  public int save(long userid, Message message) {
    var msg = new Messages(message.getIdLong(), userid, message.getContentRaw(),
        message.getChannel().getIdLong());
    return save(msg);
  }

  /**
   * Save a message into the database
   * 
   * @param message the message to be saved
   * @return the number of rows inserted
   */
  public int save(Messages message) {
    return dsl.insertInto(MESSAGES).values(message).execute();
  }

  /**
   * Returns a list of messages as strings belonging to a particular user
   * 
   * @param userid the target user
   * @return the list of messages
   */
  public List<String> getByUser(long userid) {
    return dsl.select(MESSAGES.CONTENT).from(MESSAGES).where(MESSAGES.USERID.eq(userid))
        .fetchInto(String.class);
  }

  /**
   * Batch inserts a list of {@link uk.co.markg.bertrand.db.tables.records.MessagesRecord
   * MessageRecord}
   * 
   * @param batch the batch of messages to insert
   */
  public void batchInsert(List<MessagesRecord> batch) {
    dsl.batchInsert(batch).execute();
  }

}
