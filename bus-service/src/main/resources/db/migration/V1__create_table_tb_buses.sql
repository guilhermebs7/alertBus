CREATE TABLE tb_buses(
                       id BIGSERIAL PRIMARY KEY ,
                       placa VARCHAR(10) NOT NULL UNIQUE ,
                       modelo VARCHAR(255),
                       capacidade INT,
                       status VARCHAR(255) NOT NULL
);