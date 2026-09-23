package es.unizar.webeng.hello.controller

import org.springframework.beans.factory.annotation.Value
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


@Controller
class HelloController(
    @param:Value($$"${app.message:Hello World}") private val message: String,
    private val clock: Clock = Clock.systemDefaultZone()
) {
    
    /**
     * Function mapped to the API route "/". 
     * This functions return the main webpage to be rendered to the browser to be loaded,
     * as well as passing the variables to be used in the html, replacing their refrences with
     * the values set in this function
     */
    @GetMapping("/")
    fun welcome(
        model: Model,
        @RequestParam(defaultValue = "") name: String
    ): String {
        val time = LocalTime.now(clock).hour
        var timeGreeting = when (time) {
            in 6..<13 -> "morning"
            in 13..<21 -> "afternoon"
            else -> "night"
        }
        val greeting = if (name.isNotBlank()) "Good $timeGreeting, $name!" else message
        model.addAttribute("message", greeting)
        model.addAttribute("name", name)
        return "welcome"
    }
}

@RestController
class HelloApiController (
    private val clock: Clock = Clock.systemDefaultZone()
) {
    
    /**
     * Function mapped to the API route "/api/hello". 
     * Every time this function is called, this function will return a string with the name of the user
     * and a greeting depending the hour of the day.
     * In case this route is called with the parameter "name" as empty or null, it will instead return
     * "Hello world!"
     */
    @GetMapping("/api/hello", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun helloApi(@RequestParam(defaultValue = "World") name: String): Map<String, String> {
        var time = LocalTime.now(clock).hour
        var greeting = when (time) {
            in 6..<13 -> "morning"
            in 13..<21 -> "afternoon"
            else -> "night"
        }
        return mapOf(
            "message" to "Good $greeting, $name!",
            "timestamp" to "$time"
        )
    }
}
