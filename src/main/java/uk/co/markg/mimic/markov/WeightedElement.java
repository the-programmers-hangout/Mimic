package uk.co.markg.mimic.markov;

public class WeightedElement<T> {

  private final T element;

  private int weight;

  /**
   * Required for serialisation.
   */
  private WeightedElement() {
    element = null;
  }

  public WeightedElement(T element, int weight) {
    this.element = element;
    this.weight = weight;
  }

  /**
   * Gets the name of the {@link uk.co.markg.mimic.markov.WeightedElement WeightedElement}.
   * 
   * @return The element name
   */
  public T getElement() {
    return element;
  }

  /**
   * Gets the weight of the {@link uk.co.markg.mimic.markov.WeightedElement WeightedElement}.
   * 
   * @return The element's weight
   */
  public int getWeight() {
    return weight;
  }

  /**
   * Updates the weight of the {@link uk.co.markg.mimic.markov.WeightedElement WeightedElement}.
   * 
   * @param weight The weight to set
   */
  public void setWeight(int weight) {
    this.weight = weight;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((element == null) ? 0 : element.hashCode());
    result = prime * result + weight;
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    WeightedElement other = (WeightedElement) obj;
    if (element == null) {
      if (other.element != null)
        return false;
    } else if (!element.equals(other.element))
      return false;
    if (weight != other.weight)
      return false;
    return true;
  }
}
