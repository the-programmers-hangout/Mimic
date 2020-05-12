package uk.co.markg.bertrand.command;

import static uk.co.markg.bertrand.db.tables.Users.USERS;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jooq.DSLContext;
import disparse.parser.reflection.CommandHandler;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import uk.co.markg.bertrand.db.tables.records.MessagesRecord;
import uk.co.markg.bertrand.listener.MessageReader;

public class OptIn {

  @CommandHandler(commandName = "opt-in", description = "Opt-in")
  public void execute(MessageReceivedEvent event, DSLContext dsl) {
    long userid = event.getAuthor().getIdLong();
    if (isUserOptedIn(dsl, userid)) {
      event.getChannel().sendMessage("You're already in!").queue();
    } else {
      optInUser(event, dsl, userid);
    }
  }

  private void optInUser(MessageReceivedEvent event, DSLContext dsl, long userid) {
    dsl.insertInto(USERS).values(userid).execute();
    event.getChannel().sendMessage("You have been opted-in").queue();
    saveUserHistory(event, dsl, userid);
  }

  private void saveUserHistory(MessageReceivedEvent event, DSLContext dsl, long userid) {
    var validHistoryMessages = getUserHistory(event, userid).thenApply(filterMessages());
    var messages = buildMessageList(userid, validHistoryMessages);
    messages.thenAccept(msgs -> dsl.batchInsert(msgs).execute());
  }

  private CompletableFuture<List<MessagesRecord>> buildMessageList(long userid,
      CompletableFuture<? extends List<Message>> validHistoryMessages) {
    return validHistoryMessages.thenApply(history -> history.stream()
        .map(msg -> buildMessage(msg, userid)).collect(Collectors.toList()));
  }

  private MessagesRecord buildMessage(Message message, long userid) {
    return new MessagesRecord(message.getIdLong(), userid, message.getContentRaw());
  }

  private Function<? super List<Message>, ? extends List<Message>> filterMessages() {
    return userHistory -> userHistory.stream().filter(msg -> MessageReader.messageIsValid(msg))
        .collect(Collectors.toList());
  }

  private CompletableFuture<List<Message>> getUserHistory(MessageReceivedEvent event, long userid) {
    return event.getChannel().getIterableHistory().takeAsync(20_000).thenApply(list -> list.stream()
        .filter(m -> m.getAuthor().getIdLong() == userid).collect(Collectors.toList()));
  }

  public static boolean isUserOptedIn(DSLContext dsl, long userid) {
    return dsl.selectFrom(USERS).where(USERS.USERID.eq(userid)).fetchOne(0, int.class) != 0;
  }
}
