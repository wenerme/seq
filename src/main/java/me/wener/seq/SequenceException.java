package me.wener.seq;

/**
 * 序列异常
 */
public class SequenceException extends RuntimeException
{
    public SequenceException()
    {
    }

    public SequenceException(String message)
    {
        super(message);
    }

    public SequenceException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public SequenceException(Throwable cause)
    {
        super(cause);
    }
}
