package uk.co.markg.bertrand.command;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import disparse.discord.jda.DiscordRequest;
import disparse.parser.reflection.Populate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import uk.co.markg.bertrand.markov.MarkovSender;

public class OptIn {
  private static final Logger logger = LogManager.getLogger(OptIn.class);
  private static final int HISTORY_LIMIT = 50_000;
  private MessageReceivedEvent event;
  private UserRepository userRepo;
  private ChannelRepository channelRepo;

  /**
   * Required for static invokation of savehistory. Injecting other dependencies is unnecessary for
   * this case.
   */
  private OptIn() {
  }

  /**
   * Command execution method held by Disparse
   *
   * @param request     The discord request dispatched to this command
   * @param userRepo    The user repository used to communicate with the database
   * @param channelRepo The channel repository used to communicate with the database
   */
  @Populate
  public OptIn(DiscordRequest request, UserRepository userRepo, ChannelRepository channelRepo) {
    this.event = request.getEvent();
    this.userRepo = userRepo;
    this.channelRepo = channelRepo;
  }

  /**
   * Command execution method held by Disparse
   */
  @CommandHandler(commandName = "opt-in", description = "Opt-in for your messages to be read.")
  public void optInCommand() {
    this.execute();
  }

  /**
   * Executes the command. If the user is already opted in a message is returned to discord.
   * Otherwise the user is opted in.
   */
  private void execute() {
    long userid = event.getAuthor().getIdLong();
    if (userRepo.isUserOptedIn(userid)) {
      MarkovSender.alreadyOptedIn(event.getChannel());
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
    MarkovSender.optedIn(event.getChannel());
    var channels = channelRepo.getAll();
    for (Channels channel : channels) {
      var textChannel = event.getJDA().getTextChannelById(channel.getChannelid());
      if (textChannel != null && channel.getReadPerm()) {
        saveUserHistory(textChannel, List.of(userid));
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
  public static void initiateSaveUserHistory(TextChannel textChannel, List<Users> users) {
    new OptIn().saveUserHistory(users, textChannel);
  }

  /**
   * Collects, filters, and saves user history for a specific user in a specific channel.
   *
   * @param textChannel the target text channel
   * @param userid      the target user
   */
  private void saveUserHistory(List<Users> users, TextChannel textChannel) {
    var userids = users.stream().map(Users::getUserid).collect(Collectors.toList());
    saveUserHistory(textChannel, userids);
  }


  private void saveUserHistory(TextChannel textChannel, List<Long> userids) {
    var validHistoryMessages = getUserHistory(textChannel, userids).thenApply(filterMessages());
    var messages = buildMessageList(validHistoryMessages);
    MessageRepository messageRepository = MessageRepository.getRepository();
    messages.thenAccept(messageRepository::batchInsert);
    logger.info("Finished saving history");
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
  private CompletableFuture<List<MessagesRecord>> buildMessageList(
      CompletableFuture<? extends List<Message>> validHistoryMessages) {
    return validHistoryMessages.thenApply(
        history -> history.stream().map(msg -> buildMessage(msg)).collect(Collectors.toList()));
  }

  /**
   * Builds a {@link uk.co.markg.bertrand.db.tables.records.MessagesRecord MessagesRecord} from the
   * userid and {@link net.dv8tion.jda.api.entities.Message Message} content
   *
   * @param message the discord message content
   * @param userid  the sender of the message
   * @return the message as a {@link uk.co.markg.bertrand.db.tables.records.MessagesRecord}
   */
  private MessagesRecord buildMessage(Message message) {
    return new MessagesRecord(message.getIdLong(), message.getAuthor().getIdLong(),
        message.getContentRaw(), message.getChannel().getIdLong());
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
  private CompletableFuture<List<Message>> getUserHistory(TextChannel channel, List<Long> userids) {
    return channel.getIterableHistory().takeAsync(HISTORY_LIMIT).thenApply(list -> list.stream()
        .filter(m -> userids.contains(m.getAuthor().getIdLong())).collect(Collectors.toList()));
  }
}
