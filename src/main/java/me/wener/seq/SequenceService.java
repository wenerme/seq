package me.wener.seq;

import javax.annotation.concurrent.ThreadSafe;

/**
 * @author <a href="http://github.com/wenerme">wener</a>
 */
@ThreadSafe
public interface SequenceService
{
    /**
     * @return 序列的当前值, 不保证返回的值为确切的当前值, 只保证 Happen-Before
     */
    long getValue(String seq);

    /**
     * @return 下一个序列值
     */
    long getNextValue(String seq);

    /**
     * @param count 数量
     * @return 接下来的 {@code count} 个序列值,不保证返回的值是相邻的序列
     */
    long[] getNextValue(String seq, int count);
}
