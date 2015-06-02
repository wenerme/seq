package me.wener.seq.internal;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import me.wener.seq.SequenceException;

/**
 * @author <a href="http://github.com/wenerme">wener</a>
 */
public class Maths
{
    private Maths() {}

    /**
     * <b>无边界检查</b>
     */
    public static long round(long v, long max, long min, long inc, long cache, boolean cycle)
    {
        long r = min + v * cache;
        if (inc > 0)
        {
            // 递增序列
            r += inc;
            // 检测上界
            if (r > max)
            {
                if (cycle)
                {
                    r %= max;
                } else
                {
                    throw new SequenceException("No more value, exceed the max value");
                }
            }
        } else if (inc < 0)
        {
            // 递减序列
            r += inc;
            if (r < min)
            {
                if (cycle)
                {
                    r %= min;
                } else
                {
                    throw new SequenceException("No more value, exceed the min value");
                }
            }
        } else
        {
            throw new IllegalArgumentException("Inc must not equals 0");
        }
        return r;
    }

    public static long next(long v, long max, long min, long inc, long cache, boolean cycle, Supplier<Integer> next)
    {
        long r = v;
        if (inc > 0)
        {
            // 递增序列
            r += inc;
            // 检测上界
            if (r > max)
            {
                if (cycle)
                {
                    r %= max;
                } else
                {
                    throw new SequenceException("No more value, exceed the max value");
                }
            }
        } else if (inc < 0)
        {
            // 递减序列
            r += inc;
            if (r < min)
            {
                if (cycle)
                {
                    r %= min;
                } else
                {
                    throw new SequenceException("No more value, exceed the min value");
                }
            }
        } else
        {
            throw new IllegalArgumentException("Inc must not equals 0");
        }
        return r;
    }

    public static void main(String[] args)
    {
//        for (int i = 0; i < 10; i++)
//        {
//            System.out.println(round(i, 15, 10, 1, 2, true));
//        }
        long v = 10 + 0 * 2;
        for (int i = 0; i < 10; i++)
        {
            v = next(v, 15, 10, 1, 2, true, Suppliers.ofInstance(1));
            System.out.println(v);
        }

    }
}
