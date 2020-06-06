package uk.co.markg.bertrand.command.markov;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import disparse.parser.reflection.CommandHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import uk.co.markg.bertrand.database.MessageRepository;
import uk.co.markg.bertrand.database.UserRepository;

public class MarkvStats {



  @CommandHandler(commandName = "stats", description = "Displays statistics for the bot")
  public static void execute(MessageReceivedEvent event, UserRepository userRepo,
      MessageRepository messageRepo) {

    var messages = messageRepo.getByUsers(userRepo.getAllMarkovCandidateIds());
    var wordMap = calculateWordFrequency(messages);
    int tokens = getTokenCount(messages);

    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Statistics");
    eb.setColor(Color.decode("#eb7701"));
    eb.addField("**Total Users**", "```" + userRepo.getCount() + "```", true);
    eb.addBlankField(true);
    eb.addField("**Total Messages**", "```" + messageRepo.getCount() + "```", true);
    eb.addField("**Total Tokens**", "```" + tokens + "```", true);
    eb.addBlankField(true);
    eb.addField("**Total Unique Words**", "```" + wordMap.size() + "```", true);
    eb.addField("**Most Common Words**", "```abc, def, ghi, jkl, mno, pqr, stu, vwx, yz```", false);

    event.getChannel().sendMessage(eb.build()).queue();
  }

  private static int getTokenCount(List<String> messages) {
    int count = 0;
    for (String message : messages) {
      var tokens = message.split("\\s+\\v?");
      count += tokens.length;
    }
    return count;
  }

  private static Map<String, Integer> calculateWordFrequency(List<String> messages) {
    var map = new HashMap<String, Integer>();
    for (String message : messages) {
      var tokens = message.split("\\s+\\v?");
      for (String token : tokens) {
        if (map.containsKey(token)) {
          map.replace(token, map.get(token) + 1);
        } else {
          map.put(token, 1);
        }
      }
    }
    return map;
  }

}
