package io.aerosend.web.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Local development only: sets the customerId request attribute that the controllers require.
 * This must stay behind the local profile. Outside it the attribute stays unset and those routes
 * return 400, which is safer than every signed-in user seeing one customer's domains.
 */
@Configuration
@Profile("local")
public class LocalCustomerConfig implements WebMvcConfigurer {

    private final long customerId;

    public LocalCustomerConfig(@Value("${aerosend.local.customer-id}") long customerId) {
        this.customerId = customerId;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                request.setAttribute("customerId", customerId);
                return true;
            }
        });
    }
}
