package xyz.sunziyue.common.redis.utils;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import xyz.sunziyue.common.model.Paging;
import xyz.sunziyue.common.redis.utils.JedisTemplate.JedisAction;

import java.util.*;

public abstract class RedisClient {
    public RedisClient() {
    }

    public static Long listLen(JedisTemplate jedisTemplate, final String key) {
        return (Long)jedisTemplate.execute(new JedisAction<Long>() {
            public Long action(Jedis jedis) {
                return jedis.llen(key);
            }
        });
    }

    public static List<Long> listAll2Long(JedisTemplate jedisTemplate, final String key) {
        return (List)jedisTemplate.execute(new JedisAction<List<Long>>() {
            public List<Long> action(Jedis jedis) {
                List<String> strVals = jedis.lrange(key, 0L, -1L);
                List<Long> longVals = Lists.transform(strVals, new Function<String, Long>() {
                    public Long apply(String strVal) {
                        return Long.valueOf(strVal);
                    }
                });
                return longVals;
            }
        });
    }

    public static Long listRemOne(JedisTemplate jedisTemplate, String key, Object val) {
        return listRem(jedisTemplate, key, val, 1L);
    }

    public static Long listRemAll(JedisTemplate jedisTemplate, String key, Object val) {
        return listRem(jedisTemplate, key, val, 0L);
    }

    private static Long listRem(JedisTemplate jedisTemplate, final String key, final Object val, final Long count) {
        return (Long)jedisTemplate.execute(new JedisAction<Long>() {
            public Long action(Jedis jedis) {
                return jedis.lrem(key, count, String.valueOf(val));
            }
        });
    }

    public static Long listRemOne(JedisTemplate jedisTemplate, List<String> keys, Object val) {
        return listRem(jedisTemplate, keys, val, 1L);
    }

    public static Long listRemAll(JedisTemplate jedisTemplate, List<String> keys, Object val) {
        return listRem(jedisTemplate, keys, val, 0L);
    }

    private static Long listRem(JedisTemplate jedisTemplate, final List<String> keys, final Object val, final Long count) {
        return (Long)jedisTemplate.execute(new JedisAction<Long>() {
            public Long action(Jedis jedis) {
                Pipeline p = jedis.pipelined();
                Long deleted = 0L;

                String key;
                for(Iterator i = keys.iterator(); i.hasNext(); deleted = deleted + (Long)p.lrem(key, count, String.valueOf(val)).get()) {
                    key = (String)i.next();
                }

                p.sync();
                return deleted;
            }
        });
    }

    public static Long listRem(JedisTemplate jedisTemplate, final List<String> keys, final Map<String, List<?>> keyVals) {
        return (Long)jedisTemplate.execute(new JedisAction<Long>() {
            public Long action(Jedis jedis) {
                Pipeline p = jedis.pipelined();
                Long removed = 0L;
                Iterator iterator = keys.iterator();

                while(iterator.hasNext()) {
                    String key = (String)iterator.next();
                    List<?> vals = (List)keyVals.get(key);

                    for(Iterator i = vals.iterator(); i.hasNext(); removed = removed + 1L) {
                        Object val = i.next();
                        p.lrem(key, 1L, String.valueOf(val));
                    }
                }

                p.sync();
                return removed;
            }
        });
    }

    public static Long listRem(JedisTemplate jedisTemplate, final String key, final List<?> vals) {
        return (Long)jedisTemplate.execute(new JedisAction<Long>() {
            public Long action(Jedis jedis) {
                Pipeline p = jedis.pipelined();
                Long removed = 0L;

                for(Iterator i = vals.iterator(); i.hasNext(); removed = removed + 1L) {
                    Object val = i.next();
                    p.lrem(key, 1L, String.valueOf(val));
                }

                p.sync();
                return removed;
            }
        });
    }

    public static List<Long> listPaging2Long(JedisTemplate jedisTemplate, final String key, final Integer offset, final Integer limit) {
        return (List)jedisTemplate.execute(new JedisAction<List<Long>>() {
            public List<Long> action(Jedis jedis) {
                List<String> ids = jedis.lrange(key, (long)offset, (long)(offset + limit - 1));
                return Lists.transform(ids, new Function<String, Long>() {
                    public Long apply(String s) {
                        return s == null ? null : Long.valueOf(s);
                    }
                });
            }
        });
    }

    public static Paging<Long> listPaging(JedisTemplate jedisTemplate, final String key, final Integer offset, final Integer limit) {
        return (Paging)jedisTemplate.execute(new JedisAction<Paging<Long>>() {
            public Paging<Long> action(Jedis jedis) {
                Pipeline pipeline = jedis.pipelined();
                Response<Long> r = pipeline.zcard(key);
                Response<Set<String>> i = pipeline.zrevrange(key, (long)offset, (long)(offset + limit - 1));
                pipeline.sync();
                Long total = (Long)r.get();
                if (total <= 0L) {
                    return new Paging(0L, Collections.emptyList());
                } else {
                    List<Long> ids = Lists.newArrayListWithCapacity(total.intValue());
                    Iterator iterator = ((Set)i.get()).iterator();

                    while(iterator.hasNext()) {
                        String s = (String)iterator.next();
                        ids.add(Long.parseLong(s));
                    }

                    return new Paging(total, ids);
                }
            }
        });
    }

    public static Long listAdd(JedisTemplate jedisTemplate, final List<String> keys, final String val) {
        return (Long)jedisTemplate.execute(new JedisAction<Long>() {
            public Long action(Jedis jedis) {
                Pipeline p = jedis.pipelined();
                Long pushed = 0L;

                for(Iterator i = keys.iterator(); i.hasNext(); pushed = pushed + 1L) {
                    String key = (String)i.next();
                    p.lpush(key, new String[]{val});
                }

                p.sync();
                return pushed;
            }
        });
    }

    public static Long setAdd(JedisTemplate jedisTemplate, final String key, final String val) {
        return (Long)jedisTemplate.execute(new JedisAction<Long>() {
            public Long action(Jedis jedis) {
                Long inserted = jedis.sadd(key, new String[]{val});
                return inserted;
            }
        });
    }

    public static Long setAdd(JedisTemplate jedisTemplate, final String key, final String... vals) {
        return (Long)jedisTemplate.execute(new JedisAction<Long>() {
            public Long action(Jedis jedis) {
                Long inserted = jedis.sadd(key, vals);
                return inserted;
            }
        });
    }

    public static List<Long> setCounts(JedisTemplate jedisTemplate, final List<String> keys) {
        return (List)jedisTemplate.execute(new JedisAction<List<Long>>() {
            public List<Long> action(Jedis jedis) {
                Pipeline p = jedis.pipelined();
                List<Response<Long>> resp = Lists.newArrayListWithCapacity(keys.size());
                Iterator iterator = keys.iterator();

                while(iterator.hasNext()) {
                    String key = (String)iterator.next();
                    resp.add(p.scard(key));
                }

                p.sync();
                List<Long> counts = Lists.transform(resp, new Function<Response<Long>, Long>() {
                    public Long apply(Response<Long> rl) {
                        return (Long)rl.get();
                    }
                });
                return counts;
            }
        });
    }
}
