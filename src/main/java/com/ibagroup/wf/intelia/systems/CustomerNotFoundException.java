package com.ibagroup.wf.intelia.systems;

/**
 * Exception occurs when customer in the target system can not be found.
 */
public class CustomerNotFoundException extends RuntimeException {
    
    private static final long serialVersionUID = 7579555993883146650L;

    // Parameterless Constructor
    public CustomerNotFoundException() {}

    // Constructor that accepts a message
    public CustomerNotFoundException(String message) {
        super(message);
    }
}
