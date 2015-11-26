package me.wener.seq.internal;

import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.results.format.ResultFormat;
import org.openjdk.jmh.results.format.ResultFormatFactory;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Map;

/**
 * @author wener
 * @since 15/11/25
 */
public class Benchmarks {

    private static PrintStream out = System.out;
    private static ResultFormat format = ResultFormatFactory.getInstance(ResultFormatType.TEXT, out);

    public static void output(PrintStream o) {
        if (o == null) {
            out = new PrintStream(ByteStreams.nullOutputStream());
        } else {
            out = o;
        }
        format = ResultFormatFactory.getInstance(ResultFormatType.TEXT, out);
    }

    public static Map<Integer, Collection<RunResult>> threads(ChainedOptionsBuilder builder, Integer... threads) throws RunnerException {
        Map<Integer, Collection<RunResult>> map = Maps.newHashMap();
        for (Integer thread : threads) {
            Collection<RunResult> results = new Runner(builder.threads(thread).build()).run();
            out.printf("For %s thread(s)\n", thread);
            format.writeOut(results);
            map.put(thread, results);
        }
        out.println();
        for (Map.Entry<Integer, Collection<RunResult>> entry : map.entrySet()) {
            out.printf("For %s thread(s)\n", entry.getKey());
            format.writeOut(entry.getValue());
        }
        return map;
    }
}
