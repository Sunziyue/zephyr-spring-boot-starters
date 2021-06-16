package xyz.szy.boot.swagger.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "swagger")
public class SwaggerProperties {
    private Boolean enable;
    private String title;
    private String groupName;
    private String version;
    private String webHandler = "swagger-ui.html";
    private String webLocations = "classpath:/META-INF/resources/";
    private String webjarsHandler = "/webjars/**";
    private String webjarsLocations = "classpath:/META-INF/resources/webjars/";

    public SwaggerProperties() {
        this.enable = Boolean.TRUE;
        this.version = "1.0";
    }
}
