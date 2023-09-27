package phase1.server.implementation;

import phase1.server.Phase1ServerBase;
import types.requests.AbstractAuthenticatedDoRequest;
import types.requests.AbstractAuthenticatedRegisterRequest;
import types.responses.AbstractAuthenticatedDoResponse;
import types.responses.AbstractAuthenticatedRegisterResponse;

import java.io.IOException;
import java.io.Serializable;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

public class Phase1ServerImpl<K extends Serializable, V extends Serializable, M extends Serializable> extends Phase1ServerBase<K, V, M> {

  public final byte[] signingKey;
  public final byte[] verificationKey;
  public Hashtable<String, byte[]> activeUsers;

  public Phase1ServerImpl() throws IOException { 
    super(); 
    this.signingKey = super.signingKey;
    this.verificationKey = super.verificationKey;
    this.activeUsers = new Hashtable<>();
  }

  private PrivateKey createPrivateKey(byte[] privateBytes) {
    return KeyFactory.getInstance("DSA").generatePrivate(new PKCS8EncodedKeySpec(privateBytes));
  }

  private PublicKey createPublicKey(byte[] publicBytes) {
    return KeyFactory.getInstance("DSA").generatePublic(new X509EncodedKeySpec(publicBytes));
  }

  public AbstractAuthenticatedRegisterResponse authenticatedRegister(AbstractAuthenticatedRegisterRequest req) {
    Signature sign = Signature.getInstance("DSA");
    sign.initVerify(this.createPublicKey(req.verificationKey));
    sign.update(req.userId.getBytes());
    boolean valid = sign.verify(req.digitalSignature);

    AbstractAuthenticatedRegisterResponse.Status status;
    if (!valid) {
      status = AbstractAuthenticatedRegisterResponse.Status.AuthenticationFailure;
    } else if (activeUsers.containsKey(req.userId)) {
      status = AbstractAuthenticatedRegisterResponse.Status.UserAlreadyExists;
    }
    else {
      activeUsers.put(req.userId, req.verificationKey);
      status = AbstractAuthenticatedRegisterResponse.Status.OK;
    }
    Signature server_sign = Signature.getInstance("DSA");
    server_sign.initSign(this.createPrivateKey(this.signingKey));
    server_sign.update(status.name().getBytes());


    return new AbstractAuthenticatedRegisterResponse(status, server_sign.sign());
  }

  public AbstractAuthenticatedDoResponse<K, V, M> authenticatedDo(AbstractAuthenticatedDoRequest<K, V, M> req) {
    Signature sign = Signature.getInstance("DSA");
    sign.initVerify(this.createPublicKey(req.verificationKey)); // CONTINUE FROM HERE
    sign.update(req.userId.getBytes());
    boolean valid = sign.verify(req.digitalSignature);
    return null;
  }
}
