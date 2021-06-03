package edu.sysu.gmall.cart.exception;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-06-03 10:06
 */
public class CartException extends RuntimeException {
    public CartException() {
        super();
    }

    public CartException(String message) {
        super(message);
    }

    public CartException(String message, Throwable cause) {
        super(message, cause);
    }
}
