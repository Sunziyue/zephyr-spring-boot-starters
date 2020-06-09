package xyz.sunziyue.boot.swagger.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "swagger")
public class SwaggerProperties {
    private Boolean enable;
    private String title;
    private String groupName;
    private String version;

    public SwaggerProperties() {
        this.enable = Boolean.TRUE;
        this.version = "1.0";
    }
}
