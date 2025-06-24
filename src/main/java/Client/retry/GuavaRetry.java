package Client.retry;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.github.rholder.retry.Attempt;
import com.github.rholder.retry.RetryListener;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;

import Client.rpcClient.RpcClient;
import common.Message.RpcRequest;
import common.Message.RpcResponse;

public class GuavaRetry {

    private RpcClient client;

    public RpcResponse sendServiceWithRetry(RpcRequest request, RpcClient client) {
        this.client = client;
        RetryerBuilder<RpcResponse> builder = RetryerBuilder.newBuilder();
        Retryer<RpcResponse> retryer = builder
                                        .retryIfException()     // 出现异常则重试
                                        .retryIfResult(response -> Objects.equals(response.getCode(), 500))     // 返回500状态码则重试
                                        .withWaitStrategy(WaitStrategies.fixedWait(2, TimeUnit.SECONDS))    // 每次重试先等待2s
                                        .withStopStrategy(StopStrategies.stopAfterAttempt(3))           //最多重试3次
                                        .withRetryListener(new RetryListener() {    // 重试监听器，每次重试打印日志
                                            @Override
                                            public <V> void onRetry(Attempt<V> attempt) {
                                                System.out.println("RetryListener: 第" + attempt.getAttemptNumber() + "次调用");
                                            }
                                        })
                                        .build();
        try {
            return retryer.call(() -> client.sendRequest(request));
        } catch(Exception e) {
            e.printStackTrace();
        }
        return RpcResponse.fail();
    }
    
}
