package hello.controller;


import hello.entity.User;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class AuthController {
    private UserDetailsService userDetailsService;
    private AuthenticationManager authenticationManager;


    public AuthController(UserDetailsService userDetailsService,
                          AuthenticationManager authenticationManager) {
        this.userDetailsService = userDetailsService;
        this.authenticationManager = authenticationManager;
    }

//    @Inject//这句代码意味着:Spring会知道我需要UserDetailsService这样的实例;
//    public AuthController(UserDetailsService userDetailsService) {
//        this.userDetailsService = userDetailsService;
//    }

    @GetMapping("/auth")
    @ResponseBody
    public Object auth() {
        return new Result("fail", "用户没有登录", false);
    }

    @PostMapping("/auth/login")
    @ResponseBody//可以将json数据限制于body里面
    public Result login(@RequestBody Map<String, Object> usernameAndPassword) {
        String username = usernameAndPassword.get("username").toString();
        String password = usernameAndPassword.get("password").toString();

        UserDetails userDetails = null;
        try{
        //去数据库里面获取真正的密码
         userDetails = userDetailsService.loadUserByUsername(username);
        }catch (UsernameNotFoundException e){
            return new Result("fail","用户不存在",false);
        }

        //把用户名和密码比对一下,看登陆的人是不是这个人!
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());

        try {
            authenticationManager.authenticate(token);
            SecurityContextHolder.getContext().setAuthentication(token);
            User loggerInUser = new User(1, "Zhangsan");
            return new Result("ok", "登录成功", true, loggerInUser);
        } catch (BadCredentialsException e) {
            return new Result("fail", "密码不正确", false);
        }

    }

    private static class Result {
        //假如成功或者不成功,会做下面的事(接口文档里面已经注明了),

        String status;
        String msg;
        boolean isLogin;
        Object data;

        public Result(String status, String msg, boolean isLogin) {
            this(status, msg, isLogin, null);
        }

        public Result(String status, String msg, boolean isLogin, Object data) {
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
}
