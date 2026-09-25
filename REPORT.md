# Lab 1 Git Race -- Project Report

This note uses the same disclosure fields as the group-project **AI use (10%)** slice. Lab 1 is still **limited**: assistive GenAI only — not a full or substantial generated solution. The project will later expect agents plus `AGENTS.md` and one skill; you do **not** need those here.

Do not invent a percentage of “AI vs original” lines. Empty or fake disclosure fails this lab.

## What I specified

The main things I've decided to add were mainly a greeting that varies depending the time of the day as well as
being able to change what language you wanted to be greeted (English and Spanish).
To make sure it works, I would modify and add new tests to ensure these new features follow the intended behavior, as well as
checking manually that there additions worked correctly on the webpage.

# What I changed

To add the time dependent greeting, I've modified these files:
```
src/main/kotlin/controller/HelloController.kt
src/test/kotlin/controller/HelloControllerUnitTests.kt
src/test/kotlin/controller/HelloControllerMVCTests.kt
src/test/kotlin/IntegrationTest.kt
```

The main thing added to the controller is to simply obtain the current time of the machine and depending of the time of day, give a different greeting.
The tests were changed to adapt to the new behavior, as well as adding new test to check the new functionality works as expected

To add language dependent greetings, I've modified all the previos files mentioned, as well as adding there files:
```
src/main/kotlin/config/WebConfig.kt
src/main/application.properties
src/main/message_es.properties
src/main/message.properties
```
These were added as they were required to configure Springboot to allow different languages

## Technical decisions

As the features that I've decided to add didn't require extensive API modifications, no major API changes were done, only changing the values that it gives.
The tests did require major modifications, as it requires testing depending the time of the day. To ease up testing, I've made so the classes would have an
additional attribute that by default would use the system's clock, but you can changed it by setting a fixed clock, useful for testing.
There is also many edge cases to test too, so I've used parametrized tests to avoid repeating the same tests just with different values.
Adding the ability to change the language you get greeted, there has been modifications to the SpringBoot configuration, mainly about the Locale
I've also added the messages.properties depending so the strings become variables and can change atomically due to the locale.

## How I verified

To check the application's correctness I've always used `./gradlew check` to verify the API always returned the correct answer.
The first time it was run after the first additions (adding time dependent greetings) it failed due to the tests not being adapted to the new answers. To fix it, I've
changed the test to take into account the clock, as well as adding additional test to ensure it changed correctly. After that the tests passed.
When adding the different language greetings and adding the tests, the tests failed. After checking what could be failing, I've decided to change on how the API 
gets the Locale, changing from getting it as a parameter to just using the .getLocale() method. After that, the tests passed.

## AI disclosure

- **Tools / skills:** Claude Sonnet 5
- **Purpose:** To assist in adding the locale feature and doing some tests
- **Representative prompts:** "Can you check why these test failed", "I'd like to add new tests that ensure the following characteristics works as intended", "How do I change the greeting depending of the language chosen"
- **Affected files/sections:** HelloControllerUnitTests.kt, HelloControllerMVCTests.kt, WebConfig.kt, HelloController.kt
- **Validation steps:** First I've read if the suggestions or code made sense, then ran `./gradlew check` to see if it works as intended
- **Citations:** Some of the sourced used to help me out in the making of this lab:
  - https://docs.oracle.com/javase/8/docs/api/java/time/Clock.html
  - https://www.baeldung.com/spring-boot-internationalization
- **Human-reviewed:** Before integrating the code into the program, I've first checked if the code made sense and adapted correctly to what I've had at that moment

