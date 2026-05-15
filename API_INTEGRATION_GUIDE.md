# Guide des 4 API

Ce projet contient maintenant quatre integrations API dans `src/main/java/org/example/api`.

Important : si une vraie cle API a deja ete collee dans un message, un fichier, GitHub ou une capture d'ecran, considere-la comme compromise. Il faut la supprimer/revoquer dans le site du fournisseur, puis creer une nouvelle cle.

## 1. API Email avec Brevo

Objectif : envoyer des rappels, confirmations d'inscription et notifications.

Variables d'environnement :

```text
BREVO_API_KEY=COLLE_TA_CLE_BREVO_ICI
BREVO_SENDER_EMAIL=ton_email@gmail.com
BREVO_SENDER_NAME=Plateforme Cours
```

Exemple Java :

```java
EmailApiService emailApi = new EmailApiService();
emailApi.sendCourseReminder(
        "etudiant@example.com",
        "Amina",
        "Java debutant",
        "20/05/2026 a 10:00"
);
```

### Erreur Brevo : IP non reconnue

Si Brevo renvoie une erreur HTTP 401 avec un message comme :

```text
We have detected you are using an unrecognised IP address
```

ce n'est pas un probleme de code Java. Brevo bloque l'appel parce que la cle API est utilisee depuis une nouvelle adresse IP.

Dans ton email Brevo "Verifiez une nouvelle IP" :

- si c'etait bien toi, clique sur `Oui, autoriser la nouvelle adresse IP`, puis relance l'application ;
- si ce n'etait pas toi, clique sur `Non, changer les cles API`, supprime/revoque l'ancienne cle, cree une nouvelle cle API, puis remplace `BREVO_API_KEY`.

Dans la capture fournie, l'IP bloquee est `196.238.8.45`. Il faut l'autoriser dans Brevo avant que l'envoi d'email fonctionne depuis cette connexion.

## 2. API Visioconference avec Zoom

Objectif : creer automatiquement une reunion live pour un cours.

Variables d'environnement :

```text
ZOOM_ACCOUNT_ID=COLLE_TON_ACCOUNT_ID_ICI
ZOOM_CLIENT_ID=COLLE_TON_CLIENT_ID_ICI
ZOOM_CLIENT_SECRET=COLLE_TON_CLIENT_SECRET_ICI
```

L'application utilise l'OAuth Zoom Server-to-Server. La reunion doit etre creee par le mode enseignant. Le lien etudiant vient de `join_url` retourne par Zoom, et le lien privilegie `start_url` n'est pas affiche aux etudiants.

Exemple Java :

```java
VideoConferenceApiService videoApi = new VideoConferenceApiService();
MeetingInfo meeting = videoApi.createMeeting(new MeetingRequest(
        "Session Java debutant",
        "2026-05-20T10:00:00",
        60,
        "Africa/Lagos"
));

System.out.println(meeting.joinUrl());
```

## 3. API Stockage de fichiers avec Cloudinary

Objectif : stocker les supports de cours, devoirs, images ou PDF.

Variables d'environnement :

```text
CLOUDINARY_CLOUD_NAME=ton_cloud_name
CLOUDINARY_UPLOAD_PRESET=ton_upload_preset
```

Les resumes etudiants utilisent la meme configuration Cloudinary. Formats acceptes dans l'application : PDF, DOC, DOCX, PNG, JPG, JPEG et WEBP. Chaque resume est lie a l'etudiant selectionne et au cours selectionne.

Exemple Java :

```java
FileStorageApiService storageApi = new FileStorageApiService();
UploadResult result = storageApi.uploadCourseFile(Path.of("support-cours.pdf"));

System.out.println(result.secureUrl());
```

## 4. API IA de generation de cours avec GroqAI

Objectif : donner une idee ou une description de cours, puis recevoir un nouveau cours structure avec titre, description, categorie, niveau, prix et chapitres.

Variables d'environnement :

```text
GROQ_API_KEY=COLLE_TA_CLE_GROQ_ICI
GROQ_COURSE_MODEL=llama-3.3-70b-versatile
```

`GROQ_COURSE_MODEL` est optionnel. Si tu ne le mets pas, le code utilise `llama-3.3-70b-versatile`.

Exemple Java :

```java
CourseGenerationApiService courseAiApi = new CourseGenerationApiService();
GeneratedCourse generatedCourse = courseAiApi.generateCourse(
        "Un cours pratique pour apprendre JavaFX et creer une marketplace de cours."
);

System.out.println(generatedCourse.cours().getTitre());
generatedCourse.chapitres().forEach(chapitre ->
        System.out.println(chapitre.getOrdre() + ". " + chapitre.getTitre())
);
```

Si tu veux enregistrer le resultat dans la base de donnees :

```java
CoursService coursService = new CoursService();
ChapitreService chapitreService = new ChapitreService();

coursService.ajouter(generatedCourse.cours());
int coursId = generatedCourse.cours().getId();

for (Chapitres chapitre : generatedCourse.chapitres()) {
    chapitre.setCoursId(coursId);
    chapitreService.ajouter(chapitre);
}
```

## Pourquoi les cles sont dans les variables d'environnement ?

Les cles API sont secretes. Il ne faut pas les mettre directement dans le code Java ni dans Git.
Avec cette methode, ton code reste propre et tu peux changer les cles sans modifier le projet.

## Comment mettre les variables sous PowerShell

Ces commandes marchent seulement dans PowerShell et seulement pour le terminal actuel :

```powershell
$env:BREVO_API_KEY="COLLE_TA_NOUVELLE_CLE_ICI"
$env:BREVO_SENDER_EMAIL="ton_email@gmail.com"
$env:BREVO_SENDER_NAME="Plateforme Cours"

$env:ZOOM_ACCOUNT_ID="COLLE_TON_ACCOUNT_ID_ICI"
$env:ZOOM_CLIENT_ID="COLLE_TON_CLIENT_ID_ICI"
$env:ZOOM_CLIENT_SECRET="COLLE_TON_CLIENT_SECRET_ICI"

$env:CLOUDINARY_CLOUD_NAME="ton_cloud_name"
$env:CLOUDINARY_UPLOAD_PRESET="ton_upload_preset"

$env:GROQ_API_KEY="COLLE_TA_CLE_GROQ_ICI"
$env:GROQ_COURSE_MODEL="llama-3.3-70b-versatile"
```

Verification :

```powershell
echo $env:BREVO_API_KEY
```

Ensuite lance l'application Java depuis le meme terminal, par exemple :

```powershell
.\mvnw.cmd javafx:run
```

Si `mvnw.cmd` n'existe pas dans ton projet, installe Maven ou lance le projet depuis IntelliJ avec les variables ajoutees dans la configuration d'execution.

## Comment mettre les variables sous CMD

```cmd
set BREVO_API_KEY=COLLE_TA_NOUVELLE_CLE_ICI
set BREVO_SENDER_EMAIL=ton_email@gmail.com
set BREVO_SENDER_NAME=Plateforme Cours
```

Verification :

```cmd
echo %BREVO_API_KEY%
```

## Dans IntelliJ IDEA

Va dans `Run` > `Edit Configurations` > ta configuration Java/JavaFX > `Environment variables`.

Ajoute les variables sous cette forme :

```text
BREVO_API_KEY=COLLE_TA_NOUVELLE_CLE_ICI;BREVO_SENDER_EMAIL=ton_email@gmail.com;BREVO_SENDER_NAME=Plateforme Cours
```

Pour Zoom et Cloudinary, ajoute aussi les autres variables dans le meme champ.
Pour GroqAI, ajoute aussi `GROQ_API_KEY=COLLE_TA_CLE_GROQ_ICI;GROQ_COURSE_MODEL=llama-3.3-70b-versatile`.
