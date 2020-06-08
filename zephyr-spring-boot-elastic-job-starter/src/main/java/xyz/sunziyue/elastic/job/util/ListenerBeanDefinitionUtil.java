package xyz.sunziyue.elastic.job.util;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

/**
 * @author Sunziyue
 */
public class ListenerBeanDefinitionUtil {
    public ListenerBeanDefinitionUtil() {
    }

    public static BeanDefinition getJobListenerBeanDefinition(String listeners) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(listeners);
        factory.setScope("prototype");
        return factory.getBeanDefinition();
    }

    public static BeanDefinition getJobDistributedListenersBeanDefinition(String distributedListeners, long startedTimeoutMilliseconds, long completedTimeoutMilliseconds) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(distributedListeners);
        factory.setScope("prototype");
        factory.addConstructorArgValue(startedTimeoutMilliseconds);
        factory.addConstructorArgValue(completedTimeoutMilliseconds);
        return factory.getBeanDefinition();
    }
}
