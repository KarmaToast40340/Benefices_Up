package _Benefices_Up;

import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Save {

    private static final String ASSO_FILE = "associations.csv";
    private static final String DATA_POINTS_FILE = "points.csv";
    private static final String DATA_SF_FILE = "savoirfaire.csv";
    private static final String PARAMS_FILE = "params.csv"; // poids SF + points

    // === Associations ===
    public static void saveAssociations(DefaultTableModel assoModel) throws IOException {
        File f = new File(ASSO_FILE);
        System.out.println("Écriture associations dans : " + f.getAbsolutePath());
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8))) {
            for (int i = 0; i < assoModel.getRowCount(); i++) {
                String name = assoModel.getValueAt(i, 0).toString();
                writer.write(name);
                writer.newLine();
            }
        }
    }

    // === Points (table variableModel) ===
    public static void savePoints(DefaultTableModel variableModel) throws IOException {
        File f = new File(DATA_POINTS_FILE);
        System.out.println("Écriture points dans : " + f.getAbsolutePath());
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8))) {
            int colCount = variableModel.getColumnCount();
            int rowCount = variableModel.getRowCount();
            for (int r = 0; r < rowCount; r++) {
                StringBuilder line = new StringBuilder();
                line.append(variableModel.getValueAt(r, 0) == null ? "" : variableModel.getValueAt(r, 0).toString());
                for (int c = 1; c < colCount; c++) {
                    line.append(";");
                    Object v = variableModel.getValueAt(r, c);
                    line.append(v == null ? "" : v.toString());
                }
                writer.write(line.toString());
                writer.newLine();
            }
        }
    }

    // === Savoir-faire cochés (table sfModel) ===
    public static void saveSavoirFaire(DefaultTableModel sfModel, int sfCount) throws IOException {
        File f = new File(DATA_SF_FILE);
        System.out.println("Écriture savoirfaire dans : " + f.getAbsolutePath());
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8))) {
            for (int r = 0; r < sfModel.getRowCount(); r++) {
                StringBuilder line = new StringBuilder();
                line.append(sfModel.getValueAt(r, 0) == null ? "" : sfModel.getValueAt(r, 0).toString());
                for (int i = 0; i < sfCount; i++) {
                    line.append(";");
                    Object v = sfModel.getValueAt(r, i + 1);
                    boolean b = (v instanceof Boolean) && (Boolean) v;
                    line.append(b);
                }
                writer.write(line.toString());
                writer.newLine();
            }
        }
    }

    // === Paramètres (poids SF + poids PointType) ===
    public static void saveParams(List<SavoirFaire> sfTypes, List<PointType> pointTypes) throws IOException {
        File f = new File(PARAMS_FILE);
        System.out.println("Écriture params dans : " + f.getAbsolutePath());
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8))) {

            // Ligne 1 : SF;nom;poids;nom;poids;...
            StringBuilder sfLine = new StringBuilder("SF");
            for (SavoirFaire sf : sfTypes) {
                System.out.println("SF param : " + sf.getNom() + " = " + sf.getPoids());
                sfLine.append(";").append(sf.getNom()).append(";").append(sf.getPoids());
            }
            writer.write(sfLine.toString());
            writer.newLine();

            // Ligne 2 : PT;nom;poids;nom;poids;...
            StringBuilder ptLine = new StringBuilder("PT");
            for (PointType pt : pointTypes) {
                System.out.println("PT param : " + pt.getNom() + " = " + pt.getPoids());
                ptLine.append(";").append(pt.getNom()).append(";").append(pt.getPoids());
            }
            writer.write(ptLine.toString());
            writer.newLine();
        }
    }

    public static void loadParams(List<SavoirFaire> sfTypes, List<PointType> pointTypes) throws IOException {
    File f = new File(PARAMS_FILE);
    System.out.println("Lecture params depuis : " + f.getAbsolutePath());
    if (!f.exists()) {
        System.out.println("params.csv n'existe pas encore, on garde les valeurs par défaut.");
        return;
    }

    sfTypes.clear();
    pointTypes.clear();

    try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(";");
            if (parts.length < 1) continue;

            if ("SF".equals(parts[0])) {
                // SF;nom;poids;nom;poids;...
                for (int i = 1; i + 1 < parts.length; i += 2) {
                    String nom = parts[i];
                    double poids = Double.parseDouble(parts[i + 1]);
                    sfTypes.add(new SavoirFaire(nom, poids));
                }
            } else if ("PT".equals(parts[0])) {
                // PT;nom;poids;nom;poids;...
                for (int i = 1; i + 1 < parts.length; i += 2) {
                    String nom = parts[i];
                    double poids = Double.parseDouble(parts[i + 1]);
                    pointTypes.add(new PointType(nom, poids));
                }
            }
        }
    }
}

}
