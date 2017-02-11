CREATE TABLE room_model (
	id varchar(25) NOT NULL PRIMARY KEY,
	height_map text NOT NULL,
	door_position varchar(9) NOT NULL
);

CREATE TABLE room (
	id integer NOT NULL PRIMARY KEY,
	name varchar(25) NOT NULL,
	description varchar(128) NOT NULL,
	model_id varchar(25) NOT NULL REFERENCES room_model
);

CREATE TABLE player (
	id integer PRIMARY KEY,
	nickname varchar(25) NOT NULL,
	password bytea NOT NULL,
	password_salt bytea NOT NULL
);

INSERT INTO room_model (id, height_map, door_position) VALUES (
    'model_a',
    E'xxxxxxxxxxxx\nxxxx00000000\nxxxx00000000\nxxxx00000000\nxxxx00000000\nxxxx00000000\nxxxx00000000\nxxxx00000000\nxxxx00000000\nxxxx00000000\nxxxx00000000\nxxxx00000000\nxxxx00000000\nxxxx00000000\nxxxxxxxxxxxx\nxxxxxxxxxxxx',
    '3;5'
);

INSERT INTO room (id, name, description, model_id) VALUES (
    1,
    'Test',
    'Test room',
    'model_a'
);

-- Password: test
INSERT INTO player (id, nickname, password, password_salt) VALUES (
    1, 'Steve', E'\\xC6BE0D6DFC70198BF88E36520B8FC4A4DCB6353D', E'\\x121F3EA5F4E9CC2AA5C097851E7767682AAD1C06'
);