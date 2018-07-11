package org.springframework.social.cafe24.config.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Set;

public class UserInterceptor extends HandlerInterceptorAdapter {
    private static final Logger logger = LoggerFactory.getLogger(UserInterceptor.class);
    private final UsersConnectionRepository repository;

    private final UserCookieGenerator userCookieGenerator = new UserCookieGenerator();

    private static String CONTEXT_PATH;

    public UserInterceptor(UsersConnectionRepository connectionRepository) {
        this.repository = connectionRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        CONTEXT_PATH = request.getContextPath();
        logger.debug("UserInterceptor preHandle called ");
//        return super.preHandle(request, response, handler);
        String currUrl = CONTEXT_PATH + request.getServletPath();
        logger.debug("preHandle: " + currUrl);


        /* request로 전달되는 값 확인 */
        Map<String, String[]> params = request.getParameterMap();
        if (params.size() > 0) {
            Set<String> keys = params.keySet();
            for (String key : keys) {
                for (String value : params.get(key)) {
                    logger.debug("preHandle params.get( " + key + " ): " + value);
                }
            }
        } else {
            logger.debug("preHandle params.size() <= 0");
        }

        if (currUrl.contains("/admin/cafe24")){
            logger.debug("preHandle in " + CONTEXT_PATH + "/admin/cafe24");
            return true;

        }

        if (currUrl.contains("/admin/login")){
            logger.debug("preHandle in " + CONTEXT_PATH + "/admin/login");
            return true;

        }

        if (currUrl.contains("/admin/result")) {
            logger.debug("preHandle in " + CONTEXT_PATH + "/admin/result");
            return true;
        }

        response.sendRedirect(CONTEXT_PATH + "/admin/login");
        return false;



        /*String userId = userCookieGenerator.readCookieValue(request);
        logger.debug("preHandle userId: " + userId);
        if (userId != null) {
            logger.debug("preHandle userId != null");

            *//*  *//*
            if (!repository
                    .findUserIdsConnectedTo("cafe24", Collections.singleton(userId))
                    .isEmpty()) {
                logger.debug("preHandle userId Collections.singleton(userId) is Empty");

                logger.debug("loading user {} from cookie", userId);
                SecurityContext.setCurrentUser(new User(userId));
                return true;
            } else {
                logger.warn("user {} is not known!", userId);
                logger.debug("preHandle userId Collections.singleton(userId) is Not Empty");

                userCookieGenerator.removeCookie(response);
            }
        } else {
            *//* 실제 접속한 유저를 등록하는 과정 *//*
            userId = request.getParameter("mall_id");

            SecurityContext.setCurrentUser(new User(userId));
            logger.debug("preHandle userId == null");
            response.sendRedirect(CONTEXT_PATH + "/admin/cafe24");
            return false;

        }*/



    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        super.postHandle(request, response, handler, modelAndView);
    }
}
