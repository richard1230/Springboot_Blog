package hello.entity;

public class Result {
    //假如成功或者不成功,会做下面的事(接口文档里面已经注明了),

    String status;
    String msg;
    boolean isLogin;
    Object data;

    public static Result success(String message, boolean isLogin, Object data) {
        return new Result("ok", message, isLogin, data);
    }

    public static Result failure(String message) {
        return new Result("fail", message, false);
    }

    private Result(String status, String msg, boolean isLogin) {
        this(status, msg, isLogin, null);
    }

    private Result(String status, String msg, boolean isLogin, Object data) {
        this.status = status;
        this.msg = msg;
        this.isLogin = isLogin;
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    public boolean isLogin() {
        return isLogin;
    }

    public Object getData() {
        return data;
    }
}
