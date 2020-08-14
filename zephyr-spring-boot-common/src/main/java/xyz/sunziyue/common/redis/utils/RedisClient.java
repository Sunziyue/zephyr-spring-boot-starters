package xyz.sunziyue.common.redis.utils;

import com.google.common.collect.Lists;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import xyz.sunziyue.common.model.Paging;

import java.util.*;
import java.util.stream.Collectors;

public abstract class RedisClient {

    public static Long listLen(JedisTemplate jedisTemplate, final String key) {
        return jedisTemplate.execute(jedi -> {
            return jedi.llen(key);
        });
    }

    public static List<Long> listAll2Long(JedisTemplate jedisTemplate, final String key) {
        return jedisTemplate.execute(jedi -> {
            List<String> strValues = jedi.lrange(key, 0L, -1L);
            return strValues.stream().map(Long::valueOf).collect(Collectors.toList());
        });
    }

    public static Long listRemOne(JedisTemplate jedisTemplate, String key, Object val) {
        return listRem(jedisTemplate, key, val, 1L);
    }

    public static Long listRemAll(JedisTemplate jedisTemplate, String key, Object val) {
        return listRem(jedisTemplate, key, val, 0L);
    }

    private static Long listRem(JedisTemplate jedisTemplate, final String key, final Object val, final Long count) {
        return jedisTemplate.execute(jedis -> {
            return jedis.lrem(key, count, String.valueOf(val));
        });
    }

    public static Long listRemOne(JedisTemplate jedisTemplate, List<String> keys, Object val) {
        return listRem(jedisTemplate, keys, val, 1L);
    }

    public static Long listRemAll(JedisTemplate jedisTemplate, List<String> keys, Object val) {
        return listRem(jedisTemplate, keys, val, 0L);
    }

    private static Long listRem(JedisTemplate jedisTemplate, final List<String> keys, final Object val, final Long count) {
        return jedisTemplate.execute(jedi -> {
            Pipeline pipeline = jedi.pipelined();
            long deleted = 0L;
            String key;
            for(Iterator<String> i = keys.iterator(); i.hasNext(); deleted = deleted + pipeline.lrem(key, count, String.valueOf(val)).get()) {
                key = i.next();
            }
            pipeline.sync();
            return deleted;
        });
    }

    public static Long listRem(JedisTemplate jedisTemplate, final List<String> keys, final Map<String, List<?>> keyVals) {
        return jedisTemplate.execute(jedi -> {
            Pipeline pipeline = jedi.pipelined();
            long removed = 0L;
            for (String key : keys) {
                List<?> values = keyVals.get(key);
                for (Iterator<?> i = values.iterator(); i.hasNext(); removed = removed + 1L) {
                    Object val = i.next();
                    pipeline.lrem(key, 1L, String.valueOf(val));
                }
            }
            pipeline.sync();
            return removed;
        });
    }

    public static Long listRem(JedisTemplate jedisTemplate, final String key, final List<?> vals) {
        return jedisTemplate.execute(jedis -> {
            Pipeline pipeline = jedis.pipelined();
            long removed = 0L;
            for(Iterator<?> i = vals.iterator(); i.hasNext(); removed = removed + 1L) {
                Object val = i.next();
                pipeline.lrem(key, 1L, String.valueOf(val));
            }
            pipeline.sync();
            return removed;
        });
    }

    public static List<Long> listPaging2Long(JedisTemplate jedisTemplate, final String key, final Integer offset, final Integer limit) {
        return jedisTemplate.execute(jedi -> {
            List<String> ids = jedi.lrange(key, (long)offset, offset + limit - 1);
            return ids.stream().map(s -> s == null ? null : Long.valueOf(s)).collect(Collectors.toList());
        });
    }

    public static Paging<Long> listPaging(JedisTemplate jedisTemplate, final String key, final Integer offset, final Integer limit) {
        return jedisTemplate.execute(jedi -> {
            Pipeline pipeline = jedi.pipelined();
            Response<Long> r = pipeline.zcard(key);
            Response<Set<String>> response = pipeline.zrevrange(key, (long)offset, offset + limit - 1);
            pipeline.sync();
            Long total = r.get();
            if (total <= 0L) {
                return new Paging<>(0L, Collections.emptyList());
            } else {
                List<Long> ids = Lists.newArrayListWithCapacity(total.intValue());
                for (Object o : response.get()) {
                    String s = (String) o;
                    ids.add(Long.parseLong(s));
                }
                return new Paging<>(total, ids);
            }
        });
    }

    public static Long listAdd(JedisTemplate jedisTemplate, final List<String> keys, final String val) {
        return jedisTemplate.execute(jedi -> {
            Pipeline pipeline = jedi.pipelined();
            long pushed = 0L;
            for(Iterator<String> i = keys.iterator(); i.hasNext(); pushed = pushed + 1L) {
                String key = i.next();
                pipeline.lpush(key, val);
            }
            pipeline.sync();
            return pushed;
        });
    }

    public static Long setAdd(JedisTemplate jedisTemplate, final String key, final String val) {
        return jedisTemplate.execute((Jedis jedi) -> jedi.sadd(key, val));
    }

    public static Long setAdd(JedisTemplate jedisTemplate, final String key, final String... vals) {
        return jedisTemplate.execute((Jedis jedi) -> jedi.sadd(key, vals));
    }

    public static List<Long> setCounts(JedisTemplate jedisTemplate, final List<String> keys) {
        return jedisTemplate.execute(jedi -> {
            Pipeline pipeline = jedi.pipelined();
            List<Response<Long>> resp = Lists.newArrayListWithCapacity(keys.size());
            for (String key : keys) {
                resp.add(pipeline.scard(key));
            }
            pipeline.sync();
            return resp.stream().map(Response::get).collect(Collectors.toList());
        });
    }
}
