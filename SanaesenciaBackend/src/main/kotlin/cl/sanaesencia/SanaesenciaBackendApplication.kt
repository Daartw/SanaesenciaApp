package cl.sanaesencia

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class SanaesenciaBackendApplication

fun main(args: Array<String>) {
    runApplication<SanaesenciaBackendApplication>(*args)
}