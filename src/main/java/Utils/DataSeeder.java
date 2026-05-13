package Utils;

import Entities.Post;
import Services.PostCRUD;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class DataSeeder {

    private static final Path IMAGES_DIR = Path.of("src", "main", "resources", "images", "posts");

    private static final String[] CONTENTS = {
            "Decouvrez ma nouvelle toile abstraite - melange de couleurs chaudes et froides 🎨",
            "Sculpture en argile inspiree de la nature - disponible sur notre marketplace !",
            "Portrait au fusain realise lors de mon dernier atelier de dessin",
            "Photographie artistique - lumiere doree au coucher du soleil 📸",
            "Digital art : illustration fantastique pour ma prochaine collection Artevia",
            "Aquarelle florale - chaque petale peint a la main avec amour 🌸",
            "Ma derniere oeuvre exposee a la galerie - venez la decouvrir !"
    };

    private static final String[] IMAGE_URLS = {
            "https://images.unsplash.com/photo-1579783902614-a3fb3927b6a5?w=600&q=80",
            "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=600&q=80",
            "https://images.unsplash.com/photo-1536924940846-227afb31e2a5?w=600&q=80",
            "https://images.unsplash.com/photo-1501854140801-50d01698950b?w=600&q=80",
            "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600&q=80",
            "https://images.unsplash.com/photo-1460661419201-fd4cecdf8a8b?w=600&q=80",
            "https://images.unsplash.com/photo-1547826039-bdbedd329933?w=600&q=80"
    };

    private DataSeeder() {
    }

    public static void seed() {
        Thread thread = new Thread(DataSeeder::seedAsync, "data-seeder");
        thread.setDaemon(true);
        thread.start();
    }

    private static void seedAsync() {
        try {
            Files.createDirectories(IMAGES_DIR);
            PostCRUD postCRUD = new PostCRUD();
            List<Post> existingPosts = postCRUD.afficher();
            if (existingPosts.stream().anyMatch(post -> CONTENTS[0].equals(post.getContenu()))) {
                return;
            }

            for (int i = 0; i < CONTENTS.length; i++) {
                Path imagePath = IMAGES_DIR.resolve("sample_" + (i + 1) + ".jpg");
                downloadImageIfMissing(IMAGE_URLS[i], imagePath);

                Post post = new Post(CONTENTS[i], new Timestamp(System.currentTimeMillis()));
                post.setImagePath(imagePath.toString().replace("\\", "/"));
                post.setStatut("accepte");
                postCRUD.ajouter(post);
            }
        } catch (Exception e) {
            System.out.println("Seed images/posts ignore: " + e.getMessage());
        }
    }

    private static void downloadImageIfMissing(String url, Path destination) {
        if (Files.exists(destination)) return;

        try (InputStream input = new URL(url).openStream()) {
            Files.copy(input, destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            System.out.println("Image seed ignoree: " + destination.getFileName() + " - " + e.getMessage());
        }
    }
}
