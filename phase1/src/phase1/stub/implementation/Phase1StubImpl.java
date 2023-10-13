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

  public Phase1StubImpl() throws IOException, NotBoundException {
    super();
    this.serverVerificationKey = super.serverVerificationKey;
    this.userKeys = new Hashtable<>();
  }

  private PublicKey createPublicKey(byte[] publicBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
    return KeyFactory.getInstance("DSA").generatePublic(new X509EncodedKeySpec(publicBytes));
  }

  private byte[] createSignature(PrivateKey privateKey, byte[] contents) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    Signature server_sign = Signature.getInstance("SHA224withDSA");
    server_sign.initSign(privateKey);
    server_sign.update(contents); // For now – add security later
    return server_sign.sign();
  }

  private boolean verifySignature(PublicKey publicKey, byte[] contents, byte[] signature) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    Signature sign = Signature.getInstance("SHA224withDSA");
    sign.initVerify(publicKey);
    sign.update(contents);
    return sign.verify(signature);
  }

  private KeyPair createKeyPair() throws NoSuchAlgorithmException {
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DSA");
    keyPairGenerator.initialize(2048);
    return keyPairGenerator.generateKeyPair();
  }

  private byte[] convertKVMtoByte(K key, V val, M metaVal) throws IOException {
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    ObjectOutputStream stream = new ObjectOutputStream(byteStream);
    stream.writeObject(key);
    stream.writeObject(val);
    stream.writeObject(metaVal);
    stream.flush();

    return byteStream.toByteArray();
  }


  private Boolean getValid(AbstractAuthenticatedDoResponse<K, V, M> response, String idNonce) throws NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException, IOException {
    byte[] kvmArray = convertKVMtoByte(response.outcome.key, response.outcome.val, response.outcome.metaVal);

    byte[] combined = new byte[idNonce.getBytes().length + kvmArray.length + response.outcome.outcome.name().getBytes().length];
    ByteBuffer buffer = ByteBuffer.wrap(combined);
    buffer.put(idNonce.getBytes());
    buffer.put(kvmArray);
    buffer.put(response.outcome.outcome.name().getBytes());

    // contents: userid + nonce + key + val + metaVal + outcome
    return this.verifySignature(this.createPublicKey(this.serverVerificationKey), buffer.array(), response.digitalSignature);
  }

  private String extractId(String idNonce) {
    return idNonce.substring(0, idNonce.length()-36);
  }

  private AbstractAuthenticatedDoResponse<K, V, M> requestAndGetResponse(String idNonce, DoOperation<K, V, M> doOperation, String mode) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException {
    byte[] kvmArray = convertKVMtoByte(doOperation.key, doOperation.val, doOperation.metaVal);

    byte[] combined = createToSign((idNonce + mode).getBytes(), kvmArray);

    byte[] signature = this.createSignature(userKeys.get(this.extractId(idNonce)).getPrivate(), combined); //TODO: Don't just sign userId

    AbstractAuthenticatedDoRequest<K, V, M> req = new AuthenticatedDoRequest<>(idNonce, doOperation, signature);

    return this.network.handleAuthenticatedDo(req);
  }

  private String addNonce(String userId) {
    return userId + UUID.randomUUID();
  }

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

  private byte[] createToSign(byte[] idNonce, byte[] functionSpec) {
    byte[] combined = new byte[idNonce.length + functionSpec.length];
    ByteBuffer buffer = ByteBuffer.wrap(combined);
    buffer.put(idNonce);
    buffer.put(functionSpec);
    return buffer.array();
  }

  public Boolean registerUser(String userId) throws RemoteException {
    try {
      KeyPair keyPair = this.createKeyPair();
      this.userKeys.put(userId, keyPair);

      String idNonce = this.addNonce(userId); // TODO: Think more carefully about what to sign
      byte[] combined = createToSign(idNonce.getBytes(), keyPair.getPublic().getEncoded());

      byte[] signature = this.createSignature(keyPair.getPrivate(), combined); //TODO: Don't just sign userID

      AbstractAuthenticatedRegisterRequest req = new AuthenticatedRegisterRequest(idNonce, keyPair.getPublic().getEncoded(), signature);

      AbstractAuthenticatedRegisterResponse response = this.network.handleAuthenticatedRegister(req);

      byte[] return_combined = new byte[idNonce.getBytes().length + response.status.name().getBytes().length];
      ByteBuffer return_buffer = ByteBuffer.wrap(return_combined);
      return_buffer.put(idNonce.getBytes());
      return_buffer.put(response.status.name().getBytes());

      boolean valid = this.verifySignature(this.createPublicKey(this.serverVerificationKey), return_buffer.array(), response.digitalSignature); //TODO: Contents to verify will change

      return valid && response.status == AbstractAuthenticatedRegisterResponse.Status.OK;
    } catch (Exception e) {
      return false;
    }
  }

  public Boolean create(String userId, K key, V val, M metaVal) throws RemoteException {
    try {
      String idNonce = this.addNonce(userId);

      DoOperation<K, V, M> doOperation = new DoOperation<>(key, val, metaVal, DoOperation.Operation.CREATE);

      AbstractAuthenticatedDoResponse<K, V, M> response =  requestAndGetResponse(idNonce, doOperation, DoOperation.Operation.CREATE.name());

      return getValid(response, idNonce) && response.outcome.outcome == DoOperationOutcome.Outcome.SUCCESS;
    } catch (Exception e) {
      return false;
    }
  }

  public Boolean delete(String userId, K key) throws RemoteException {
    try {
      String idNonce = this.addNonce(userId);

      DoOperation<K, V, M> doOperation = new DoOperation<>(key, null, null, DoOperation.Operation.DELETE); // TODO: Make these null?

      AbstractAuthenticatedDoResponse<K, V, M> response =  requestAndGetResponse(idNonce, doOperation, DoOperation.Operation.DELETE.name());

      return getValid(response, idNonce) && response.outcome.outcome == DoOperationOutcome.Outcome.SUCCESS;
    } catch (Exception e) {
      return false;
    }
  }

  public V readVal(String userId, K key) throws RemoteException, NoSuchElementException {
    try {
      String idNonce = this.addNonce(userId);

      DoOperation<K, V, M> doOperation = new DoOperation<>(key, null, null, DoOperation.Operation.READVAL); // TODO: Make these null?

      AbstractAuthenticatedDoResponse<K, V, M> response =  requestAndGetResponse(idNonce, doOperation, DoOperation.Operation.READVAL.name());

      boolean valid = getValid(response, idNonce);

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
      return null;
    }
  }

  public M readMetaVal(String userId, K key) throws RemoteException, NoSuchElementException {
    try {
      String idNonce = this.addNonce(userId);

      DoOperation<K, V, M> doOperation = new DoOperation<>(key, null, null, DoOperation.Operation.READMETAVAL); // TODO: Make these null?

      AbstractAuthenticatedDoResponse<K, V, M> response =  requestAndGetResponse(idNonce, doOperation, DoOperation.Operation.READMETAVAL.name());

      boolean valid = getValid(response, idNonce);

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
      return null;
    }
  }

  public Boolean writeVal(String userId, K key, V newVal) throws RemoteException, IllegalArgumentException {
    try {
      String idNonce = this.addNonce(userId);

      DoOperation<K, V, M> doOperation = new DoOperation<>(key, newVal, null, DoOperation.Operation.WRITEVAL); // TODO: Make these null?

      AbstractAuthenticatedDoResponse<K, V, M> response =  requestAndGetResponse(idNonce, doOperation, DoOperation.Operation.WRITEVAL.name());

      return returnWriteSuccess(response, getValid(response, idNonce));

    } catch (Exception e) {
      return false;
    }
  }

  public Boolean writeMetaVal(String userId, K key, M newMetaVal) throws RemoteException, IllegalArgumentException {
    try {
      String idNonce = this.addNonce(userId);

      DoOperation<K, V, M> doOperation = new DoOperation<>(key, null, newMetaVal, DoOperation.Operation.WRITEMETAVAL); // TODO: Make these null?

      AbstractAuthenticatedDoResponse<K, V, M> response =  requestAndGetResponse(idNonce, doOperation, DoOperation.Operation.WRITEMETAVAL.name());

      return returnWriteSuccess(response, getValid(response, idNonce));
    } catch (Exception e) {
      return false;
    }
  }
}
      