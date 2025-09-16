DROP TABLE IF EXISTS trader;
DROP TABLE IF EXISTS stock_item;
DROP TABLE IF EXISTS trader_v2;
DROP TABLE IF EXISTS stock_item_v2;


CREATE TABLE trader (
    id int AUTO_INCREMENT primary key,
    name VARCHAR(50),
    balance int
);

CREATE TABLE trader_v2 (
    id int AUTO_INCREMENT primary key,
    name VARCHAR(50),
    balance double
);

CREATE TABLE stock_item (
    id int AUTO_INCREMENT primary key,
    trader_id int,
    stock VARCHAR(10),
    quantity int,
    foreign key (trader_id) references trader(id)
);

CREATE TABLE stock_item_v2 (
    id VARCHAR(36) primary key, -- UUID
    trader_id int,
    stock VARCHAR(10),
    quantity int,
    purchase_price double,
    foreign key (trader_id) references trader_v2(id)
);

insert into trader(name, balance)
    values
        ('John', 10000),
        ('Joe', 10000),
        ('Jake', 10000);

insert into trader_v2(name, balance)
    values
        ('John_V2', 10000.0),
        ('Joe_V2', 10000.0),
        ('Jake_V2', 10000.0);


