package top.orosirian.config;


import cn.hutool.setting.dialect.Props;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OroRpcConfigManager {
    private static final String DEFAULT_CONFIG_FILE = "application.properties";
    private static volatile OroRpcConfig configInstance;

    // 推荐使用方式：显式初始化
    public static void initialize(OroRpcConfig customConfig) {
        configInstance = customConfig;
        log.info("RPC 框架初始化，配置 = {}", customConfig);
    }

    // 简化文件加载入口
    public static void initialize() {
        initialize(loadConfig());
    }

    // 核心配置加载逻辑（内联简化版）
    private static OroRpcConfig loadConfig() {
        Props properties = new Props(DEFAULT_CONFIG_FILE);

        if (properties.isEmpty()) {
            log.warn("配置文件 {} 为空，使用默认配置", DEFAULT_CONFIG_FILE);
            return new OroRpcConfig();
        }

        log.info("成功加载配置文件: {}", DEFAULT_CONFIG_FILE);
        try {
            // 直接转换配置对象
            return properties.toBean(OroRpcConfig.class, "rpc");
        } catch (Exception e) {
            log.error("配置转换异常，使用默认配置", e);
            return new OroRpcConfig();
        }
    }

    // 获取配置（线程安全）
    public static OroRpcConfig getConfig() {
        if (configInstance == null) {
            synchronized (OroRpcConfigManager.class) {
                if (configInstance == null) {
                    initialize();
                }
            }
        }
        return configInstance;
    }
}
