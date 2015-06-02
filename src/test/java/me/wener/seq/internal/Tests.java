package me.wener.seq.internal;

import static org.junit.Assert.assertArrayEquals;

import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import java.util.Arrays;

/**
 * @author <a href="http://github.com/wenerme">wener</a>
 */
public class Tests
{
    public static void expected(Supplier<Long> src, long... numbers)
    {
        long[] actuals = new long[numbers.length];
        for (int i = 0; i < actuals.length; i++)
        {
            actuals[i] = src.get();
        }
        try
        {
            assertArrayEquals(numbers, actuals);
        } catch (Throwable e)
        {
            System.out.printf("Failed %s <> %s\n", Arrays.toString(actuals), Arrays.toString(numbers));
            Throwables.propagate(e);
        }
    }
}
