//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package network;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import types.requests.AbstractAuthenticatedDoRequest;
import types.requests.AbstractAuthenticatedRegisterRequest;
import types.responses.AbstractAuthenticatedDoResponse;
import types.responses.AbstractAuthenticatedRegisterResponse;

public interface Network<K extends Serializable, V extends Serializable, M extends Serializable> extends Remote {
    AbstractAuthenticatedRegisterResponse handleAuthenticatedRegister(AbstractAuthenticatedRegisterRequest var1) throws RemoteException;

    AbstractAuthenticatedDoResponse<K, V, M> handleAuthenticatedDo(AbstractAuthenticatedDoRequest<K, V, M> var1) throws RemoteException;
}
