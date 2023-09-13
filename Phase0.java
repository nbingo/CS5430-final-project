import java.lang.IllegalArgumentException;
import java.util.NoSuchElementException;

public interface Phase0<K, V, M> {  

  public void create(K key, V initVal, M initMetaVal);

  public void writeVal(K key, V newVal) throws IllegalArgumentException;

  public void writeMetaVal(K key, M newMetaVal) throws IllegalArgumentException;

  public V readVal(K key) throws NoSuchElementException;

  public M readMetaVal(K key) throws NoSuchElementException;

  public void delete(K key);
}