package top.orosirian.client.circuitBreaker;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CircuitBreakerProvider {

    private static final Map<String, CircuitBreaker> circuitBreakerMap = new ConcurrentHashMap<>();

    public synchronized CircuitBreaker getCircuitBreaker(String serviceName) {
        // 如果serviceName存在，则返回对应的value
        // 如果serviceName不存在，则执行后面的语句，并将结果放入map中，然后返回结果value
        return circuitBreakerMap.computeIfAbsent(serviceName, key -> {
            log.info("服务 [{}] 不存在熔断器，创建新的熔断器实例", serviceName);
            return new CircuitBreaker(5, 10000, 0.5);
        });
    }



}
