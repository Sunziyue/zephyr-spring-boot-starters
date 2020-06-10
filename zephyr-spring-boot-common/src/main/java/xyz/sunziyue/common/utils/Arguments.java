package xyz.sunziyue.common.utils;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.util.Collection;

public final class Arguments {
    public Arguments() {
    }

    public static <T extends Iterable> boolean isNullOrEmpty(T t) {
        return isNull(t) || isEmpty(t);
    }

    public static boolean isNull(Object o) {
        return o == null;
    }

    public static boolean notNull(Object o) {
        return o != null;
    }

    public static boolean isEmpty(String s) {
        return Strings.isNullOrEmpty(s);
    }

    public static <T extends Iterable> boolean isEmpty(T t) {
        if (t instanceof Collection) {
            return ((Collection)t).isEmpty();
        } else {
            return !t.iterator().hasNext();
        }
    }

    public static boolean notEmpty(String s) {
        return !isEmpty(s);
    }

    public static <T extends Iterable> boolean notEmpty(T l) {
        if (isNull(l)) {
            return false;
        } else if (l instanceof Collection) {
            return !((Collection)l).isEmpty();
        } else {
            return l.iterator().hasNext();
        }
    }

    public static boolean positive(Number n) {
        return n.doubleValue() > 0.0D;
    }

    public static boolean isPositive(Number n) {
        return n != null && n.doubleValue() > 0.0D;
    }

    public static boolean negative(Number n) {
        return n.doubleValue() < 0.0D;
    }

    public static boolean isNegative(Number n) {
        return n != null && n.doubleValue() < 0.0D;
    }

    public static <T> boolean equalWith(T source, T target) {
        return Objects.equal(source, target);
    }

    public static boolean not(Boolean t) {
        Preconditions.checkArgument(notNull(t));
        return !t;
    }

    public static boolean isDecimal(String str) {
        char[] chars = str.toCharArray();
        for(int i = 0; i < chars.length; ++i) {
            char c = chars[i];
            if (c < '0' || c > '9') {
                return false;
            }
        }

        return true;
    }

    public static boolean isNumberic(String numStr) {
        boolean flag = false;
        char[] numCharArr = numStr.toCharArray();
        for(int i = 0; i < numCharArr.length; ++i) {
            char num = numCharArr[i];
            if (num == '.' && !flag) {
                flag = true;
            } else {
                if (num == '.') {
                    return false;
                }
                if (num < '0' || num > '9') {
                    return false;
                }
            }
        }
        return true;
    }
}
