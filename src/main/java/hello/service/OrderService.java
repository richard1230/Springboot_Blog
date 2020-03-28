package hello.service;

import javax.inject.Inject;

public class OrderService {
    //    @Resource
//    @Autowired//这两种方式都是声明依赖的,即:OrderService是依赖UserService的
    private UserService userService;

    @Inject
    public OrderService(UserService userService) {
        this.userService = userService;
    }
}
