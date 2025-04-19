package io.github.programmerchenyu.beans.exception;

/**
 * @author 爱吃小鱼的橙子
 */
public class BeanCreationException extends RuntimeException {
    public BeanCreationException(String message) {
        super(message);
    }

    public BeanCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
