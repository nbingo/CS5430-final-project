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

import java.io.IOException;
import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Hashtable;
import java.util.NoSuchElementException;

public class Phase1StubImpl<K extends Serializable, V extends Serializable, M extends Serializable> extends Phase1StubBase<K, V, M> {

  private final byte[] serverVerificationKey;

  private final Hashtable<String, KeyPair> userKeys;

  public Phase1StubImpl() throws IOException, NotBoundException {
    super();
    this.serverVerificationKey = super.serverVerificationKey;
    this.network = super.network;
    this.userKeys = new Hashtable<>();
  }

  private PrivateKey createPrivateKey(byte[] privateBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
    return KeyFactory.getInstance("DSA").generatePrivate(new PKCS8EncodedKeySpec(privateBytes));
  }

  private PublicKey createPublicKey(byte[] publicBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
    return KeyFactory.getInstance("DSA").generatePublic(new X509EncodedKeySpec(publicBytes));
  }

  private byte[] createSignature(PrivateKey privateKey, byte[] contents) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    Signature server_sign = Signature.getInstance("DSA");
    server_sign.initSign(privateKey);
    server_sign.update(contents); // For now – add security later
    return server_sign.sign();
  }

  private boolean verifySignature(PublicKey publicKey, byte[] contents, byte[] signature) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    Signature sign = Signature.getInstance("DSA");
    sign.initVerify(publicKey);
    sign.update(contents);
    return sign.verify(signature);
  }

  private KeyPair createKeyPair() throws NoSuchAlgorithmException {
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DSA");
    keyPairGenerator.initialize(2048);
    return keyPairGenerator.generateKeyPair();
  }

  public Boolean registerUser(String userId) throws RemoteException {
    try {
      KeyPair keyPair = this.createKeyPair();
      this.userKeys.put(userId, keyPair);

      byte[] signature = this.createSignature(keyPair.getPrivate(), userId.getBytes()); //TODO: Don't just sign userID

      AbstractAuthenticatedRegisterRequest req = new AuthenticatedRegisterRequest(userId, keyPair.getPublic().getEncoded(), signature);

      AbstractAuthenticatedRegisterResponse response = this.network.handleAuthenticatedRegister(req);

      boolean valid = this.verifySignature(this.createPublicKey(this.serverVerificationKey), response.status.name().getBytes(), response.digitalSignature); //TODO: Contents to verify will change

      return valid && response.status == AbstractAuthenticatedRegisterResponse.Status.OK;
    } catch (Exception e) {
      return false;
    }
  }

  public Boolean create(String userId, K key, V val, M metaVal) throws RemoteException {
    try {
      DoOperation<K, V, M> doOperation = new DoOperation<>(key, val, metaVal, DoOperation.Operation.CREATE);
      byte[] signature = this.createSignature(userKeys.get(userId).getPrivate(), userId.getBytes()); //TODO: Don't just sign userId

      AbstractAuthenticatedDoRequest<K, V, M> req = new AuthenticatedDoRequest<>(userId, doOperation, signature);

      AbstractAuthenticatedDoResponse<K, V, M> response = this.network.handleAuthenticatedDo(req);

      boolean valid = this.verifySignature(this.createPublicKey(this.serverVerificationKey), response.outcome.outcome.name().getBytes(), response.digitalSignature);

      return valid && response.outcome.outcome == DoOperationOutcome.Outcome.SUCCESS;
    } catch (Exception e) {
      return false;
    }
  }

  public Boolean delete(String userId, K key) throws RemoteException {
    try {
      DoOperation<K, V, M> doOperation = new DoOperation<>(key, null, null, DoOperation.Operation.DELETE); // TODO: Make these null?
      byte[] signature = this.createSignature(userKeys.get(userId).getPrivate(), userId.getBytes()); //TODO: Don't just sign userId

      AbstractAuthenticatedDoRequest<K, V, M> req = new AuthenticatedDoRequest<>(userId, doOperation, signature);

      AbstractAuthenticatedDoResponse<K, V, M> response = this.network.handleAuthenticatedDo(req);

      boolean valid = this.verifySignature(this.createPublicKey(this.serverVerificationKey), response.outcome.outcome.name().getBytes(), response.digitalSignature);

      return valid && response.outcome.outcome == DoOperationOutcome.Outcome.SUCCESS;
    } catch (Exception e) {
      return false;
    }
  }

  public V readVal(String userId, K key) throws RemoteException, NoSuchElementException {
    try {
      DoOperation<K, V, M> doOperation = new DoOperation<>(key, null, null, DoOperation.Operation.READVAL); // TODO: Make these null?
      byte[] signature = this.createSignature(userKeys.get(userId).getPrivate(), userId.getBytes()); //TODO: Don't just sign userId

      AbstractAuthenticatedDoRequest<K, V, M> req = new AuthenticatedDoRequest<>(userId, doOperation, signature);

      AbstractAuthenticatedDoResponse<K, V, M> response = this.network.handleAuthenticatedDo(req);

      boolean valid = this.verifySignature(this.createPublicKey(this.serverVerificationKey), response.outcome.outcome.name().getBytes(), response.digitalSignature);

      if (!valid || response.outcome.outcome == DoOperationOutcome.Outcome.AUTHENTICATION_FAILURE) { // TODO: Okay but like what
        return null;
      } else if (response.outcome.outcome == DoOperationOutcome.Outcome.SUCCESS) {
        return response.outcome.val;
      } else if (response.outcome.outcome == DoOperationOutcome.Outcome.NOSUCHELEMENT) {
        throw new NoSuchElementException();
      } else {
        return null; // We should never get here!
      }
    } catch (Exception e) {
      return null;
    }
  }

  public M readMetaVal(String userId, K key) throws RemoteException, NoSuchElementException {
    try {
      DoOperation<K, V, M> doOperation = new DoOperation<>(key, null, null, DoOperation.Operation.READMETAVAL); // TODO: Make these null?
      byte[] signature = this.createSignature(userKeys.get(userId).getPrivate(), userId.getBytes()); //TODO: Don't just sign userId

      AbstractAuthenticatedDoRequest<K, V, M> req = new AuthenticatedDoRequest<>(userId, doOperation, signature);

      AbstractAuthenticatedDoResponse<K, V, M> response = this.network.handleAuthenticatedDo(req);

      boolean valid = this.verifySignature(this.createPublicKey(this.serverVerificationKey), response.outcome.outcome.name().getBytes(), response.digitalSignature);

      if (!valid || response.outcome.outcome == DoOperationOutcome.Outcome.AUTHENTICATION_FAILURE) { // TODO: Okay but like what
        return null;
      } else if (response.outcome.outcome == DoOperationOutcome.Outcome.SUCCESS) {
        return response.outcome.metaVal;
      } else if (response.outcome.outcome == DoOperationOutcome.Outcome.NOSUCHELEMENT) {
        throw new NoSuchElementException();
      } else {
        return null; // We should never get here!
      }
    } catch (Exception e) {
      return null;
    }
  }

  public Boolean writeVal(String userId, K key, V newVal) throws RemoteException, IllegalArgumentException {
    try {
      DoOperation<K, V, M> doOperation = new DoOperation<>(key, newVal, null, DoOperation.Operation.WRITEVAL); // TODO: Make these null?
      byte[] signature = this.createSignature(userKeys.get(userId).getPrivate(), userId.getBytes()); //TODO: Don't just sign userId

      AbstractAuthenticatedDoRequest<K, V, M> req = new AuthenticatedDoRequest<>(userId, doOperation, signature);

      AbstractAuthenticatedDoResponse<K, V, M> response = this.network.handleAuthenticatedDo(req);

      boolean valid = this.verifySignature(this.createPublicKey(this.serverVerificationKey), response.outcome.outcome.name().getBytes(), response.digitalSignature);

      if (!valid || response.outcome.outcome == DoOperationOutcome.Outcome.AUTHENTICATION_FAILURE) { // TODO: Okay but like what
        return false;
      } else if (response.outcome.outcome == DoOperationOutcome.Outcome.SUCCESS) {
        return true;
      } else if (response.outcome.outcome == DoOperationOutcome.Outcome.ILLEGALARGUMENT) {
        throw new IllegalArgumentException();
      } else {
        return false; // We should never get here!
      }
    } catch (Exception e) {
      return false;
    }
  }

  public Boolean writeMetaVal(String userId, K key, M newMetaVal) throws RemoteException, IllegalArgumentException {
    try {
      DoOperation<K, V, M> doOperation = new DoOperation<>(key, null, newMetaVal, DoOperation.Operation.WRITEMETAVAL); // TODO: Make these null?
      byte[] signature = this.createSignature(userKeys.get(userId).getPrivate(), userId.getBytes()); //TODO: Don't just sign userId

      AbstractAuthenticatedDoRequest<K, V, M> req = new AuthenticatedDoRequest<>(userId, doOperation, signature);

      AbstractAuthenticatedDoResponse<K, V, M> response = this.network.handleAuthenticatedDo(req);

      boolean valid = this.verifySignature(this.createPublicKey(this.serverVerificationKey), response.outcome.outcome.name().getBytes(), response.digitalSignature);

      if (!valid || response.outcome.outcome == DoOperationOutcome.Outcome.AUTHENTICATION_FAILURE) { // TODO: Okay but like what
        return false;
      } else if (response.outcome.outcome == DoOperationOutcome.Outcome.SUCCESS) {
        return true;
      } else if (response.outcome.outcome == DoOperationOutcome.Outcome.ILLEGALARGUMENT) {
        throw new IllegalArgumentException();
      } else {
        return false; // We should never get here!
      }
    } catch (Exception e) {
      return false;
    }
  }
}
      