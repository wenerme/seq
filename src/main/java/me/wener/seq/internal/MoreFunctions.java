package me.wener.seq.internal;

import com.google.common.base.Function;
import com.google.common.base.Functions;

public class MoreFunctions
{
    public static <A, B, C, D, E, F, G> Function<A, G> compose(Function<A, ? extends B> a, Function<B, ? extends C> b, Function<C, ? extends D> c, Function<D, ? extends E> d, Function<E, ? extends F> e, Function<F, G> f)
    {
        return compose(compose(compose(compose(compose(a, b), c), d), e), f);
    }

    public static <A, B, C, D, E, F> Function<A, F> compose(Function<A, ? extends B> a, Function<B, ? extends C> b, Function<C, ? extends D> c, Function<D, ? extends E> d, Function<E, F> e)
    {
        return compose(compose(compose(compose(a, b), c), d), e);
    }

    public static <A, B, C, D, E> Function<A, E> compose(Function<A, ? extends B> a, Function<B, ? extends C> b, Function<C, ? extends D> c, Function<D, E> d)
    {
        return compose(compose(compose(a, b), c), d);
    }

    public static <A, B, C, D> Function<A, D> compose(Function<A, ? extends B> a, Function<B, ? extends C> b, Function<C, D> c)
    {
        return compose(compose(a, b), c);
    }

    public static <A, B, C> Function<A, C> compose(Function<A, ? extends B> a, Function<B, C> b)
    {
        return Functions.compose(b, a);
    }
}
