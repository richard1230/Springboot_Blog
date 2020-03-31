package hello.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hello.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
class AuthControllerTest {

    private MockMvc mvc;

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationManager authenticationManager;

    //和真是项目里面的加密器一样
    private BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();


    @BeforeEach
    void setUp() {
        //这里我只测AuthController这么一个Controller(),其他不关心;
        mvc = MockMvcBuilders.standaloneSetup(new AuthController(userService, authenticationManager)).build();
    }

    @Test
    void returnNotLoginByDefault() throws Exception {
        //mvc执行这么一个请求(/auth),并期望返回这200状态
        mvc.perform(get("/auth")).andExpect(status().isOk())
                .andExpect(Result -> {
                    Assertions.assertTrue(Result.getResponse().getContentAsString().contains("user is not login"));
//                System.out.println(Result.getResponse().getContentAsString());
                });
    }

    @Test
    void testLogin() throws Exception {
        /**
         * 未登录的时候,返回未登录状态
         */
        mvc.perform(get("/auth")).andExpect(status().isOk())
                .andExpect(result -> {
                    Assertions.assertTrue(result.getResponse().getContentAsString().contains("user is not login"));
//                System.out.println(Result.getResponse().getContentAsString());
                });

        //使用/auth/login登陆,如果没有下面的自动化测试,就要使用postman来手动进行测试
        Map<String, String> usernamePassword = new HashMap<>();
        usernamePassword.put("username", "MyUser");
        usernamePassword.put("password", "MyPassword");

        Mockito.when(userService.loadUserByUsername("MyUser")).thenReturn(new User("MyUser", bCryptPasswordEncoder.encode("MyPassword"), Collections.emptyList()));
        Mockito.when(userService.getUserByUsername("MyUser")).thenReturn(new hello.entity.User(123, "MyUser", bCryptPasswordEncoder.encode("MyPassword")));

        System.out.println(new ObjectMapper().writeValueAsString(usernamePassword));

        //post请求,
        MvcResult response = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(new ObjectMapper().writeValueAsString(usernamePassword)))//content里面的是json
                .andExpect(status().isOk())
                .andExpect(result -> Assertions.assertTrue(result.getResponse().getContentAsString().contains("登陆成功")))
                .andReturn();

//        System.out.println(Arrays.toString(response.getResponse().getCookies()));
        //logout或者把浏览器关掉才会表示一个会话结束
        HttpSession session = response.getRequest().getSession();

        //再次检查/auth的返回值,处于登陆状态
        mvc.perform(get("/auth").session((MockHttpSession) session)).andExpect(status().isOk())
                .andExpect(result -> {
                    System.out.println(result.getResponse().getContentAsString());
                    Assertions.assertTrue(result.getResponse().getContentAsString().contains("MyUser"));
//                System.out.println(Result.getResponse().getContentAsString());
                });

    }
}