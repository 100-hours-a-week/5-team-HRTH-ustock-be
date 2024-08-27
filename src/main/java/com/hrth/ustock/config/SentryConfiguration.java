package com.hrth.ustock.config;

import io.sentry.spring.jakarta.EnableSentry;
import org.springframework.context.annotation.Configuration;

@EnableSentry(dsn = "https://f4549cec259eb3cf4977fbe8960b9405@o4507837261021184.ingest.us.sentry.io/4507837264035840")
@Configuration
class SentryConfiguration {
}
