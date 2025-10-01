package xpenshare.controller;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import jakarta.validation.Valid;
import xpenshare.model.dto.user.*;
import xpenshare.service.UserService;

import io.micronaut.security.annotation.Secured;
import static io.micronaut.security.rules.SecurityRule.IS_ANONYMOUS;

@Controller("/api/users")
public class UserController {

    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Secured(IS_ANONYMOUS)
    @Post
    public HttpResponse<UserDto> createUser(@Body @Valid CreateUserRequest request) {
        return HttpResponse.created(userService.createUser(request));
    }

    @Secured(IS_ANONYMOUS)
    @Get("/{userId}")
    public HttpResponse<UserDto> getUser(Long userId) {
        return HttpResponse.ok(userService.getUser(userId));
    }
}