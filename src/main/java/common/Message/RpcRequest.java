package common.Message;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RpcRequest implements Serializable {

    private String interfaceName;   // 服务类的名字

    private String methodName;      // 调用的方法名

    private Object[] params;        // 参数列表

    private Class<?>[] paramsType;  // 参数类型
    
}
