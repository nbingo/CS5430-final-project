package phase1.server.implementation;

import phase1.server.Phase1ServerBase;
import types.requests.AbstractAuthenticatedDoRequest;
import types.requests.AbstractAuthenticatedRegisterRequest;
import types.responses.AbstractAuthenticatedDoResponse;
import types.responses.AbstractAuthenticatedRegisterResponse;

import java.io.IOException;
import java.io.Serializable;

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
    boolean valid = sign.verify(req.digitalSignature);

    AbstractAuthenticatedRegisterResponse response;
    if (!valid) {
      response = new AbstractAuthenticatedRegisterResponse(AbstractAuthenticatedRegisterResponse.Status.AuthenticationFailure, req.digitalSignature); // DOES THE SERVER SIGN HERE?
    } else if (activeUsers.contains(req.userId)) {
      response = new AbstractAuthenticatedRegisterResponse(AbstractAuthenticatedRegisterResponse.Status.UserAlreadyExists, req.digitalSignature); // DOES THE SERVER SIGN HERE?
    }
    else {
      activeUsers.add(req.userId);
      response = new AbstractAuthenticatedRegisterResponse(AbstractAuthenticatedRegisterResponse.Status.OK, req.digitalSignature); // DOES THE SERVER SIGN HERE?
    }
    return response;
  }

  public AbstractAuthenticatedDoResponse<K, V, M> authenticatedDo(AbstractAuthenticatedDoRequest<K, V, M> req) {
    // implement me
    return null;
  }
}
