package top.orosirian.server.ratelimit.provider;

import top.orosirian.server.ratelimit.RateLimit;
import top.orosirian.server.ratelimit.impl.TokenBucketRateLimitImpl;

import java.util.HashMap;
import java.util.Map;

public class RateLimitProvider {

    private Map<String, RateLimit> rateLimitMap = new HashMap<>();

    public RateLimit getRateLimit(String interfaceName) {
        RateLimit rateLimit = rateLimitMap.getOrDefault(interfaceName, new TokenBucketRateLimitImpl(100, 10));
        rateLimitMap.put(interfaceName, rateLimit);
        return rateLimit;
    }
    
}
