package uk.co.markg.bertrand.listener;

import static uk.co.markg.bertrand.db.tables.Messages.MESSAGES;
import static uk.co.markg.bertrand.db.tables.Users.USERS;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import org.jooq.DSLContext;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageReader extends ListenerAdapter {

  private DSLContext dsl;

  public MessageReader(DSLContext context) {
    dsl = context;
  }

  private static List<Predicate<String>> getMessagePredicates() {
    List<Predicate<String>> predicates = new ArrayList<>();
    predicates.add(msg -> msg.matches("^\\W+[.*\\s\\S]*"));
    predicates.add(msg -> msg.split("\\s").length < 4);
    predicates.add(msg -> msg.startsWith("`"));
    return predicates;
  }

  @Override
  public void onMessageReceived(MessageReceivedEvent e) {
    if (e.getAuthor().isBot()) {
      return;
    }
    long userid = e.getAuthor().getIdLong();

    if (authorIsOptedIn(userid) && messageIsValid(e.getMessage())) {
      saveMessage(userid, e.getMessage());
    }

  }

  public static boolean messageIsValid(Message message) {
    String text = message.getContentRaw();
    return !getMessagePredicates().stream().anyMatch(predicate -> predicate.test(text));
  }

  private void saveMessage(long userid, Message message) {
    dsl.insertInto(MESSAGES).values(message.getIdLong(), userid, message.getContentRaw()).execute();
  }

  private boolean authorIsOptedIn(long userid) {
    return dsl.selectFrom(USERS).where(USERS.USERID.eq(userid)).fetchOne(0, int.class) != 0;
  }

}
