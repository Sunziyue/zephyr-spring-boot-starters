# mybatis 的 starter 工程
### application.yml
```yaml
# mybatis配置
mybatis:
  # 映射xml的文件位置
  mapper-locations: classpath:mappers/*.xml
  # 实体类所在包，简化xml中resultMap中实体类的全类名写法
  type-aliases-package: >
    xyz.sunziyue.api.modle.po
```
---
### java code
```java
package xyz.sunziyue.service.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import xyz.sunziyue.api.modle.po.User;

@Slf4j
@Repository
public class UserDao extends MyBatisDao<User>{

}
```
---
### Mapper XML
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="User">
    <resultMap id="UserMapper" type="User">
        <id column="id" property="id"></id>
        <result column="account" property="account"></result>
        <result column="password" property="password"></result>
        <result column="role_extend" property="roleExtend"></result>
    </resultMap>

    <sql id="tableName">
        user
    </sql>

    <sql id="column">
        id,<include refid="column_exclude_id"></include>
    </sql>

    <sql id="column_exclude_id">
        account, password, role_extend
    </sql>

    <sql id="criteria">
        1 = 1
        <if test="id != null">AND id = #{id}</if>
        <if test="account != null">AND account = #{account}</if>
    </sql>

    <select id="findByUniqueIndex" parameterType="java.util.Map" resultMap="UserMapper">
        SELECT
            <include refid="column"/>
        FROM
            <include refid="tableName"/>
        WHERE
            <include refid="criteria"/>
    </select>

    <insert id="create" parameterType="User">
        INSERT INTO
        <include refid="tableName"/>
        (<include refid="column_exclude_id"/>)
        VALUES
        (#{id}, #{account}, #{password}, #{roleExtend})
    </insert>
</mapper>
```