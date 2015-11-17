package me.wener.seq.util;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 选项信息类
 */
public interface Option {

    /**
     * @param flag 标志名
     * @return 是否包含标志
     */
    boolean contain(String flag);

    /**
     * @param opt 选项名
     * @return 选项值
     */
    String get(String opt);

    /**
     * @param opt 选项名
     * @param def 如果选项未定义,则返回该默认值
     * @return 选项值
     */
    String get(String opt, String def);

    <T extends Serializable> T get(String opt, T def);

    <T extends Serializable> T get(String opt, Class<T> type);

    List<String> list(String key, char del);

    List<String> list(String key);

    /**
     * 设置选项
     *
     * @param opt 选项名
     * @param val 选项值
     */
    Option set(String opt, String val);

    Option set(String opt, Serializable val);

    Option remove(String opt);

    /**
     * 添加标记位
     *
     * @param flag 标记
     */
    Option mark(String flag);

    Option load(Option opt, boolean override);

    Map<String, String> options();

}
