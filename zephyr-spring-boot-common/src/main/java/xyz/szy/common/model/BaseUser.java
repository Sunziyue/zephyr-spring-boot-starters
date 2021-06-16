package xyz.szy.common.model;

import java.io.Serializable;
import java.util.List;

public interface BaseUser extends Serializable {
    Long getId();

    String getName();

    Integer getType();

    String getTypeName();

    List<String> getRoles();
}