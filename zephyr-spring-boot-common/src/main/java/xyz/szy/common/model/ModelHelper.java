package xyz.szy.common.model;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ModelHelper {

    public static List<Long> extradIds(List<? extends Indexable> indexables) {
        List<Long> ids = Lists.newArrayList();
        Iterator iterator = indexables.iterator();

        while(iterator.hasNext()) {
            Indexable indexable = (Indexable)iterator.next();
            ids.add(indexable.getId());
        }

        return ids;
    }

    public static <T> List<T> extractFiled(List<? extends Serializable> models, String filedName) {
        if (models != null && !models.isEmpty() && !Strings.isNullOrEmpty(filedName)) {
            Class objectType = ((Serializable)models.get(0)).getClass();
            ArrayList result = Lists.newArrayList();

            try {
                Field field = objectType.getDeclaredField(filedName);
                boolean accessible = field.isAccessible();
                field.setAccessible(true);
                Iterator iterator = models.iterator();

                while(iterator.hasNext()) {
                    Object model = iterator.next();
                    T value = (T) field.get(model);
                    result.add(value);
                }

                field.setAccessible(accessible);
            } catch (NoSuchFieldException noSuchFieldException) {
                noSuchFieldException.printStackTrace();
            } catch (IllegalAccessException illegalAccessException) {
                illegalAccessException.printStackTrace();
            }

            return result;
        } else {
            return Collections.emptyList();
        }
    }
}
