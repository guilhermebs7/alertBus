package alertbus.trip_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String TRIP_EXCHANGE = "trip.exchange";
    public static final String TRIP_CREATED_QUEUE = "trip.created.queue"; // nome da fila responsável por receber mensagem relacionadas a criação de uma viagem
    public static final String TRIP_STATUS_QUEUE = "trip.status.queue";                            // fila relacionada à atualização do status de uma viagem

    public static final String ROUTING_KEY_CREATED = "trip.created";                   //identifica mensagens relacionadas a criação de uma viagem
    public static final String ROUTING_KEY_STATUS = "trip.status.updated";

    @Bean
    public TopicExchange tripExchange() {
        return new TopicExchange(TRIP_EXCHANGE);
    }

    @Bean
    public Queue tripCreatedQueue() {
        return new Queue(TRIP_CREATED_QUEUE, true);
    }

    @Bean
    public Queue tripStatusQueue() {
        return new Queue(TRIP_STATUS_QUEUE, true);
    }

    @Bean
    public Binding bindingCreated(Queue tripCreatedQueue, TopicExchange tripExchange) {
        return
                BindingBuilder.bind(tripCreatedQueue)
                        .to(tripExchange)
                        .with(ROUTING_KEY_CREATED);
    }

    @Bean
    public Binding bindingStatus(Queue tripStatusQueue, TopicExchange tripExchange) {
        return BindingBuilder.bind(tripStatusQueue)
                .to(tripExchange)
                .with(ROUTING_KEY_STATUS);
    }

    // Converte os objetos Java em JSON ao enviar para a fila
    @Bean
    public JacksonJsonMessageConverter messageConverter(){
        return new JacksonJsonMessageConverter();
    }

}
