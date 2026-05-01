package dev.lueem.shared.config

import jakarta.enterprise.context.ApplicationScoped
import org.eclipse.microprofile.config.inject.ConfigProperty

@ApplicationScoped
class CategorizationProperties {
    @ConfigProperty(name = "embedding.service.url", defaultValue = "http://localhost:8000")
    lateinit var serviceUrl: String
}
