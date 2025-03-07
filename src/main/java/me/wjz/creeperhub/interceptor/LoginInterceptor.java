package me.wjz.creeperhub.interceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.wjz.creeperhub.constant.ErrorType;
import me.wjz.creeperhub.entity.Result;
import me.wjz.creeperhub.entity.Token;
import me.wjz.creeperhub.exception.CreeperException;
import me.wjz.creeperhub.service.RedisService;
import me.wjz.creeperhub.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Autowired
    private TokenService tokenService;
    @Autowired
    private RedisService redisService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = null;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("token")) {
                token = cookie.getValue();
                break;
            }
        }
        //校验token
        if (token == null || token.isEmpty() || !tokenService.validateToken(token)) {
            throw new CreeperException(Result.error(ErrorType.UN_LOGIN));
        }
        //更新token有效期
        redisService.expire(TokenService.TOKEN_PREFIX + token, 60 * 60 * 24 * 30, TimeUnit.SECONDS);

        //用户存在，放行
        return true;
    }
}
