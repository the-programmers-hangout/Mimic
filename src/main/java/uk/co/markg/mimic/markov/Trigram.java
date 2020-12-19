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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import com.esotericsoftware.kryo.io.Output;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Trigram implements Markov {

  private static final Logger logger = LogManager.getLogger(Trigram.class);
  private static final String FILE_END = ".trigram";
  private Map<String, WeightedCollection<Pair>> wordMap;
  private Set<String> startWords;
  private Set<String> endWords;

  public Trigram() {
    wordMap = new HashMap<>();
    startWords = new HashSet<>();
    endWords = new HashSet<>();
  }

  @Override
  public String generate(String start) {
    String word = getStartWord(start);
    List<String> sentence = new ArrayList<>();
    sentence.add(word);
    boolean endWordHit = false;
    var currentPair = wordMap.get(word);
    var pair = currentPair.getRandom().getElement();
    sentence.add(pair.getFirst());
    while (!endWordHit) {
      String secondWord = pair.getFirst();
      var secondWordCollection = wordMap.get(secondWord);
      String thirdWord = pair.getSecond();

      if (secondWord == null || secondWord.equals(END_WORD)) {
        break;
      }

      if (thirdWord == null || thirdWord.equals(END_WORD)) {
        break;
      }
      sentence.add(thirdWord);

      var secondPair = findPair(secondWordCollection, thirdWord);
      if (secondPair.isPresent()) {
        pair = secondPair.get().getElement();
      } else {
        break;
      }
      // iterate through new pairs
      // if pair starts with third word use it
      // else get map for third word
    }
    String s = String.join(" ", sentence);
    logger.debug("Generated: {}", s);
    if (s.matches("(.*[^.!?`+>\\-=_+:@~;'#\\[\\]{}\\(\\)\\/\\|\\\\]$)")) {
      s = s + SENTENCE_ENDS.getRandom().getElement();
    }
    if (!start.isEmpty() && !s.startsWith(start)) {
      s = start + " " + s;
    }
    return s;
  }

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

  private Optional<WeightedElement<Pair>> findPair(WeightedCollection<Pair> wc, String word) {
    var pairs = wc.getAll();
    for (WeightedElement<Pair> weightedElement : pairs) {
      if (weightedElement.getElement().getFirst().equals(word)) {
        return Optional.of(weightedElement);
      }
    }
    return Optional.empty();
  }

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
        insertOrUpdate(word, END_WORD);
        continue;
      }

      if (i == tokens.length - 1) {
        insertOrUpdate(word, END_WORD);
        break;
      }

      String second = tokens[i + 1];
      if (second.isEmpty()) {
        continue;
      }
      if (i == tokens.length - 2) {
        insertOrUpdate(word, second, END_WORD);
        break;
      }
      String third = tokens[i + 2];
      insertOrUpdate(word, second, third);
    }
  }

  private void insertOrUpdate(String word, String followWord) {
    insertOrUpdate(word, followWord, followWord);
  }

  private void insertOrUpdate(String word, String second, String third) {
    WeightedCollection<Pair> followFrequency;
    if (wordMap.containsKey(word)) {
      followFrequency = wordMap.get(word);
    } else {
      followFrequency = new WeightedCollection<Pair>();
    }
    var pair = new Pair(second, third);
    followFrequency.get(pair).ifPresentOrElse(fw -> followFrequency.update(fw, fw.getWeight() + 1),
        () -> followFrequency.add(new WeightedElement<Pair>(pair, 1)));
    wordMap.put(word, followFrequency);
  }

  @Override
  public void save(String file) throws IOException {
    var f = new File(file + FILE_END);
    logger.info("Saving file {}", f.getAbsolutePath());
    Output output = new Output(new FileOutputStream(new File(file + FILE_END)));
    kryo.writeObject(output, this);
    output.close();
  }

  @Override
  public String getFileEnd() {
    return FILE_END;
  }

}
