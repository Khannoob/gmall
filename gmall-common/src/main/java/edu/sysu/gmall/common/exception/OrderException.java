package edu.sysu.gmall.common.exception;

/**
 * @program: gmall
 * @author: Khan
 * @create: 2021-06-05 21:38
 */
public class OrderException extends RuntimeException {
    public OrderException() {
        super();
    }

    public OrderException(String message) {
        super(message);
    }
}
