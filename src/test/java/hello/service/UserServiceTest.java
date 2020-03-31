package hello.service;

import hello.entity.User;
import hello.mapper.UserMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    BCryptPasswordEncoder mockEncoder;

    @Mock
    UserMapper mockMapper;

    //进行单元测试可能需要一些'假的'服务,这时候就需要mock服务了
    //mock就是创造一个假的服务/类;
    @InjectMocks
    UserService userService;

    @Test
    public void testSave() {
        //调用userService
        //验证userService将请求转发给了userMapper

        //given:给定一个条件,
        when(mockEncoder.encode("myPassword")).thenReturn("myEncodedPassword");

        //when:当逻辑被执行
        userService.save("myUser", "myPassword");

        //then:
        verify(mockMapper).save("myUser", "myEncodedPassword");
    }

    @Test
    public void testGetUserByUsername() {
        userService.getUserByUsername("myUser");

        verify(mockMapper).findUserByUsername("myUser");

    }

    @Test
    public void throwExceptionWhenUserNotFound() {
        //当UserMapper找不到用户的时候
//        Mockito.when(mockMapper.findUserByUsername("myUser")).thenReturn(null);

        //保证这个调用( ()->userService.loadUserByUsername("myUser"))
        // 一定会丢出UsernameNotFoundException异常
        Assertions.assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("myUser"));

    }

    @Test
    public void returnUserDetailsWhenUserFound() {
        //当我使用myUser调用getUserByUsername(findUserByUsername)方法的时候
        //希望返回这样的一个User: User(123,"myUser","myEncodedPassword"))
        when(mockMapper.findUserByUsername("myUser"))
                .thenReturn(new User(123, "myUser", "myEncodedPassword"));

        UserDetails userDetails = userService.loadUserByUsername("myUser");

        //断言操作
        //预期得到的结果myUser,实际得到的结果userDetails.getUsername()
        Assertions.assertEquals("myUser", userDetails.getUsername());
        Assertions.assertEquals("myEncodedPassword", userDetails.getPassword());

    }
}