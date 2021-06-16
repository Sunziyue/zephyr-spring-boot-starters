package xyz.szy.common.redis.utils;

import com.google.common.base.CaseFormat;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public abstract class KeyUtils {
    public KeyUtils() {
    }

    /**
     * user:count
     * @param entityClass user
     * @param <T> user.class
     * @return user:count
     */
    public static <T> String entityCount(Class<T> entityClass) {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, entityClass.getSimpleName()) + ":count";
    }

    /**
     * user:996600
     * @param entityClass user
     * @param id 996600L
     * @param <T> user.class
     * @return user:996600
     */
    public static <T> String entityId(Class<T> entityClass, long id) {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, entityClass.getSimpleName()) + ":" + id;
    }

    /**
     * user:hello
     * @param entityClass user
     * @param id hello
     * @param <T> user.class
     * @return user:hello
     */
    public static <T> String entityId(Class<T> entityClass, String id) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(id), "id can not be null");
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, entityClass.getSimpleName()) + ":" + id;
    }
}
