package phase1.server.implementation;

import phase1.server.Phase1ServerBase;
import types.requests.AbstractAuthenticatedDoRequest;
import types.requests.AbstractAuthenticatedRegisterRequest;
import types.responses.AbstractAuthenticatedDoResponse;
import types.responses.AbstractAuthenticatedRegisterResponse;

import java.io.IOException;
import java.io.Serializable;
import java.security.Signature;
import java.util.Set;

public class Phase1ServerImpl<K extends Serializable, V extends Serializable, M extends Serializable> extends Phase1ServerBase<K, V, M> {

  public final byte[] signingKey;
  public final byte[] verificationKey;
  public Set<String> activeUsers;

  public Phase1ServerImpl() throws IOException { 
    super(); 
    this.signingKey = super.signingKey;
    this.verificationKey = super.verificationKey;
    this.activeUsers = new HashSet<String>();
  }

  public AbstractAuthenticatedRegisterResponse authenticatedRegister(AbstractAuthenticatedRegisterRequest req) {
    Signature sign = Signature.getInstance("DSA");
    sign.initVerify(req.verificationKey); // This is a byte [] but might need to be a PublicKey? Also, do we want to use the servers verification key???????
    sign.update(req.userId);
    boolean valid = sign.verify(req.digitalSignature);

    AbstractAuthenticatedRegisterResponse response;
    AbstractAuthenticatedRegisterResponse.Status status;
    if (!valid) {
      status = AbstractAuthenticatedRegisterResponse.Status.AuthenticationFailure;
    } else if (activeUsers.contains(req.userId)) {
      status = AbstractAuthenticatedRegisterResponse.Status.UserAlreadyExists;
    }
    else {
      activeUsers.add(req.userId);
      status = AbstractAuthenticatedRegisterResponse.Status.OK;
    }
    Signature server_sign = Signature.getInstance("DSA"); // ERROR?
    server_sign.initSign(this.signingKey); // FIX TYPES
    server_sign.update(status); // FIX TYPES

    response = new AbstractAuthenticatedRegisterResponse(status, server_sign.sign());
    return response;
  }

  public AbstractAuthenticatedDoResponse<K, V, M> authenticatedDo(AbstractAuthenticatedDoRequest<K, V, M> req) {
    // implement me
    return null;
  }
}
