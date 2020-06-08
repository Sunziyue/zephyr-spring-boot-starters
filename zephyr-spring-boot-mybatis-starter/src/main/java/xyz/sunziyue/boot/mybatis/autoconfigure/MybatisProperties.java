package xyz.sunziyue.boot.mybatis.autoconfigure;

import lombok.Data;
import org.apache.ibatis.session.ExecutorType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
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
        List<Resource> resources = new ArrayList();
        if (this.mapperLocations != null) {
            String[] var2 = this.mapperLocations;
            int var3 = var2.length;

            for (int var4 = 0; var4 < var3; ++var4) {
                String mapperLocation = var2[var4];

                try {
                    Resource[] mappers = (new PathMatchingResourcePatternResolver()).getResources(mapperLocation);
                    resources.addAll(Arrays.asList(mappers));
                } catch (IOException var8) {
                }
            }
        }

        Resource[] mapperLocations = new Resource[resources.size()];
        mapperLocations = (Resource[]) resources.toArray(mapperLocations);
        return mapperLocations;
    }

    public MybatisProperties() {
        this.executorType = ExecutorType.SIMPLE;
    }
}
