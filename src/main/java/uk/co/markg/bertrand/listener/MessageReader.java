package uk.co.markg.bertrand.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import uk.co.markg.bertrand.App;
import uk.co.markg.bertrand.database.ChannelRepository;
import uk.co.markg.bertrand.database.MessageRepository;
import uk.co.markg.bertrand.database.UserRepository;

public class MessageReader extends ListenerAdapter {

  private static final Logger logger = LogManager.getLogger(MessageReader.class);

  private ChannelRepository channelRepo;
  private UserRepository userRepo;
  private MessageRepository messageRepo;

  public MessageReader() {
    this.channelRepo = ChannelRepository.getRepository();
    this.userRepo = UserRepository.getRepository();
    this.messageRepo = MessageRepository.getRepository();
  }

  /**
   * Builds a list of predicates to filter messages that will be used for building the markov chains
   * 
   * @return the list of predicates
   */
  private static List<Predicate<String>> getMessagePredicates() {
    var predicates = new ArrayList<Predicate<String>>();
    predicates.add(msg -> msg.matches("^\\W+[.*\\s\\S]*"));
    predicates.add(msg -> msg.split("\\s+|\\v").length < 3);
    predicates.add(msg -> msg.startsWith("`"));
    predicates.add(msg -> msg.startsWith(App.PREFIX));
    return predicates;
  }

  /**
   * Listener method triggered by the discord bot receiving a message
   * 
   * @param event the discord event
   */
  @Override
  public void onMessageReceived(MessageReceivedEvent e) {
    if (e.getAuthor().isBot()) {
      return;
    }
    if (isMessageConstraintsMet(e)) {
      logger.info("User: {}; Saved message: {}", e.getAuthor().getIdLong(),
          e.getMessage().getContentRaw());
      messageRepo.save(e.getAuthor().getIdLong(), e.getMessage());
    }
  }

  /**
   * Convenience method to hold message constraints. Checks whether the invoking user is opted in,
   * whether the bot has read access to the channel, and whether the message is considered valid
   * 
   * @param e      the discord event
   * @param userid the target userid
   * @return true if all constraints are satisfied
   */
  private boolean isMessageConstraintsMet(MessageReceivedEvent e) {
    return userRepo.isUserOptedIn(e.getAuthor().getIdLong())
        && messageIsValid(e.getMessage().getContentRaw())
        && channelRepo.hasReadPermission(e.getChannel().getIdLong());
  }

  /**
   * Tests a message against the list of predicates
   * 
   * @param message the message to test
   * @return true if the message is valid
   */
  public static boolean messageIsValid(Message message) {
    return messageIsValid(message.getContentRaw());
  }

  /**
   * Tests a message against the list of predicates
   * 
   * @param message the message to test
   * @return true if the message is valid
   */
  public static boolean messageIsValid(String message) {
    return !getMessagePredicates().stream().anyMatch(predicate -> predicate.test(message));
  }
}
