package types.responses.implementation;

import types.responses.AbstractAuthenticatedRegisterResponse;

public class AuthenticatedRegisterResponse extends AbstractAuthenticatedRegisterResponse {
  
  public AuthenticatedRegisterResponse(AbstractAuthenticatedRegisterResponse.Status status, byte[] digitalSignature) {
    super(status, digitalSignature);
  }
}
