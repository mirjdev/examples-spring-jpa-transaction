package com.mirjdev.examplesspring.aop;

import com.mirjdev.examplesspring.controller.ClientController;
import com.mirjdev.examplesspring.controller.IsolationLevelController;
import com.mirjdev.examplesspring.dto.ClientCreateRequest;
import org.aspectj.lang.annotation.Pointcut;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;

import static org.assertj.core.api.Assertions.assertThat;

class ClientLoggingPrivacyTest {

    @ParameterizedTest
    @ValueSource(strings = {"adsServicePointcut", "adsExceptionPointcut"})
    void excludesClientPersonalDataAndPreservesExistingControllerLogging(String methodName) throws Exception {
        var annotation = LoggingAspect.class.getMethod(methodName).getAnnotation(Pointcut.class);
        var pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression(annotation.value());

        assertThat(pointcut.matches(ClientController.class.getMethod("create", ClientCreateRequest.class),
                ClientController.class)).isFalse();
        assertThat(pointcut.matches(IsolationLevelController.class.getMethod("generate"),
                IsolationLevelController.class)).isTrue();
    }
}
