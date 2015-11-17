package me.wener.seq.util;

import com.google.common.base.*;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.reflect.Invokable;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="http://github.com/wenerme">wener</a>
 */
public class Options {
    private static final Splitter[] splitters = new Splitter[255];// Order is matter
    private final static Splitter COMMA_SPLITTER = splitter(',');
    private static final Joiner.MapJoiner JOINER = Joiner.on(',')
            .withKeyValueSeparator("=");

    private Options() {
    }

    static Splitter splitter(char c) {
        Splitter s;
        if (c < 255) {
            s = splitters[c];
            if (s == null) {
                synchronized (splitters) {
                    if (splitters[c] == null) {
                        splitters[c] = Splitter
                                .on(c)
                                .omitEmptyStrings()
                                .trimResults();
                    }
                }
            }
            s = splitters[c];
        } else {
            s = Splitter.on(c)
                    .omitEmptyStrings()
                    .trimResults();
        }
        return s;
    }

    public static Option create() {
        return new Opt();
    }

    public static Option immutable(Option o) {
        return new Opt(ImmutableMap.copyOf(o.options()));
    }

//    public static Option withPrefix(String prefix)
//    {
//        return null;
//    }

    /**
     * 解析字符串为选项
     *
     * @param opt 选项字符串
     */
    public static Option parse(String opt) {
        Option option = new Opt();
        if (opt == null) {
            return option;
        }
        for (String s : COMMA_SPLITTER.split(opt)) {
            int i = s.indexOf('=');
            if (i < 1)
                option.options().put(s.trim(), "true");
            else
                option.options().put(s.substring(0, i).trim(), s.substring(i + 1).trim());
        }
        return option;
    }

    public static Option from(Option o, String prefix) {
        return from(o.options(), prefix);
    }

    public static Option from(Map<String, String> o, String prefix) {
        return from(o, prefix, ".");
    }

    public static Option from(Map<String, String> o, String prefix, String delimiter) {
        Option opt = parse(o.get(prefix));
        for (Map.Entry<String, String> entry : o.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(prefix + delimiter) && key.length() > prefix.length()) {
                opt.options().put(key.substring(prefix.length() + delimiter.length()), entry.getValue());
            }
        }
        return opt;
    }

    public static Option from(Map<String, String> map) {
        return new Opt(map);
    }

    private static class Opt implements Option {
        final Map<String, String> options;

        public Opt(Map<String, String> options) {
            this.options = options;
        }

        public Opt() {
            options = Maps.newConcurrentMap();
        }


        /**
         * @param flag 标志名
         * @return 是否包含标志
         */
        public boolean contain(String flag) {
            String s = get(flag);
            return s != null && Boolean.parseBoolean(s);
        }

        /**
         * @param opt 选项名
         * @return 选项值
         */
        public String get(String opt) {
            return options.get(opt);
        }

        /**
         * @param opt 选项名
         * @param def 如果选项未定义,则返回该默认值
         * @return 选项值
         */
        public String get(String opt, String def) {
            return Strings.isNullOrEmpty(get(opt)) ? def : get(opt);
        }

        @SuppressWarnings("unchecked")
        public <T extends Serializable> T get(String opt, T def) {
            String s = get(opt);
            if (s == null) {
                return def;
            }
            return (T) get(opt, def.getClass());
        }

        @SuppressWarnings("unchecked")
        public <T extends Serializable> T get(String opt, Class<T> type) {
            String s = get(opt);
            if (s == null) {
                return null;
            }
            if (type == String.class) {
                return (T) s;
            }


            try {
                Invokable<?, Object> method = null;
                try {
                    // Integer, Long, Byte ....
                    method = Invokable.from(type.getMethod("decode", String.class));
                    if (!(method.isPublic() && method.isStatic())) method = null;
                } catch (NoSuchMethodException ignored) {
                }
                if (method == null) {
                    try {
                        // custom
                        method = Invokable.from(type.getMethod("fromString", String.class));
                        if (!(method.isPublic() && method.isStatic())) method = null;
                    } catch (NoSuchMethodException ignored) {
                    }
                }

                if (method == null) {
                    try {
                        method = Invokable.from(type.getMethod("valueOf", String.class));
                        if (!(method.isPublic() && method.isStatic())) method = null;
                    } catch (NoSuchMethodException ignored) {
                    }
                }

                if (method != null) {
                    return (T) method.invoke(null, s);
                }
            } catch (InvocationTargetException e) {
                Throwables.propagate(e.getCause());
            } catch (IllegalAccessException e) {
                Throwables.propagate(e);
            }

            throw new IllegalArgumentException("Con not convert to " + type);
        }

        @Override
        public List<String> list(String key, char del) {
            return splitter(del).splitToList(get(key, ""));
        }

        @Override
        public List<String> list(String key) {
            return list(key, '|');
        }


        /**
         * 设置选项
         *
         * @param opt 选项名
         * @param val 选项值
         */
        public Option set(String opt, String val) {
            if (val == null) {
                options.remove(opt);
            } else {
                options.put(opt, val);
            }
            return this;
        }

        public Option set(String opt, Serializable val) {
            set(opt, val.toString());
            return this;
        }

        public Option remove(String opt) {
            options.remove(opt);
            return this;
        }

        /**
         * 添加标记位
         *
         * @param flag 标记
         */
        public Option mark(String flag) {
            options.put(flag, "true");
            return this;
        }

        public Option load(Option opt, boolean override) {
            for (Map.Entry<String, String> entry : opt.options().entrySet()) {
                if (!options.containsKey(entry.getKey()) || override) {
                    options.put(entry.getKey(), entry.getValue());
                }
            }
            return this;
        }

        public Map<String, String> options() {
            return options;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Option)) return false;
            Option option = (Option) o;
            return Objects.equal(options, option.options());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(options);
        }

        @Override
        public String toString() {
            return Options.JOINER.join(options);
        }
    }
}
