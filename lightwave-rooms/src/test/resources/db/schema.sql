CREATE TABLE `room_model` (
	`id` varchar(25) NOT NULL PRIMARY KEY,
	`height_map` text NOT NULL,
	`door_position` varchar(9) NOT NULL
);

CREATE TABLE `room` (
	`id` integer NOT NULL PRIMARY KEY,
	`name` varchar(25) NOT NULL,
	`description` varchar(128) NOT NULL,
	`model_id` varchar(25) NOT NULL REFERENCES room_model
);
