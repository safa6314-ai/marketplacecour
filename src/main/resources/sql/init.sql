CREATE DATABASE IF NOT EXISTS `artevia`
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_general_ci;

USE `artevia`;

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";

-- Table abonnement
CREATE TABLE `abonnement` (
  `id_abonnement` int(11) NOT NULL,
  `id_user` int(11) NOT NULL,
  `nom` varchar(50) NOT NULL,
  `prix` decimal(10,2) NOT NULL,
  `duree_mois` int(11) NOT NULL,
  `description` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 
COLLATE=utf8mb4_general_ci;

-- Table achat
CREATE TABLE `achat` (
  `id` int(11) NOT NULL,
  `nom_oeuvre` varchar(100) NOT NULL,
  `nom_acheteur` varchar(100) NOT NULL,
  `prix` double NOT NULL,
  `date_achat` date NOT NULL,
  `statut` varchar(30) NOT NULL DEFAULT 'En attente'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 
COLLATE=utf8mb4_general_ci;

-- Table audit_logs
CREATE TABLE `audit_logs` (
  `id` bigint(20) NOT NULL,
  `user_email` varchar(255) DEFAULT NULL,
  `action` varchar(100) DEFAULT NULL,
  `details` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 
COLLATE=utf8mb4_general_ci;

-- Table avis
CREATE TABLE `avis` (
  `id` int(11) NOT NULL,
  `chapitre_id` int(11) NOT NULL,
  `commentaire` text DEFAULT NULL,
  `note` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 
COLLATE=utf8mb4_general_ci;

-- Table chapitres
CREATE TABLE `chapitres` (
  `id` int(11) NOT NULL,
  `titre` varchar(255) NOT NULL,
  `contenu` text DEFAULT NULL,
  `ordre` int(11) DEFAULT NULL,
  `cours_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 
COLLATE=utf8mb4_general_ci;

-- Table commentaire
CREATE TABLE `commentaire` (
  `id` int(11) NOT NULL,
  `contenu` varchar(255) DEFAULT NULL,
  `date_creation` timestamp NOT NULL 
    DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `post_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 
COLLATE=utf8mb4_general_ci;

-- Table cours
CREATE TABLE `cours` (
  `id` int(11) NOT NULL,
  `titre` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  `prix` double DEFAULT 0,
  `categorie` varchar(100) DEFAULT NULL,
  `niveau` varchar(50) DEFAULT 'Debutant'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 
COLLATE=utf8mb4_general_ci;

-- Table evenement
CREATE TABLE `evenement` (
  `id_event` int(11) NOT NULL,
  `titre` varchar(100) NOT NULL,
  `date_debut` datetime NOT NULL,
  `date_fin` datetime NOT NULL,
  `lieu` varchar(100) NOT NULL,
  `capacite` int(11) NOT NULL,
  `type` varchar(50) NOT NULL,
  `statut` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 
COLLATE=utf8mb4_general_ci;

-- Table favoris
CREATE TABLE `favoris` (
  `id_personne` int(11) NOT NULL,
  `id_event` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 
COLLATE=utf8mb4_general_ci;

-- Table flouci_payments
CREATE TABLE `flouci_payments` (
  `id_payment` int(11) NOT NULL,
  `order_reference` varchar(120) NOT NULL,
  `payment_id` varchar(150) NOT NULL,
  `amount_millimes` int(11) NOT NULL,
  `product_name` varchar(255) DEFAULT NULL,
  `payment_url` varchar(600) DEFAULT NULL,
  `status` varchar(40) NOT NULL DEFAULT 'PENDING',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 
COLLATE=utf8mb4_general_ci;

-- Table konnect_payments
CREATE TABLE `konnect_payments` (
  `id_payment` int(11) NOT NULL,
  `order_reference` varchar(120) NOT NULL,
  `payment_ref` varchar(150) NOT NULL,
  `amount_millimes` int(11) NOT NULL,
  `product_name` varchar(255) DEFAULT NULL,
  `pay_url` varchar(600) DEFAULT NULL,
  `status` varchar(40) NOT NULL DEFAULT 'PENDING',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 
COLLATE=utf8mb4_general_ci;

-- Table souscription
CREATE TABLE `souscription` (
  `id_souscription` int(11) NOT NULL,
  `id_user` int(11) NOT NULL,
  `nom_client` varchar(100) NOT NULL,
  `date_debut` date NOT NULL,
  `date_fin` date NOT NULL,
  `statut` varchar(20) NOT NULL,
  `id_abonnement` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 
COLLATE=utf8mb4_general_ci;

-- Table users
CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `username` varchar(100) NOT NULL,
  `email` varchar(150) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` varchar(50) DEFAULT 'USER',
  `statut` varchar(20) DEFAULT 'active',
  `phone` varchar(20) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 
COLLATE=utf8mb4_general_ci;

-- Cles primaires
ALTER TABLE `abonnement` ADD PRIMARY KEY (`id_abonnement`);
ALTER TABLE `achat` ADD PRIMARY KEY (`id`);
ALTER TABLE `audit_logs` ADD PRIMARY KEY (`id`);
ALTER TABLE `avis` ADD PRIMARY KEY (`id`);
ALTER TABLE `chapitres` ADD PRIMARY KEY (`id`);
ALTER TABLE `cours` ADD PRIMARY KEY (`id`);
ALTER TABLE `evenement` ADD PRIMARY KEY (`id_event`);
ALTER TABLE `flouci_payments` 
  ADD PRIMARY KEY (`id_payment`),
  ADD UNIQUE KEY `payment_id` (`payment_id`);
ALTER TABLE `konnect_payments` 
  ADD PRIMARY KEY (`id_payment`),
  ADD UNIQUE KEY `payment_ref` (`payment_ref`);
ALTER TABLE `souscription` ADD PRIMARY KEY (`id_souscription`);
ALTER TABLE `users` 
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `idx_users_phone` (`phone`);

-- AUTO_INCREMENT
ALTER TABLE `abonnement`
  MODIFY `id_abonnement` int(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE `souscription`
  MODIFY `id_souscription` int(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;
ALTER TABLE `achat`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=28;
ALTER TABLE `evenement`
  MODIFY `id_event` int(11) NOT NULL AUTO_INCREMENT;

-- Cle etrangere souscription -> abonnement
ALTER TABLE `souscription`
  ADD CONSTRAINT `fk_souscription_abonnement`
  FOREIGN KEY (`id_abonnement`)
  REFERENCES `abonnement` (`id_abonnement`);

COMMIT;
