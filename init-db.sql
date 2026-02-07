-- init-db.sql
CREATE DATABASE IF NOT EXISTS ronnie CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS ifit CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Opcional: crear usuarios específicos
-- CREATE USER 'ronnie_user'@'%' IDENTIFIED BY 'password123';
-- GRANT ALL PRIVILEGES ON ronnie.* TO 'ronnie_user'@'%';
-- 
-- CREATE USER 'ifit_user'@'%' IDENTIFIED BY 'password456';
-- GRANT ALL PRIVILEGES ON ifit.* TO 'ifit_user'@'%';

FLUSH PRIVILEGES;