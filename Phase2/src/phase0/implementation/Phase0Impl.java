package phase0.implementation;

import java.lang.IllegalArgumentException;
import java.util.NoSuchElementException;
import java.util.Dictionary;
import java.util.Hashtable;


public class Phase0Impl<K, V, M> implements Phase0<K, V, M> {
  private Dictionary<K, Value<V, M>> store;


  /**
   * Phase0Impl constructor
   */
  public Phase0Impl() {
    this.store = new Hashtable<K, Value<V, M>>();
  }


  /**
   * Create a <K,V,M> triple in the store with the key pointing to the value and meta-value in the store
   * 
   * @param key The key for the triple to be created
   * @param initVal The value for the triple to be created
   * @param initMetaVal The meta-value for the triple to be created
   */
  public void create(K key, V initVal, M initMetaVal) {
    this.store.put(key, new Value<V, M>(initVal, initMetaVal));
  }


  /**
   * Writes a new value for the given key in the store
   * 
   * @param key The key whose value is being overwritten
   * @param newVal The new value for the given key
   * @throws IllegalArgumentException If key not in store
   */
  public void writeVal(K key, V newVal) throws IllegalArgumentException {
    if (this.store.get(key) == null) {
      throw new IllegalArgumentException("Key does not exist");
    } else {
      this.store.get(key).setVal(newVal);
    }
  }


  /**
   * Writes a new meta-value for the given key in the store
   * 
   * @param key The key whose meta-value is being overwritten
   * @param newMetaVal The new value for the given key
   * @throws IllegalArgumentException If the key does not exist
   */
  public void writeMetaVal(K key, M newMetaVal) throws IllegalArgumentException {
    if (this.store.get(key) == null) {
      throw new IllegalArgumentException("Key does not exist");
    } else {
      this.store.get(key).setMetaVal(newMetaVal);
    }
  }


  /**
   * Returns the value for the given key
   * 
   * @param key The key whose value is to be read
   * @return The value for the given key
   * @throws NoSuchElementException If the key does not exist
   */
  public V readVal(K key) throws NoSuchElementException {
    if (this.store.get(key) == null) {
      throw new NoSuchElementException("Key does not exist");
    } else {
      return this.store.get(key).getVal();
    }
  }


  /**
   * Returns the meta-value for the given key
   * 
   * @param key The key whose meta-value is to be read
   * @return The meta-value for the given key
   * @throws NoSuchElementException If the key does not exist
   */
  public M readMetaVal(K key) throws NoSuchElementException {
    if (this.store.get(key) == null) {
      throw new NoSuchElementException("Key does not exist");
    } else {
      return this.store.get(key).getMetaVal();
    }
  }


  /** 
   * Remove the <K,V,M> triple for the given key from the store if it exists, do nothing otherwise
   * 
   * @param key The key whose <K,V,M> triple is to be removed
   */
  public void delete(K key) {
    this.store.remove(key);
  }


  /**
   * Returns true if the store contains the given key
   * 
   * @param key The key to check for in the store
   * @return True if the store contains the key and false otherwise
   */
  public boolean containsKey(K key) {
    if (this.store.get(key) == null) {
      return false;
    }
    return true;
  }
}