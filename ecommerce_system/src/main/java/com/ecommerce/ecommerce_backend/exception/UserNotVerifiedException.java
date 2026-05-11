package com.ecommerce.ecommerce_backend.exception;

public class UserNotVerifiedException extends Exception{

    private boolean NewEmailSend;

    public UserNotVerifiedException(boolean NewEmailSend) {
        this.NewEmailSend = NewEmailSend;
    }

    public boolean isNewEmailSend() {
        return NewEmailSend;
    }
}
