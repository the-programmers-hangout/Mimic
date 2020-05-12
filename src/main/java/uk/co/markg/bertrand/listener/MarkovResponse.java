package uk.co.markg.bertrand.listener;

import static uk.co.markg.bertrand.db.tables.Messages.MESSAGES;
import java.util.concurrent.ThreadLocalRandom;
import org.jooq.DSLContext;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import uk.co.markg.bertrand.command.OptIn;
import uk.co.markg.bertrand.markov.Markov;

public class MarkovResponse extends ListenerAdapter {

  private DSLContext dsl;

  public MarkovResponse(DSLContext context) {
    dsl = context;
  }

  @Override
  public void onMessageReceived(MessageReceivedEvent event) {
    if (event.getAuthor().isBot()) {
      return;
    }
    long userid = event.getAuthor().getIdLong();
    if (messageContainsBotMention(event) && OptIn.isUserOptedIn(dsl, userid)) {
      Markov markov = loadMarkov(userid);
      int noOfSentences = ThreadLocalRandom.current().nextInt(5) + 1;
      event.getChannel().sendMessage(markov.generate(noOfSentences)).queue();
    }
  }

  private boolean messageContainsBotMention(MessageReceivedEvent event) {
    Member botMember = event.getGuild().getSelfMember();
    return event.getMessage().getMentionedMembers().contains(botMember);
  }

  private Markov loadMarkov(long userid) {
    var inputs = dsl.select(MESSAGES.CONTENT).from(MESSAGES).where(MESSAGES.USERID.eq(userid))
        .fetchInto(String.class);
    return new Markov(inputs);
  }

}
