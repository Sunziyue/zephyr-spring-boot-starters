# Swagger 的 starter 工程
### application.yml
```yaml
swagger:
  enable: true
  title: "GIRL-TEAM"
  group-name: "RPC"
  version: "2.2.4.RELEASE"
```
---
### java code
```java
package xyz.sunziyue.controller.controller;

import org.apache.dubbo.config.annotation.Reference;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import xyz.sunziyue.api.api.UserService;
import xyz.sunziyue.api.modle.po.User;
import xyz.sunziyue.common.model.Response;

/**
 * @author Sunziyue
 */

@Slf4j
@RestController
@Api(tags = "用户操作")
public class UserController {

    @Reference
    private UserService userService;

    @ApiOperation("用户信息")
    @GetMapping("user/info")
    public Response<User> userInfo(User request){
        log.info(request.toString());
        Response<User> response = new Response<>();
        response.setResult(this.userService.findUserInfo(request));
        return response;
    }

}
```
```java
package xyz.sunziyue.api.modle.po;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import xyz.sunziyue.common.model.Criteria;

import java.io.Serializable;

/**
 * @author sunziyue
 * 用户 modle
 */
@Data
@Builder
@ApiModel("用户")
public class User extends Criteria implements Serializable {
    private static final long serialVersionUID = 1L;
    @ApiModelProperty("ID")
    private String id;
    @ApiModelProperty("账号")
    private String account;
    @ApiModelProperty("密码")
    private String password;
    @ApiModelProperty("角色ID")
    private String roleExtend;
}
```