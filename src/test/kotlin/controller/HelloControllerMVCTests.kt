package es.unizar.webeng.hello.controller

import org.hamcrest.CoreMatchers.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.Clock
import java.time.Instant
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.mock.web.MockHttpSession

@WebMvcTest(HelloController::class, HelloApiController::class)
class HelloControllerMVCTests {
    @Value($$"${app.message:Welcome to the Modern Web App!}")
    private lateinit var message: String

    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @MockitoBean
    private lateinit var clock: Clock
    
    @BeforeEach
    fun setupClock() {
        // Fijamos la hora a las 09:00 AM
        val fixedInstant = Instant.parse("2026-09-17T09:00:00Z")
        `when`(clock.instant()).thenReturn(fixedInstant)
    }

    @Test
    fun `should return home page with default message`() {
        mockMvc.perform(get("/"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(view().name("welcome"))
            .andExpect(model().attribute("message", equalTo(message)))
            .andExpect(model().attribute("name", equalTo("")))
    }
    
    @Test
    fun `should return home page with personalized message`() {
        mockMvc.perform(get("/").param("name", "Developer"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(view().name("welcome"))
            .andExpect(model().attribute("message", equalTo("Good morning, Developer!")))
            .andExpect(model().attribute("name", equalTo("Developer")))
    }
    
    @Test
    fun `should return API response as JSON`() {
        mockMvc.perform(get("/api/hello").param("name", "Test"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.message", equalTo("Good morning, Test!")))
            .andExpect(jsonPath("$.timestamp").exists())
    }
    
    @ParameterizedTest(name = " at {0} the greeting should be \"{1}\"")
    @CsvSource(
        "2026-09-17T05:59:00Z, night",
        "2026-09-17T06:00:00Z, morning",
        "2026-09-17T12:59:00Z, morning",
        "2026-09-17T13:00:00Z, afternoon",
        "2026-09-17T20:59:00Z, afternoon",
        "2026-09-17T21:00:00Z, night",
        "2026-09-17T23:59:00Z, night"
    )
    fun `should return time-based greeting on personalized home page`(instant: String, expectedGreeting: String) {
        `when`(clock.instant()).thenReturn(Instant.parse(instant))

        mockMvc.perform(get("/").param("name", "Ana"))
            .andExpect(status().isOk)
            .andExpect(model().attribute("message", equalTo("Good $expectedGreeting, Ana!")))
    }

    @ParameterizedTest(name = "at {0} /api/hello should be greeting \"{1}\"")
    @CsvSource(
        "2026-09-17T05:59:00Z, night",
        "2026-09-17T06:00:00Z, morning",
        "2026-09-17T12:59:00Z, morning",
        "2026-09-17T13:00:00Z, afternoon",
        "2026-09-17T20:59:00Z, afternoon",
        "2026-09-17T21:00:00Z, night",
        "2026-09-17T23:59:00Z, night"
    )
    fun `should return time-based greeting from API endpoint`(instant: String, expectedGreeting: String) {
        `when`(clock.instant()).thenReturn(Instant.parse(instant))

        mockMvc.perform(get("/api/hello").param("name", "Ana"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message", equalTo("Good $expectedGreeting, Ana!")))
    }
    
    @Test
    fun `should return home page in spanish when lang=es`() {
        mockMvc.perform(get("/").param("name", "Ana").param("lang", "es"))
            .andExpect(status().isOk)
            .andExpect(model().attribute("message", equalTo("Buenos días, Ana!")))
    }

    @Test
    fun `should return API response in spanish when lang=es`() {
        mockMvc.perform(get("/api/hello").param("name", "Ana").param("lang", "es"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message", equalTo("Buenos días, Ana!")))
    }

    @Test
    fun `should fall back to english when requested language is not supported`() {
        mockMvc.perform(get("/").param("name", "Ana").param("lang", "fr"))
            .andExpect(status().isOk)
            .andExpect(model().attribute("message", equalTo("Good morning, Ana!")))
    }

    @Test
    fun `should keep chosen language across requests without repeating lang param`() {
        val result = mockMvc.perform(get("/").param("name", "Ana").param("lang", "es"))
            .andExpect(model().attribute("message", equalTo("Buenos días, Ana!")))
            .andReturn()

        val session = result.request.session as MockHttpSession

        mockMvc.perform(get("/").param("name", "Ana").session(session))
            .andExpect(model().attribute("message", equalTo("Buenos días, Ana!")))
    }
}

