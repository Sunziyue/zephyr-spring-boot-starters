# Redis 的 starter 工程
### application.yml
```yaml
redis:
  host: ${REDIS_HOST:192.168.99.100}
  port: ${REDIS_PORT:6380}
  auth: ${AUTH:}
  maxTotal: ${MAXTOTAL:}
  maxIdle: ${MAXIDLE:}
  maxWaitMillis: ${MAXWAITMILLIS:}
  testOnBorrow: ${TESTONBORROW:}
  cluster: ${CLUSTER:false}
  sentinelHosts: ${SENTINELHOSTS:}
  sentinelMasterName: ${SENTINELMASTERNAME:}
```
---
### java code
```java
package xyz.sunziyue.service.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import xyz.sunziyue.api.modle.po.User;
import xyz.sunziyue.common.redis.dao.RedisBaseDao;
import xyz.sunziyue.common.redis.utils.JedisTemplate;

@Slf4j
@Repository
public class RedisTest extends RedisBaseDao<User> {
    @Autowired
    public RedisTest(JedisTemplate template) {
        super(template);
    }

    // 新增元素
    public Boolean save(String key, String value) {
        return this.template.execute(jedis -> {
            // 新增元素
            Long num = jedis.zadd(key, System.currentTimeMillis(), value);
            // 设置key的过期时间
            int expireSeconds = 10 * 24 * 60 * 60;
            jedis.expire(key, expireSeconds);
            return num;
        }) >= 0;
    }
}
```