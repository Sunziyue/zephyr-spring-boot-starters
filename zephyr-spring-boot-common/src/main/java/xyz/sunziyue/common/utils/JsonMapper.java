package xyz.sunziyue.common.utils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.text.DateFormat;

@Slf4j
public class JsonMapper {
    public static final JsonMapper JSON_NON_EMPTY_MAPPER;
    public static final JsonMapper JSON_NON_DEFAULT_MAPPER;
    private final ObjectMapper mapper = new ObjectMapper();

    private JsonMapper(Include include) {
        this.mapper.setSerializationInclusion(include);
        this.mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        this.mapper.registerModule(new GuavaModule());
    }

    public static JsonMapper nonEmptyMapper() {
        return JSON_NON_EMPTY_MAPPER;
    }

    public static JsonMapper nonDefaultMapper() {
        return JSON_NON_DEFAULT_MAPPER;
    }

    public String toJson(Object object) {
        try {
            return this.mapper.writeValueAsString(object);
        } catch (IOException ioException) {
            log.warn("write to json string error:{}", object, ioException);
            return null;
        }
    }

    public <T> T fromJson(String jsonString, Class<T> clazz) {
        if (Strings.isNullOrEmpty(jsonString)) {
            return null;
        } else {
            try {
                return this.mapper.readValue(jsonString, clazz);
            } catch (IOException ioException) {
                log.warn("parse json string error:{}", jsonString, ioException);
                return null;
            }
        }
    }

    public <T> T fromJson(String jsonString, JavaType javaType) {
        if (Strings.isNullOrEmpty(jsonString)) {
            return null;
        } else {
            try {
                return this.mapper.readValue(jsonString, javaType);
            } catch (Exception e) {
                log.warn("parse json string error:{}", jsonString, e);
                return null;
            }
        }
    }

    public JsonNode treeFromJson(String jsonString) throws IOException {
        return this.mapper.readTree(jsonString);
    }

    public <T> T treeToValue(JsonNode node, Class<T> clazz) throws JsonProcessingException {
        return this.mapper.treeToValue(node, clazz);
    }

    public JavaType createCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {
        return this.mapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }

    public <T> T update(String jsonString, T object) {
        try {
            return this.mapper.readerForUpdating(object).readValue(jsonString);
        } catch (IOException e) {
            log.warn("update json string:{}, to object:{} error.", jsonString, object, e);
        }

        return null;
    }

    public String toJsonP(String functionName, Object object) {
        return this.toJson(new JSONPObject(functionName, object));
    }

    public void enableEnumUseToString() {
        this.mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        this.mapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
    }

    public void setDateFormat(DateFormat format) {
        this.mapper.setDateFormat(format);
    }

    public ObjectMapper getMapper() {
        return this.mapper;
    }

    static {
        JSON_NON_EMPTY_MAPPER = new JsonMapper(Include.NON_EMPTY);
        JSON_NON_DEFAULT_MAPPER = new JsonMapper(Include.NON_DEFAULT);
    }
}
