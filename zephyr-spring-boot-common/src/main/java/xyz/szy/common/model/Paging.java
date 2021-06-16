package xyz.szy.common.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Data
public class Paging<T> implements Serializable {
    private static final long serialVersionUID = 755183539178076901L;
    private Long total;
    private List<T> data;

    public Paging() {
    }

    public Paging(Long total, List<T> data) {
        this.data = data;
        this.total = total;
    }

    public Boolean isEmpty() {
        return Objects.equals(0L, this.total) || this.data == null || this.data.isEmpty();
    }

    public static <T> Paging<T> empty(Class<T> clazz) {
        List<T> emptyList = Collections.emptyList();
        return new Paging(0L, emptyList);
    }

    public static <T> Paging<T> empty() {
        return new Paging(0L, Collections.emptyList());
    }
}
