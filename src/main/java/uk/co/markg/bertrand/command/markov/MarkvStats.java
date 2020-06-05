package uk.co.markg.bertrand.command.markov;

import disparse.parser.reflection.CommandHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import java.awt.Color;

public class MarkvStats {


  @CommandHandler(commandName = "stats", description = "Displays statistics for the bot")
  public static void execute(MessageReceivedEvent event) {
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Statistics");
    eb.setColor(Color.decode("#eb7701"));
    eb.addField("**Total Users**", "```5```", true);
    eb.addBlankField(true);
    eb.addField("**Total Messages**", "```5```", true);
    eb.addField("**Total Tokens**", "```5```", true);
    eb.addBlankField(true);
    eb.addField("**Total Unique Words**", "```5```", true);
    eb.addField("**Most Common Words**", "```abc, def, ghi, jkl, mno, pqr, stu, vwx, yz```", false);

    event.getChannel().sendMessage(eb.build()).queue();
  }

}
