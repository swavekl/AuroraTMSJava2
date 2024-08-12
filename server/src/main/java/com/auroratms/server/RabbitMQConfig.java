package com.auroratms.server;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // default topic exchange used for binding topic queues generated on the fly for subscribers
    public static final String DEFAULT_TOPIC_EXCHANGE = "amq.topic";

    // values to use for RabbitMQ message broker
    @Value("${message.broker.rabbitmq.host}")
    private String rabbitMQHost;

    @Value("${message.broker.rabbitmq.port}")
    private Integer rabbitMQPort;

    @Value("${message.broker.rabbitmq.login}")
    private String rabbitMQLogin;

    @Value("${message.broker.rabbitmq.passcode}")
    private String rabbitMQPasscode;

    @Bean
    public CachingConnectionFactory connectionFactory() {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
		String addresses = String.format("%s:%s", rabbitMQHost, rabbitMQPort);
		cachingConnectionFactory.setAddresses(addresses);
        cachingConnectionFactory.setUsername(rabbitMQLogin);
        cachingConnectionFactory.setPassword(rabbitMQPasscode);
        return cachingConnectionFactory;
    }

    @Bean
    public RabbitAdmin amqpAdmin() {
        return new RabbitAdmin(connectionFactory());
    }

    /**
     * Message converter for proper conversion of messages to JSON string
     * @return
     */
    @Bean
    public Jackson2JsonMessageConverter messageConverter () {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Defined to set the message converter in the RabbitTemplated used for sending messages
     * @param messageConverter
     * @return
     */
    @Bean
    public RabbitTemplate getTemplate(Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(DEFAULT_TOPIC_EXCHANGE);
    }
}
