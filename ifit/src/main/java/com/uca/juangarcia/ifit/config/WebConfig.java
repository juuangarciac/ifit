package com.uca.juangarcia.ifit.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración de recursos estáticos.
 *
 * <p>Expone las imágenes del catálogo de ejercicios en la ruta
 * {@code /exercise-images/**} mapeada desde {@code classpath:/exercises/}.
 *
 * <p>Si renombras la carpeta de imágenes, actualiza únicamente
 * el valor de {@code IMAGES_CLASSPATH}.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final String IMAGES_URL_PATTERN  = "/exercise-images/**";
    private static final String IMAGES_CLASSPATH     = "classpath:/exercises/";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(IMAGES_URL_PATTERN)
                .addResourceLocations(IMAGES_CLASSPATH);
    }
}
