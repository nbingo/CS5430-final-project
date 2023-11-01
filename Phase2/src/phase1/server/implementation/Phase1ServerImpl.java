package phase1.server.implementation;

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

public class Phase1ServerImpl<K extends Serializable, V extends Serializable, M extends Serializable> extends Phase1ServerBase<K, V, M> {

  private final byte[] signingKey;
  private final byte[] verificationKey;
  private final Hashtable<String, byte[]> activeUsers;

  protected final Phase0Impl<K, V, M> store;

  public Phase1ServerImpl() throws IOException {
    super();
    this.signingKey = super.signingKey;
    this.verificationKey = super.verificationKey;
    this.activeUsers = new Hashtable<>();
    this.store = new Phase0Impl<>();
  }

  /**
   * Take an encoded private key as a byte array and provide it as a PrivateKey object via DSA.
   * @param privateBytes The encoded DSA private key to use.
   * @return The DSA private key as a PrivateKey object ready to be used in signing.
   * @throws NoSuchAlgorithmException If DSA does not exist for some reason.
   * @throws InvalidKeySpecException If the specification is incorrect for some reason.
   */
  private PrivateKey createPrivateKey(byte[] privateBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
    return KeyFactory.getInstance("DSA").generatePrivate(new PKCS8EncodedKeySpec(privateBytes));
  }

  /**
   * Take an encoded public key as a byte array and provide it as a PublicKey object via DSA.
   * @param publicBytes The encoded DSA public key to use.
   * @return The DSA public key as a PublicKey object ready to be used in signing.
   * @throws NoSuchAlgorithmException If DSA does not exist for some reason.
   * @throws InvalidKeySpecException If the specification is incorrect for some reason.
   */
  private PublicKey createPublicKey(byte[] publicBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
    return KeyFactory.getInstance("DSA").generatePublic(new X509EncodedKeySpec(publicBytes));
  }

  /**
   * Create a signature using the given private/signing key to sign the given contents.
   * @param privateKey The private key to use
   * @param contents The contents to be signed
   * @return The signature with the signed contents
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeyException
   * @throws SignatureException
   */
  private byte[] createSignature(PrivateKey privateKey, byte[] contents) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
    Signature server_sign = Signature.getInstance("SHA224withDSA");
    server_sign.initSign(privateKey);
    server_sign.update(contents); // For now – add security later
    return server_sign.sign();
  }

  /**
   * Verify a signature against some contents with a given public/verification key.
   * @param publicKey The public/verification key to use to decode the signature.
   * @param contents The contents to match to the decoded contents of the signature.
   * @param signature The encoded signature as a byte array.
   * @return Whether the signature was correctly verified or not.
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
   * Take a concatenated userID and nonce string to extract just the userID from it.
   * @param idNonce The concatenated userID and nonce string.
   * @return Just the userID.
   */
  private String extractId(String idNonce) {
    return idNonce.substring(0, idNonce.length()-36);
  }

  /**
   * Take a key, value, meta-value triple and use the fact that they're serializable to convert them to a byte array.
   * @param key The key in the triple.
   * @param val The value in the triple.
   * @param metaVal The meta-value in the triple.
   * @return The key, value, meta-value triple as a byte array from their serialization implementation.
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

  // TODO: Add Javadocs for me!
  protected AuthenticatedDoResponse<K, V, M> getSignedAuthenticatedDoResponse(AbstractAuthenticatedDoRequest<K, V, M> req, K key, V val, M metaVal, DoOperationOutcome.Outcome outcome) throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidKeySpecException {
    // Put the resulting key, value, and meta-value in our do operation outcome, along with the outcome status. If we
    // performed a read operation, then the value or meta-value, whichever was requested, is changed and returned via
    // the DoOperationResponse.
    DoOperationOutcome<K, V, M> doOperationOutcome = new DoOperationOutcome<>(key, val, metaVal, outcome);

    // Convert the K,V,M triple to a byte array for signing
    byte[] newKeyValMetaVal = convertKVMtoByte(key, val, metaVal);

    // Combine the converted K,V,M triple with the user id, the given nonce, and the outcome of the operation into one
    // byte array for signing.
    byte[] new_combined = new byte[req.userId.getBytes().length + newKeyValMetaVal.length + outcome.name().getBytes().length];
    ByteBuffer new_buffer = ByteBuffer.wrap(new_combined);
    new_buffer.put(req.userId.getBytes());
    new_buffer.put(newKeyValMetaVal);
    new_buffer.put(outcome.name().getBytes());

    // Sign the byte array using the server's private/signing key
    byte[] signature = this.createSignature(this.createPrivateKey(this.signingKey), new_buffer.array());

    // Return the response
    return new AuthenticatedDoResponse<>(doOperationOutcome, signature);
  }


  /**
   * This method can handle an AbstractAuthenticatedRegisterRequest to create a new user. It will attempt to create the
   * new user if one with the same username does not exist and otherwise fail with a UserAlreadyExists status. The
   * registration may also fail if the user's signature is incorrect constructed, giving an AuthenticationFailure
   * status. If the user is successfully registered, the server will respond with an OK.
   *
   * The server signs its userID, the given nonce, and its response status in its signature.
   * @param req The RegisterRequest to handle.
   * @return The RegisterResponse to the user with the status of their requested register operation.
   */
  public AbstractAuthenticatedRegisterResponse authenticatedRegister(AbstractAuthenticatedRegisterRequest req) {
    try {
      // Extract the userID from the userID + nonce string
      String extractedId = this.extractId(req.userId);

      // Create the contents that we will use against the user's signature for verification.
      byte[] combined = new byte[req.userId.getBytes().length + req.verificationKey.length];
      ByteBuffer buffer = ByteBuffer.wrap(combined);
      buffer.put(req.userId.getBytes());
      buffer.put(req.verificationKey);

      // Determine whether the user's signature is valid
      boolean valid = verifySignature(this.createPublicKey(req.verificationKey), buffer.array(), req.digitalSignature);

      AbstractAuthenticatedRegisterResponse.Status status;

      if (!valid) {
        // The user's signature was not valid, so we respond with an authentication failure.
        status = AbstractAuthenticatedRegisterResponse.Status.AuthenticationFailure;
      } else if (activeUsers.containsKey(extractedId)) {
        // The username already exists, so respond accordingly
        status = AbstractAuthenticatedRegisterResponse.Status.UserAlreadyExists;
      } else {
        // The user's signature is valid and their requested username does not exist, so register them and respond OK.
        activeUsers.put(extractedId, req.verificationKey);
        status = AbstractAuthenticatedRegisterResponse.Status.OK;
      }

      // Create contents of signature to be sent back to the user. This includes the user ID, the given nonce, and the
      // status from the operation.
      byte[] return_combined = new byte[req.userId.getBytes().length + status.name().getBytes().length];
      ByteBuffer return_buffer = ByteBuffer.wrap(return_combined);
      return_buffer.put(req.userId.getBytes());
      return_buffer.put(status.name().getBytes());

      // Create the signature
      byte[] signature = createSignature(this.createPrivateKey(this.signingKey), return_buffer.array());

      return new AuthenticatedRegisterResponse(status, signature);
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Handle a DoRequest, which may be to create/delete a K,V,M triple, or read to/write from a K,V,M triple. This will
   * fail if the user's signature is not properly created or if the operation itself does not semantically make sense.
   * @param req The DoRequest to handle.
   * @return The result from the handled DoRequest.
   */
  public AbstractAuthenticatedDoResponse<K, V, M> authenticatedDo(AbstractAuthenticatedDoRequest<K, V, M> req) {
    try {
      // Extract the key, value, meta-value from the request.
      K key = req.doOperation.key;
      V val = req.doOperation.val;
      M metaVal = req.doOperation.metaVal;

      // Convert the userID, given nonce, and name of the requested operation to a byte array to be used in signature.
      // verification.
      byte[] idNonceEnum = (req.userId + req.doOperation.operation.name()).getBytes();

      // Convert the key, value, meta-value in the DoRequest to a byte array to be used in signature.
      byte[] keyValMetaVal = convertKVMtoByte(key, val, metaVal);

      // Combine the userID, given nonce, name of the requested operation, key, value, and meta-value in the DoRequest
      // as one byte array.
      byte[] combined = new byte[idNonceEnum.length + keyValMetaVal.length];
      ByteBuffer buffer = ByteBuffer.wrap(combined);
      buffer.put(idNonceEnum);
      buffer.put(keyValMetaVal);

      // Use the created byte array to verify the user's signature using the stored public key for the user saved from
      // their registration.
      boolean valid = verifySignature(this.createPublicKey(this.activeUsers.get(this.extractId(req.userId))), buffer.array(), req.digitalSignature);
      // Our default setting is to have an authentication failure if some miscellaneous error occurs.
      DoOperationOutcome.Outcome outcome = DoOperationOutcome.Outcome.AUTHENTICATION_FAILURE;

      if (valid) {
        // If the signature is valid, then we will attempt the operation.
        switch (req.doOperation.operation) {
          case CREATE:
            // Create a K,V,M triple using the given items.
            store.create(req.doOperation.key, req.doOperation.val, req.doOperation.metaVal);
            outcome = DoOperationOutcome.Outcome.SUCCESS;
            break;
          case DELETE:
            // Delete the requested key whether it exists or not in the store
            store.delete(req.doOperation.key);
            outcome = DoOperationOutcome.Outcome.SUCCESS;
            break;
          case READVAL:
            // Try to read the value of the requested key if it exists and otherwise throw a NoSuchElement exception.
            try {
              val = store.readVal(req.doOperation.key);
              outcome = DoOperationOutcome.Outcome.SUCCESS;
            } catch (NoSuchElementException e) {
              outcome = DoOperationOutcome.Outcome.NOSUCHELEMENT;
            }
            break;
          case WRITEVAL:
            // Try to write the meta-value of the requested key if it exists and otherwise throw an IllegalArgument exception.
            try {
              store.writeVal(req.doOperation.key, req.doOperation.val);
              outcome = DoOperationOutcome.Outcome.SUCCESS;
            } catch (IllegalArgumentException e) {
              outcome = DoOperationOutcome.Outcome.ILLEGALARGUMENT;
            }
            break;
          case READMETAVAL:
            // Try to read the meta-value of the requested key if it exists and otherwise throw a NoSuchElement exception.
            try {
              metaVal = store.readMetaVal(req.doOperation.key);
              outcome = DoOperationOutcome.Outcome.SUCCESS;
            } catch (NoSuchElementException e) {
              outcome = DoOperationOutcome.Outcome.NOSUCHELEMENT;
            }
            break;
          case WRITEMETAVAL:
            // Try to write the meta-value of the requested key if it exists and otherwise throw an IllegalArgument exception.
            try {
              store.writeMetaVal(req.doOperation.key, req.doOperation.metaVal);
              outcome = DoOperationOutcome.Outcome.SUCCESS;
            } catch (IllegalArgumentException e) {
              outcome = DoOperationOutcome.Outcome.ILLEGALARGUMENT;
            }
            break;
        }
      }

      return getSignedAuthenticatedDoResponse(req, key, val, metaVal, outcome);
    } catch (Exception e) {
      return null;
    }
  }
}
