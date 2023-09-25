package phase1.stub.implementation;

import phase1.stub.Phase1StubBase;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Phase1StubImpl<K extends Serializable, V extends Serializable, M extends Serializable> extends Phase1StubBase<K, V, M> {

  private final byte[] serverVerificationKey;
  
  public Phase1StubImpl() throws IOException, NotBoundException {
    super();
    this.serverVerificationKey = super.serverVerificationKey;
    this.network = super.network;
  }

  public Boolean registerUser(String userId) {
    // implement me
    return false;
  }

  public Boolean create(String userId, K key, V val, M metaVal) {
   // implement me
    return false;
  }

  public Boolean delete(String userId, K key) {
    // implement me
    return false;
  }

  public V readVal(String userId, K key) {
    // implement me
    return null;
  }

  public M readMetaVal(String userId, K key) {
    // implement me
    return null;
  }

  public Boolean writeVal(String userId, K key, V newVal) {
    // implement me
    return false;
  }

  public Boolean writeMetaVal(String userId, K key, M newMetaVal) {
    // implement me
    return false;
  }
}
      