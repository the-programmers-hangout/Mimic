package uk.co.markg.bertrand.command;

import static uk.co.markg.bertrand.db.tables.Users.USERS;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jooq.DSLContext;
import disparse.parser.reflection.CommandHandler;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import uk.co.markg.bertrand.database.ChannelRepository;
import uk.co.markg.bertrand.database.JooqConnection;
import uk.co.markg.bertrand.database.UserRepository;
import uk.co.markg.bertrand.db.tables.pojos.Channels;
import uk.co.markg.bertrand.db.tables.pojos.Users;
import uk.co.markg.bertrand.db.tables.records.MessagesRecord;
import uk.co.markg.bertrand.listener.MessageReader;

public class OptIn {

  private static final int HISTORY_LIMIT = 100_000;
  private MessageReceivedEvent event;
  private UserRepository userRepo;
  private ChannelRepository channelRepo;

  private OptIn() {
  }

  public OptIn(MessageReceivedEvent event, UserRepository userRepo, ChannelRepository channelRepo) {
    this.event = event;
    this.userRepo = userRepo;
    this.channelRepo = channelRepo;
  }

  @CommandHandler(commandName = "opt-in", description = "Opt-in for your messages to be read.")
  public static void execute(MessageReceivedEvent event, UserRepository userRepo,
      ChannelRepository channelRepo) {
    new OptIn(event, userRepo, channelRepo).execute();
  }

  private void execute() {
    long userid = event.getAuthor().getIdLong();
    if (userRepo.isUserOptedIn(userid)) {
      event.getChannel().sendMessage("You're already in!").queue();
    } else {
      optInUser(userid);
    }
  }

  private void optInUser(long userid) {
    userRepo.save(userid);
    event.getChannel().sendMessage("You have been opted-in").queue();
    var channels = channelRepo.getAll();
    for (Channels channel : channels) {
      var textChannel = event.getJDA().getTextChannelById(channel.getChannelid());
      if (textChannel != null) {
        saveUserHistory(textChannel, userid);
      }
    }
  }

  public static void saveUserHistory(TextChannel textChannel, Users user) {
    new OptIn().saveUserHistory(textChannel, user.getUserid());
  }

  private void saveUserHistory(TextChannel textChannel, long userid) {
    var validHistoryMessages = getUserHistory(textChannel, userid).thenApply(filterMessages());
    var messages = buildMessageList(userid, validHistoryMessages);
    DSLContext dsl = JooqConnection.getJooqContext();
    messages.thenAccept(msgs -> dsl.batchInsert(msgs).execute());
  }

  private CompletableFuture<List<MessagesRecord>> buildMessageList(long userid,
      CompletableFuture<? extends List<Message>> validHistoryMessages) {
    return validHistoryMessages.thenApply(history -> history.stream()
        .map(msg -> buildMessage(msg, userid)).collect(Collectors.toList()));
  }

  private MessagesRecord buildMessage(Message message, long userid) {
    return new MessagesRecord(message.getIdLong(), userid, message.getContentRaw(),
        message.getChannel().getIdLong());
  }

  private Function<? super List<Message>, ? extends List<Message>> filterMessages() {
    return userHistory -> userHistory.stream().filter(msg -> MessageReader.messageIsValid(msg))
        .collect(Collectors.toList());
  }

  private CompletableFuture<List<Message>> getUserHistory(TextChannel channel, long userid) {
    return channel.getIterableHistory().takeAsync(HISTORY_LIMIT).thenApply(list -> list.stream()
        .filter(m -> m.getAuthor().getIdLong() == userid).collect(Collectors.toList()));
  }

  public static boolean isUserOptedIn(DSLContext dsl, long userid) {
    return dsl.selectFrom(USERS).where(USERS.USERID.eq(userid)).fetchOne(0, int.class) != 0;
  }
}
