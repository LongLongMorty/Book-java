package com.bit.dbs.config;

import com.bit.dbs.utils.UploadUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * @author fanhongtao
 * 2022/10/13 15:31
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    //设置跨域
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    //添加静态资源映射：上传目录 + classpath 内置静态资源
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadLocation = new File(UploadUtils.getUploadRoot(), "static").toURI().toString();
        if (!uploadLocation.endsWith("/")) {
            uploadLocation += "/";
        }
        registry.addResourceHandler("/static/**")
                .addResourceLocations(uploadLocation, "classpath:/static/");
    }
}
