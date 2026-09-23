package es.unizar.webeng.hello.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.ui.Model
import org.springframework.ui.ExtendedModelMap
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.StaticMessageSource
import java.util.Locale

class HelloControllerUnitTests {
    private lateinit var controller: HelloController
    private lateinit var model: Model
    private lateinit var fixedClock: Clock
    private lateinit var messageSource: StaticMessageSource
    
    @BeforeEach
    fun setup() {
        fixedClock = Clock.fixed(Instant.parse("2026-09-17T09:00:00Z"), ZoneId.of("UTC"))
        messageSource = buildMessageSource()
        controller = HelloController("Test Message", fixedClock, messageSource)
        model = ExtendedModelMap()
        //Fix the locale for these tests, so it won't get the locale of the local machine
        LocaleContextHolder.setLocale(Locale.ENGLISH)
    }
    
    private fun buildMessageSource(): StaticMessageSource =
        StaticMessageSource().apply {
            addMessage("greeting.morning", Locale.ENGLISH, "Good morning")
            addMessage("greeting.afternoon", Locale.ENGLISH, "Good afternoon")
            addMessage("greeting.night", Locale.ENGLISH, "Good night")
            addMessage("greeting.morning", Locale("es"), "Buenos días")
            addMessage("greeting.afternoon", Locale("es"), "Buenas tardes")
            addMessage("greeting.night", Locale("es"), "Buenas noches")
        }

    @AfterEach
    fun tearDown() {
        LocaleContextHolder.resetLocaleContext()
    }
    
    @Test
    fun `should return welcome view with default message`() {
        val view = controller.welcome(model, "")
        
        assertThat(view).isEqualTo("welcome")
        assertThat(model.getAttribute("message")).isEqualTo("Test Message")
        assertThat(model.getAttribute("name")).isEqualTo("")
    }
    
    @Test
    fun `should return welcome view with personalized message`() {
        val view = controller.welcome(model, "Developer")
        
        assertThat(view).isEqualTo("welcome")
        assertThat(model.getAttribute("message")).isEqualTo("Good morning, Developer!")
        assertThat(model.getAttribute("name")).isEqualTo("Developer")
    }
    
    @Test
    fun `should return API response with timestamp`() {
        val apiController = HelloApiController(fixedClock, messageSource)
        val response = apiController.helloApi("Test")
        
        assertThat(response).containsKey("message")
        assertThat(response).containsKey("timestamp")
        assertThat(response["message"]).isEqualTo("Good morning, Test!")
        assertThat(response["timestamp"]).isNotNull()
    }
    
    @ParameterizedTest(name = "at {0}, the greeting should be \"{1}\"")
    @CsvSource(
        "2026-09-17T05:59:00Z, night",
        "2026-09-17T06:00:00Z, morning",
        "2026-09-17T12:59:00Z, morning",
        "2026-09-17T13:00:00Z, afternoon",
        "2026-09-17T20:59:00Z, afternoon",
        "2026-09-17T21:00:00Z, night",
        "2026-09-17T23:59:00Z, night",
        "2026-09-17T00:00:00Z, night"
    )
    fun `should return correct time-based greeting for personalized message`(instant: String, expectedGreeting: String) {
        val clockAtInstant = Clock.fixed(Instant.parse(instant), ZoneId.of("UTC"))
        val apiController = HelloApiController(clockAtInstant, messageSource)
        val testModel = ExtendedModelMap()

        val response = apiController.helloApi("Ana")

        assertThat(response["message"]).isEqualTo("Good $expectedGreeting, Ana!")
    }

    @Test
    fun `default message should not depend on time of day`() {
        val clockAtMidnight = Clock.fixed(Instant.parse("2026-09-17T00:00:00Z"), ZoneId.of("UTC"))
        val testController = HelloController("Test Message", clockAtMidnight, messageSource)
        val testModel = ExtendedModelMap()

        testController.welcome(testModel, "")

        assertThat(testModel.getAttribute("message")).isEqualTo("Test Message")
    }
    
    @Test
    fun `should return greeting in spanish when locale is es`() {
        LocaleContextHolder.setLocale(Locale("es"))
        val testModel = ExtendedModelMap()

        controller.welcome(testModel, "Ana")

        assertThat(testModel.getAttribute("message")).isEqualTo("Buenos días, Ana!")
    }

    @Test
    fun `should return greeting in english by default when no locale is given`() {
        val testModel = ExtendedModelMap()

        controller.welcome(testModel, "Ana")

        assertThat(testModel.getAttribute("message")).isEqualTo("Good morning, Ana!")
    }
}
