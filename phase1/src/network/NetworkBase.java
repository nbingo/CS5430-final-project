//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package network;

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import phase1.server.Phase1Server;

public abstract class NetworkBase<K extends Serializable, V extends Serializable, M extends Serializable> implements Network<K, V, M> {
    public Phase1Server<K, V, M> phase1Server;

    public NetworkBase() throws NotBoundException, RemoteException {
        String var1 = "Phase1Server";
        Registry var2 = LocateRegistry.getRegistry();
        this.phase1Server = (Phase1Server)var2.lookup(var1);
    }

    public void registerNetwork() throws ClassCastException, RemoteException {
        String var1 = "Network";
        Network var2 = (Network)UnicastRemoteObject.exportObject(this, 0);
        Registry var3 = LocateRegistry.getRegistry();
        var3.rebind(var1, var2);
    }
}
