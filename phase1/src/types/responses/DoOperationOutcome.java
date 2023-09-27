//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package types.responses;

import java.io.Serializable;

public class DoOperationOutcome<K extends Serializable, V extends Serializable, M extends Serializable> implements Serializable {
    public K key;
    public V val;
    public M metaVal;
    public Outcome outcome;

    public DoOperationOutcome(K var1, V var2, M var3, Outcome var4) {
        this.key = var1;
        this.val = var2;
        this.metaVal = var3;
        this.outcome = var4;
    }

    public static enum Outcome {
        AUTHENTICATION_FAILURE,
        SUCCESS,
        NOSUCHELEMENT,
        ILLEGALARGUMENT;

        private Outcome() {
        }
    }
}
