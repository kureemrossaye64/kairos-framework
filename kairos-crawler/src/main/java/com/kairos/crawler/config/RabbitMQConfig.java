package com.kairos.crawler.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String URL_QUEUE_NAME = "kairos.crawler.url.queue";

    @Bean
    public Queue urlQueue() {
        // A durable queue will survive broker restarts.
        return new Queue(URL_QUEUE_NAME, true);
    }

    /**
     * A message converter that automatically serializes/deserializes objects to/from JSON.
     * This allows us to send rich message objects instead of just strings.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}