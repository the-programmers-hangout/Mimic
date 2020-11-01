package uk.co.markg.mimic.command.markov;

import java.awt.Color;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import disparse.discord.jda.DiscordRequest;
import disparse.discord.jda.DiscordResponse;
import disparse.parser.dispatch.CooldownScope;
import disparse.parser.reflection.CommandHandler;
import disparse.parser.reflection.Cooldown;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import uk.co.markg.mimic.database.MessageRepository;
import uk.co.markg.mimic.database.UserRepository;
import uk.co.markg.mimic.markov.MarkovSender;

public class MarkovStats {

  private static final List<String> BLACKLIST_WORDS =
      List.of("the", "i", "and", "a", "it", "is", "to", "be", "you", "that", "in", "for", "of",
          "but", "if", "can", "with", "have", "not", "on", "or", "as", "that's", "just", "like",
          "this", "do", "so", "we", "at", "its", "an", "it's", "im", "i'm", "are", "was");

  /**
   * Method held by Disparse to begin command execution. Has a cooldown of five seconds per user.
   * 
   * Executes the command. Displays the user's statistics for message count, total tokens, unique
   * words and a list of the most commonly used words. To execute, user must be opt-ed in.
   * 
   * @param request     The {@link disparse.discord.jda.DiscordRequest DiscordRequest} dispatched to
   *                    this command
   * @param userRepo    The {@link uk.co.markg.mimic.database.UserRepository UserRepository}
   *                    instance
   * @param messageRepo The {@link uk.co.markg.mimic.database.MessageRepository MessageRepository}
   *                    instance
   * @return Embed message of the user's statistics
   */
  @Cooldown(amount = 5, unit = ChronoUnit.SECONDS, scope = CooldownScope.USER,
      sendCooldownMessage = false)
  @CommandHandler(commandName = "stats", description = "Display statistics of your messages")
  public static DiscordResponse execute(DiscordRequest request, UserRepository userRepo,
      MessageRepository messageRepo) {
    MessageReceivedEvent event = request.getEvent();
    long userid = event.getAuthor().getIdLong();
    if (!userRepo.isUserOptedIn(userid, event.getGuild().getIdLong())) {
      MarkovSender.notOptedIn(event.getChannel());
      return DiscordResponse.noop();
    }
    event.getChannel().sendTyping().queue();
    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Statistics");
    eb.setColor(Color.decode("#eb7701"));
    var userMessages = messageRepo.getByUsers(List.of(userid), event.getGuild().getIdLong());
    var userWordMap = calculateWordFrequency(userMessages);
    int userTokens = getTokenCount(userMessages);
    eb.addField("**Your Messages**", "```" + messageRepo.getCountByUserId(userid) + "```", true);
    eb.addField("**Your Total Tokens**", "```" + userTokens + "```", true);
    eb.addField("**Your Unique Words**", "```" + userWordMap.size() + "```", true);
    eb.addField("**Your Most Common Words**",
        "```" + String.join(", ", getMostUsedWords(userWordMap, 30)) + "```", false);
    return DiscordResponse.of(eb);
  }

  /**
   * Method held by Disparse to begin command execution. Has cooldown of one minute per channel.
   * 
   * Executes the command. Displays all users statistics for number of opted-in users message count,
   * total tokens, unique words and a list of the most commonly used words. To execute, user must be
   * opt-ed in.
   * 
   * @param request     The discord request dispatched to this command
   * @param userRepo    The {@link uk.co.markg.mimic.database.UserRepository UserRepository}
   *                    instance
   * @param messageRepo The {@link uk.co.markg.mimic.database.MessageRepository MessageRepository}
   *                    instance
   * @return Embed message of the server's statistics
   */
  @Cooldown(amount = 1, unit = ChronoUnit.MINUTES, scope = CooldownScope.CHANNEL,
      sendCooldownMessage = false)
  @CommandHandler(commandName = "allstats", description = "Display statistics for all users")
  public static DiscordResponse executeAll(DiscordRequest request, UserRepository userRepo,
      MessageRepository messageRepo) {
    var event = request.getEvent();
    event.getChannel().sendTyping().queue();
    long serverid = event.getGuild().getIdLong();
    var messages = messageRepo.getByUsers(userRepo.getAllMarkovCandidateIds(serverid), serverid);
    var wordMap = calculateWordFrequency(messages);

    EmbedBuilder eb = new EmbedBuilder();
    eb.setTitle("Statistics");
    eb.setColor(Color.decode("#eb7701"));
    eb.addField("**Total Users**", "```" + userRepo.getCount() + "```", true);
    eb.addBlankField(true);
    eb.addField("**Total Messages**", "```" + messageRepo.getCount() + "```", true);
    eb.addField("**Total Tokens**", "```" + getTokenCount(messages) + "```", true);
    eb.addBlankField(true);
    eb.addField("**Total Unique Words**", "```" + messageRepo.getUniqueWordCount() + "```", true);
    eb.addField("**Most Common Words**",
        "```" + String.join(", ", getMostUsedWords(wordMap, 30)) + "```", false);
    return DiscordResponse.of(eb);
  }

  /**
   * Gets the valid token count from list of messages saved.
   * 
   * @param messages The list of all {@link net.dv8tion.jda.api.entities.Message Messages} saved
   * @return The count of valid tokens in the list of {@link net.dv8tion.jda.api.entities.Message
   *         Messages}
   */
  private static int getTokenCount(List<String> messages) {
    int count = 0;
    for (String message : messages) {
      var tokens = message.split("\\s+\\v?");
      count += tokens.length;
    }
    return count;
  }

  /**
   * Calculates the frequency of words in the list of messages provided and creates a map of every
   * word and its frequency in that list.
   * 
   * @param messages The list of all {@link net.dv8tion.jda.api.entities.Message Messages} saved
   * @return The map of words and their frequency
   */
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

  /**
   * Gets the numOfWords most commonly used from the word map provided.
   * 
   * @param wordMap    The map of words and their frequency
   * @param numOfWords The desired number of most commonly used words
   * @return The stream of most commonly used words
   */
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
    return words.stream().limit(numOfWords).map(Entry::getKey).collect(Collectors.toList());
  }

}
