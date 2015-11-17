package me.wener.seq;

import javax.annotation.concurrent.ThreadSafe;

/**
 * 主要的序列操作接口
 *
 * @author <a href="http://github.com/wenerme">wener</a>
 */
@ThreadSafe
public interface Sequence extends SequenceMeta {
    /**
     * @return 返回上一次调用 {@link #getNextValue()} 返回的值, 不保证返回的值为确切的当前值, 只保证 Happen-Before
     */
    long getValue();

    /**
     * @return 下一个序列值
     */
    long getNextValue();

    /**
     * @param count 数量
     * @return 接下来的 {@code count} 个序列值,不保证返回的值是相邻的序列
     */
    long[] getNextValue(int count);
}
