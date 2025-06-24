package top.orosirian.spi;

import cn.hutool.core.io.resource.ResourceUtil;
import lombok.extern.slf4j.Slf4j;
import top.orosirian.serializer.myserializer.Serializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 这个类似乎没用到，有机会再看
 * META-INF要用的时候移到core的resources下
 */

// api就是先写代码，再发布接口（让别人调用）
// spi就是先发布接口，再让别人写代码（自己加载别人写的代码）
@Slf4j
public class SpiLoader {

    private static final Map<String, Map<String, Class<? extends Serializer>>> loadedSpiMap = new ConcurrentHashMap<>();

    private static final Map<String, Object> instanceCache = new ConcurrentHashMap<>();

    private static final String SPI_CONFIG_DIR = "META-INF/serializer";

    // 加载指定接口的 SPI 实现类
    public static void loadSpi(Class<?> serviceInterface) {
        String interfaceName = serviceInterface.getName();

        // 如果已经加载过该接口的 SPI 实现，直接返回
        if (loadedSpiMap.containsKey(interfaceName)) {
            return;
        }

        Map<String, Class<? extends Serializer>> keyClassMap = new HashMap<>();

        // 读取配置文件，获取所有实现类
        List<URL> resources = ResourceUtil.getResources(SPI_CONFIG_DIR + serviceInterface.getName());
        for (URL resource : resources) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.openStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty() && !line.startsWith("#")) {
                        String[] parts = line.split("=");
                        if (parts.length == 2) {
                            String key = parts[0].trim();
                            String className = parts[1].trim();
                            Class<?> implClass = Class.forName(className);
                            if (serviceInterface.isAssignableFrom(implClass)) {
                                keyClassMap.put(key, (Class<? extends Serializer>) implClass);
                            }
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                log.error("Failed to load SPI resource: " + resource, e);
            }
        }

        // 将该接口的 SPI 实现类存入缓存
        loadedSpiMap.put(interfaceName, keyClassMap);
    }

    // 根据接口和 key 获取 SPI 实现类实例
    public static <T> T getInstance(Class<T> serviceInterface, String key) {
        String interfaceName = serviceInterface.getName();
        Map<String, Class<? extends Serializer>> keyClassMap = loadedSpiMap.get(interfaceName);

        if (keyClassMap == null) {
            throw new RuntimeException("SPI not loaded for " + interfaceName);
        }

        Class<? extends Serializer> implClass = keyClassMap.get(key);
        if (implClass == null) {
            throw new RuntimeException("No SPI implementation found for key " + key);
        }

        // 从缓存中获取实例，如果不存在则创建
        String implClassName = implClass.getName();
        if (!instanceCache.containsKey(implClassName)) {
            try {
                instanceCache.put(implClassName, implClass.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Failed to instantiate SPI implementation: " + implClassName, e);
            }
        }

        return (T) instanceCache.get(implClassName);
    }


}
