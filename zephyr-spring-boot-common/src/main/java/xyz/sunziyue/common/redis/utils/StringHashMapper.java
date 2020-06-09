package xyz.sunziyue.common.redis.utils;

import com.fasterxml.jackson.databind.JavaType;
import com.google.common.collect.Lists;
import xyz.sunziyue.common.utils.JsonMapper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class StringHashMapper<T> {
    private final JsonMapper mapper = JsonMapper.nonDefaultMapper();
    private final JavaType userType;
    private final JavaType mapType;

    public StringHashMapper(Class<T> type) {
        this.mapType = this.mapper.createCollectionType(HashMap.class, new Class[]{String.class, String.class});
        this.userType = this.mapper.getMapper().getTypeFactory().constructType(type);
    }

    public T fromHash(Map<String, String> hash) {
        return hash.isEmpty() ? null : this.mapper.getMapper().convertValue(hash, this.userType);
    }

    public Map<String, String> toHash(T object) {
        Map<String, String> hash = (Map) this.mapper.getMapper().convertValue(object, this.mapType);
        List<String> nullKeys = Lists.newArrayListWithCapacity(hash.size());
        Iterator var4 = hash.entrySet().iterator();

        while (var4.hasNext()) {
            Entry<String, String> entry = (Entry) var4.next();
            if (entry.getValue() == null) {
                nullKeys.add(entry.getKey());
            }
        }

        var4 = nullKeys.iterator();

        while (var4.hasNext()) {
            String nullKey = (String) var4.next();
            hash.remove(nullKey);
        }

        return hash;
    }
}