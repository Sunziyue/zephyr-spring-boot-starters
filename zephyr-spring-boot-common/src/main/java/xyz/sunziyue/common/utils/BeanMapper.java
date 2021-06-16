package xyz.sunziyue.common.utils;

import com.google.common.collect.Lists;
import org.dozer.DozerBeanMapper;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class BeanMapper {
    private final static DozerBeanMapper dozer = new DozerBeanMapper();

    private BeanMapper() {
    }

    public static <T> T map(Object source, Class<T> destinationClass) {
        return dozer.map(source, destinationClass);
    }

    public static <T> List<T> mapList(Collection<?> sourceList, Class<T> destinationClass) {
        List<T> destinationList = Lists.newArrayList();
        for (Object sourceObject : sourceList) {
            T destinationObject = dozer.map(sourceObject, destinationClass);
            destinationList.add(destinationObject);
        }
        return destinationList;
    }

    public static void copy(Object source, Object destinationObject) {
        dozer.map(source, destinationObject);
    }

    public static Map<String, Object> convertObjectToMap(Object obj) throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        Map<String, Object> objectAsMap = new HashMap<>();
        BeanInfo info = Introspector.getBeanInfo(obj.getClass());
        PropertyDescriptor[] propertyDescriptors = info.getPropertyDescriptors();
        for (PropertyDescriptor pd : propertyDescriptors) {
            Method reader = pd.getReadMethod();
            if (reader != null && !reader.isAccessible()) {
                reader.setAccessible(Boolean.TRUE);
            }
            objectAsMap.put(pd.getName(), reader.invoke(obj));
        }

        return objectAsMap;
    }
}
