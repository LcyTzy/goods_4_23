package com.zhantu.controller;

import com.zhantu.common.Result;
import com.zhantu.entity.User;
import com.zhantu.service.UserService;
import com.zhantu.util.CaptchaUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "用户模块")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/captcha")
    @Operation(summary = "获取图片验证码")
    public Result<Map<String, String>> getCaptcha(HttpSession session) {
        CaptchaUtil.CaptchaResult captcha = CaptchaUtil.generateCaptcha();
        session.setAttribute("captcha", captcha.getCode());
        Map<String, String> result = new HashMap<>();
        result.put("image", "data:image/jpeg;base64," + captcha.getImage());
        return Result.success(result);
    }

    @PostMapping("/register")
    @Operation(summary = "注册")
    public Result<String> register(@RequestBody Map<String, String> registerRequest, HttpSession session) {
        String nickname = registerRequest.get("nickname");
        String phone = registerRequest.get("phone");
        String password = registerRequest.get("password");
        String captcha = registerRequest.get("captcha");

        String storedCaptcha = (String) session.getAttribute("captcha");
        if (storedCaptcha == null || !storedCaptcha.equalsIgnoreCase(captcha)) {
            throw new RuntimeException("验证码错误");
        }
        session.removeAttribute("captcha");

        String token = userService.register(nickname, phone, password);
        return Result.success(token);
    }

    @PostMapping("/login")
    @Operation(summary = "登录")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        String phone = loginRequest.get("phone");
        String password = loginRequest.get("password");
        Map<String, Object> result = userService.login(phone, password);
        return Result.success(result);
    }

    @GetMapping("/info")
    @Operation(summary = "获取当前用户信息")
    public Result<User> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getCurrentUser(userId);
        return Result.success(user);
    }
}
