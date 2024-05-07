
package com.example.whatsup.aspect;

import com.example.whatsup.service.UserActivityService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.example.whatsup.utils.AuthUtils.getAuthenticatedUser;

@Aspect
@Component
public class UserActivityAspect {
    @Autowired
    private UserActivityService userActivityService;

    @Pointcut("@annotation(com.example.whatsup.aspect.TrackUserActivity)")
    public void controllerMethods() {
    }

    @AfterReturning(pointcut = "controllerMethods()", returning = "result")
    public void logUserActivity(JoinPoint joinPoint, Object result) {
        String username = getAuthenticatedUser().getUsername();
        userActivityService.trackUser(username);
    }
}
