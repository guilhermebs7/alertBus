package alertbus.notification_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String NOTIFICATION_TRIP_QUEUE = "notification.trip.events.queue";
    public static final String TRIP_EXCHANGE="trip.exchange";



    @Bean
    public Queue notificationTripQueue() {
        return new Queue(NOTIFICATION_TRIP_QUEUE, true);
    }
    @Bean
    public TopicExchange tripExchange() {
        return new TopicExchange(TRIP_EXCHANGE);
    }
    @Bean
    public Binding bindingNotification(Queue notificationQueue, TopicExchange tripExchange) {
        return BindingBuilder.bind(notificationQueue).to(tripExchange).with("trip.#");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}