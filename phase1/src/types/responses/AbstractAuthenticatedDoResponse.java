//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package types.responses;

import java.io.Serializable;

public abstract class AbstractAuthenticatedDoResponse<K extends Serializable, V extends Serializable, M extends Serializable> implements Serializable {
    public DoOperationOutcome<K, V, M> outcome;
    public byte[] digitalSignature;

    public AbstractAuthenticatedDoResponse(DoOperationOutcome<K, V, M> var1, byte[] var2) {
        this.outcome = var1;
        this.digitalSignature = var2;
    }
}
