package phase2.stub.implementation;

import phase1.stub.implementation.Phase1StubImpl;
import types.acl.AbstractClientACLObject;
import types.acl.AbstractServerACLObject;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

//TODO: Javadoc / comment me
public class Phase2StubImpl<K extends Serializable, V extends Serializable>
    extends Phase1StubImpl<K, V, AbstractClientACLObject> {

  private final byte[] serverVerificationKey;

  public Phase2StubImpl() throws IOException, NotBoundException {
    super();
    this.serverVerificationKey = super.getServerVerificationKey();
  }

  public Boolean registerUser(String userId) throws RemoteException {
    return super.registerUser(userId);
  }

  public Boolean create(String userId, K key, V val, AbstractClientACLObject metaVal) throws RemoteException {
    return super.create(userId, key, val, metaVal);
  }

  public Boolean delete(String userId, K key) throws RemoteException {
    return super.delete(userId, key);
  }

  public V readVal(String userId, K key) throws RemoteException {
    return super.readVal(userId, key);
  }

  public AbstractClientACLObject readMetaVal(String userId, K key) throws RemoteException {
    return super.readMetaVal(userId, key);
  }

  public Boolean writeVal(String userId, K key, V newVal) throws RemoteException {
    return super.writeVal(userId, key, newVal);
  }

  public Boolean writeMetaVal(String userId, K key, AbstractClientACLObject newMetaVal) throws RemoteException {
    return super.writeMetaVal(userId, key, newMetaVal);
  }
}
