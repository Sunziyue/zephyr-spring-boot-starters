package xyz.sunziyue.boot.mybatis.autoconfigure;

import org.apache.ibatis.io.VFS;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SpringBootVFS extends VFS {
    private final ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver(this.getClass().getClassLoader());

    public boolean isValid() {
        return true;
    }

    protected List<String> list(URL url, String path) throws IOException {
        Resource[] resources = this.resourceResolver.getResources("classpath*:" + path + "/**/*.class");
        List<String> resourcePaths = new ArrayList();
        for(int i = 0; i < resources.length; ++i) {
            Resource resource = resources[i];
            resourcePaths.add(preserveSubpackageName(resource.getURI(), path));
        }
        return resourcePaths;
    }

    private static String preserveSubpackageName(URI uri, String rootPath) {
        String uriStr = uri.toString();
        int start = uriStr.indexOf(rootPath);
        return uriStr.substring(start);
    }
}

