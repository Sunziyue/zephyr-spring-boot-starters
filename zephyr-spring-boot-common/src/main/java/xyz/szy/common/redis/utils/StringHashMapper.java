package xyz.szy.common.redis.utils;

import com.fasterxml.jackson.databind.JavaType;
import xyz.szy.common.utils.Arguments;
import xyz.szy.common.utils.JsonMapper;

import java.util.HashMap;
import java.util.Map;

public class StringHashMapper<T> {
    private final JsonMapper mapper = JsonMapper.nonDefaultMapper();
    private final JavaType userType;
    private final JavaType mapType;

    /**
     * @param type 对象的类型
     */
    public StringHashMapper(Class<T> type) {
        this.mapType = this.mapper.createCollectionType(HashMap.class, String.class, String.class);
        this.userType = this.mapper.getMapper().getTypeFactory().constructType(type);
    }

    /**
     * hash 转换成对象
     * @param hash hash集合
     * @return 对象
     */
    public T fromHash(Map<String, String> hash) {
        return hash.isEmpty() ? null : this.mapper.getMapper().convertValue(hash, this.userType);
    }

    /**
     * 对象转换成hash并去除空属性
     * @param object 对象
     * @return hash
     */
    public Map<String, String> toHash(T object) {
        Map<String, String> hash = this.mapper.getMapper().convertValue(object, this.mapType);
        hash.forEach((key,val)->{
            if (Arguments.isEmpty(val)) {
                hash.remove(key);
            }
        });
        return hash;
    }
}
