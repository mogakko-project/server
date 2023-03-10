package com.example.mogakko.global.interceptor;

import com.example.mogakko.domain.user.controller.SessionConst;
import com.example.mogakko.domain.user.exception.UnauthorizedException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        System.out.println("requestURI = " + requestURI);
        HttpSession session = request.getSession();
        System.out.println("session = " + session);
//        System.out.println("interceptor! session.getAttribute(SessionConst.LOGIN_USER) = " + session.getAttribute(SessionConst.LOGIN_USER));

        if (session == null || session.getAttribute(SessionConst.LOGIN_USER) == null) {
            throw new UnauthorizedException("로그인 후 이용 가능합니다.");
        }
        System.out.println("session.getAttribute(SessionConst.LOGIN_USER) = " + session.getAttribute(SessionConst.LOGIN_USER));
        return true;
    }
}
