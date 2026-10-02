-- For h2-database
-- create table users(username varchar_ignorecase(50) not null primary key,password varchar_ignorecase(500) not null,enabled boolean not null);
-- create table authorities (username varchar_ignorecase(50) not null,authority varchar_ignorecase(50) not null,constraint fk_authorities_users foreign key(username) references users(username));
-- create unique index ix_auth_username on authorities (username,authority);

-- For PostgreSQL
DROP TABLE IF EXISTS authorities;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
                       username varchar(50) NOT NULL PRIMARY KEY,
                       password varchar(100) NOT NULL,
                       enabled boolean NOT NULL
);

CREATE TABLE authorities (
                             username varchar(50) NOT NULL,
                             authority varchar(50) NOT NULL,
                             constraint fk_authorities_users foreign key (username) references users (username)
);

CREATE UNIQUE INDEX ix_auth_username on authorities (username, authority);

-- For MySQL
-- The above PostgreSQL schema is compatible with MySQL