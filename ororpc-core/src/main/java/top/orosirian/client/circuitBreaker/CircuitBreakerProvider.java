package top.orosirian.client.circuitBreaker;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class CircuitBreakerProvider {

    private static final Map<String, CircuitBreaker> circuitBreakerMap = new ConcurrentHashMap<>();

    // 方法粒度熔断
    public synchronized CircuitBreaker getCircuitBreaker(String methodSignature) {
        // 如果methodSignature存在，则返回对应的value
        // 如果methodSignature不存在，则执行后面的语句，并将结果放入map中，然后返回结果value
        return circuitBreakerMap.computeIfAbsent(methodSignature, key -> {
            log.info("方法 [{}] 不存在熔断器，创建新的熔断器实例", methodSignature);
            return new CircuitBreaker(5, 10000, 0.5);
        });
    }



}
