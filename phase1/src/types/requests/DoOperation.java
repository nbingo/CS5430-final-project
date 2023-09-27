//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package types.requests;

import java.io.Serializable;

public class DoOperation<K extends Serializable, V extends Serializable, M extends Serializable> implements Serializable {
    public K key;
    public V val;
    public M metaVal;
    public Operation operation;

    public DoOperation(K var1, V var2, M var3, Operation var4) {
        this.key = var1;
        this.val = var2;
        this.metaVal = var3;
        this.operation = var4;
    }

    public static enum Operation {
        CREATE,
        DELETE,
        READVAL,
        READMETAVAL,
        WRITEVAL,
        WRITEMETAVAL;

        private Operation() {
        }
    }
}
