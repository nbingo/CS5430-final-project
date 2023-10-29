package phase2.stub.implementation;

import phase1.stub.implementation.Phase1StubImpl;
import types.acl.AbstractClientACLObject;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.NotBoundException;

public class Phase2StubImpl<K extends Serializable, V extends Serializable> extends Phase1StubImpl<K, V, AbstractClientACLObject> {

  private final byte[] serverVerificationKey;
  
  public Phase2StubImpl() throws IOException, NotBoundException {
    super();
    this.serverVerificationKey = super.getServerVerificationKey();
  }

  public Boolean registerUser(String userId) {
    // implement me
    return false;
  }

  public Boolean create(String userId, K key, V val, AbstractClientACLObject metaVal) {
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

  public AbstractClientACLObject readMetaVal(String userId, K key) {
    // implement me
    return null;
  }

  public Boolean writeVal(String userId, K key, V newVal) {
    // implement me
    return false;
  }

  public Boolean writeMetaVal(String userId, K key, AbstractClientACLObject newMetaVal) {
    // implement me
    return false;
  }
}
      