CREATE TABLE tb_users(
    id UUID PRIMARY KEY DEFAULT  gen_random_uuid(),
    name varchar(255) NOT NULL ,
    email varchar(255) NOT NULL ,
    password VARCHAR(255) NOT NULL
)