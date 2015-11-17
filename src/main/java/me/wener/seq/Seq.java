package me.wener.seq;

import java.util.Map;

/**
 * @author wener
 * @since 15/11/17
 */
public interface Seq {
    void create(String name, Map<String, String> attributes);

    void drop(String name);
}
