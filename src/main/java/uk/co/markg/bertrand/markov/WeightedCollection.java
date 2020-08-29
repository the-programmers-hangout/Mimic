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

  public void update(String element, double newWeight) {
    var item = get(element);
    item.ifPresentOrElse(i -> update(i, newWeight),
        () -> new IllegalArgumentException("No such element"));
  }

  public void update(WeightedElement element, double newWeight) {
    updateElement(element, newWeight);
  }

  private void updateElement(WeightedElement element, double newWeight) {
    double diff = newWeight - element.getWeight();
    element.setWeight(newWeight);
    weightedSum += diff;
  }

  public Optional<WeightedElement> get(String element) {
    for (WeightedElement weightedElement : collection) {
      if (element.equals(weightedElement.getElement())) {
        return Optional.of(weightedElement);
      }
    }
    return Optional.empty();
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
