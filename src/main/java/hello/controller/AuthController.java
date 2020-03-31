package hello.controller;


import hello.entity.Result;
import hello.entity.User;
import hello.service.UserService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Map;

@Controller
public class AuthController {
    private UserService userService;
    private AuthenticationManager authenticationManager;

    @Inject//这里推荐的是构造器注入
    public AuthController(UserService userService,
                          AuthenticationManager authenticationManager) {
        this.userService = userService;
//        this.userDetailsService = userDetailsService;
        this.authenticationManager = authenticationManager;
    }


    @GetMapping("/auth")//这个接口用于判断用户登录状态
    @ResponseBody
    public Object auth() {

//        String userName = SecurityContextHolder.getContext().getAuthentication().getName();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        //用户状态的维持是通过cookies
        User loggedInUser = userService.getUserByUsername(authentication == null ? null : authentication.getName());
        if (loggedInUser == null) {
            return Result.failure("user is not login");
//            return new Result("fail", "用户没有登录", false);
        } else {
            return Result.success(null, true, loggedInUser);
//            return new Result("ok", null, true, loggedInUser);
        }
    }


    @GetMapping("/auth/logout")
    @ResponseBody
    public Object logout() {

        String userName = SecurityContextHolder.getContext().getAuthentication().getName();

        User loggedInUser = userService.getUserByUsername(userName);
        if (loggedInUser == null) {
            //static factory method
            return Result.failure("user is not login");
        } else {
            //登出就是将其上下文状态清掉
            SecurityContextHolder.clearContext();//需要google一下
            return Result.success("logout success", false, null);
//            return new Result("ok", "logout success", false);
        }
    }

    @PostMapping("/auth/register")
    @ResponseBody//可以将json数据限制于body里面
    public Result register(
            @RequestBody Map<String, String> usernameAndPassword
    ) {
        String username = usernameAndPassword.get("username");
        String password = usernameAndPassword.get("password");
        if (username == null || password == null) {
            return Result.failure("username/password == null");
        }

        if (username.length() < 1 || username.length() > 15) {
            return Result.failure("invalid username");
        }

        if (password.length() < 6 || password.length() > 16) {
            return Result.failure("invalid password");
        }

        try {
            userService.save(username, password);
        } catch (DuplicateKeyException e) {
            return Result.failure("user alreadly exists");
        }
        return Result.success("success!!!", false, null);
//        return new Result("ok","success!!!",false);


    }


    @PostMapping("/auth/login")
    @ResponseBody//可以将json数据限制于body里面
    public Result login(@RequestBody Map<String, Object> usernameAndPassword) {
        String username = usernameAndPassword.get("username").toString();
        String password = usernameAndPassword.get("password").toString();

        UserDetails userDetails;
        try {
            //去数据库里面获取真正的密码
            userDetails = userService.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            return Result.failure("用户不存在");
        }

        //把用户名和密码比对一下,看登陆的人是不是这个人!
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());

        try {
            authenticationManager.authenticate(token);
            //把用户信息保存在一个地方
            //cookies
            SecurityContextHolder.getContext().setAuthentication(token);
//            User loggerInUser = new User(1, "Zhangsan");
            return Result.success("登陆成功", true, userService.getUserByUsername(username));
//            return new Result("ok", "登录成功", true, userService.getUserByUsername(username));
        } catch (BadCredentialsException e) {
            return Result.failure("密码不正确");
        }

    }


}
