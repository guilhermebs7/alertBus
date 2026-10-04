package alertbus.api_gateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GlobalAuthFilterTest {

    private GlobalAuthFilter filter;
    private GatewayFilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new GlobalAuthFilter();
        chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());
    }

    private MockServerWebExchange exchange(String path, String authorization) {
        MockServerHttpRequest.BaseBuilder<?> builder = MockServerHttpRequest.get(path);
        if (authorization != null) {
            builder.header("Authorization", authorization);
        }
        return MockServerWebExchange.from(builder.build());
    }

    @Test
    void getOrder_deveRodarAntesDosDemaisFiltros() {
        assertThat(filter.getOrder()).isEqualTo(-1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/trips", "/buses/1", "/routes", "/eta/trip/5", "/users"})
    void semHeaderAuthorization_deveRetornar401ENaoSeguirACadeia(String path) {
        MockServerWebExchange exchange = exchange(path, null);

        filter.filter(exchange, chain).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(chain, never()).filter(any());
    }

    @Test
    void comHeaderAuthorization_deveSeguirACadeia() {
        MockServerWebExchange exchange = exchange("/trips", "Bearer qualquer-coisa");

        filter.filter(exchange, chain).block();

        verify(chain).filter(exchange);
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"/auth/login", "/auth/register"})
    void rotasPublicasDeAuth_devemPassarSemHeader(String path) {
        MockServerWebExchange exchange = exchange(path, null);

        filter.filter(exchange, chain).block();

        verify(chain).filter(exchange);
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

}
