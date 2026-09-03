CREATE TABLE tb_routes(
    id BIGSERIAL PRIMARY KEY ,
    codigo VARCHAR(20) NOT NULL UNIQUE ,
    nome VARCHAR(255) NOT NULL ,
    origem VARCHAR(255),
    destino VARCHAR(255)
);