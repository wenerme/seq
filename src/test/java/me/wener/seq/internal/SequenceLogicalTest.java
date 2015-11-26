package me.wener.seq.internal;

import me.wener.seq.SequenceDeclare;
import me.wener.seq.SequenceException;
import org.junit.Test;

import static me.wener.seq.internal.Sequences.create;
import static me.wener.seq.internal.Tests.expected;

/**
 * @author <a href="http://github.com/wenerme">wener</a>
 */
public class SequenceLogicalTest {
    @Test
    public void testUnboundAsc() {
        expected(create(SequenceDeclare.asc().increment(2).build()), 1, 3, 5, 7, 9, 11, 13, 15, 17, 19);
        expected(create(SequenceDeclare.asc().increment(2).min(10).build()), 10, 12, 14, 16, 18, 20);
        expected(create(SequenceDeclare.asc().increment(20).min(10).build()), 10, 30, 50, 70);
    }

    @Test
    public void testUnboundDesc() {
        expected(create(SequenceDeclare.desc().build()), -1, -2, -3, -4, -5, -6, -7);
        expected(create(SequenceDeclare.desc().increment(-2).build()), -1, -3, -5, -7, -9, -11, -13, -15, -17, -19, -21);
        expected(create(SequenceDeclare.desc().max(-10).increment(-2).build()), -10, -12, -14, -16, -18, -20, -22);
        expected(create(SequenceDeclare.desc().max(-10).increment(-20).build()), -10, -30, -50, -70, -90);
    }

    @Test
    public void testCycle() {
        expected(create(SequenceDeclare.asc().cycle(true).max(10).increment(3).min(2).build()), 2, 5, 8, 2, 5, 8);
    }

    @Test(expected = SequenceException.class)
    public void testNotEnoughAsc() {
        expected(create(SequenceDeclare.asc().cycle(false).max(10).increment(3).min(2).build()), 2, 5, 8, 2, 5, 8);
    }

    @Test(expected = SequenceException.class)
    public void testNotEnoughDesc() {
        expected(create(SequenceDeclare.asc().cycle(false).max(-3).increment(-3).min(-9).build()), -3, -6, -9, -3, -6, -9);
    }

    @Test
    public void testAscOne() {
        expected(create(10, 16, 1, 1, true), 10, 11, 12, 13, 14, 15, 16, 10, 11, 12, 13, 14, 15, 16);
        expected(create(-1, 2, 1, 1, true), -1, 0, 1, 2, -1, 0, 1, 2, -1, 0, 1, 2);
        expected(create(-1, 2, 1, 1, true), -1, 0, 1, 2, -1, 0, 1, 2, -1, 0, 1, 2);
    }

    @Test
    public void testAscN() {
        expected(create(10, 16, 2, 1, true), 10, 12, 14, 16, 10, 12, 14, 16);
        expected(create(-1, 10, 4, 1, true), -1, 3, 7, -1, 3, 7);
        expected(create(-1, 7, 5, 1, true), -1, 4, -1, 4);
    }

    @Test
    public void testDescOne() {
        expected(create(-2, 1, -1, 1, true), 1, 0, -1, -2, 1, 0, -1, -2, 1, 0, -1, -2, 1, 0, -1, -2);
        expected(create(-3, -1, -1, 1, true), -1, -2, -3, -1, -2, -3, -1, -2, -3, -1, -2, -3);
    }

    @Test
    public void testDescN() {
        expected(create(10, 16, -2, 1, true), 16, 14, 12, 10, 16, 14, 12, 10);
        expected(create(-1, 10, -4, 1, true), 10, 6, 2, 10, 6, 2);
        expected(create(-1, 7, -5, 1, true), 7, 2, 7, 2);
        expected(create(-10, -2, -3, 1, true), -2, -5, -8, -2, -5, -8, -2, -5, -8);
        expected(create(-7, -1, -4, 1, true), -1, -5, -1, -5, -1, -5, -1, -5);
    }
}
