package de.bund.digitalservice.useid.statics

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import reactor.core.publisher.Mono

@Controller
class HomeController {
    @GetMapping("/")
    fun home(): Mono<String> = Mono.just("redirect:https://digitalservice.bund.de")
}