-- activate with "spring.sql.init.mode=always" and "spring.jpa.defer-datasource-initialization=true"
insert into catalog(product_id, product_name, stock, unit_price) values('CATALOG-001', 'Berlin', 100, 1500);
insert into catalog(product_id, product_name, stock, unit_price) values('CATALOG-002', 'Tokyo', 110, 1000);
insert into catalog(product_id, product_name, stock, unit_price) values('CATALOG-003', 'Stockholm', 120, 2000);
