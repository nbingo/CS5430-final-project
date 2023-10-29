package network.implementation;

import network.NetworkBase;

import types.requests.AbstractAuthenticatedDoRequest;
import types.requests.AbstractAuthenticatedRegisterRequest;
import types.responses.AbstractAuthenticatedDoResponse;
import types.responses.AbstractAuthenticatedRegisterResponse;

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class NetworkImpl<K extends Serializable, V extends Serializable, M extends Serializable> extends NetworkBase<K, V, M> {

  public NetworkImpl() throws NotBoundException, RemoteException {
    super();
  }

  public AbstractAuthenticatedRegisterResponse handleAuthenticatedRegister(AbstractAuthenticatedRegisterRequest request) throws RemoteException {
    return super.server.authenticatedRegister(request);
  }

  public AbstractAuthenticatedDoResponse<K, V, M> handleAuthenticatedDo(AbstractAuthenticatedDoRequest<K, V, M> request) throws RemoteException {
    return super.server.authenticatedDo(request);
  }
}
