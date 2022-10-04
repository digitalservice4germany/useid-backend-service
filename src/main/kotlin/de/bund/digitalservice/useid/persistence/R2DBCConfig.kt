package de.bund.digitalservice.useid.persistence

import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement

@Configuration
@EnableR2dbcRepositories
@EnableR2dbcAuditing
@EnableTransactionManagement
internal class R2DBCConfig
