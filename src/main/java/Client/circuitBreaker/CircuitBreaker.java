package Client.circuitBreaker;

import lombok.Getter;

import java.util.concurrent.atomic.AtomicInteger;

public class CircuitBreaker {

    @Getter
    private CircuitBreakerState state;

    private final AtomicInteger requestCount = new AtomicInteger(0);

    private final AtomicInteger failureCount = new AtomicInteger(0);

    private final AtomicInteger successCount = new AtomicInteger(0);

    private final int FAILURE_THRESHOLD;        // CLOSE -> OPEN 失败次数阈值

    private final long RETRY_INTERVAL;               // OPEN -> HALF_OPEN 恢复时间

    private final double HALF_OPEN_THRESHOLD;   // HALF_OPEN -> CLOSE 成功比例

    private long lastFailureTime;               // 上次失败时间

    public CircuitBreaker(int failureThreshold, long retryInterval, double halfOpenThreshold) {
        state = CircuitBreakerState.CLOSE;
        this.FAILURE_THRESHOLD = failureThreshold;
        this.RETRY_INTERVAL = retryInterval;
        this.HALF_OPEN_THRESHOLD = halfOpenThreshold;
    }

    // 能否执行请求
    public synchronized boolean allowRequest() {
        long currentTime = System.currentTimeMillis();
        switch (state) {
            case CLOSE:         // 熔断器关闭代表正常请求，这不是个开关
                return true;
            case HALF_OPEN:     // HALF_OPEN转OPEN需要在请求逻辑中处理，而不是此处处理
                requestCount.incrementAndGet();
                return true;
            case OPEN:          // 熔断器打开
                if(currentTime - lastFailureTime > RETRY_INTERVAL) {
                    System.out.println("熔断时间已过，尝试请求");
                    state = CircuitBreakerState.HALF_OPEN;
                    resetCounts();
                    return true;
                } else {
                    System.out.println("处于熔断状态，无法请求");
                    return false;
                }
            default:
                return false;
        }
    }

    public synchronized void recordSuccess() {
        switch (state) {
            case CLOSE:
                resetCounts();
                break;
            case HALF_OPEN:
                successCount.incrementAndGet();
                if(successCount.get() >= requestCount.get() * HALF_OPEN_THRESHOLD) {
                    state = CircuitBreakerState.CLOSE;
                    resetCounts();
                }
                break;
            case OPEN:
                resetCounts();  // 其实不可能走到这步
                break;
        }
    }

    public synchronized void recordFailure() {
        failureCount.incrementAndGet();
        switch(state) {
            case CLOSE:
                if(failureCount.get() >= FAILURE_THRESHOLD) {
                    state = CircuitBreakerState.OPEN;
                }
                break;
            case HALF_OPEN:
                state = CircuitBreakerState.OPEN;
                lastFailureTime = System.currentTimeMillis();
                break;
            case OPEN:
                break;
        }
    }

    public void resetCounts() {
        requestCount.set(0);
        failureCount.set(0);
        successCount.set(0);
    }

}

// 熔断器关闭、半开、打开
enum CircuitBreakerState {
    CLOSE, HALF_OPEN, OPEN
}