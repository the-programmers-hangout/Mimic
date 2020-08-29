package uk.co.markg.bertrand.markov;

public class WeightedElement {

  private final String element;
  private final double weight;

  public WeightedElement(String element, double weight) {
    this.element = element;
    this.weight = weight;
  }

  /**
   * @return the element
   */
  public String getElement() {
    return element;
  }

  /**
   * @return the weight
   */
  public double getWeight() {
    return weight;
  }
}
