package phase1.server.implementation;

import phase0.implementation.Phase0;
import phase0.implementation.Phase0Impl;
import phase1.server.Phase1ServerBase;
import types.requests.AbstractAuthenticatedDoRequest;
import types.requests.AbstractAuthenticatedRegisterRequest;
import types.responses.AbstractAuthenticatedDoResponse;
import types.responses.AbstractAuthenticatedRegisterResponse;
import types.responses.DoOperationOutcome;
import types.responses.implementation.AuthenticatedDoResponse;
import types.responses.implementation.AuthenticatedRegisterResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Hashtable;
import java.util.NoSuchElementException;

// TODO: TO GENERATE UNIQUE VALUES WITH LOW PROBABLITY OF REPEAT USE UUID
public class Phase1ServerImpl<K extends Serializable, V extends Serializable, M extends Serializable> extends Phase1ServerBase<K, V, M> {

  private final byte[] signingKey;
  private final byte[] verificationKey;
  private final Hashtable<String, byte[]> activeUsers;

  private Phase0Impl<K, V, M> store; //TODO: Should have a hash table of user IDs to stores :(

  public Phase1ServerImpl() throws IOException {
    super();
    this.signingKey = super.signingKey;
    this.verificationKey = super.verificationKey;
    this.activeUsers = new Hashtable<>();
    this.store = new Phase0Impl<>();
  }

  private PrivateKey createPrivateKey(byte[] privateBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
    return KeyFactory.getInstance("DSA").generatePrivate(new PKCS8EncodedKeySpec(privateBytes));
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

  private String extractId(String idNonce) {
    return idNonce.substring(0, idNonce.length()-36);
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


  public AbstractAuthenticatedRegisterResponse authenticatedRegister(AbstractAuthenticatedRegisterRequest req) {
    try {
      String extractedId = this.extractId(req.userId);

      byte[] combined = new byte[req.userId.getBytes().length + req.verificationKey.length];
      ByteBuffer buffer = ByteBuffer.wrap(combined);
      buffer.put(req.userId.getBytes());
      buffer.put(req.verificationKey);

      boolean valid = verifySignature(this.createPublicKey(req.verificationKey), buffer.array(), req.digitalSignature); // TODO: We shouldn't just be signing the userID – V. vulnerable to replay attacks

      AbstractAuthenticatedRegisterResponse.Status status;

      if (!valid) {
        status = AbstractAuthenticatedRegisterResponse.Status.AuthenticationFailure;
      } else if (activeUsers.containsKey(extractedId)) {
        status = AbstractAuthenticatedRegisterResponse.Status.UserAlreadyExists;
      } else {
        activeUsers.put(extractedId, req.verificationKey);
        status = AbstractAuthenticatedRegisterResponse.Status.OK;
      }

      byte[] return_combined = new byte[req.userId.getBytes().length + status.name().getBytes().length];
      ByteBuffer return_buffer = ByteBuffer.wrap(return_combined);
      return_buffer.put(req.userId.getBytes());
      return_buffer.put(status.name().getBytes());

      byte[] signature = createSignature(this.createPrivateKey(this.signingKey), return_buffer.array()); //TODO: Don't just sign status

      return new AuthenticatedRegisterResponse(status, signature);
    } catch (Exception e) {
      return null;
    }
  }

  public AbstractAuthenticatedDoResponse<K, V, M> authenticatedDo(AbstractAuthenticatedDoRequest<K, V, M> req) {
    try {
      K key = req.doOperation.key;
      V val = req.doOperation.val;
      M metaVal = req.doOperation.metaVal;

      byte[] idNonceEnum = (req.userId + req.doOperation.operation.name()).getBytes();

      byte[] keyValMetaVal = convertKVMtoByte(key, val, metaVal);

      byte[] combined = new byte[idNonceEnum.length + keyValMetaVal.length];
      ByteBuffer buffer = ByteBuffer.wrap(combined);
      buffer.put(idNonceEnum);
      buffer.put(keyValMetaVal);

      boolean valid = verifySignature(this.createPublicKey(this.activeUsers.get(this.extractId(req.userId))), buffer.array(), req.digitalSignature); // TODO: Figure out what to actually have signed here (userId + operation???)
      DoOperationOutcome.Outcome outcome = DoOperationOutcome.Outcome.AUTHENTICATION_FAILURE;

      if (valid) {
        switch (req.doOperation.operation) {
          case CREATE:
            store.create(req.doOperation.key, req.doOperation.val, req.doOperation.metaVal);
            outcome = DoOperationOutcome.Outcome.SUCCESS;
            break;
          case DELETE:
            store.delete(req.doOperation.key);
            outcome = DoOperationOutcome.Outcome.SUCCESS;
            break;
          case READVAL:
            try {
              val = store.readVal(req.doOperation.key);
              outcome = DoOperationOutcome.Outcome.SUCCESS;
            } catch (NoSuchElementException e) {
              outcome = DoOperationOutcome.Outcome.NOSUCHELEMENT;
            }
            break;
          case WRITEVAL:
            try {
              store.writeVal(req.doOperation.key, req.doOperation.val);
              outcome = DoOperationOutcome.Outcome.SUCCESS;
            } catch (IllegalArgumentException e) {
              outcome = DoOperationOutcome.Outcome.ILLEGALARGUMENT;
            }
            break;
          case READMETAVAL:
            try {
              metaVal = store.readMetaVal(req.doOperation.key);
              outcome = DoOperationOutcome.Outcome.SUCCESS;
            } catch (NoSuchElementException e) {
              outcome = DoOperationOutcome.Outcome.NOSUCHELEMENT;
            }
            break;
          case WRITEMETAVAL:
            try {
              store.writeMetaVal(req.doOperation.key, req.doOperation.metaVal);
              outcome = DoOperationOutcome.Outcome.SUCCESS;
            } catch (IllegalArgumentException e) {
              outcome = DoOperationOutcome.Outcome.ILLEGALARGUMENT;
            }
            break;
        }
      }
      DoOperationOutcome<K, V, M> doOperationOutcome = new DoOperationOutcome<>(key, val, metaVal, outcome);

      byte[] newKeyValMetaVal = convertKVMtoByte(key, val, metaVal);

      byte[] new_combined = new byte[req.userId.getBytes().length + newKeyValMetaVal.length + outcome.name().getBytes().length];
      ByteBuffer new_buffer = ByteBuffer.wrap(new_combined);
      new_buffer.put(req.userId.getBytes());
      new_buffer.put(newKeyValMetaVal);
      new_buffer.put(outcome.name().getBytes());

      byte[] signature = this.createSignature(this.createPrivateKey(this.signingKey), new_buffer.array()); // TODO: don't just sign the outcome

      return new AuthenticatedDoResponse<K, V, M>(doOperationOutcome, signature);
    } catch (Exception e) {
      return null;
    }
  }
}
