package Server.ratelimit.provider;

import java.util.HashMap;
import java.util.Map;

import Server.ratelimit.RateLimit;
import Server.ratelimit.impl.TokenBucketRateLimitImpl;

public class RateLimitProvider {

    private Map<String, RateLimit> rateLimitMap = new HashMap<>();

    public RateLimit getRateLimit(String interfaceName) {
        RateLimit rateLimit = rateLimitMap.getOrDefault(interfaceName, new TokenBucketRateLimitImpl(100, 10));
        rateLimitMap.put(interfaceName, rateLimit);
        return rateLimit;
    }
    
}
