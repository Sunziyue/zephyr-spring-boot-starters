package xyz.szy.common.utils;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Map.Entry;

public class Params {

    public static Map<String, Object> filterNullOrEmpty(Map<String, Object> criteria) {
        return Maps.filterEntries(criteria, new Predicate<Entry<String, Object>>() {
            public boolean apply(Entry<String, Object> entry) {
                Object v = entry.getValue();
                if (v instanceof String) {
                    return !Strings.isNullOrEmpty((String)v);
                } else {
                    return v != null;
                }
            }
        });
    }

    public static String trimToNull(String str) {
        return str != null ? Strings.emptyToNull(str.replace(' ', ' ').trim()) : null;
    }

    public static String trimToNull(Object obj) {
        return obj != null ? trimToNull(obj.toString()) : null;
    }
}
