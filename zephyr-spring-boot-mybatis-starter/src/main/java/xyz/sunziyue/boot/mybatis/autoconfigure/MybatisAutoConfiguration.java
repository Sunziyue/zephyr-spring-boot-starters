package xyz.sunziyue.boot.mybatis.autoconfigure;

import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.List;

@ConditionalOnClass({SqlSessionFactory.class, SqlSessionFactoryBean.class})
@ConditionalOnMissingBean({SqlSessionTemplate.class})
@ConditionalOnBean({DataSource.class})
@EnableConfigurationProperties({MybatisProperties.class})
@AutoConfigureAfter({DataSourceAutoConfiguration.class})
public class MybatisAutoConfiguration {
    private DatabaseIdProvider databaseIdProvider;
    private MybatisProperties mybatisProperties;
    @Autowired(
            required = false
    )
    private List<Interceptor> interceptors;

    @Autowired
    public MybatisAutoConfiguration(MybatisProperties mybatisProperties, ObjectProvider<DatabaseIdProvider> databaseIdProvider) {
        this.databaseIdProvider = (DatabaseIdProvider)databaseIdProvider.getIfAvailable();
        this.mybatisProperties = mybatisProperties;
    }

    @Bean
    @ConditionalOnMissingBean({PlatformTransactionManager.class})
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setVfs(SpringBootVFS.class);
        if (!ObjectUtils.isEmpty(this.interceptors)) {
            factory.setPlugins((Interceptor[])this.interceptors.toArray(new Interceptor[this.interceptors.size()]));
        }

        if (this.databaseIdProvider != null) {
            factory.setDatabaseIdProvider(this.databaseIdProvider);
        }

        if (StringUtils.hasLength(this.mybatisProperties.getTypeAliasesPackage())) {
            factory.setTypeAliasesPackage(TypeAliasPackageParser.flatPackageNames(this.mybatisProperties.getTypeAliasesPackage()));
        }

        if (StringUtils.hasLength(this.mybatisProperties.getTypeHandlersPackage())) {
            factory.setTypeHandlersPackage(this.mybatisProperties.getTypeHandlersPackage());
        }

        Resource[] mapperLocations = this.mybatisProperties.resolveMapperLocations();
        if (!ObjectUtils.isEmpty(mapperLocations)) {
            factory.setMapperLocations(mapperLocations);
        }

        return factory.getObject();
    }

    @Bean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory, this.mybatisProperties.getExecutorType());
    }
}
