//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package phase1.server;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import types.requests.AbstractAuthenticatedDoRequest;
import types.requests.AbstractAuthenticatedRegisterRequest;
import types.responses.AbstractAuthenticatedDoResponse;
import types.responses.AbstractAuthenticatedRegisterResponse;

public interface Phase1Server<K extends Serializable, V extends Serializable, M extends Serializable> extends Remote {
    AbstractAuthenticatedRegisterResponse authenticatedRegister(AbstractAuthenticatedRegisterRequest var1) throws IllegalArgumentException, RemoteException;

    AbstractAuthenticatedDoResponse<K, V, M> authenticatedDo(AbstractAuthenticatedDoRequest<K, V, M> var1) throws IllegalArgumentException, RemoteException;
}
