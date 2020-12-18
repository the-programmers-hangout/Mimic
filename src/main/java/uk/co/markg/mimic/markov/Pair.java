package uk.co.markg.mimic.markov;

public class Pair {

  private final String first;
  private final String second;

  public Pair(String first, String second) {
    this.first = first;
    this.second = second;
  }
  
  public static Pair empty() {
    return new Pair(null, null);
  }
  
  public boolean isEmpty() {
    return first == null || second == null;
  }

  /**
   * @return the first
   */
  public String getFirst() {
    return first;
  }

  /**
   * @return the second
   */
  public String getSecond() {
    return second;
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
    result = prime * result + ((first == null) ? 0 : first.hashCode());
    result = prime * result + ((second == null) ? 0 : second.hashCode());
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
    Pair other = (Pair) obj;
    if (first == null) {
      if (other.first != null)
        return false;
    } else if (!first.equals(other.first))
      return false;
    if (second == null) {
      if (other.second != null)
        return false;
    } else if (!second.equals(other.second))
      return false;
    return true;
  }
  
  

}
