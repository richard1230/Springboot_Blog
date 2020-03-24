package hello;

import hello.entity.User;
import hello.service.UserService;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
public class HelloController {

    private UserService userService;

//    @Inject
    public HelloController(UserService userService) {
        this.userService = userService;
    }



    @RequestMapping("/")
    public User index() {
        return this.userService.getUserById(1);
    }

}