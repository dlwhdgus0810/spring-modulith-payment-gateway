package me.hyunlee.laundry

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.modulith.core.ApplicationModules

@SpringBootTest
class LaundryApplicationTests {

    @Test
    fun `verify modular boundaries`() {
        val modules = ApplicationModules.of(Application::class.java)
        modules.verify()
    }

}
