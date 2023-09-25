package types.requests.implementation;

import types.requests.AbstractAuthenticatedRegisterRequest;

import java.io.Serializable;

public class AuthenticatedRegisterRequest extends AbstractAuthenticatedRegisterRequest {

  public AuthenticatedRegisterRequest(String userId, byte[] verificationKey, byte[] digitalSignature) {
    super(userId, verificationKey, digitalSignature);
  }
}
