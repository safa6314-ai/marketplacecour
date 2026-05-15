package Utils;

import Entities.Abonnement;
import Entities.Souscription;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Exports admin data to a single UTF-8 CSV: summary metrics, then all plans (abonnements),
 * then all souscriptions with denormalized plan columns.
 */
public final class CsvExportService {

    private CsvExportService() {}

    public static void exportAdminData(File out, List<Abonnement> abonnements, List<Souscription> souscriptions)
            throws IOException {
        Map<Integer, Abonnement> planById = new HashMap<>();
        for (Abonnement a : abonnements) {
            planById.put(a.getIdAbonnement(), a);
        }

        long active = souscriptions.stream().filter(s -> "active".equalsIgnoreCase(s.getStatut())).count();
        double revenue = 0;
        for (Souscription s : souscriptions) {
            Abonnement a = planById.get(s.getIdAbonnement());
            if (a != null) {
                revenue += a.getPrix();
            }
        }

        try (BufferedWriter w = Files.newBufferedWriter(out.toPath(), StandardCharsets.UTF_8)) {
            w.write('\uFEFF');

            w.write("metric,value");
            w.newLine();
            w.write(row("exported_at", Instant.now().toString()));
            w.newLine();
            w.write(row("total_plans_abonnements", String.valueOf(abonnements.size())));
            w.newLine();
            w.write(row("total_souscriptions", String.valueOf(souscriptions.size())));
            w.newLine();
            w.write(row("souscriptions_actives", String.valueOf(active)));
            w.newLine();
            w.write(row("revenu_estime_dt", String.format(Locale.US, "%.2f", revenue)));
            w.newLine();
            w.newLine();

            w.write("id_abonnement,id_user,nom,prix,duree_mois,description");
            w.newLine();
            for (Abonnement a : abonnements) {
                w.write(row(
                        String.valueOf(a.getIdAbonnement()),
                        String.valueOf(a.getIdUser()),
                        a.getNom(),
                        String.format(Locale.US, "%.2f", a.getPrix()),
                        String.valueOf(a.getDureeMois()),
                        a.getDescription()
                ));
                w.newLine();
            }

            w.newLine();
            w.write("id_souscription,id_user,nom_client,date_debut,date_fin,statut,id_abonnement,plan_nom,plan_prix_dt,plan_duree_mois");
            w.newLine();
            for (Souscription s : souscriptions) {
                Abonnement p = planById.get(s.getIdAbonnement());
                String planNom = p != null ? p.getNom() : "";
                String planPrix = p != null ? String.format(Locale.US, "%.2f", p.getPrix()) : "";
                String planDuree = p != null ? String.valueOf(p.getDureeMois()) : "";
                w.write(row(
                        String.valueOf(s.getIdSouscription()),
                        String.valueOf(s.getIdUser()),
                        s.getNomClient(),
                        s.getDateDebut() != null ? s.getDateDebut().toString() : "",
                        s.getDateFin() != null ? s.getDateFin().toString() : "",
                        s.getStatut(),
                        String.valueOf(s.getIdAbonnement()),
                        planNom,
                        planPrix,
                        planDuree
                ));
                w.newLine();
            }
        }
    }

    private static String row(String... cells) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cells.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(escapeCsv(cells[i]));
        }
        return sb.toString();
    }

    private static String escapeCsv(String v) {
        if (v == null) {
            return "";
        }
        String s = v.replace("\r\n", "\n").replace('\r', '\n');
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
