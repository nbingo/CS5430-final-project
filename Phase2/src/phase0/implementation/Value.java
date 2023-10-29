package phase0.implementation;

public class Value<V, M> {
  private V val;
  private M metaVal; 

  public Value(V val, M metaVal) {
    this.val = val;
    this.metaVal = metaVal;
  }
  
  public V getVal() {
    return this.val;
  }
  
  public M getMetaVal() {
    return this.metaVal;
  }

  public void setVal(V val) {
    this.val = val;
  }

  public void setMetaVal(M metaVal) {
    this.metaVal = metaVal;
  }

  @Override
  public String toString() {
    return "Value: " + this.val + ", Meta Value: " + this.metaVal; 
  }
}