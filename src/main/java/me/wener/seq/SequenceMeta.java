package me.wener.seq;

/**
 * 序列的元数据信息
 *
 * @see <a href=http://docs.oracle.com/cd/B28359_01/server.111/b28286/statements_6015.htm#SQLRF01314>Oracle CREATE SEQUENCE</a>
 */
public interface SequenceMeta {
    String getName();

    /**
     * Specify the maximum value the sequence can generate. This integer
     * value can have 28 or fewer digits. MAXVALUE must be equal to or
     * greater than START WITH and must be greater than MINVALUE.
     */
    long getMaxValue();

    /**
     * Specify the minimum value of the sequence. This integer value can have
     * 28 or fewer digits. MINVALUE must be less than or equal to START WITH
     * and must be less than MAXVALUE.
     */
    long getMinValue();

    /**
     * Specify the interval between sequence numbers. This integer value
     * can be any positive or negative integer, but it cannot be 0.
     * This value can have 28 or fewer digits. The absolute of this value
     * must be less than the difference of MAXVALUE and MINVALUE. If this
     * value is negative, then the sequence descends. If the value is
     * positive, then the sequence ascends. If you omit this clause, then
     * the interval defaults to 1.
     */
    long getIncrement();

    /**
     * Specify how many values of the sequence the database preallocates
     * and keeps in memory for faster access. This integer value can have
     * 28 or fewer digits. The minimum value for this parameter is 2. For
     * sequences that cycle, this value must be less than the number of
     * values in the cycle. You cannot cache more values than will fit in
     * a given cycle of sequence numbers. Therefore, the maximum value allowed
     * for CACHE must be less than the value determined by the following
     * formula:
     * <p>
     * <code>(CEIL (MAXVALUE - MINVALUE)) / ABS (INCREMENT)</code>
     * <p>
     * If a system failure occurs, then all cached sequence values that
     * have not been used in committed DML statements are lost. The potential
     * number of lost values is equal to the value of the CACHE parameter.
     * <p>
     * <b>注意</b>该值与具体实现相关
     */
    long getCache();

    /**
     * Specify CYCLE to indicate that the sequence continues to generate values
     * after reaching either its maximum or minimum value. After an ascending
     * sequence reaches its maximum value, it generates its minimum value.
     * After a descending sequence reaches its minimum, it generates its maximum
     * value.
     * <p>
     * <b>注意</b>该值与具体实现相关
     */
    boolean isCycle();

    /**
     * Specify ORDER to guarantee that sequence numbers are generated in order
     * of request. This clause is useful if you are using the sequence numbers
     * as timestamps. Guaranteeing order is usually not important for sequences
     * used to generate primary keys.
     * <p>
     * ORDER is necessary only to guarantee ordered generation if you are using
     * Oracle Real Application Clusters. If you are using exclusive mode, then
     * sequence numbers are always generated in order.
     * <p>
     * <b>注意</b>该值与具体实现相关
     */
    boolean isOrder();

    /**
     * @return 递增序列
     */
    boolean isAscending();

    /**
     * @return 递减序列
     */
    boolean isDescending();

    /**
     * @return 序列是否有限的
     */
    boolean isUnBounded();
}
