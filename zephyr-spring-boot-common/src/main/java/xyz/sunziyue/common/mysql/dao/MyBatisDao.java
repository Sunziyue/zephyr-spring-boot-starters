package xyz.sunziyue.common.mysql.dao;

import com.google.common.collect.Maps;
import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import xyz.sunziyue.common.model.Paging;

import java.lang.reflect.ParameterizedType;
import java.util.*;

public abstract class MyBatisDao<T> {
    @Autowired
    protected SqlSession sqlSession;
    protected static final String CREATE = "create";
    protected static final String CREATES = "creates";
    protected static final String DELETE = "delete";
    protected static final String DELETES = "deletes";
    protected static final String UPDATE = "update";
    protected static final String FIND_BY_ID = "findById";
    protected static final String FIND_BY_UNIQUE_INDEX = "findByUniqueIndex";
    protected static final String FIND_BY_IDS = "findByIds";
    protected static final String LIST = "list";
    protected static final String COUNT = "count";
    protected static final String PAGING = "paging";
    public final String nameSpace;

    public MyBatisDao() {
        if (this.getClass().getGenericSuperclass() instanceof ParameterizedType) {
            this.nameSpace = ((Class)((ParameterizedType)this.getClass().getGenericSuperclass()).getActualTypeArguments()[0]).getSimpleName();
        } else {
            this.nameSpace = ((Class)((ParameterizedType)this.getClass().getSuperclass().getGenericSuperclass()).getActualTypeArguments()[0]).getSimpleName();
        }

    }

    public Boolean create(T t) {
        return this.sqlSession.insert(this.sqlId("create"), t) == 1;
    }

    public Integer creates(List<T> ts) {
        return this.sqlSession.insert(this.sqlId("creates"), ts);
    }

    public Integer creates(T t0, T t1, T... tn) {
        return this.sqlSession.insert(this.sqlId("creates"), Arrays.asList(t0, t1, tn));
    }

    public Boolean delete(Long id) {
        return this.sqlSession.delete(this.sqlId("delete"), id) == 1;
    }

    public Integer deletes(List<Long> ids) {
        return this.sqlSession.delete(this.sqlId("deletes"), ids);
    }

    public Integer deletes(Long id0, Long id1, Long... idn) {
        return this.sqlSession.delete(this.sqlId("deletes"), Arrays.asList(id0, id1, idn));
    }

    public Boolean update(T t) {
        return this.sqlSession.update(this.sqlId("update"), t) == 1;
    }

    public T findById(Integer id) {
        return this.findById((long)id);
    }

    public T findById(Long id) {
        return this.sqlSession.selectOne(this.sqlId("findById"), id);
    }

    public T findByUniqueIndex(Map<String, Object> criteria) {
        return this.sqlSession.selectOne(this.sqlId("findByUniqueIndex"), criteria);
    }

    public List<T> findByIds(List<Long> ids) {
        return CollectionUtils.isEmpty(ids) ? Collections.emptyList() : this.sqlSession.selectList(this.sqlId("findByIds"), ids);
    }

    public List<T> findByIds(Long id0, Long id1, Long... idn) {
        return this.sqlSession.selectList(this.sqlId("findByIds"), Arrays.asList(id0, id1, idn));
    }

    public List<T> listAll() {
        return this.list((T)null);
    }

    public List<T> list(T t) {
        return this.sqlSession.selectList(this.sqlId("list"), t);
    }

    public List<T> list(Map<?, ?> criteria) {
        return this.sqlSession.selectList(this.sqlId("list"), criteria);
    }

    public Paging<T> paging(Integer offset, Integer limit) {
        return this.paging(offset, limit, new HashMap());
    }

    public Paging<T> paging(Integer offset, Integer limit, Map<String, Object> criteria) {
        if (criteria == null) {
            criteria = Maps.newHashMap();
        }

        Long total = this.sqlSession.selectOne(this.sqlId("count"), criteria);
        if (total <= 0L) {
            return new Paging<>(0L, Collections.emptyList());
        } else {
            criteria.put("offset", offset);
            criteria.put("limit", limit);
            List<T> dataList = this.sqlSession.selectList(this.sqlId("paging"), criteria);
            return new Paging<>(total, dataList);
        }
    }

    public Paging<T> paging(Map<String, Object> criteria) {
        if (criteria == null) {
            criteria = Maps.newHashMap();
        }

        Long total = this.sqlSession.selectOne(this.sqlId("count"), criteria);
        if (total <= 0L) {
            return new Paging<>(0L, Collections.emptyList());
        } else {
            List<T> dataList = this.sqlSession.selectList(this.sqlId("paging"), criteria);
            return new Paging<>(total, dataList);
        }
    }

    protected String sqlId(String id) {
        return this.nameSpace + "." + id;
    }

    protected SqlSession getSqlSession() {
        return this.sqlSession;
    }
}
