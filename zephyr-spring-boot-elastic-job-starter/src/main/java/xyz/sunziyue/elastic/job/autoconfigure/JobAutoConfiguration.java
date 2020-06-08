package xyz.sunziyue.elastic.job.autoconfigure;

import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import xyz.sunziyue.elastic.job.parser.JobConfigParser;

/**
 * @author Sunziyue
 */
@Component
@EnableConfigurationProperties({ZookeeperProperties.class})
public class JobAutoConfiguration {

    private final ZookeeperProperties zookeeperProperties;

    @Autowired
    public JobAutoConfiguration(ZookeeperProperties zookeeperProperties) {
        this.zookeeperProperties = zookeeperProperties;
    }

    @Bean(
            initMethod = "init"
    )
    public ZookeeperRegistryCenter zookeeperRegistryCenter() {
        ZookeeperConfiguration zkConfig = new ZookeeperConfiguration(this.zookeeperProperties.getServerLists(), this.zookeeperProperties.getNamespace());
        zkConfig.setBaseSleepTimeMilliseconds(this.zookeeperProperties.getBaseSleepTimeMilliseconds());
        zkConfig.setConnectionTimeoutMilliseconds(this.zookeeperProperties.getConnectionTimeoutMilliseconds());
        zkConfig.setDigest(this.zookeeperProperties.getDigest());
        zkConfig.setMaxRetries(this.zookeeperProperties.getMaxRetries());
        zkConfig.setMaxSleepTimeMilliseconds(this.zookeeperProperties.getMaxSleepTimeMilliseconds());
        zkConfig.setSessionTimeoutMilliseconds(this.zookeeperProperties.getSessionTimeoutMilliseconds());
        return new ZookeeperRegistryCenter(zkConfig);
    }

    @Bean
    public JobConfigParser jobConfParser() {
        return new JobConfigParser();
    }
}
