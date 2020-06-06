package uk.co.markg.bertrand.command.markov;

import java.awt.Color;
import disparse.parser.reflection.CommandHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import uk.co.markg.bertrand.database.MessageRepository;
import uk.co.markg.bertrand.database.UserRepository;

public class MarkvStats {


  @CommandHandler(commandName = "stats", description = "Displays statistics for the bot")
  public static void execute(MessageReceivedEvent event, UserRepository userRepo,
      MessageRepository messageRepo) {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Statistics");
    eb.setColor(Color.decode("#eb7701"));
    eb.addField("**Total Users**", "```" + userRepo.getCount() + "```", true);
    eb.addBlankField(true);
    eb.addField("**Total Messages**", "```" + messageRepo.getCount() + "```", true);
    eb.addField("**Total Tokens**", "```5```", true);
    eb.addBlankField(true);
    eb.addField("**Total Unique Words**", "```5```", true);
    eb.addField("**Most Common Words**", "```abc, def, ghi, jkl, mno, pqr, stu, vwx, yz```", false);

    event.getChannel().sendMessage(eb.build()).queue();
  }

}
