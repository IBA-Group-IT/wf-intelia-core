package com.ibagroup.wf.intelia.systems;

/**
 * Exception occurs when account in the target system can not be found.
 */
public class AccountNotFoundException extends RuntimeException {
    
    private static final long serialVersionUID = 7657926830381749849L;

    // Parameterless Constructor
    public AccountNotFoundException() {}

    // Constructor that accepts a message
    public AccountNotFoundException(String message) {
        super(message);
    }
}
