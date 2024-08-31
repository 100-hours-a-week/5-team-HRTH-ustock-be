package com.hrth.ustock.config;

import com.hrth.ustock.exception.common.CustomException;
import io.sentry.Sentry;
import io.sentry.SentryOptions;
import io.sentry.spring.jakarta.EnableSentry;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Profile("prod")
@EnableSentry(dsn = "https://f4549cec259eb3cf4977fbe8960b9405@o4507837261021184.ingest.us.sentry.io/4507837264035840")
@Configuration
class SentryConfiguration implements Sentry.OptionsConfiguration {
    @Override
    public void configure(SentryOptions sentryOptions) {
        sentryOptions.addIgnoredExceptionForType(CustomException.class);
        sentryOptions.addIgnoredExceptionForType(NoResourceFoundException.class);
    }
}
