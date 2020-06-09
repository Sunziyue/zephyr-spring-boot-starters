# Redis 的 starter 工程
### application.yml
```yaml
redis:
  sentinelHosts: ${REDIS_SENTINELS}
  sentinelMasterName: ${MASTER_NAME}
  port: ${REDIS_PORT}
  password: ${REDIS_PASSWORD:}
  database: ${REDIS_DB_INDEX:0}
  cluster: true
  auth: ${REDIS_PASSWORD:}
```
---
### java code
```java
package xyz.sunziyue.service.dao;

import lombok.extern.slf4j.Slf4j;
import xyz.sunziyue.common.redis.utils.JedisTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class BrowsingItemDao {
    private final JedisTemplate jedisTemplate;

    @Autowired
    public BrowsingItemDao(JedisTemplate jedisTemplate) {
        this.jedisTemplate = jedisTemplate;
    }
    
    // 新增元素
    public Boolean save(String key, String value) {
        return jedisTemplate.execute(jedis -> {
            // 新增元素
            Long num = jedis.zadd(key, System.currentTimeMillis(), value);
            // 设置key的过期时间
            int expireSeconds = expireDays * 24 * 60 * 60;
            jedis.expire(key, expireSeconds);

            // 清除已经过期的元素, 并限制存储数量
            jedis.zremrangeByScore(key, 0, System.currentTimeMillis() - (long) expireSeconds * 1000);
            jedis.zremrangeByRank(key, 0, -limit - 1);
            return num;
        }) >= 0;
    }
    
    // 删除元素
    public Boolean delete(String key) {
        return jedisTemplate.execute(jedis -> {
            return jedis.zrem(key);
        }) == 1;
    }

}
```