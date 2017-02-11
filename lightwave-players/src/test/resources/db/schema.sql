CREATE TABLE player (
	id integer PRIMARY KEY,
	nickname varchar(25) NOT NULL,
	password bytea NOT NULL,
	password_salt bytea NOT NULL
);
