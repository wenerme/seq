package me.wener.seq;

/**
 * @author <a href="http://github.com/wenerme">wener</a>
 */
public class Exceptions {
    public static final int UNKNOWN = 0;
    public static final int NOT_FOUND = 1;
    public static final int ALREADY_EXISTS = 2;
    public static final int EXHAUSTED = 3;
    public static final int BAD_CREATE = 4;

    private Exceptions() {
    }


    public static SequenceException create(int code, String format, Object... args) {
        return new SequenceException(String.format(format, args), code);
    }

    public static SequenceException create(Throwable e, String format, Object... args) {
        return new SequenceException(String.format(format, args), e);
    }
}
