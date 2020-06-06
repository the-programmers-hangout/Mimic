package uk.co.markg.bertrand.command.markov;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import disparse.parser.reflection.CommandHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import uk.co.markg.bertrand.database.MessageRepository;
import uk.co.markg.bertrand.database.UserRepository;

public class MarkvStats {

  private static final List<String> BLACKLIST_WORDS =
      List.of("the", "i", "and", "a", "it", "is", "to", "be", "you", "that", "in", "for", "of",
          "but", "if", "can", "with", "have", "not", "on", "or", "as", "that's", "just", "like",
          "this", "do", "so", "we", "at", "its", "an", "it's", "im", "i'm", "are", "was");

  @CommandHandler(commandName = "stats", description = "Displays statistics for the bot")
  public static void execute(MessageReceivedEvent event, UserRepository userRepo,
      MessageRepository messageRepo) {
    event.getChannel().sendTyping().queue();
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
    eb.addField("**Most Common Words**",
        "```" + String.join(", ", getMostUsedWords(wordMap, 30)) + "```", false);

    long userid = event.getAuthor().getIdLong();
    if (userRepo.isUserOptedIn(userid)) {
      var userMessages = messageRepo.getByUsers(List.of(userid));
      var userWordMap = calculateWordFrequency(userMessages);
      int userTokens = getTokenCount(userMessages);
      eb.addField("**Your Messages**", "```" + messageRepo.getCountByUserId(userid) + "```", true);
      eb.addField("**Your Total Tokens**", "```" + userTokens + "```", true);
      eb.addField("**Your Unique Words**", "```" + userWordMap.size() + "```", true);
      eb.addField("**Your Most Common Words**",
          "```" + String.join(", ", getMostUsedWords(userWordMap, 30)) + "```", false);
    }

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

  private static List<String> getMostUsedWords(Map<String, Integer> wordMap, int numOfWords) {
    List<Entry<String, Integer>> words = new ArrayList<>(wordMap.entrySet());
    words.sort(Entry.comparingByValue());
    Collections.reverse(words);
    for (Iterator<Entry<String, Integer>> iter = words.listIterator(); iter.hasNext();) {
      var entry = iter.next();
      if (BLACKLIST_WORDS.contains(entry.getKey().toLowerCase())) {
        iter.remove();
      }
    }
    var result = words.stream().limit(numOfWords).map(Entry::getKey).collect(Collectors.toList());
    return result;
  }

}
