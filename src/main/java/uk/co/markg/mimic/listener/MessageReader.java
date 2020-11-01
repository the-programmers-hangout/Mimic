package uk.co.markg.mimic.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import uk.co.markg.mimic.App;
import uk.co.markg.mimic.database.ChannelRepository;
import uk.co.markg.mimic.database.MessageRepository;
import uk.co.markg.mimic.database.UserRepository;

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
   * @return The list of predicates
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
   * @param event The discord event
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
   * @param e      The discord event
   * @param userid The target userid
   * @return True if all constraints are satisfied
   */
  private boolean isMessageConstraintsMet(MessageReceivedEvent e) {
    return userRepo.isUserOptedIn(e.getAuthor().getIdLong(), e.getGuild().getIdLong())
        && messageIsValid(e.getMessage().getContentRaw())
        && channelRepo.hasReadPermission(e.getChannel().getIdLong());
  }

  /**
   * Tests a message against the list of predicates
   * 
   * @param message The message to test
   * @return True if the message is valid
   */
  public static boolean messageIsValid(Message message) {
    return messageIsValid(message.getContentRaw());
  }

  /**
   * Tests a message against the list of predicates
   * 
   * @param message The message to test
   * @return True if the message is valid
   */
  public static boolean messageIsValid(String message) {
    return getMessagePredicates().stream().noneMatch(predicate -> predicate.test(message));
  }
}
