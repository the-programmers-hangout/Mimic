package uk.co.markg.bertrand.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import uk.co.markg.bertrand.App;
import uk.co.markg.bertrand.database.ChannelRepository;
import uk.co.markg.bertrand.database.MessageRepository;
import uk.co.markg.bertrand.database.UserRepository;

public class MessageReader extends ListenerAdapter {

  private ChannelRepository channelRepo;
  private UserRepository userRepo;
  private MessageRepository messageRepo;

  public MessageReader() {
    this.channelRepo = ChannelRepository.getRepository();
    this.userRepo = UserRepository.getRepository();
    this.messageRepo = MessageRepository.getRepository();
  }

  private static List<Predicate<String>> getMessagePredicates() {
    var predicates = new ArrayList<Predicate<String>>();
    predicates.add(msg -> msg.matches("^\\W+[.*\\s\\S]*"));
    predicates.add(msg -> msg.split("\\s").length < 4);
    predicates.add(msg -> msg.startsWith("`"));
    predicates.add(msg -> msg.startsWith(App.PREFIX));
    return predicates;
  }

  @Override
  public void onMessageReceived(MessageReceivedEvent e) {
    if (e.getAuthor().isBot()) {
      return;
    }
    long userid = e.getAuthor().getIdLong();
    if (isMessageConstraintsMet(e, userid)) {
      messageRepo.save(userid, e.getMessage());
    }
  }

  private boolean isMessageConstraintsMet(MessageReceivedEvent e, long userid) {
    return userRepo.isUserOptedIn(userid) && messageIsValid(e.getMessage())
        && channelRepo.isChannelAdded(userid);
  }

  public static boolean messageIsValid(Message message) {
    String text = message.getContentRaw();
    return !getMessagePredicates().stream().anyMatch(predicate -> predicate.test(text));
  }
}
