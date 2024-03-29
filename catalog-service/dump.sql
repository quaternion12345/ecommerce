-- H2 2.2.224;
;             
CREATE USER IF NOT EXISTS "CATALOG_USER" SALT '97a2f455783999be' HASH 'feb13ef8463d543e61b8d961f0ce9eabebc574a8c2e085c662e625a28a25a5ea' ADMIN;               
CREATE MEMORY TABLE "PUBLIC"."CATALOG"(
    "ID" BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL,
    "CREATED_AT" TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP NOT NULL,
    "PRODUCT_ID" CHARACTER VARYING(120) NOT NULL,
    "PRODUCT_NAME" CHARACTER VARYING(255) NOT NULL,
    "STOCK" INTEGER NOT NULL,
    "UNIT_PRICE" INTEGER NOT NULL
);            
ALTER TABLE "PUBLIC"."CATALOG" ADD CONSTRAINT "PUBLIC"."CONSTRAINT_4" PRIMARY KEY("ID");      
-- 0 +/- SELECT COUNT(*) FROM PUBLIC.CATALOG; 
ALTER TABLE "PUBLIC"."CATALOG" ADD CONSTRAINT "PUBLIC"."UK_9GGGYSLU2USN0RXS32MF055WQ" UNIQUE("PRODUCT_ID");   
