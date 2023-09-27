//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package types.responses;

import java.io.Serializable;

public abstract class AbstractAuthenticatedRegisterResponse implements Serializable {
    public Status status;
    public byte[] digitalSignature;

    public AbstractAuthenticatedRegisterResponse(Status var1, byte[] var2) {
        this.status = var1;
        this.digitalSignature = var2;
    }

    public static enum Status {
        OK,
        UserAlreadyExists,
        AuthenticationFailure;

        private Status() {
        }
    }
}
