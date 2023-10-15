package phase1.stub.implementation;

import phase1.stub.Phase1StubBase;
import types.requests.AbstractAuthenticatedDoRequest;
import types.requests.AbstractAuthenticatedRegisterRequest;
import types.requests.DoOperation;
import types.requests.implementation.AuthenticatedDoRequest;
import types.requests.implementation.AuthenticatedRegisterRequest;
import types.responses.AbstractAuthenticatedDoResponse;
import types.responses.AbstractAuthenticatedRegisterResponse;
import types.responses.DoOperationOutcome;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.UUID;

public class Phase1StubImpl<K extends Serializable, V extends Serializable, M extends Serializable> extends Phase1StubBase<K, V, M> {

  private final byte[] serverVerificationKey;

  private final Hashtable<String, KeyPair> userKeys;

  /**
  * Phase1StubImpl constructor. Calls the constructor of Phase1StubBase and then creates a hashtable in which to store user keys. 
  */
  public Phase1StubImpl() throws IOException, NotBoundException {
    super();
    this.serverVerificationKey = super.serverVerificationKey;
    this.userKeys = new Hashtable<>();
  }

  /**
  * Converts encoded public keys (byte arrays) to PublicKey objects.
  * 
  * @param publicBytes An encoded public key as a byte array
  * @return The public key given in parameter publicBytes as a PublicKey
  * @throws NoSuchAlgorithmException
  * @throws InvalidKeySpecException
  */
  private PublicKey createPublicKey(byte[] publicBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
    return KeyFactory.getInstance("DSA").generatePublic(new X509EncodedKeySpec(publicBytes));
  }

  /**
   * Signs the passed in contents with the passed in private key
   * 
   * @param privateKey The private key with which to sign the objects as a PrivateKey object
   * @param contents The contents to sign as a byte array
   * @return The signed contents as a byte array
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeyException
   * @throws SignatureException
   */
  private byte[] createSignature(PrivateKey privateKey, byte[] contents) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    Signature server_sign = Signature.getInstance("SHA224withDSA");
    server_sign.initSign(privateKey);
    server_sign.update(contents);
    return server_sign.sign();
  }

  /**
   * Uses the passed in public key to ensure that the signature is the given contents signed with the corresponding private key
   * 
   * @param publicKey The public key to verify the signature with as a PublicKey object
   * @param contents The contents to verify the signature against as a byte array
   * @param signature The signature to verify as a byte array
   * @return A boolean indicating if the signature is the given contents signed with the private key pair of the given public key
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeyException
   * @throws SignatureException
   */
  private boolean verifySignature(PublicKey publicKey, byte[] contents, byte[] signature) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    Signature sign = Signature.getInstance("SHA224withDSA");
    sign.initVerify(publicKey);
    sign.update(contents);
    return sign.verify(signature);
  }


  /**
   * Create a KeyPair object with a public key / private key pair using the DSA encryption scheme
   * 
   * @return A KeyPair object for the DSA encryption scheme with 2048 bit keys
   * @throws NoSuchAlgorithmException
   */
  private KeyPair createKeyPair() throws NoSuchAlgorithmException {
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DSA");
    keyPairGenerator.initialize(2048);
    return keyPairGenerator.generateKeyPair();
  }

  /**
   * Convert the given key, value, and meta-value to a byte array
   * 
   * @param key The key to add to the byte array
   * @param val The value to add to the byte array
   * @param metaVal The meta-value to add to the byte array
   * @return A byte array containing the key, value, and meta-value
   * @throws IOException
   */
  private byte[] convertKVMtoByte(K key, V val, M metaVal) throws IOException {
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    ObjectOutputStream stream = new ObjectOutputStream(byteStream);
    stream.writeObject(key);
    stream.writeObject(val);
    stream.writeObject(metaVal);
    stream.flush();

    return byteStream.toByteArray();
  }

  /**
   * Verify the signature in the AbstractAuthenticatedDoResponse
   * 
   * @param response The AbstractAuthenticatedDoResponse containing the digital signature and the key, value, meta-value, and outcome to check the signature against
   * @param idNonce The userId and unique nonce to check the signature against
   * @return Whether the signature in the response is verifiable
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeySpecException
   * @throws SignatureException
   * @throws InvalidKeyException
   * @throws IOException
   */
  private Boolean getValid(AbstractAuthenticatedDoResponse<K, V, M> response, String idNonce) throws NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException, IOException {
    // Convert the key, value, meta-value in the response to a byte array
    byte[] kvmArray = convertKVMtoByte(response.outcome.key, response.outcome.val, response.outcome.metaVal);

    // Combine the userId, nonce, key, value, meta-vlaue, and outcome to a single byte array
    byte[] combined = new byte[idNonce.getBytes().length + kvmArray.length + response.outcome.outcome.name().getBytes().length];
    ByteBuffer buffer = ByteBuffer.wrap(combined);
    buffer.put(idNonce.getBytes());
    buffer.put(kvmArray);
    buffer.put(response.outcome.outcome.name().getBytes());

    // Return true if the signature is the byte array combined created above signed with server's private key and false otherwise
    return this.verifySignature(this.createPublicKey(this.serverVerificationKey), buffer.array(), response.digitalSignature);
  }

  /**
   * Extract the userId from the given userId / nonce string
   * 
   * @param idNonce The userId and nonce in a single string
   * @return The userId
   */
  private String extractId(String idNonce) {
    return idNonce.substring(0, idNonce.length()-36);
  }

  /**
   * Given a userId, unique nonce, doOperation, and mode create the necessary signature and send a AuthenticatedDoRequest to the network
   * 
   * @param idNonce The userId sending the request and the unique nonce associated with the request
   * @param doOperation The doOperation to be included in the request
   * @param mode A string describing the operation (e.g. READVAL, CREATE) to be undertaken
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeyException
   * @throws SignatureException
   * @throws IOException
   */
  private AbstractAuthenticatedDoResponse<K, V, M> requestAndGetResponse(String idNonce, DoOperation<K, V, M> doOperation, String mode) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException {
    // Sign the userId, unique nonce, mode, key, value, and meta-value
    byte[] kvmArray = convertKVMtoByte(doOperation.key, doOperation.val, doOperation.metaVal);
    byte[] combined = createToSign((idNonce + mode).getBytes(), kvmArray);
    byte[] signature = this.createSignature(userKeys.get(this.extractId(idNonce)).getPrivate(), combined);

    // Create the AuthenticatedDoRequest to send to the network
    AbstractAuthenticatedDoRequest<K, V, M> req = new AuthenticatedDoRequest<>(idNonce, doOperation, signature);

    // Send the AuthenticatedDoRequest to the network and return the response
    return this.network.handleAuthenticatedDo(req);
  }
  
  /**
   * Add a unique nonce to the given userId
   * 
   * @param userId The userId to add the nonce to
   * @return A string with the userId and unique nonce
   */
  private String addNonce(String userId) {
    return userId + UUID.randomUUID();
  }

  /**
   * Return whether a write request was successful or throw the appropriate error
   * 
   * @param response The response from the server for the write request
   * @param valid Whether the signature from the server is valid
   * @return A boolean describing if the write request was successful
   */
  private Boolean returnWriteSuccess(AbstractAuthenticatedDoResponse<K, V, M> response, boolean valid) {
    if (valid) {
      if (response.outcome.outcome == DoOperationOutcome.Outcome.SUCCESS) {
        return true;
      } else if (response.outcome.outcome == DoOperationOutcome.Outcome.ILLEGALARGUMENT) {
        throw new IllegalArgumentException();
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  /** 
   * Combine the userId, unique nonce, and function specific contents into a single byte array
   * 
   * @param idNonce The userId making the request and the unique nonce associated with the action
   * @param functionSpec A byte array with the function specific contents to encode
   * @return A byte array with the userId, unique nonce, and function specific contents in a single byte array
   */
  private byte[] createToSign(byte[] idNonce, byte[] functionSpec) {
    byte[] combined = new byte[idNonce.length + functionSpec.length];
    ByteBuffer buffer = ByteBuffer.wrap(combined);
    buffer.put(idNonce);
    buffer.put(functionSpec);
    return buffer.array();
  }

  /**
   * Send a registration request to the server for a user with the given userId
   * 
   * @param userId The userId to register
   * @return A boolean describing if the registration is successful
   */
  public Boolean registerUser(String userId) throws RemoteException {
    try {
      // Create a signing (private) key / verification (public) key pair for the new user and add to the maintained hashtable of users and keyparis
      KeyPair keyPair = this.createKeyPair();
      this.userKeys.put(userId, keyPair);

      // Create a unique nonce for this request
      String idNonce = this.addNonce(userId);

      // Sign the userId, unique nonce, and user's public key
      byte[] combined = createToSign(idNonce.getBytes(), keyPair.getPublic().getEncoded());
      byte[] signature = this.createSignature(keyPair.getPrivate(), combined);

      // Create a registration request object
      AbstractAuthenticatedRegisterRequest req = new AuthenticatedRegisterRequest(idNonce, keyPair.getPublic().getEncoded(), signature);

      // Send request to the network and receive response
      AbstractAuthenticatedRegisterResponse response = this.network.handleAuthenticatedRegister(req);

      // Create byte array to verify the server's signature against
      // NOTE: The nonce used to create this array is the one created at the beginning of the function and not one 
      // returned by the server. This is to prevent replay attacks.
      byte[] return_combined = new byte[idNonce.getBytes().length + response.status.name().getBytes().length];
      ByteBuffer return_buffer = ByteBuffer.wrap(return_combined);
      return_buffer.put(idNonce.getBytes());
      return_buffer.put(response.status.name().getBytes());

      // Verify the signature from the server
      boolean valid = this.verifySignature(this.createPublicKey(this.serverVerificationKey), return_buffer.array(), response.digitalSignature);

      // Return true if the signature is verifiable and if the registration was successful and false otherwise
      return valid && response.status == AbstractAuthenticatedRegisterResponse.Status.OK;
    } catch (Exception e) {
      // Return false if any part of the registration throws an exception
      return false;
    }
  }
  
  /**
   * Send a request to the server for the userId to create the given key, value, meta-value to create
   * 
   * @param userId The user creating the new K,V,M triple
   * @param key The key of the K,V,M triple to create
   * @param val The value of the K,V,M triple to create
   * @param metaVal The meta-value of the K,V,M triple to create
   * @throws RemoteException
   */
  public Boolean create(String userId, K key, V val, M metaVal) throws RemoteException {
    try {
      // Create a unique nonce for the request
      String idNonce = this.addNonce(userId);

      // Create the DoOperation to send to server
      DoOperation<K, V, M> doOperation = new DoOperation<>(key, val, metaVal, DoOperation.Operation.CREATE);

      // Send request to server and receive response 
      AbstractAuthenticatedDoResponse<K, V, M> response =  requestAndGetResponse(idNonce, doOperation, DoOperation.Operation.CREATE.name());

      // Returns true if the response has verifiable signature and the request was successful and false otherwise
      return getValid(response, idNonce) && response.outcome.outcome == DoOperationOutcome.Outcome.SUCCESS;
    } catch (Exception e) {
      // Return false if any part of the request throws an exception
      return false;
    }
  }

  /**
   * Delete the given key from the store for the specified user
   * 
   * @param userId The user requesting the key be deleted
   * @param key The key to delete from the store
   * @return A boolean describing if the request was sucessful
   * @throws RemoteException
   */
  public Boolean delete(String userId, K key) throws RemoteException {
    try {
      // Create a unique nonce for the request
      String idNonce = this.addNonce(userId);

      // Create the DoOperation to send to server
      // The value and meta-value are left null since they are not needed for this operation
      DoOperation<K, V, M> doOperation = new DoOperation<>(key, null, null, DoOperation.Operation.DELETE);

      // Send request to server and receive response
      AbstractAuthenticatedDoResponse<K, V, M> response =  requestAndGetResponse(idNonce, doOperation, DoOperation.Operation.DELETE.name());

      // Returns true if the response has verifiable signature and the request was successful and false otherwise
      return getValid(response, idNonce) && response.outcome.outcome == DoOperationOutcome.Outcome.SUCCESS;
    } catch (Exception e) {
      // Return false if any part of the request throws an exception
      return false;
    }
  }

  /**
   * Returns the value associated with the given key for the given user
   * 
   * @param userId The user requesting to read the value
   * @param key The key whose value is to be read
   * @return The value if the request is successful and null otherwise
   * @throws RemoteException
   * @throws NoSuchElementException
   */
  public V readVal(String userId, K key) throws RemoteException, NoSuchElementException {
    try {
      // Create a unique nonce for the request
      String idNonce = this.addNonce(userId);

      // Create the DoOperation to send to server
      // The value and meta-value are left null since they are not needed for this operation
      DoOperation<K, V, M> doOperation = new DoOperation<>(key, null, null, DoOperation.Operation.READVAL);

      // Send request to server and receive response
      AbstractAuthenticatedDoResponse<K, V, M> response =  requestAndGetResponse(idNonce, doOperation, DoOperation.Operation.READVAL.name());

      // Verify the signature
      boolean valid = getValid(response, idNonce);

      // Returns the value if the response has verifiable signature and the request was successful, throws NoSuchElementException if the key does not exist, and returns null otherwise 
      if (valid) {
        if (response.outcome.outcome == DoOperationOutcome.Outcome.SUCCESS) {
          return response.outcome.val;
        } else if (response.outcome.outcome == DoOperationOutcome.Outcome.NOSUCHELEMENT) {
          throw new NoSuchElementException();
        } else {
          return null;
        }
      } else {
        return null;
      }
    } catch (Exception e) {
      // Return false if any part of the request throws an exception
      return null;
    }
  }

  /**
   * Returns the meta-value associated with the given key for the given user
   * 
   * @param userId The user requesting to read the meta-value
   * @param key The key whose meta-value is to be read
   * @return The meta-value if the request is successful and null otherwise
   * @throws RemoteException
   * @throws NoSuchElementException
   */
  public M readMetaVal(String userId, K key) throws RemoteException, NoSuchElementException {
    try {
      // Create a unique nonce for the request
      String idNonce = this.addNonce(userId);

      // Create the DoOperation to send to server
      // The value and meta-value are left null since they are not needed for this operation
      DoOperation<K, V, M> doOperation = new DoOperation<>(key, null, null, DoOperation.Operation.READMETAVAL);

      // Send request to server and receive response
      AbstractAuthenticatedDoResponse<K, V, M> response =  requestAndGetResponse(idNonce, doOperation, DoOperation.Operation.READMETAVAL.name());

      // Verify the signature
      boolean valid = getValid(response, idNonce);

      // Returns the meta-value if the response has verifiable signature and the request was successful, throws NoSuchElementException if the key does not exist, and returns null otherwise 
      if (valid) {
        if (response.outcome.outcome == DoOperationOutcome.Outcome.SUCCESS) {
          return response.outcome.metaVal;
        } else if (response.outcome.outcome == DoOperationOutcome.Outcome.NOSUCHELEMENT) {
          throw new NoSuchElementException();
        } else {
          return null;
        }
      } else {
        return null;
      }
    } catch (Exception e) {
      // Return false if any part of the request throws an exception
      return null;
    }
  }

  /**
   * Write the new value to the store for the given key for the given user
   * 
   * @param userId The user requesting to write the value
   * @param key The key whose value should be changed
   * @param newVal The new value to write
   * @return True if the write is successful and false otherwise
   * @throws
   */
  public Boolean writeVal(String userId, K key, V newVal) throws RemoteException, IllegalArgumentException {
    try {
      // Create a unique nonce for the request
      String idNonce = this.addNonce(userId);

      // Create the DoOperation to send to server
      // The meta-value is left null since it is not needed for this operation
      DoOperation<K, V, M> doOperation = new DoOperation<>(key, newVal, null, DoOperation.Operation.WRITEVAL);
      // Send request to server and receive response
      AbstractAuthenticatedDoResponse<K, V, M> response =  requestAndGetResponse(idNonce, doOperation, DoOperation.Operation.WRITEVAL.name());

      // Returns true if the response has verifiable signature and the request was successful and false otherwise
      return returnWriteSuccess(response, getValid(response, idNonce));
    } catch (Exception e) {
      // Return false if any part of the request throws an exception
      return false;
    }
  }

  /**
   * Write the new meta-value to the store for the given key for the given user
   * 
   * @param userId The user requesting to write the meta-value
   * @param key The key whose meta-value should be changed
   * @param newMetaVal The new meta-value to write
   * @return True if the write is successful and false otherwise
   * @throws
   */
  public Boolean writeMetaVal(String userId, K key, M newMetaVal) throws RemoteException, IllegalArgumentException {
    try {
      // Create a unique nonce for the request
      String idNonce = this.addNonce(userId);

      // Create the DoOperation to send to server
      // The value is left null since it is not needed for this operation
      DoOperation<K, V, M> doOperation = new DoOperation<>(key, null, newMetaVal, DoOperation.Operation.WRITEMETAVAL);

      // Send request to server and receive response
      AbstractAuthenticatedDoResponse<K, V, M> response =  requestAndGetResponse(idNonce, doOperation, DoOperation.Operation.WRITEMETAVAL.name());

      // Returns true if the response has verifiable signature and the request was successful and false otherwise
      return returnWriteSuccess(response, getValid(response, idNonce));
    } catch (Exception e) {
      // Return false if any part of the request throws an exception
      return false;
    }
  }
}
      