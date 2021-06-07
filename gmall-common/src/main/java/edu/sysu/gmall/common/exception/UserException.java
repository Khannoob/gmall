package edu.sysu.gmall.common.exception;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-06-01 15:25
 */

public class UserException extends RuntimeException {

    public UserException() {
        super();
    }

    public UserException(String message) {
        super(message);
    }
}
