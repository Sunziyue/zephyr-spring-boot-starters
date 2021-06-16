package xyz.szy.common.redis.utils;

import com.google.common.collect.Lists;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import xyz.szy.common.model.Paging;

import java.util.*;
import java.util.stream.Collectors;

public abstract class RedisClient {

    /**
     * List集合API
     * 返回列表 key 的长度。
     * @param jedisTemplate jedisTemplate Redis线程池模板
     * @param key 键
     * @return 返回列表 key 的长度。
     *         如果 key 不存在，则 key 被解释为一个空列表，返回 0 .
     *         如果 key 不是列表类型，返回一个错误。
     */
    public static Long listLen(JedisTemplate jedisTemplate, final String key) {
        return jedisTemplate.execute(jedi -> {
            return jedi.llen(key);
        });
    }

    /**
     * List集合API
     * 返回对应Key集合下的所有元素(元素类型为Long时选用)
     * @param jedisTemplate Redis线程池模板
     * @param key 键
     * @return 对应Key的集合结果
     */
    public static List<Long> listAll2Long(JedisTemplate jedisTemplate, final String key) {
        return jedisTemplate.execute(jedi -> {
            List<String> strValues = jedi.lrange(key, 0L, -1L);
            return strValues.stream().map(Long::valueOf).collect(Collectors.toList());
        });
    }

    /**
     * List集合API
     * 从表头开始向表尾搜索，移除1个 与 val 相等的元素
     * @param jedisTemplate Redis线程池模板
     * @param key 键
     * @param val 值
     * @return 移除值的总数
     */
    public static Long listRemOne(JedisTemplate jedisTemplate, String key, Object val) {
        return listRem(jedisTemplate, key, val, 1L);
    }

    /**
     * List集合API
     * 移除集合中所有与 val 相等的值
     * @param jedisTemplate Redis线程池模板
     * @param key 键
     * @param val 值
     * @return 移除值的总数
     */
    public static Long listRemAll(JedisTemplate jedisTemplate, String key, Object val) {
        return listRem(jedisTemplate, key, val, 0L);
    }

    /**
     * List集合API
     * 根据参数 count 的值，移除列表中与参数 value 相等的元素
     * @param jedisTemplate Redis线程池模板
     * @param key 键
     * @param val 值
     * @param count count > 0 : 从表头开始向表尾搜索，移除与 val 相等的元素，数量为 count 。
     *              count < 0 : 从表尾开始向表头搜索，移除与 val 相等的元素，数量为 count 的绝对值。
     *              count = 0 : 移除集合中所有与 val 相等的值。
     * @return 移除值的总数
     */
    private static Long listRem(JedisTemplate jedisTemplate, final String key, final Object val, final Long count) {
        return jedisTemplate.execute(jedis -> {
            return jedis.lrem(key, count, String.valueOf(val));
        });
    }

    /**+
     * 在多个集合中从表头开始向表尾搜索，移除1个 与 val 相等的元素
     * @param jedisTemplate Redis线程池模板
     * @param keys 键集合
     * @param val 值
     * @return 移除值的总数
     */
    public static Long listRemOne(JedisTemplate jedisTemplate, List<String> keys, Object val) {
        return listRem(jedisTemplate, keys, val, 1L);
    }

    /**+
     * 在多个集合中移除集合中所有与 val 相等的值
     * @param jedisTemplate Redis线程池模板
     * @param keys 键集合
     * @param val 值
     * @return 移除值的总数
     */
    public static Long listRemAll(JedisTemplate jedisTemplate, List<String> keys, Object val) {
        return listRem(jedisTemplate, keys, val, 0L);
    }

    /**
     * List集合API
     * 根据参数 count 的值，移除【多个】列表中与参数 value 相等的元素
     * @param jedisTemplate Redis线程池模板
     * @param keys 键集合
     * @param val 值
     * @param count count > 0 : 从表头开始向表尾搜索，移除与 val 相等的元素，数量为 count 。
     *              count < 0 : 从表尾开始向表头搜索，移除与 val 相等的元素，数量为 count 的绝对值。
     *              count = 0 : 移除集合中所有与 val 相等的值。
     * @return 移除值的总数
     */
    private static Long listRem(JedisTemplate jedisTemplate, final List<String> keys, final Object val, final Long count) {
        return jedisTemplate.execute(jedi -> {
            Pipeline pipeline = jedi.pipelined();
            long deleted = 0L;
            String key;
            for(Iterator<String> i = keys.iterator(); i.hasNext();
                deleted = deleted + pipeline.lrem(key, count, String.valueOf(val)).get()) {
                key = i.next();
            }
            pipeline.sync();
            return deleted;
        });
    }

    /**
     * List集合API
     * 对多个集合进行删除操作，在每个集合中从表头开始向表尾搜索，删除多个对应 keyVals 的值一次
     * @param jedisTemplate Redis线程池模板
     * @param keys 键集合
     * @param keyVals 对应集合需要删除的值
     * @return 删除元素的总数
     */
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

    /**
     * List集合API
     * 对单个集合进行删除操作，从表头开始向表尾搜索，删除vals中的多个值，每个值删除一次
     * @param jedisTemplate Redis线程池模板
     * @param key 键
     * @param vals 值集合
     * @return 删除元素的总数
     */
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

    /**
     * List集合API
     * 分页查询一页集合中区间内的所有元素
     * @param jedisTemplate Redis线程池模板
     * @param key 键
     * @param offset 偏移量（lrange开始位置）
     * @param limit 每页上限（lrange结束位置）
     * @return 集合中区间内的所有元素
     */
    public static List<Long> listPaging2Long(JedisTemplate jedisTemplate, final String key, final Integer offset, final Integer limit) {
        return jedisTemplate.execute(jedi -> {
            List<String> ids = jedi.lrange(key, offset, offset + limit - 1);
            return ids.stream().map(s -> s == null ? null : Long.valueOf(s)).collect(Collectors.toList());
        });
    }

    /**
     * 有序集合分页
     * @param jedisTemplate Redis线程池模板
     * @param key 键
     * @param offset offset 偏移量（zrevrange）
     * @param limit limit 每页上限（zrevrange）
     * @return 集合中区间内的所有元素
     */
    public static Paging<Long> listPaging(JedisTemplate jedisTemplate, final String key, final Integer offset, final Integer limit) {
        return jedisTemplate.execute(jedi -> {
            Pipeline pipeline = jedi.pipelined();
            // set集合元素count
            Response<Long> r = pipeline.zcard(key);
            //
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
