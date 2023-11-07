package phase2.stub.implementation;

import phase1.stub.implementation.Phase1StubImpl;
import types.acl.AbstractClientACLObject;
import types.acl.AbstractServerACLObject;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.NoSuchElementException;

public class Phase2StubImpl<K extends Serializable, V extends Serializable>
    extends Phase1StubImpl<K, V, AbstractClientACLObject> {

  private final byte[] serverVerificationKey;


  /**
  * Phase2StubImpl constructor. Calls the constructor of Phase1StubImpl. 
  */
  public Phase2StubImpl() throws IOException, NotBoundException {
    super();
    this.serverVerificationKey = super.getServerVerificationKey();
  }


  /**
   * Sends a registration request to the Phase1 Stub implementation for a user with the given userId
   * 
   * @param userId The userId to register
   * @return A boolean describing if the registration is successful
   * @throws RemoteException
   */
  public Boolean registerUser(String userId) throws RemoteException {
    return super.registerUser(userId);
  }


  /**
   * Sends a request to the Phase1 Stub implementation for the userId to create the given key, value, meta-value to create
   * 
   * @param userId The user creating the new <K,V,M> triple
   * @param key The key of the <K,V,M> triple to create
   * @param val The value of the <K,V,M> triple to create
   * @param metaVal The meta-value of the <K,V,M> triple to create
   * @throws RemoteException
   */
  public Boolean create(String userId, K key, V val, AbstractClientACLObject metaVal) throws RemoteException {
    return super.create(userId, key, val, metaVal);
  }


  /**
   * Sends a request to the Phase1 Stub implementation for the userId to delete the given key
   * 
   * @param userId The user requesting the key be deleted
   * @param key The key to delete from the store
   * @return A boolean describing if the request was sucessful
   * @throws RemoteException
   */
  public Boolean delete(String userId, K key) throws RemoteException {
    return super.delete(userId, key);
  }


  /**
   * Sends a request to the Phase1 Stub implementation for the userId to read the associated value for the given key
   * 
   * @param userId The user requesting to read the value
   * @param key The key whose value is to be read
   * @return The value if the request is successful and null otherwise
   * @throws RemoteException
   * @throws NoSuchElementException
   */
  public V readVal(String userId, K key) throws RemoteException, NoSuchElementException {
    return super.readVal(userId, key);
  }


  /**
   * Sends a request to the Phase1 Stub implementation for the userId to read the associated meta-value for the given key
   * 
   * @param userId The user requesting to read the meta-value
   * @param key The key whose meta-value is to be read
   * @return The meta-value if the request is successful and null otherwise
   * @throws RemoteException
   * @throws NoSuchElementException
   */
  public AbstractClientACLObject readMetaVal(String userId, K key) throws RemoteException, NoSuchElementException {
    return super.readMetaVal(userId, key);
  }


  /**
   * Sends a request to the Phase1 Stub implementation for the userId to write a new value for the given key
   * 
   * @param userId The user requesting to write the value
   * @param key The key whose value should be changed
   * @param newVal The new value to write
   * @return True if the write is successful and false otherwise
   * @throws RemoteException
   * @throws IllegalArgumentException
   */
  public Boolean writeVal(String userId, K key, V newVal) throws RemoteException, IllegalArgumentException {
    return super.writeVal(userId, key, newVal);
  }


  /**
   * Sends a request to the Phase1 Stub implementation for the userId to write a new meta-value for the given key
   * 
   * @param userId The user requesting to write the meta-value
   * @param key The key whose meta-value should be changed
   * @param newMetaVal The new meta-value to write
   * @return True if the write is successful and false otherwise
   * @throws RemoteException
   * @throws IllegalArgumentException
   */
  public Boolean writeMetaVal(String userId, K key, AbstractClientACLObject newMetaVal) throws RemoteException, IllegalArgumentException {
    return super.writeMetaVal(userId, key, newMetaVal);
  }
}
