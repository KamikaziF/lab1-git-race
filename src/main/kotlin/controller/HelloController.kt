package es.unizar.webeng.hello.controller

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Clock
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.ZoneOffset
import java.util.Locale



@Controller
class HelloController(
    @param:Value($$"${app.message:Hello World}") private val message: String,
    private val clock: Clock = Clock.systemDefaultZone(),
    private val messageSource: MessageSource
) {
    
    /**
     * Function mapped to the API route "/". 
     * This functions return the main webpage to be rendered to the browser to be loaded,
     * as well as passing the variables to be used in the html, replacing their refrences with
     * the values set in this function
     * The greeting is translated depending what Locale is being selected by LocaleResolver 
     */
    @GetMapping("/")
    fun welcome(
        model: Model,
        @RequestParam(defaultValue = "") name: String,
    ): String {
        val time = clock.instant().atZone(ZoneOffset.UTC).hour
        var periodKey = when (time) {
            in 6..<13 -> "greeting.morning"
            in 13..<21 -> "greeting.afternoon"
            else -> "greeting.night"
        }
        val period = messageSource.getMessage(periodKey, null, LocaleContextHolder.getLocale())
        val greeting = if (name.isNotBlank()) "$period, $name!" else message
        model.addAttribute("message", greeting)
        model.addAttribute("name", name)
        return "welcome"
    }
}

@RestController
class HelloApiController (
    private val clock: Clock = Clock.systemDefaultZone(),
    private val messageSource: MessageSource
) {
    
    /**
     * Function mapped to the API route "/api/hello". 
     * Every time this function is called, this function will return a string with the name of the user
     * and a greeting depending the hour of the day.
     * In case this route is called with the parameter "name" as empty or null, it will instead return
     * "Hello world!"
     * The greeting is translated depending what Locale is being selected by LocaleResolver 
     */
    @GetMapping("/api/hello", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun helloApi(
        @RequestParam(defaultValue = "World") name: String
    ): Map<String, String> {
        val hour = clock.instant().atZone(ZoneOffset.UTC).hour
        var periodKey  = when (hour) {
            in 6..<13 -> "greeting.morning"
            in 13..<21 -> "greeting.afternoon"
            else -> "greeting.night"
        }
        val period = messageSource.getMessage(periodKey, null, LocaleContextHolder.getLocale())
        return mapOf(
            "message" to "$period, $name!",
            "timestamp" to "$hour"
        )
    }
}
