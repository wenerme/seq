package me.wener.seq.internal;

import me.wener.seq.SequenceDeclare;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.VerboseMode;

/**
 * @author wener
 * @since 15/11/26
 */
@Fork(1)
@State(Scope.Benchmark)
public class BenchAtomicLong {
    private LongSupplier supplier;
    //
    @Param({"asc", "desc", "asc-round", "desc-round"})
    private String type;

    public static void main(String[] args) throws RunnerException {
        ChainedOptionsBuilder builder = new OptionsBuilder()
                .include(BenchAtomicLong.class.getSimpleName())
                .warmupIterations(5)
                .measurementIterations(5)
                .verbosity(VerboseMode.NORMAL)
                .forks(1);

        Benchmarks.threads(builder, 1, 2, 4, 6);
    }

    @Setup
    public void setup() {
        SequenceDeclare declare;
        switch (type) {
            case "asc":
                declare = SequenceDeclare.asc().name("asc").build();
                break;
            case "desc":
                declare = SequenceDeclare.desc().name("desc").build();
                break;
            case "asc-round":
                declare = SequenceDeclare.asc().min(1000).max(10000).cycle(true).name("asc-round").build();
                break;
            case "desc-round":
                declare = SequenceDeclare.desc().min(-10000).max(-1000).cycle(true).name("asc-round").build();
                break;
            default:
                throw new AssertionError();
        }
        supplier = Sequences.create(declare);
    }

    @Benchmark
    public void get(Blackhole bh) {
        bh.consume(supplier.getAsLong());
    }

}
/*
Example
1
Benchmark                (type)   Mode  Cnt         Score         Error  Units
BenchAtomicLong.get         asc  thrpt    5  82329579.432 ± 5702647.241  ops/s
BenchAtomicLong.get        desc  thrpt    5  78482784.971 ± 4165923.479  ops/s
BenchAtomicLong.get   asc-round  thrpt    5  41228321.509 ± 1429805.059  ops/s
BenchAtomicLong.get  desc-round  thrpt    5  36762235.310 ± 1368935.499  ops/s
2
Benchmark                (type)   Mode  Cnt         Score         Error  Units
BenchAtomicLong.get         asc  thrpt    5  28128029.630 ± 1321382.872  ops/s
BenchAtomicLong.get        desc  thrpt    5  36587482.688 ±  581587.604  ops/s
BenchAtomicLong.get   asc-round  thrpt    5  41752756.662 ± 1002780.720  ops/s
BenchAtomicLong.get  desc-round  thrpt    5  42097080.511 ± 1466928.970  ops/s
4
Benchmark                (type)   Mode  Cnt         Score         Error  Units
BenchAtomicLong.get         asc  thrpt    5  22457998.315 ± 1622487.421  ops/s
BenchAtomicLong.get        desc  thrpt    5  23036551.881 ± 1420357.351  ops/s
BenchAtomicLong.get   asc-round  thrpt    5  25304102.365 ± 1117676.421  ops/s
BenchAtomicLong.get  desc-round  thrpt    5  29491129.117 ±  461410.450  ops/s
6
Benchmark                (type)   Mode  Cnt         Score         Error  Units
BenchAtomicLong.get         asc  thrpt    5  24212668.024 ±  881800.083  ops/s
BenchAtomicLong.get        desc  thrpt    5  29294493.435 ± 2986667.134  ops/s
BenchAtomicLong.get   asc-round  thrpt    5  29508270.038 ±  501744.576  ops/s
BenchAtomicLong.get  desc-round  thrpt    5  28682792.647 ± 1173346.906  ops/s

No autoboxing speedup a lot!
For 1 thread(s)
Benchmark                (type)   Mode  Cnt          Score         Error  Units
BenchAtomicLong.get         asc  thrpt    5  101812318.142 ± 3441938.655  ops/s
BenchAtomicLong.get        desc  thrpt    5  103903874.590 ± 4445527.966  ops/s
BenchAtomicLong.get   asc-round  thrpt    5   50706958.076 ± 2237396.853  ops/s
BenchAtomicLong.get  desc-round  thrpt    5   94622124.937 ± 2143751.253  ops/s
For 2 thread(s)
Benchmark                (type)   Mode  Cnt         Score         Error  Units
BenchAtomicLong.get         asc  thrpt    5  49098160.922 ± 2092578.939  ops/s
BenchAtomicLong.get        desc  thrpt    5  48851055.468 ± 1168656.123  ops/s
BenchAtomicLong.get   asc-round  thrpt    5  42438096.482 ±  792136.380  ops/s
BenchAtomicLong.get  desc-round  thrpt    5  50669441.855 ± 2712342.480  ops/s
For 4 thread(s)
Benchmark                (type)   Mode  Cnt         Score         Error  Units
BenchAtomicLong.get         asc  thrpt    5  42300975.839 ± 1515016.089  ops/s
BenchAtomicLong.get        desc  thrpt    5  43257944.696 ± 3954482.460  ops/s
BenchAtomicLong.get   asc-round  thrpt    5  39954861.478 ± 1657218.781  ops/s
BenchAtomicLong.get  desc-round  thrpt    5  42737179.203 ± 2131977.419  ops/s
For 6 thread(s)
Benchmark                (type)   Mode  Cnt         Score         Error  Units
BenchAtomicLong.get         asc  thrpt    5  43223589.789 ± 2717634.138  ops/s
BenchAtomicLong.get        desc  thrpt    5  44891340.292 ± 6603524.356  ops/s
BenchAtomicLong.get   asc-round  thrpt    5  42229678.497 ±  938968.178  ops/s
BenchAtomicLong.get  desc-round  thrpt    5  42696368.278 ±  467949.029  ops/s

 */
