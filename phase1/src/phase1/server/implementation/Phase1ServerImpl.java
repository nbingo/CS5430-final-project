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

  public Phase1ServerImpl() throws IOException { 
    super(); 
    this.signingKey = super.signingKey;
    this.verificationKey = super.verificationKey;
  }

  public AbstractAuthenticatedRegisterResponse authenticatedRegister(AbstractAuthenticatedRegisterRequest req) {
    // implement me
    return null;
  }

  public AbstractAuthenticatedDoResponse<K, V, M> authenticatedDo(AbstractAuthenticatedDoRequest<K, V, M> req) {
    // implement me
    return null;
  }
}
