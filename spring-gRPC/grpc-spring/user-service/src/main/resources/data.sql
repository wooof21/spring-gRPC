DROP TABLE IF EXISTS trader;
DROP TABLE IF EXISTS stock_item;


CREATE TABLE trader (
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

insert into trader(name, balance)
    values
        ('John', 10000),
        ('Joe', 10000),
        ('Jake', 10000);