package types.requests.implementation;

import types.requests.AbstractAuthenticatedDoRequest;
import types.requests.DoOperation;

import java.io.Serializable;

public class AuthenticatedDoRequest<K extends Serializable, V extends Serializable, M extends Serializable> extends AbstractAuthenticatedDoRequest<K, V, M> {
  
  public AuthenticatedDoRequest(String userId, DoOperation<K, V, M> doOperation, byte[] digitalSignature) {
    super(userId, doOperation, digitalSignature);
  }
}
