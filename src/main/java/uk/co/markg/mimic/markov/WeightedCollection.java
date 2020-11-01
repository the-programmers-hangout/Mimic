package uk.co.markg.mimic.markov;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class WeightedCollection {

  private List<WeightedElement> collection;
  private double weightedSum;

  public WeightedCollection() {
    collection = new ArrayList<>();
  }

  public WeightedCollection(List<WeightedElement> collection, double weightedSum) {
    this.collection = collection;
    this.weightedSum = weightedSum;
  }

  /**
   * Adds a new {@link uk.co.markg.mimic.markov.WeightedElement WeightedElement} instance to the
   * collection and updates its weighted sum.
   * 
   * @param weightedElement The weightedElement to be added
   */
  public void add(WeightedElement weightedElement) {
    collection.add(weightedElement);
    weightedSum += weightedElement.getWeight();
  }

  /**
   * Updates the weight of a preexisting {@link uk.co.markg.mimic.markov.WeightedElement
   * WeightedElement} in the collection.
   * 
   * @param element   The name of the weightedElement you want to update
   * @param newWeight The new weight of the element
   */
  public void update(String element, double newWeight) {
    var item = get(element);
    item.ifPresentOrElse(i -> update(i, newWeight),
        () -> new IllegalArgumentException("No such element"));
  }

  /**
   * Updates the weight of a preexisting {@link uk.co.markg.mimic.markov.WeightedElement
   * WeightedElement} in the collection.
   * 
   * @param element   The {@link uk.co.markg.mimic.markov.WeightedElement WeightedElement} instance
   *                  you want to update
   * @param newWeight The new weight of the element
   */
  public void update(WeightedElement element, double newWeight) {
    updateElement(element, newWeight);
  }

  /**
   * Updates the weight of a preexisting {@link uk.co.markg.mimic.markov.WeightedElement
   * WeightedElement} in the collection.
   * 
   * @param element   The {@link uk.co.markg.mimic.markov.WeightedElement WeightedElement} instance
   *                  you want to update
   * @param newWeight The new weight of the element
   */
  private void updateElement(WeightedElement element, double newWeight) {
    double diff = newWeight - element.getWeight();
    element.setWeight(newWeight);
    weightedSum += diff;
  }

  /**
   * Gets the {@link uk.co.markg.mimic.markov.WeightedElement WeightedElement} instance from the
   * collection.
   * 
   * @param element The name of the WeightedElement
   * @return The weightedElement instance if it exists, null otherwise
   */
  public Optional<WeightedElement> get(String element) {
    for (WeightedElement weightedElement : collection) {
      if (element.equals(weightedElement.getElement())) {
        return Optional.of(weightedElement);
      }
    }
    return Optional.empty();
  }

  /**
   * Gets a random element from the collection.
   * 
   * @return A random {@link uk.co.markg.mimic.markov.WeightedElement WeightedElement} or null if
   *         the collection is empty.
   */
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
