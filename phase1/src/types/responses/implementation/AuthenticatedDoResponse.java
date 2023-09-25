package types.responses.implementation;

import types.responses.AbstractAuthenticatedDoResponse;
import types.responses.DoOperationOutcome;

import java.io.Serializable;

public class AuthenticatedDoResponse<K extends Serializable, V extends Serializable, M extends Serializable> extends AbstractAuthenticatedDoResponse<K, V, M> {
  
  public AuthenticatedDoResponse(DoOperationOutcome<K, V, M> outcome, byte[] digitalSignature) { 
    super(outcome, digitalSignature);
  }
}
