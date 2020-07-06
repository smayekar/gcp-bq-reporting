package io.ctl.cloudintegration.gcp.exception;

import com.google.common.base.MoreObjects;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class FaultException extends Exception {
    final String message;
    final int code;

    public FaultException(final String message, final int code) {
        this.message = message;
        this.code = code;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("message", message)
                .add("code", code)
                .toString();
    }
}
