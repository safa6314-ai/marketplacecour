USE artevia;

CREATE TABLE IF NOT EXISTS achat (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom_oeuvre VARCHAR(255) NOT NULL,
    nom_acheteur VARCHAR(150) NOT NULL,
    prix DOUBLE NOT NULL,
    date_achat DATE NOT NULL,
    statut VARCHAR(30) NOT NULL DEFAULT 'En attente'
);

CREATE TABLE IF NOT EXISTS vente (
    id INT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    description TEXT,
    prix DOUBLE NOT NULL,
    categorie VARCHAR(100),
    nom_artiste VARCHAR(150),
    id_achat INT NULL,
    quantite INT NOT NULL DEFAULT 1,
    image_path VARCHAR(500) DEFAULT NULL
);

SET @schema_name = DATABASE();

SET @sql = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'vente' AND COLUMN_NAME = 'id_achat') = 0,
    'ALTER TABLE vente ADD COLUMN id_achat INT NULL',
    'SELECT "vente.id_achat already exists"'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'vente' AND COLUMN_NAME = 'quantite') = 0,
    'ALTER TABLE vente ADD COLUMN quantite INT NOT NULL DEFAULT 1',
    'SELECT "vente.quantite already exists"'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'vente' AND COLUMN_NAME = 'image_path') = 0,
    'ALTER TABLE vente ADD COLUMN image_path VARCHAR(500) DEFAULT NULL',
    'SELECT "vente.image_path already exists"'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql = IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'achat' AND COLUMN_NAME = 'statut') = 0,
    'ALTER TABLE achat ADD COLUMN statut VARCHAR(30) NOT NULL DEFAULT ''En attente''',
    'SELECT "achat.statut already exists"'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS rating (
    id_rating INT AUTO_INCREMENT PRIMARY KEY,
    id_vente INT NOT NULL,
    customer_id VARCHAR(100) NOT NULL DEFAULT 'Client test',
    note INT NOT NULL,
    date_rating DATE NOT NULL,
    UNIQUE KEY uk_rating_user_vente (id_vente, customer_id)
);

CREATE TABLE IF NOT EXISTS loyalty_transactions (
    id_transaction INT AUTO_INCREMENT PRIMARY KEY,
    customer_id VARCHAR(100) NOT NULL,
    points INT NOT NULL,
    type VARCHAR(40) NOT NULL,
    reason VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    external_ref VARCHAR(120) UNIQUE
);

CREATE TABLE IF NOT EXISTS simulated_card_payments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    payment_ref VARCHAR(120) NOT NULL UNIQUE,
    customer_id VARCHAR(100) NOT NULL,
    amount DOUBLE NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'TND',
    status VARCHAR(40) NOT NULL DEFAULT 'MOCK',
    provider_response TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS flouci_payments LIKE simulated_card_payments;
CREATE TABLE IF NOT EXISTS konnect_payments LIKE simulated_card_payments;

CREATE TABLE IF NOT EXISTS stripe_checkout_payments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    payment_ref VARCHAR(120) NOT NULL UNIQUE,
    stripe_session_id VARCHAR(255) NOT NULL UNIQUE,
    customer_id VARCHAR(100) NOT NULL,
    amount DOUBLE NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'usd',
    status VARCHAR(40) NOT NULL,
    checkout_url TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
