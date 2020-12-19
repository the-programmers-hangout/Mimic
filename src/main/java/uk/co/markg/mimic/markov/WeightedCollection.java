package uk.co.markg.mimic.markov;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class WeightedCollection<T> {

  private Map<WeightedElement<T>, WeightedElement<T>> collection;
  private int weightedSum;

  public WeightedCollection() {
    collection = new HashMap<>();
  }

  public WeightedCollection(Map<WeightedElement<T>, WeightedElement<T>> collection,
      int weightedSum) {
    this.collection = collection;
    this.weightedSum = weightedSum;
  }

  /**
   * Adds a new {@link uk.co.markg.mimic.markov.WeightedElement WeightedElement} instance to the
   * collection and updates its weighted sum.
   * 
   * @param weightedElement The weightedElement to be added
   */
  public void add(WeightedElement<T> weightedElement) {
    collection.put(weightedElement, weightedElement);
    weightedSum += weightedElement.getWeight();
  }

  /**
   * Updates the weight of a preexisting {@link uk.co.markg.mimic.markov.WeightedElement
   * WeightedElement} in the collection.
   * 
   * @param element   The name of the weightedElement you want to update
   * @param newWeight The new weight of the element
   */
  public void update(T element, int newWeight) {
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
  public void update(WeightedElement<T> element, int newWeight) {
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
  private void updateElement(WeightedElement<T> element, int newWeight) {
    double diff = newWeight - element.getWeight();
    weightedSum += diff;
    // element.setWeight(newWeight);
    var item = collection.get(element);
    item.setWeight(newWeight);

  }

  /**
   * Gets the {@link uk.co.markg.mimic.markov.WeightedElement WeightedElement} instance from the
   * collection.
   * 
   * @param element The name of the WeightedElement
   * @return The weightedElement instance if it exists, null otherwise
   */
  public Optional<WeightedElement<T>> get(T element) {
    if (collection.containsKey(element)) {
      return Optional.of(collection.get(element));
    }
    return Optional.empty();
  }

  public Set<WeightedElement<T>> getAll() {
    return Collections.unmodifiableSet(collection.keySet());
  }

  /**
   * Gets a random element from the collection.
   * 
   * @return A random {@link uk.co.markg.mimic.markov.WeightedElement WeightedElement} or null if
   *         the collection is empty.
   */
  public WeightedElement<T> getRandom() {
    var rand = ThreadLocalRandom.current().nextInt(weightedSum);
    Iterator<WeightedElement<T>> iter = collection.keySet().iterator();
    for (int i = 0; i < rand;) {
      if (iter.hasNext()) {
        var item = iter.next();
        i += item.getWeight();
      } else {
        return collection.keySet().iterator().next();
      }
    }
    if (iter.hasNext()) {
      return iter.next();
    } else {
      return collection.keySet().iterator().next();
    }
  }
}
