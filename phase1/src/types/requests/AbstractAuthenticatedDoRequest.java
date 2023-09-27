//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package types.requests;

import java.io.Serializable;

public abstract class AbstractAuthenticatedDoRequest<K extends Serializable, V extends Serializable, M extends Serializable> implements Serializable {
    public String userId;
    public DoOperation<K, V, M> doOperation;
    public byte[] digitalSignature;

    public AbstractAuthenticatedDoRequest(String var1, DoOperation<K, V, M> var2, byte[] var3) {
        this.userId = var1;
        this.doOperation = var2;
        this.digitalSignature = var3;
    }
}
