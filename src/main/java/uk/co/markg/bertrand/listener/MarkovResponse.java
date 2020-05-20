package uk.co.markg.bertrand.listener;

import java.util.concurrent.ThreadLocalRandom;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import uk.co.markg.bertrand.database.ChannelRepository;
import uk.co.markg.bertrand.database.MessageRepository;
import uk.co.markg.bertrand.database.UserRepository;
import uk.co.markg.bertrand.markov.Markov;

public class MarkovResponse extends ListenerAdapter {
  private static final Logger logger = LogManager.getLogger(MarkovResponse.class);

  private ChannelRepository channelRepo;
  private MessageRepository messageRepo;
  private UserRepository userRepo;

  public MarkovResponse() {
    this.channelRepo = ChannelRepository.getRepository();
    this.messageRepo = MessageRepository.getRepository();
    this.userRepo = UserRepository.getRepository();
  }

  /**
   * Listener method triggered by the discord bot receiving a message
   * 
   * @param event the discord event
   */
  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    if (event.getAuthor().isBot()) {
      return;
    }
    if (event.isFromType(ChannelType.PRIVATE)) {
      return;
    }
    long userid = event.getAuthor().getIdLong();
    if (messageConstraintsMet(event, userid)) {
      event.getChannel().sendMessage(generateReply()).queue();
    }
  }

  /**
   * Convenience method to hold message constraints. Checks whether the message contains a mention
   * of the current bot, whether the invoking user is opted in, and whether the bot has write
   * permissions in the invocation channel
   * 
   * @param event  the discord event
   * @param userid the target userid
   * @return true if all constraints are satisfied
   */
  private boolean messageConstraintsMet(MessageReceivedEvent event, long userid) {
    return messageContainsBotMention(event) && userRepo.isUserOptedIn(userid)
        && hasWritePermission(event);
  }

  /**
   * Returns whether the bot has write permissions for a channel to send a markov chain response
   * 
   * @param event the discord event
   * @return true if the bot has write permissions for the channel
   */
  private boolean hasWritePermission(MessageReceivedEvent event) {
    return channelRepo.hasWritePermission(event.getChannel().getIdLong());
  }

  /**
   * Generates a random number of sentences using a random user for each sentence. May revert, we'll
   * see how it plays out
   * 
   * @return Sentence generated
   */
  private String generateReply() {
    int noOfSentences = ThreadLocalRandom.current().nextInt(4) + 1;
    logger.info("Generating {} sentences", noOfSentences);
    var sb = new StringBuilder();
    for (int i = 0; i < noOfSentences; i++) {
      long userid = getRandomUserId();
      logger.info("Loading markov chain for user {}", userid);
      Markov markov = loadMarkov(userid);
      sb.append(markov.generate()).append(" ");
    }
    String response = sb.toString();
    logger.info("Generated response: {}", response);
    return response;
  }

  /**
   * Selects a random user from all opted in users
   * 
   * @return the random user id
   */
  private long getRandomUserId() {
    var users = userRepo.getAllMarkovCandidates();
    return users.get(ThreadLocalRandom.current().nextInt(users.size())).getUserid();
  }

  /**
   * Checks whether the message contains a mention of the current bot
   * 
   * @param event The message event from discord
   * @return true if the message contains a mention of the current bot
   */
  private boolean messageContainsBotMention(MessageReceivedEvent event) {
    Member botMember = event.getGuild().getSelfMember();
    return event.getMessage().getMentionedMembers().contains(botMember);
  }

  /**
   * Loads a markov chain with messages from the specified user.
   * 
   * @param userid the target user
   * @return the markov chain for the user
   */
  private Markov loadMarkov(long userid) {
    var inputs = messageRepo.getByUser(userid);
    return new Markov(inputs);
  }

}
