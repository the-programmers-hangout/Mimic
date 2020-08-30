package uk.co.markg.mimic.markov;

public class WeightedElement {

  private final String element;
  private double weight;

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

  /**
   * @param weight the weight to set
   */
  public void setWeight(double weight) {
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
    long temp;
    temp = Double.doubleToLongBits(weight);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    if (Double.doubleToLongBits(weight) != Double.doubleToLongBits(other.weight))
      return false;
    return true;
  }


}
