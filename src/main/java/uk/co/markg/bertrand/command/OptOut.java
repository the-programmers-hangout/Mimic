package uk.co.markg.bertrand.command;

import static uk.co.markg.bertrand.db.tables.Messages.MESSAGES;
import static uk.co.markg.bertrand.db.tables.Users.USERS;
import org.jooq.DSLContext;
import disparse.parser.reflection.CommandHandler;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class OptOut {

  @CommandHandler(commandName = "opt-out", description = "Opt-out for all messages to be removed.")
  public void execute(MessageReceivedEvent event, DSLContext dsl) {
    long userid = event.getAuthor().getIdLong();
    if (OptIn.isUserOptedIn(dsl, userid)) {
      optOutUser(event, dsl, userid);
    } else {
      event.getChannel().sendMessage("You're already out!").queue();
    }
  }

  private void optOutUser(MessageReceivedEvent event, DSLContext dsl, long userid) {
    dsl.deleteFrom(USERS).where(USERS.USERID.eq(userid)).execute();
    dsl.deleteFrom(MESSAGES).where(MESSAGES.USERID.eq(userid)).execute();
    event.getChannel().sendMessage("You've been opted out!").queue();
  }
}
