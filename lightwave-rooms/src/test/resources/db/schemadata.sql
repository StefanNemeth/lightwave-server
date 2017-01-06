INSERT INTO room_model (id, height_map, door_position) VALUES ('model_test', '0', '0;0');

INSERT INTO room (id, name, description, model_id) VALUES (1, 'Test room', 'Test description', 'model_test');
INSERT INTO room (id, name, description, model_id) VALUES (2, 'Test room', 'Test description', 'model_test');