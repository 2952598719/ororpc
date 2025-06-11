package common.Message;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RpcResponse implements Serializable {

    private int code;           // 状态码

    private String message;     // 对状态码的解释

    private Object data;

    public static RpcResponse success(Object data) {
        return RpcResponse.builder()
                            .code(200)
                            .data(data)
                            .build();
    }

    public static RpcResponse fail() {
        return RpcResponse.builder()
                            .code(500)
                            .message("服务器内部错误")
                            .build();
    }
    
}
