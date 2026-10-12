package vn.edu.ptit.fooddelivery.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import org.springframework.lang.NonNull;

import java.io.IOException;

/**
 * Cấu hình phục vụ giao diện React Single Page Application (SPA) từ Spring Boot.
 * Bất kỳ client route nào (như /customer, /restaurant, /driver, /checkout)
 * khi tải lại trang sẽ tự động forward về index.html của React Router,
 * trong khi các API (/api/**) vẫn được định tuyến chính xác đến REST Controllers.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(@NonNull String resourcePath, @NonNull Resource location) throws IOException {
                        Resource requestedResource = location.createRelative(resourcePath);
                        if (requestedResource.exists() && requestedResource.isReadable()) {
                            return requestedResource;
                        }
                        // Không chặn /api để Spring MVC xử lý các REST controller
                        if (resourcePath.startsWith("api/") || resourcePath.equals("api")) {
                            return null;
                        }
                        return location.createRelative("index.html");
                    }
                });
    }
}
