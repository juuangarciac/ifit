-- Reinicar tablas SQL, cada vez que se reinicia el servidor
-- Solo para dev
DELETE FROM `message` WHERE id > 0;
DELETE FROM `message_type` WHERE id > 0;

ALTER TABLE `message` AUTO_INCREMENT = 1;
ALTER TABLE `message_type` AUTO_INCREMENT = 1;

-- Insertar tipos
INSERT INTO `message_type` (name) VALUES ('user');
INSERT INTO `message_type` (name) VALUES ('ai');
INSERT INTO `message_type` (name) VALUES ('system');