package com.ibagroup.wf.intelia.systems;

/**
 * Exception occurs when robot can't login to application (NDS/CACS)
 * due to different reason (password expired, user locked etc.).
 */
public class InvalidLoginException extends RuntimeException {

    private static final long serialVersionUID = 5589007388859554476L;

    // Parameterless Constructor
    public InvalidLoginException() {}

    // Constructor that accepts a message
    public InvalidLoginException(String message) {
        super(message);
    }
}
