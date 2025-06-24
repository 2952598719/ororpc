package Server.ratelimit.impl;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import Server.ratelimit.RateLimit;

public class TokenBucketRateLimitImpl implements RateLimit {

    private final int tokenInterval;            // 每多久产生一个令牌

    private final int capacity;                 // 桶容量

    private final AtomicInteger tokenNum;       // 当前令牌数

    private final AtomicLong lastRefillTime;    // 上次生成令牌的时间

    public TokenBucketRateLimitImpl(int ratePerSecond, int capacity) {
        this.tokenInterval = 1000 / ratePerSecond;
        this.capacity = capacity;
        this.tokenNum = new AtomicInteger(capacity);
        this.lastRefillTime = new AtomicLong(System.currentTimeMillis());
    }

    @Override
    public boolean getToken() {
        refillToken();  // 懒惰生成令牌
        int currentTokenNum = tokenNum.get();
        while(currentTokenNum > 0) {    // CAS修改
            if(tokenNum.compareAndSet(currentTokenNum, currentTokenNum - 1)) {
                return true;
            }
            currentTokenNum = tokenNum.get();
        }
        return false;
    }

    private void refillToken() {
        
    }
    
}
