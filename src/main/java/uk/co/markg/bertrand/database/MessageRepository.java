package uk.co.markg.bertrand.database;

import static uk.co.markg.bertrand.db.tables.Messages.MESSAGES;
import java.util.List;
import org.jooq.DSLContext;
import disparse.parser.reflection.Injectable;
import net.dv8tion.jda.api.entities.Message;
import uk.co.markg.bertrand.db.tables.pojos.Messages;

public class MessageRepository {

  private DSLContext dsl;

  @Injectable
  public static MessageRepository getRepository() {
    return new MessageRepository();
  }

  private MessageRepository() {
    dsl = JooqConnection.getJooqContext();
  }

  public int save(long userid, Message message) {
    var msg = new Messages(message.getIdLong(), userid, message.getContentRaw(),
        message.getChannel().getIdLong());
    return save(msg);
  }

  public int save(Messages message) {
    return dsl.insertInto(MESSAGES).values(message).execute();
  }
  
  public List<String> getByUser(long userid) {
    return dsl.select(MESSAGES.CONTENT).from(MESSAGES).where(MESSAGES.USERID.eq(userid))
        .fetchInto(String.class);
  }

}
