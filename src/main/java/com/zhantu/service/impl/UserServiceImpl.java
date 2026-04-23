package com.zhantu.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhantu.entity.User;
import com.zhantu.mapper.UserMapper;
import com.zhantu.service.UserService;
import com.zhantu.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public String register(String nickname, String phone, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        if (this.count(wrapper) > 0) {
            throw new RuntimeException("手机号已注册");
        }

        User user = new User();
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname);
        user.setRole("USER");
        this.save(user);

        return jwtUtil.generateToken(user.getId(), user.getPhone(), user.getRole());
    }

    @Override
    public java.util.Map<String, Object> login(String phone, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        User user = this.getOne(wrapper);
        
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用，请联系管理员");
        }
        
        if ("ADMIN".equals(user.getRole())) {
            throw new RuntimeException("管理员账号请使用管理后台登录");
        }
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        String token = jwtUtil.generateToken(user.getId(), user.getPhone(), user.getRole());
        
        User safeUser = new User();
        safeUser.setId(user.getId());
        safeUser.setPhone(user.getPhone());
        safeUser.setNickname(user.getNickname());
        safeUser.setAvatar(user.getAvatar());
        safeUser.setRole(user.getRole());
        safeUser.setCreateTime(user.getCreateTime());
        
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("token", token);
        result.put("user", safeUser);
        return result;
    }

    @Override
    public User getCurrentUser(Long userId) {
        User user = this.getById(userId);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }
}
