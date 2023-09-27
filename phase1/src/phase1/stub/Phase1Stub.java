//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package phase1.stub;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.NoSuchElementException;

public interface Phase1Stub<K extends Serializable, V extends Serializable, M extends Serializable> extends Remote {
    Boolean registerUser(String var1) throws RemoteException;

    Boolean create(String var1, K var2, V var3, M var4) throws RemoteException;

    Boolean writeVal(String var1, K var2, V var3) throws IllegalArgumentException, RemoteException;

    Boolean writeMetaVal(String var1, K var2, M var3) throws IllegalArgumentException, RemoteException;

    V readVal(String var1, K var2) throws NoSuchElementException, RemoteException;

    M readMetaVal(String var1, K var2) throws NoSuchElementException, RemoteException;

    Boolean delete(String var1, K var2) throws RemoteException;
}
