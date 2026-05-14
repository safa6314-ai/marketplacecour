CREATE DATABASE IF NOT EXISTS forum_artevia
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE forum_artevia;

CREATE TABLE IF NOT EXISTS post (
  id INT AUTO_INCREMENT PRIMARY KEY,
  contenu TEXT NOT NULL,
  date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  image_path VARCHAR(500) DEFAULT NULL,
  statut VARCHAR(20) DEFAULT 'en_attente',
  categorie VARCHAR(40) DEFAULT 'Discussion generale',
  moderation_reason VARCHAR(255) DEFAULT 'En attente de moderation.'
);

UPDATE post SET categorie = 'Discussion generale' WHERE categorie = 'Question generale';

CREATE TABLE IF NOT EXISTS commentaire (
  id INT AUTO_INCREMENT PRIMARY KEY,
  contenu TEXT NOT NULL,
  date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  post_id INT NOT NULL,
  statut VARCHAR(20) DEFAULT 'en_attente',
  CONSTRAINT fk_commentaire_post
    FOREIGN KEY (post_id) REFERENCES post(id)
    ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS likes (
  id INT AUTO_INCREMENT PRIMARY KEY,
  post_id INT NOT NULL,
  date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_likes_post
    FOREIGN KEY (post_id) REFERENCES post(id)
    ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS abonnement (
  id_abonnement INT AUTO_INCREMENT PRIMARY KEY,
  id_user INT NOT NULL,
  nom VARCHAR(100) NOT NULL,
  prix DOUBLE NOT NULL,
  duree_mois INT NOT NULL,
  description TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS souscription (
  id_souscription INT AUTO_INCREMENT PRIMARY KEY,
  id_user INT NOT NULL,
  nom_client VARCHAR(100) NOT NULL,
  date_debut DATE NOT NULL,
  date_fin DATE NOT NULL,
  statut VARCHAR(20) NOT NULL,
  id_abonnement INT NOT NULL,
  CONSTRAINT fk_souscription_abonnement
    FOREIGN KEY (id_abonnement) REFERENCES abonnement(id_abonnement)
    ON DELETE CASCADE
);

INSERT INTO abonnement (id_user, nom, prix, duree_mois, description)
SELECT 1, 'Basic', 29.9, 1, 'Acces aux fonctionnalites essentielles'
WHERE NOT EXISTS (SELECT 1 FROM abonnement WHERE nom = 'Basic');

INSERT INTO abonnement (id_user, nom, prix, duree_mois, description)
SELECT 1, 'Premium', 79.9, 6, 'Acces avance avec avantages premium'
WHERE NOT EXISTS (SELECT 1 FROM abonnement WHERE nom = 'Premium');

INSERT INTO abonnement (id_user, nom, prix, duree_mois, description)
SELECT 1, 'VIP', 149.9, 12, 'Acces complet pendant une annee'
WHERE NOT EXISTS (SELECT 1 FROM abonnement WHERE nom = 'VIP');

CREATE TABLE IF NOT EXISTS question (
  id INT AUTO_INCREMENT PRIMARY KEY,
  contenu VARCHAR(500) NOT NULL,
  categorie VARCHAR(100) NOT NULL,
  niveau VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS reponse (
  id INT AUTO_INCREMENT PRIMARY KEY,
  contenu VARCHAR(500) NOT NULL,
  isCorrect BOOLEAN NOT NULL,
  est_correcte BOOLEAN DEFAULT false,
  date_creation TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  question_id INT NOT NULL,
  CONSTRAINT fk_reponse_question
    FOREIGN KEY (question_id) REFERENCES question(id)
    ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS achievements (
  id INT AUTO_INCREMENT PRIMARY KEY,
  titre VARCHAR(100),
  description TEXT,
  badge_icon VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS user_achievements (
  id INT AUTO_INCREMENT PRIMARY KEY,
  achievement_id INT NOT NULL,
  date_obtention TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_user_achievements_achievement
    FOREIGN KEY (achievement_id) REFERENCES achievements(id)
    ON DELETE CASCADE
);

INSERT INTO achievements (id, titre, description, badge_icon)
VALUES
  (1, 'Premier Quiz', 'Repondre a une premiere question.', 'first'),
  (2, '5 bonnes reponses', 'Atteindre 5 bonnes reponses.', 'five-correct'),
  (3, 'Expert Renaissance', 'Reussir une question de categorie Renaissance.', 'renaissance'),
  (4, 'Sans erreur', 'Enchainer 3 bonnes reponses sans erreur.', 'clean'),
  (5, 'Niveau difficile reussi', 'Repondre correctement a une question difficile.', 'hard'),
  (6, '10 questions repondues', 'Repondre a 10 questions.', 'ten')
ON DUPLICATE KEY UPDATE
  titre = VALUES(titre),
  description = VALUES(description),
  badge_icon = VALUES(badge_icon);

INSERT INTO question (contenu, categorie, niveau)
SELECT 'Quelle est la capitale de la France ?', 'Culture generale', 'Facile'
WHERE NOT EXISTS (SELECT 1 FROM question WHERE contenu = 'Quelle est la capitale de la France ?');

INSERT INTO reponse (contenu, isCorrect, question_id)
SELECT 'Paris', TRUE, q.id
FROM question q
WHERE q.contenu = 'Quelle est la capitale de la France ?'
  AND NOT EXISTS (
    SELECT 1 FROM reponse r WHERE r.question_id = q.id AND r.contenu = 'Paris'
  );

INSERT INTO reponse (contenu, isCorrect, question_id)
SELECT 'Lyon', FALSE, q.id
FROM question q
WHERE q.contenu = 'Quelle est la capitale de la France ?'
  AND NOT EXISTS (
    SELECT 1 FROM reponse r WHERE r.question_id = q.id AND r.contenu = 'Lyon'
  );

INSERT INTO question (contenu, categorie, niveau)
SELECT 'Quel element est principalement utilise pour creer une sculpture en argile ?', 'Art', 'Facile'
WHERE NOT EXISTS (SELECT 1 FROM question WHERE contenu = 'Quel element est principalement utilise pour creer une sculpture en argile ?');

INSERT INTO reponse (contenu, isCorrect, question_id)
SELECT 'Argile', TRUE, q.id
FROM question q
WHERE q.contenu = 'Quel element est principalement utilise pour creer une sculpture en argile ?'
  AND NOT EXISTS (
    SELECT 1 FROM reponse r WHERE r.question_id = q.id AND r.contenu = 'Argile'
  );

INSERT INTO reponse (contenu, isCorrect, question_id)
SELECT 'Verre liquide', FALSE, q.id
FROM question q
WHERE q.contenu = 'Quel element est principalement utilise pour creer une sculpture en argile ?'
  AND NOT EXISTS (
    SELECT 1 FROM reponse r WHERE r.question_id = q.id AND r.contenu = 'Verre liquide'
  );

INSERT INTO question (contenu, categorie, niveau)
SELECT 'Dans un quiz, combien de bonnes reponses doit avoir une question a choix unique ?', 'Quiz', 'Moyen'
WHERE NOT EXISTS (SELECT 1 FROM question WHERE contenu = 'Dans un quiz, combien de bonnes reponses doit avoir une question a choix unique ?');

INSERT INTO reponse (contenu, isCorrect, question_id)
SELECT 'Une seule', TRUE, q.id
FROM question q
WHERE q.contenu = 'Dans un quiz, combien de bonnes reponses doit avoir une question a choix unique ?'
  AND NOT EXISTS (
    SELECT 1 FROM reponse r WHERE r.question_id = q.id AND r.contenu = 'Une seule'
  );

INSERT INTO reponse (contenu, isCorrect, question_id)
SELECT 'Toutes les reponses', FALSE, q.id
FROM question q
WHERE q.contenu = 'Dans un quiz, combien de bonnes reponses doit avoir une question a choix unique ?'
  AND NOT EXISTS (
    SELECT 1 FROM reponse r WHERE r.question_id = q.id AND r.contenu = 'Toutes les reponses'
  );

UPDATE reponse SET est_correcte = isCorrect WHERE est_correcte = false;
