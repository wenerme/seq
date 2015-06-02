package me.wener.seq.internal;

import static me.wener.seq.internal.Sequences.create;
import static me.wener.seq.internal.Tests.expected;

import org.junit.Test;

/**
 * @author <a href="http://github.com/wenerme">wener</a>
 */
public class SequenceLogicalTest
{
    @Test
    public void testUnboundAsc()
    {
        expected(create(1, Long.MAX_VALUE, 2, 3, false), 1, 3, 5, 7, 9, 11, 13, 15, 17, 19);
        expected(create(10, Long.MAX_VALUE, 2, 3, false), 10, 12, 14, 16, 18, 20);
        expected(create(10, Long.MAX_VALUE, 20, 3, false), 10, 30, 50, 70);
    }

    @Test
    public void testUnboundDesc()
    {
        expected(create(Long.MIN_VALUE, -1, -2, 10, false), -1, -3, -5, -7, -9, -11, -13, -15, -17, -19, -21);
        expected(create(Long.MIN_VALUE, -10, -2, 10, false), -10, -12, -14, -16, -18, -20, -22);
        expected(create(Long.MIN_VALUE, -10, -20, 10, false), -10, -30, -50, -70, -90);
    }

    @Test
    public void testAscOne()
    {
        expected(create(10, 16, 1, 2, true), 10, 11, 12, 13, 14, 15, 16, 10, 11, 12, 13, 14, 15, 16);
        expected(create(-1, 2, 1, 1, true), -1, 0, 1, 2, -1, 0, 1, 2, -1, 0, 1, 2);
        expected(create(-1, 2, 1, 3, true), -1, 0, 1, 2, -1, 0, 1, 2, -1, 0, 1, 2);
    }

    @Test
    public void testAscN()
    {
        expected(create(10, 16, 2, 2, true), 10, 12, 14, 16, 10, 12, 14, 16);
        expected(create(-1, 10, 4, 1, true), -1, 3, 7, -1, 3, 7);
        expected(create(-1, 7, 5, 3, true), -1, 4, -1, 4);
    }

    @Test
    public void testDescOne()
    {
        expected(create(-2, 1, -1, 2, true), 1, 0, -1, -2, 1, 0, -1, -2, 1, 0, -1, -2, 1, 0, -1, -2);
        expected(create(-3, -1, -1, 3, true), -1, -2, -3, -1, -2, -3, -1, -2, -3, -1, -2, -3);
    }

    @Test
    public void testDescN()
    {
        expected(create(10, 16, -2, 2, true), 16, 14, 12, 10, 16, 14, 12, 10);
        expected(create(-1, 10, -4, 1, true), 10, 6, 2, 10, 6, 2);
        expected(create(-1, 7, -5, 3, true), 7, 2, 7, 2);
        expected(create(-10, -2, -3, 2, true), -2, -5, -8, -2, -5, -8, -2, -5, -8);
        expected(create(-7, -1, -4, 3, true), -1, -5, -1, -5, -1, -5, -1, -5);
    }
}
