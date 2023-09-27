//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package phase1.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Base64;

public abstract class Phase1ServerBase<K extends Serializable, V extends Serializable, M extends Serializable> implements Phase1Server<K, V, M> {
    public final byte[] signingKey;
    public final byte[] verificationKey;

    public Phase1ServerBase() throws IOException {
        String var1 = "serverSigningKey";
        String var2 = "serverVerificationKey";
        ClassLoader var3 = this.getClass().getClassLoader();
        InputStream var4 = var3.getResourceAsStream(var1);
        InputStream var5 = var3.getResourceAsStream(var2);
        byte[] var6 = new byte[var4.available()];
        byte[] var7 = new byte[var5.available()];
        var4.read(var6);
        var5.read(var7);
        var4.close();
        var5.close();
        this.signingKey = Base64.getDecoder().decode(var6);
        this.verificationKey = Base64.getDecoder().decode(var7);
    }

    public void registerPhase1() throws ClassCastException, RemoteException {
        String var1 = "Phase1Server";
        Phase1Server var2 = (Phase1Server)UnicastRemoteObject.exportObject(this, 0);
        Registry var3 = LocateRegistry.getRegistry();
        var3.rebind(var1, var2);
    }
}
