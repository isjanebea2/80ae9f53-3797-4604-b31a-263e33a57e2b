# API with spring boot 3

### 
 - java JDK 17
 - mysql 8

###  database

```bash
 docker compose up mysql
```

### database diagram
https://drive.google.com/file/d/1WOhPuapq3ZDY3ikO27krDbmc3KnHeYIP/view?usp=sharing

### migrations

```sql

CREATE TABLE account_type (
  id int NOT NULL AUTO_INCREMENT,
  name varchar(100) NOT NULL,
  created_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at timestamp NULL DEFAULT NULL,
  PRIMARY KEY (id)
)


INSERT INTO caju.account_type
(name, created_at, updated_at)
VALUES
	('food', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
	('meal', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
	('cash', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);


CREATE TABLE caju.account (
  id int NOT NULL AUTO_INCREMENT,
  status varchar(20) NOT NULL,
  created_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) 


INSERT INTO caju.account (id, status,created_at,updated_at,deleted_at) VALUES
	 (1, 'idle','2024-12-20 01:40:58','2024-12-20 01:40:58', NULL),
	 (2, 'idle','2024-12-20 01:40:58','2024-12-20 01:40:58', NULL);



CREATE TABLE account_amount (
  id int NOT NULL AUTO_INCREMENT,
  value decimal(10,2) NOT NULL,
  account_id int NOT NULL,
  account_type_id int NOT NULL,
  created_at timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at timestamp NULL DEFAULT NULL,
  
  PRIMARY KEY (id),
  CONSTRAINT fk_account_amount_account_id FOREIGN KEY (account_id) REFERENCES account (id),
  CONSTRAINT fk_account_type_account_type_id FOREIGN KEY (account_type_id) REFERENCES account_type (id)
);

	
INSERT INTO caju.account_amount
(value, account_id, account_type_id, created_at, updated_at) 
VALUES
	(1000, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
	(1000, 1, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
	(1000, 1, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);



CREATE TABLE statement_output (
    id CHAR(36) PRIMARY KEY, 
    account_amount_id INT NOT NULL, 
    value DECIMAL(10, 2) NOT NULL, 
    transaction_logs_id CHAR(36) NOT NULL,
    merchant VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_statement_output_account_amount_id FOREIGN KEY (account_amount_id) REFERENCES account_amount(id)
);

```

### Curl for testing application

```bash
curl --location 'http://localhost:8080/api/v1/transactions/authorize' \
--header 'Content-Type: application/json' \
--data '{
	"account": "2",
	"totalAmount": 950.00,
	"mcc": "5411",
	"merchant": "PADARIA DO ZE               SAO PAULO BR"
}'
```