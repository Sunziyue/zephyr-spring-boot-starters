package xyz.szy.boot.mybatis.autoconfigure;

import com.google.common.base.Throwables;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ExecutorType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@Slf4j
@ConfigurationProperties(prefix = "mybatis")
public class MybatisProperties {
    public static final String MYBATIS_PREFIX = "mybatis";
    public static final String DEFAULT_DIALECT = "mysql";
    private String[] mapperLocations;
    private String typeAliasesPackage;
    private String typeHandlersPackage;
    private ExecutorType executorType;

    public String[] getMapperLocations() {
        return this.mapperLocations;
    }

    public void setMapperLocations(String[] mapperLocations) {
        this.mapperLocations = mapperLocations;
    }

    public Resource[] resolveMapperLocations() {
        List<Resource> resources = new ArrayList<>();
        if (this.mapperLocations != null) {
            for (String mapperLocation : this.mapperLocations) {
                try {
                    Resource[] mappers = (new PathMatchingResourcePatternResolver()).getResources(mapperLocation);
                    resources.addAll(Arrays.asList(mappers));
                } catch (IOException e) {
                    log.error("Mybatis.mapperLocations.{} is Configuration failed;\nERROR:{}", mapperLocation, Throwables.getStackTraceAsString(e));
                }
            }
        } else {
            log.warn("Mybatis mapperLocations is not config, please go to file application.yml found mybatis.mapperLocations");
        }

        Resource[] mapperLocations = new Resource[resources.size()];
        mapperLocations = resources.toArray(mapperLocations);
        return mapperLocations;
    }

    public MybatisProperties() {
        this.executorType = ExecutorType.SIMPLE;
    }
}
