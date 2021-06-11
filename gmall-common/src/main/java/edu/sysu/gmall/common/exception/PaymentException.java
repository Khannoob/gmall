package edu.sysu.gmall.common.exception;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-06-08 13:57
 */
public class PaymentException extends RuntimeException {
    public PaymentException() {
        super();
    }

    public PaymentException(String message) {
        super(message);
    }
}
