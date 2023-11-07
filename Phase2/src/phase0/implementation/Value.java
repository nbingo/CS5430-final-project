package phase0.implementation;

public class Value<V, M> {
  private V val;
  private M metaVal; 


  /**
   * Value constructor
   * 
   * @param val The value
   * @param metaVal The meta-value
   */
  public Value(V val, M metaVal) {
    this.val = val;
    this.metaVal = metaVal;
  }
  

  /**
   * Returns the value
   * 
   * @return The value
   */
  public V getVal() {
    return this.val;
  }
  

  /**
   * Returns the meta-value
   * 
   * @return The meta-value
   */
  public M getMetaVal() {
    return this.metaVal;
  }


  /**
   * Set the value
   * 
   * @param val The new value
   */
  public void setVal(V val) {
    this.val = val;
  }


  /**
   * Set the meta-value
   * 
   * @param metaVal The new meta-value
   */
  public void setMetaVal(M metaVal) {
    this.metaVal = metaVal;
  }


  /**
   * Converts the value to a string
   * 
   * @return The string version fo the value
   */
  @Override
  public String toString() {
    return "Value: " + this.val + ", Meta Value: " + this.metaVal; 
  }
}