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

  private boolean messageConstraintsMet(MessageReceivedEvent event, long userid) {
    return messageContainsBotMention(event) && userRepo.isUserOptedIn(userid)
        && hasWritePermission(event);
  }

  private boolean hasWritePermission(MessageReceivedEvent event) {
    return channelRepo.hasWritePermission(event.getChannel().getIdLong());
  }

  private String generateReply() {
    long userid = getRandomUserId();
    Markov markov = loadMarkov(userid);
    int noOfSentences = ThreadLocalRandom.current().nextInt(5) + 1;
    return markov.generate(noOfSentences);
  }

  private long getRandomUserId() {
    var users = userRepo.getAll();
    return users.get(ThreadLocalRandom.current().nextInt(users.size())).getUserid();
  }

  private boolean messageContainsBotMention(MessageReceivedEvent event) {
    Member botMember = event.getGuild().getSelfMember();
    return event.getMessage().getMentionedMembers().contains(botMember);
  }

  private Markov loadMarkov(long userid) {
    var inputs = messageRepo.getByUser(userid);
    return new Markov(inputs);
  }

}
