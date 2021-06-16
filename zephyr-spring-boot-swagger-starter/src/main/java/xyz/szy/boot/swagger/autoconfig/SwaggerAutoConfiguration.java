package xyz.szy.boot.swagger.autoconfig;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import springfox.documentation.swagger2.configuration.Swagger2DocumentationConfiguration;
import xyz.szy.boot.swagger.properties.SwaggerProperties;

@Configuration
@EnableSwagger2
@EnableConfigurationProperties({SwaggerProperties.class})
@Import({Swagger2DocumentationConfiguration.class})
public class SwaggerAutoConfiguration implements WebMvcConfigurer {
    private final SwaggerProperties swaggerProperties;

    @Autowired
    public SwaggerAutoConfiguration(SwaggerProperties swaggerProperties) {
        this.swaggerProperties = swaggerProperties;
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName(this.swaggerProperties.getGroupName())
                .apiInfo(this.apiInfo())
                .enable(this.swaggerProperties.getEnable())
                .select()
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title(String.format("%s 在线接口文档", this.swaggerProperties.getTitle()))
                .version(this.swaggerProperties.getVersion())
                .build();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置 web 管理入口地址
        registry.addResourceHandler(this.swaggerProperties.getWebHandler())
                .addResourceLocations(this.swaggerProperties.getWebLocations());
        // 配置 swagger 所需 webjars
        registry.addResourceHandler(this.swaggerProperties.getWebjarsHandler())
                .addResourceLocations(this.swaggerProperties.getWebjarsLocations());
    }
}
