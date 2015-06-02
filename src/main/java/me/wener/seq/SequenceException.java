package me.wener.seq;

/**
 * 序列异常
 */
public class SequenceException extends RuntimeException
{

    private final int code;

    public SequenceException(String message)
    {
        this(message, Exceptions.UNKNOWN);
    }

    public SequenceException(String message, int code)
    {
        super(message);
        this.code = code;
    }

    public SequenceException(String message, Throwable cause, int code)
    {
        super(message, cause);
        this.code = code;
    }

    public SequenceException(String message, Throwable cause)
    {
        super(message, cause);
        this.code = Exceptions.UNKNOWN;
    }

    public SequenceException(Throwable cause, int code)
    {
        super(cause);
        this.code = code;
    }
}
