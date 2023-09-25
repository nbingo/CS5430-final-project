import java.lang.IllegalArgumentException;
import java.util.NoSuchElementException;
import java.util.Dictionary;
import java.util.Hashtable;

public class Phase0Impl<K, V, M> implements Phase0<K, V, M> {
  private Dictionary<K, Value<V, M>> store;

  public Phase0Impl() {
    this.store = new Hashtable<K, Value<V, M>>();
  }

  public void create(K key, V initVal, M initMetaVal) {
    this.store.put(key, new Value<V, M>(initVal, initMetaVal));
  }

  public void writeVal(K key, V newVal) throws IllegalArgumentException {
    if (this.store.get(key) == null) {
      throw new IllegalArgumentException("Key does not exist");
    } else {
      this.store.get(key).setVal(newVal);
    }
  }

  public void writeMetaVal(K key, M newMetaVal) throws IllegalArgumentException {
    if (this.store.get(key) == null) {
      throw new IllegalArgumentException("Key does not exist");
    } else {
      this.store.get(key).setMetaVal(newMetaVal);
    }
  }

  public V readVal(K key) throws NoSuchElementException {
    if (this.store.get(key) == null) {
      throw new NoSuchElementException("Key does not exist");
    } else {
      return this.store.get(key).getVal();
    }
  }

  public M readMetaVal(K key) throws NoSuchElementException {
    if (this.store.get(key) == null) {
      throw new NoSuchElementException("Key does not exist");
    } else {
      return this.store.get(key).getMetaVal();
    }
  }

  public void delete(K key) {
    this.store.remove(key);
  }

  public static void main(String[] args) {
    Phase0Impl<String, String, String> example = new Phase0Impl<String, String, String>();
    example.create("ohBoy", "boy", "supercool");
    System.out.println(example.readMetaVal("ohBoy"));
    System.out.println(example.readVal("ohBoy"));
    example.writeVal("ohBoy", "girl");
    example.writeMetaVal("ohBoy", "cheese");
    System.out.println(example.readMetaVal("ohBoy"));
    System.out.println(example.readVal("ohBoy"));
    example.delete("ohBoy");
    example.writeMetaVal("ohBoy", "cheese");
  }
}