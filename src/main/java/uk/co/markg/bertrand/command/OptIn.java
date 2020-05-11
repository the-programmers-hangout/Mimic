package uk.co.markg.bertrand.command;

import org.jooq.DSLContext;
import disparse.parser.reflection.CommandHandler;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import static uk.co.markg.bertrand.db.tables.Users.USERS;

public class OptIn {

  @CommandHandler(commandName = "opt-in", description = "Opt-in")
  public void execute(MessageReceivedEvent event, DSLContext dsl) {
    long userid = event.getAuthor().getIdLong();
    if (isUserOptedIn(dsl, userid)) {
      event.getChannel().sendMessage("You're already in!").queue();
    } else {
      optInUser(event, dsl, userid);
    }
  }

  private void optInUser(MessageReceivedEvent event, DSLContext dsl, long userid) {
    dsl.insertInto(USERS).values(userid).execute();
    event.getChannel().sendMessage("You have been opted-in").queue();
  }

  private boolean isUserOptedIn(DSLContext dsl, long userid) {
    return dsl.fetchCount(dsl.selectFrom(USERS).where(USERS.USERID.eq(userid))) != 0;
  }
}
