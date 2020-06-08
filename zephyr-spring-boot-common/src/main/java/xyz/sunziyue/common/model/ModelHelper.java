package xyz.sunziyue.common.model;

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
        Iterator var2 = indexables.iterator();

        while(var2.hasNext()) {
            Indexable indexable = (Indexable)var2.next();
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
                Iterator var6 = models.iterator();

                while(var6.hasNext()) {
                    Object model = var6.next();
                    T value = (T) field.get(model);
                    result.add(value);
                }

                field.setAccessible(accessible);
            } catch (NoSuchFieldException var9) {
                var9.printStackTrace();
            } catch (IllegalAccessException var10) {
                var10.printStackTrace();
            }

            return result;
        } else {
            return Collections.emptyList();
        }
    }
}
