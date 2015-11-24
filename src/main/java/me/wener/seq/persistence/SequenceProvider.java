package me.wener.seq.persistence;

import com.google.common.base.Optional;

import java.util.Set;

/**
 * @author wener
 * @since 15/11/21
 */
public interface SequenceProvider {
    Set<String> getDeclared();

    Optional<String> load(String name);

    boolean storeIfAbsent(String name, String value);

    long next(String name);

    long reset(String name, long reset);

    long current(String name);
}
