package uk.co.markg.bertrand.markov;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.co.markg.bertrand.database.MessageRepository;

public class Markov {

  private static final Logger logger = LogManager.getLogger(Markov.class);

  private static final String END_WORD = "END_WORD";
  private Map<String, Map<String, Double>> wordFrequencyMap;
  private List<String> startWords;
  private List<String> endWords;

  private Markov(List<String> inputs) {
    wordFrequencyMap = new HashMap<>();
    startWords = new ArrayList<>();
    endWords = new ArrayList<>();
    parseInput(inputs);
    calculateProbabilities();
  }

  public static Markov load(long userid) {
    return load(List.of(userid));
  }

  public static Markov load(List<Long> userids) {
    logger.info("Loaded chain for {}", userids);
    var inputs = MessageRepository.getRepository().getByUsers(userids);
    return new Markov(inputs);
  }

  /**
   * Convenience method to generate multiple sentences
   * 
   * @return the sentences joined together by a space character
   */
  public String generateRandom() {
    int sentences = ThreadLocalRandom.current().nextInt(5) + 1;
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < sentences; i++) {
      sb.append(generate()).append(" ");
    }
    return sb.toString();
  }

  /**
   * Generates a sentence from the markov chain
   * 
   * @return a complete sentence
   */
  public String generate() {
    int startNo = ThreadLocalRandom.current().nextInt(startWords.size());
    String word = startWords.get(startNo);
    List<String> sentence = new ArrayList<>();
    sentence.add(word);
    boolean endWordHit = false;
    while (!endWordHit) {
      var nextEntry = wordFrequencyMap.get(word);
      double rand = ThreadLocalRandom.current().nextDouble(1);
      for (var entry : nextEntry.entrySet()) {
        rand -= entry.getValue();
        if (rand <= 0) {
          word = entry.getKey();
          if (endWords.contains(word)) {
            endWordHit = true;
          }
          break;
        }
      }
      if (word.equals(END_WORD)) {
        break;
      }
      sentence.add(word);
    }
    return String.join(" ", sentence);
  }

  /**
   * Convenience method to parse multiple sentences
   * 
   * @param inputs the list of sentences
   */
  private void parseInput(List<String> inputs) {
    for (String input : inputs) {
      parseInput(input);
    }
  }

  /**
   * Parses a sentence into the word frequency map
   * 
   * @param input the sentence to parse
   */
  private void parseInput(String input) {
    String[] tokens = input.split("\\s+\\v?");
    if (tokens.length < 3) {
      throw new IllegalArgumentException(
          "Input '" + input + "'is too short. Must be greater than 3 tokens.");
    }
    for (int i = 0; i < tokens.length; i++) {
      String word = tokens[i].trim();
      if (word.isEmpty()) {
        continue;
      }
      word = escapeUserMention(word);
      if (i == 0 || isValidStartWord(word)) {
        startWords.add(word);
      }
      if (isEndWord(word) && !endWords.contains(word)) {
        endWords.add(word);
        insertWordFrequency(word, END_WORD);
        continue;
      }
      if (i == tokens.length - 1) {
        insertWordFrequency(word, END_WORD);
        break;
      }
      String nextWord = tokens[i + 1].trim();
      if (nextWord.isEmpty()) {
        continue;
      }
      nextWord = escapeUserMention(nextWord);
      if (wordFrequencyMap.containsKey(word)) {
        updateWordFrequency(word, nextWord);
      } else {
        insertWordFrequency(word, nextWord);
      }
    }
  }

  private boolean isValidStartWord(String word) {
    return word.charAt(0) == word.toUpperCase().charAt(0) && !startWords.contains(word);
  }

  /**
   * Escapes user mentions to avoid accidental pings
   * 
   * @param token the token to escape
   * @return the escaped token
   */
  private String escapeUserMention(String token) {
    return token.matches("\\S*(<@!?[0-9]+>)\\S*") ? token.replace('@', '$') : token;
  }

  /**
   * Checks whether a word can be matched as an end word. i.e. the word ends a sentence.
   * 
   * @param word the word to check
   * @return true if the word can be matched as an end word
   */
  private boolean isEndWord(String word) {
    List<Predicate<String>> predicates = new ArrayList<>();
    predicates.add(w -> w.endsWith("."));
    predicates.add(w -> w.endsWith("!"));
    predicates.add(w -> w.endsWith("?"));
    return predicates.stream().anyMatch(predicate -> predicate.test(word));
  }

  /**
   * Inserts a new word and follow word into the wordFrequencyMap
   * 
   * @param word       the main word
   * @param followWord the follow word
   */
  private void insertWordFrequency(String word, String followWord) {
    var followFrequency = new HashMap<String, Double>();
    followFrequency.put(followWord, 1.0);
    wordFrequencyMap.put(word, followFrequency);
  }

  /**
   * Updates the follow word frequency of a word in the wordFrequencyMap
   * 
   * @param key        the main word
   * @param followWord the follow word
   */
  private void updateWordFrequency(String key, String followWord) {
    var followFrequency = wordFrequencyMap.get(key);
    if (followFrequency.containsKey(followWord)) {
      followFrequency.put(followWord, followFrequency.get(followWord) + 1);
    } else {
      followFrequency.put(followWord, 1.0);
    }
    wordFrequencyMap.put(key, followFrequency);
  }

  /**
   * Calculate the probabilities for each of the follow words of each word in the map.
   */
  private void calculateProbabilities() {
    for (var entry : wordFrequencyMap.entrySet()) {
      int sum = 0;
      for (var followEntry : entry.getValue().entrySet()) {
        sum += followEntry.getValue();
      }
      for (var followEntry : entry.getValue().entrySet()) {
        double probability = followEntry.getValue() / sum;
        followEntry.setValue(probability);
      }
    }
  }

}
