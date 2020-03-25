package hello.service;

import hello.entity.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService implements UserDetailsService {

    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Inject
    public UserService(BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        save("gebilaowang","gebilaowang");
    }

    //因为SpringBoot是多线程的,这里最好用ConcurrentHashMap
    private Map<String ,User> users = new ConcurrentHashMap<>();

    public void save(String username, String password){
        users.put(username,
                new User(1,username,bCryptPasswordEncoder.encode(password)));
    }

//    public String getPassword (String username){
//        return userPasswords.get(username);
//    }
//
//    public User getUserById(Integer id){
//    return null;
//    }

    public User getUserByUsername(String username){
        return users.get(username);
    }

    //注意这个函数的返回类型是一个接口,他不能实例化!这里需要用快捷键alt+command+B来看它有没有什么实现,找到一个官方的User
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if(!users.containsKey(username)){
            throw new UsernameNotFoundException(username + "不存在");
        }

        User user = users.get(username);

        return new org.springframework.security.core.userdetails.User(username,user.getEncryptedPassword(), Collections.emptyList());
    }


}
