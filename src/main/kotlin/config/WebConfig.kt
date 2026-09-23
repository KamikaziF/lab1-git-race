package es.unizar.webeng.hello.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor
import org.springframework.web.servlet.i18n.SessionLocaleResolver
import java.util.Locale


class WebConfig : WebMvcConfigurer {
    
    @Bean
    fun localeResolver(): LocaleResolver = SessionLocaleResolver().apply { setDefaultLocale(Locale.ENGLISH) }
    
    @Bean
    fun localeChangeInterceptor(): LocaleChangeInterceptor = LocaleChangeInterceptor().apply { paramName = "lang" }

    override fun addInterceptors(registry: InterceptorRegistry) {registry.addInterceptor(localeChangeInterceptor())}
    
}