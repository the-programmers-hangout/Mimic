package uk.co.markg.bertrand.listener;

import java.util.concurrent.ThreadLocalRandom;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import uk.co.markg.bertrand.database.ChannelRepository;
import uk.co.markg.bertrand.database.MessageRepository;
import uk.co.markg.bertrand.database.UserRepository;
import uk.co.markg.bertrand.markov.Markov;

public class MarkovResponse extends ListenerAdapter {

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
   * Selects a random user and loads up a markov chain of their messages and generates a random
   * number of sentences.
   * 
   * @return Sentence generated
   */
  private String generateReply() {
    long userid = getRandomUserId();
    Markov markov = loadMarkov(userid);
    int noOfSentences = ThreadLocalRandom.current().nextInt(5) + 1;
    return markov.generate(noOfSentences);
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
