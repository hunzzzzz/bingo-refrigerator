package team.b2.bingojango

import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import java.util.*

@SpringBootApplication
@EnableJpaAuditing
class BingoJangoApplication

fun main(args: Array<String>) {
    runApplication<BingoJangoApplication>(*args)
}

@PostConstruct
fun changeTimeKST() {
    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"))
}