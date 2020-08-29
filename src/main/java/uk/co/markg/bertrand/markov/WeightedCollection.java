package uk.co.markg.bertrand.markov;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class WeightedCollection {

  private List<WeightedElement> collection;
  private double weightedSum;

  public WeightedCollection() {
    collection = new ArrayList<WeightedElement>();
  }

  public void add(WeightedElement weightedElement) {
    collection.add(weightedElement);
    weightedSum += weightedElement.getWeight();
  }

  public Optional<WeightedElement> getRandom() {
    var rand = ThreadLocalRandom.current().nextDouble(weightedSum);
    for (WeightedElement weightedElement : collection) {
      rand -= weightedElement.getWeight();
      if (rand <= 0) {
        return Optional.of(weightedElement);
      }
    }
    return Optional.empty();
  }

}
