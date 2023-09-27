//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package phase1.stub;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Base64;
import network.Network;

public abstract class Phase1StubBase<K extends Serializable, V extends Serializable, M extends Serializable> implements Phase1Stub<K, V, M> {
    public Network<K, V, M> network;
    public final byte[] serverVerificationKey;

    public Phase1StubBase() throws IOException, NotBoundException {
        String var1 = "Network";
        Registry var2 = LocateRegistry.getRegistry();
        this.network = (Network)var2.lookup(var1);
        String var3 = "serverVerificationKey";
        ClassLoader var4 = this.getClass().getClassLoader();
        InputStream var5 = var4.getResourceAsStream(var3);
        byte[] var6 = new byte[var5.available()];
        var5.read(var6);
        var5.close();
        this.serverVerificationKey = Base64.getDecoder().decode(var6);
    }

    public void registerStub() throws ClassCastException, RemoteException {
        String var1 = "Phase1Stub";
        Phase1Stub var2 = (Phase1Stub)UnicastRemoteObject.exportObject(this, 0);
        Registry var3 = LocateRegistry.getRegistry();
        var3.rebind(var1, var2);
    }
}
