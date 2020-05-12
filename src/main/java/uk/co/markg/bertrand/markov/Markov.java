package uk.co.markg.bertrand.markov;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

public class Markov {

  private static final String END_WORD = "END_WORD";
  private Map<String, Map<String, Double>> wordFrequencyMap;
  private List<String> startWords;
  private List<String> endWords;

  public Markov(List<String> inputs) {
    wordFrequencyMap = new HashMap<>();
    startWords = new ArrayList<>();
    endWords = new ArrayList<>();
    parseInput(inputs);
    calculateProbabilities();
  }

  public Markov(String input) {
    this(List.of(input));
  }

  public String generate(int sentences) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < sentences; i++) {
      sb.append(generate()).append(" ");
    }
    return sb.toString();
  }

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

  private void parseInput(List<String> inputs) {
    for (String input : inputs) {
      parseInput(input);
    }
  }

  private void parseInput(String input) {
    String[] tokens = input.split("\\s+");
    if (tokens.length < 4) {
      throw new IllegalArgumentException("Input is too short. Must be greater than 5 tokens.");
    }
    for (int i = 0; i < tokens.length; i++) {
      String word = tokens[i].trim();
      if (word.isEmpty()) {
        continue;
      }
      if (word.charAt(0) == word.toUpperCase().charAt(0)) {
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
      if (wordFrequencyMap.containsKey(word)) {
        updateWordFrequency(word, nextWord);
      } else {
        insertWordFrequency(word, nextWord);
      }
    }
  }

  private boolean isEndWord(String word) {
    List<Predicate<String>> predicates = new ArrayList<>();
    predicates.add(w -> w.endsWith("."));
    predicates.add(w -> w.endsWith("!"));
    predicates.add(w -> w.endsWith("?"));
    return predicates.stream().anyMatch(predicate -> predicate.test(word));
  }

  private void insertWordFrequency(String word, String followWord) {
    var followFrequency = new HashMap<String, Double>();
    followFrequency.put(followWord, 1.0);
    wordFrequencyMap.put(word, followFrequency);
  }

  private void updateWordFrequency(String key, String followWord) {
    var followFrequency = wordFrequencyMap.get(key);
    if (followFrequency.containsKey(followWord)) {
      followFrequency.put(followWord, followFrequency.get(followWord) + 1);
    } else {
      followFrequency.put(followWord, 1.0);
    }
    wordFrequencyMap.put(key, followFrequency);
  }

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
