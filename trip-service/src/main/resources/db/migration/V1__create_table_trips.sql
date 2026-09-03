CREATE TABLE tb_trips (
                          id BIGSERIAL PRIMARY KEY,
                          bus_id BIGINT NOT NULL,
                          route_id BIGINT NOT NULL,
                          status VARCHAR(30) NOT NULL,
                          start_time TIMESTAMP,
                          end_time TIMESTAMP
);