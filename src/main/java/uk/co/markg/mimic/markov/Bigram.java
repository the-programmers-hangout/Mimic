package uk.co.markg.mimic.markov;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import com.esotericsoftware.kryo.io.Output;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Bigram implements Markov {

  private static final Logger logger = LogManager.getLogger(Bigram.class);
  private static final String END_WORD = "END_WORD";
  private static final String FILE_END = ".markov";
  private Map<String, WeightedCollection> wordMap;
  private Set<String> startWords;
  private Set<String> endWords;

  public Bigram() {
    wordMap = new HashMap<>();
    startWords = new HashSet<>();
    endWords = new HashSet<>();
  }

  /**
   * Saves the {@link uk.co.markg.mimic.markov.Bigram Markov} instance onto a file.
   * 
   * @param file The file
   * @throws IOException
   */
  @Override
  public void save(String file) throws IOException {
    var f = new File(file + FILE_END);
    logger.info("Saving file {}", f.getAbsolutePath());
    Output output = new Output(new FileOutputStream(new File(file + FILE_END)));
    kryo.writeObject(output, this);
    output.close();
  }

  /**
   * Gets a word the Markov sentence can start with. If the String given is a valid start word the
   * method returns the argument. If it isn't the method will return a random start word from the
   * database.
   * 
   * @param start Start word provided from the user
   * @return The start word provided or chosen
   */
  private String getStartWord(String start) {
    if (!start.isEmpty() && wordMap.containsKey(start)) {
      return start;
    }
    int startNo = ThreadLocalRandom.current().nextInt(startWords.size());
    Iterator<String> itr = startWords.iterator();
    for (int i = 0; i < startNo; i++) {
      itr.next();
    }
    return itr.next();
  }

  /**
   * Generates a sentence from the markov chain with a given start word.
   * 
   * @param start The provided start word
   * @return The generated markov chain
   */
  @Override
  public String generate(String start) {
    String word = getStartWord(start);
    List<String> sentence = new ArrayList<>();
    sentence.add(word);
    boolean endWordHit = false;
    while (!endWordHit) {
      var nextEntry = wordMap.get(word);
      word = nextEntry.getRandom().map(WeightedElement::getElement).orElse("");
      if (endWords.contains(word)) {
        endWordHit = true;
      }
      if (word.equals(END_WORD)) {
        break;
      }
      sentence.add(word);
    }
    String s = String.join(" ", sentence);
    logger.debug("Generated: {}", s);
    if (s.matches("(.*[^.!?`+>\\-=_+:@~;'#\\[\\]{}\\(\\)\\/\\|\\\\]$)")) {
      s = s + SENTENCE_ENDS.getRandom().map(WeightedElement::getElement).orElse("@@@@@@@");
    }
    if (!start.isEmpty() && !s.startsWith(start)) {
      s = start + " " + s;
    }
    return s;
  }

  /**
   * Parses a sentence into the wordMap
   * 
   * @param input The sentence to parse
   */
  @Override
  public void parseInput(String input) {
    String[] tokens = input.split("\\s+\\v?");
    if (tokens.length < 3) {
      throw new IllegalArgumentException(
          "Input '" + input + "'is too short. Must be greater than 3 tokens.");
    }
    for (int i = 0; i < tokens.length; i++) {
      String word = tokens[i];
      if (word.isEmpty()) {
        continue;
      }
      if (i == 0) {
        startWords.add(word);
      } else if (isEndWord(word)) {
        endWords.add(word);
        insertWordFrequency(word, END_WORD);
        continue;
      }
      if (i == tokens.length - 1) {
        insertWordFrequency(word, END_WORD);
        break;
      }
      String nextWord = tokens[i + 1];
      if (nextWord.isEmpty()) {
        continue;
      }
      if (wordMap.containsKey(word)) {
        updateWordFrequency(word, nextWord);
      } else {
        insertWordFrequency(word, nextWord);
      }
    }
  }

  /**
   * Checks whether a word can be matched as an end word. i.e. the word ends a sentence.
   * 
   * @param word The word to check
   * @return True if the word can be matched as an end word
   */
  private boolean isEndWord(String word) {
    for (String stop : VALID_END_WORD_STOPS) {
      if (word.endsWith(stop)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Inserts a new word and follow word into the wordMap
   * 
   * @param word       The main word
   * @param followWord The follow word
   */
  private void insertWordFrequency(String word, String followWord) {
    var wc = new WeightedCollection();
    wc.add(new WeightedElement(followWord, 1));
    wordMap.put(word, wc);
  }

  /**
   * Updates the follow word frequency of a word in the wordMap
   * 
   * @param key        The main word
   * @param followWord The follow word
   */
  private void updateWordFrequency(String key, String followWord) {
    var followFrequency = wordMap.get(key);
    followFrequency.get(followWord).ifPresentOrElse(
        fw -> followFrequency.update(fw, fw.getWeight() + 1),
        () -> followFrequency.add(new WeightedElement(followWord, 1)));
  }

  @Override
  public String getFileEnd() {
    return FILE_END;
  }
}
