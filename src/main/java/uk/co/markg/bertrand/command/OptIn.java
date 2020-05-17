package uk.co.markg.bertrand.command;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import disparse.parser.reflection.CommandHandler;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import uk.co.markg.bertrand.database.ChannelRepository;
import uk.co.markg.bertrand.database.MessageRepository;
import uk.co.markg.bertrand.database.UserRepository;
import uk.co.markg.bertrand.db.tables.pojos.Channels;
import uk.co.markg.bertrand.db.tables.pojos.Users;
import uk.co.markg.bertrand.db.tables.records.MessagesRecord;
import uk.co.markg.bertrand.listener.MessageReader;

public class OptIn {

  private static final int HISTORY_LIMIT = 10_000;
  private MessageReceivedEvent event;
  private UserRepository userRepo;
  private ChannelRepository channelRepo;

  /**
   * Required for static invokation of savehistory. Injecting other dependencies is unnecessary for
   * this case.
   */
  private OptIn() {
  }

  public OptIn(MessageReceivedEvent event, UserRepository userRepo, ChannelRepository channelRepo) {
    this.event = event;
    this.userRepo = userRepo;
    this.channelRepo = channelRepo;
  }

  /**
   * Command execution method held by Disparse
   * 
   * @param event       The message event from discord that triggered the command
   * @param userRepo    The user repository used to communicate with the database
   * @param channelRepo The channel repository used to communicate with the database
   */
  @CommandHandler(commandName = "opt-in", description = "Opt-in for your messages to be read.")
  public static void execute(MessageReceivedEvent event, UserRepository userRepo,
      ChannelRepository channelRepo) {
    new OptIn(event, userRepo, channelRepo).execute();
  }

  /**
   * Executes the command. If the user is already opted in a message is returned to discord.
   * Otherwise the user is opted in.
   */
  private void execute() {
    long userid = event.getAuthor().getIdLong();
    if (userRepo.isUserOptedIn(userid)) {
      event.getChannel().sendMessage("You're already in!").queue();
    } else {
      optInUser(userid);
    }
  }

  /**
   * Opts in a user by saving their id to the database and saving their history from all added
   * channels to the database.
   * 
   * @param userid the discord userid of the user
   */
  private void optInUser(long userid) {
    userRepo.save(userid);
    event.getChannel().sendMessage("You have been opted-in. I'll start saving your messages!")
        .queue();
    var channels = channelRepo.getAll();
    for (Channels channel : channels) {
      var textChannel = event.getJDA().getTextChannelById(channel.getChannelid());
      if (textChannel != null && channel.getReadPerm()) {
        saveUserHistory(textChannel, userid);
      }
    }
  }

  /**
   * Triggers saving of user history for a channel. This method is called externally to the class so
   * it can instanciate itself with the no-args constructor.
   * 
   * @param textChannel the target channel to read messages from
   * @param user        the target user to save history for
   */
  public static void saveUserHistory(TextChannel textChannel, Users user) {
    new OptIn().saveUserHistory(textChannel, user.getUserid());
  }

  /**
   * Collects, filters, and saves user history for a specific user in a specific channel.
   * 
   * @param textChannel the target text channel
   * @param userid      the target user
   */
  private void saveUserHistory(TextChannel textChannel, long userid) {
    var validHistoryMessages = getUserHistory(textChannel, userid).thenApply(filterMessages());
    var messages = buildMessageList(userid, validHistoryMessages);
    MessageRepository messageRepository = MessageRepository.getRepository();
    messages.thenAccept(messageRepository::batchInsert);
  }

  /**
   * Transforms the list of discord {@link net.dv8tion.jda.api.entities.Message Message}s into a
   * list of {@link uk.co.markg.bertrand.db.tables.records.MessagesRecord MessagesRecord} so that
   * they can be batch inserted into the database
   * 
   * @param userid               the target user
   * @param validHistoryMessages A list of valid history messages
   * @return the list of MessagesRecord
   */
  private CompletableFuture<List<MessagesRecord>> buildMessageList(long userid,
      CompletableFuture<? extends List<Message>> validHistoryMessages) {
    return validHistoryMessages.thenApply(history -> history.stream()
        .map(msg -> buildMessage(msg, userid)).collect(Collectors.toList()));
  }

  /**
   * Builds a {@link uk.co.markg.bertrand.db.tables.records.MessagesRecord MessagesRecord} from the
   * userid and {@link net.dv8tion.jda.api.entities.Message Message} content
   * 
   * @param message the discord message content
   * @param userid  the sender of the message
   * @return the message as a {@link uk.co.markg.bertrand.db.tables.records.MessagesRecord}
   */
  private MessagesRecord buildMessage(Message message, long userid) {
    return new MessagesRecord(message.getIdLong(), userid, message.getContentRaw(),
        message.getChannel().getIdLong());
  }

  /**
   * Filter messages by validaty predicates in {@link uk.co.markg.bertrand.listener.MessageReader
   * MessageReader}
   * 
   * @return The function to apply to a collection of messages
   */
  private Function<? super List<Message>, ? extends List<Message>> filterMessages() {
    return userHistory -> userHistory.stream().filter(msg -> MessageReader.messageIsValid(msg))
        .collect(Collectors.toList());
  }

  /**
   * Collects up to the last {@link OptIn#HISTORY_LIMIT} messages in the specified channel and
   * filters them for the specified user.
   * 
   * @param channel the target channel
   * @param userid  the target user
   * @return a {@link java.util.concurrent.CompletableFuture CompletableFuture} list of messages
   */
  private CompletableFuture<List<Message>> getUserHistory(TextChannel channel, long userid) {
    return channel.getIterableHistory().takeAsync(HISTORY_LIMIT).thenApply(list -> list.stream()
        .filter(m -> m.getAuthor().getIdLong() == userid).collect(Collectors.toList()));
  }
}
