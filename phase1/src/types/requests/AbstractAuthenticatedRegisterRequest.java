//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package types.requests;

import java.io.Serializable;

public abstract class AbstractAuthenticatedRegisterRequest implements Serializable {
    public String userId;
    public byte[] verificationKey;
    public byte[] digitalSignature;

    public AbstractAuthenticatedRegisterRequest(String var1, byte[] var2, byte[] var3) {
        this.userId = var1;
        this.verificationKey = var2;
        this.digitalSignature = var3;
    }
}
