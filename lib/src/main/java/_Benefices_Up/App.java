package _Benefices_Up;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class App extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final String ASSO_FILE = "associations.csv";      // nom;
    private static final String DATA_POINTS_FILE = "points.csv";     // type;asso1;asso2;...
    private static final String DATA_SF_FILE = "savoirfaire.csv";    // nom;prog;malle;visuel;deco (true/false)

    private JTextField totalProfitField;

    // Table 1 : associations créées
    private DefaultTableModel assoModel;

    // Table 2 : part variable (lignes = types de points, colonnes = assos)
    private DefaultTableModel variableModel;

    // Table 3 : part savoir-faire (lignes = assos, colonnes = savoir-faire cochés)
    private DefaultTableModel sfModel;

    private JTextArea resultArea;

    // Types de points (nom + poids)
    private final List<PointType> pointTypes = new ArrayList<>();

    // Savoir-faire de la charte avec leurs poids
    private final List<SavoirFaire> savoirFaireTypes = Arrays.asList(
            new SavoirFaire("Prog artistique", 11.0),
            new SavoirFaire("Malle de prévention", 3.0),
            new SavoirFaire("Visuel", 3.0),
            new SavoirFaire("Déco", 3.0)
    );

    // Pour ne pas sauvegarder pendant le chargement initial
    private boolean loading = false;

    public App() {
        Locale.setDefault(Locale.FRENCH);
        initPointTypes();
        // Charger d'abord les paramètres (poids SF + points)
        try {
            Save.loadParams(savoirFaireTypes, pointTypes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        initUI();
        loadFromFiles();
    }

    private void initPointTypes() {
        pointTypes.clear();
        pointTypes.add(new PointType("Heures de bénévolat", 1.0));
        pointTypes.add(new PointType("Tâches logistiques clés", 1.0));
        pointTypes.add(new PointType("Prises de responsabilités", 1.0));
        pointTypes.add(new PointType("Prêt de matériel", 1.0));
        pointTypes.add(new PointType("Réunions de préparation", 1.0));
        pointTypes.add(new PointType("Logistique (commandes)", 1.0));
        pointTypes.add(new PointType("Bonus débit de boisson", 1.0));
        pointTypes.add(new PointType("Bilan financier", 1.0));
    }

    private void initUI() {
        setTitle("Calcul des bénéfices - Charte UP");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);

        // ==== Haut : bénéfice total + bouton paramètres ====
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Bénéfice total (€) :"));
        totalProfitField = new JTextField(10);
        topPanel.add(totalProfitField);

        JButton settingsButton = new JButton("Paramètres");
        topPanel.add(settingsButton);

        // ==== Centre haut : création d'association ====
        JPanel createAssoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        createAssoPanel.setBorder(BorderFactory.createTitledBorder("Créer une association"));

        JTextField newAssoField = new JTextField(15);
        JButton addAssoButton = new JButton("Ajouter l'association");
        JButton removeAssoButton = new JButton("Supprimer sélection");

        createAssoPanel.add(new JLabel("Nom :"));
        createAssoPanel.add(newAssoField);
        createAssoPanel.add(addAssoButton);
        createAssoPanel.add(removeAssoButton);

        // ==== Table 1 : liste des associations (nom non éditable) ====
        assoModel = new DefaultTableModel(new String[]{"Nom"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable assoTable = new JTable(assoModel);
        JScrollPane assoScroll = new JScrollPane(assoTable);
        assoScroll.setBorder(BorderFactory.createTitledBorder("Associations"));

        // ==== Table 2 : part variable (type de point en ligne, assos en colonnes) ====
        variableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Type de point (col 0) non éditable, colonnes d'assos éditables
                return column > 0;
            }
        };
        variableModel.addColumn("Type de point");
        for (PointType pt : pointTypes) {
            variableModel.addRow(new Object[]{pt.getNom()});
        }
        JTable variableTable = new JTable(variableModel);
        JScrollPane variableScroll = new JScrollPane(variableTable);
        variableScroll.setBorder(BorderFactory.createTitledBorder("Part variable (points par type et par asso)"));

        // ==== Table 3 : part savoir-faire (lignes = assos, colonnes = savoir-faire cochés) ====
        sfModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Col 0 = Nom (non éditable), colonnes SF (booléens) éditables
                return column > 0;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return String.class;
                return Boolean.class;
            }
        };
        sfModel.addColumn("Nom");
        for (SavoirFaire sf : savoirFaireTypes) {
            sfModel.addColumn(sf.getNom());
        }
        JTable sfTable = new JTable(sfModel);
        JScrollPane sfScroll = new JScrollPane(sfTable);
        sfScroll.setBorder(BorderFactory.createTitledBorder("Part savoir-faire (cocher les savoir-faire attribués)"));

        // ==== Bouton de calcul ====
        JButton computeButton = new JButton("Calculer les bénéfices");

        // ==== Zone de résultats ====
        resultArea = new JTextArea(10, 60);
        resultArea.setEditable(false);
        JScrollPane resultScroll = new JScrollPane(resultArea);
        resultScroll.setBorder(BorderFactory.createTitledBorder("Résultats"));

        // ==== Organisation verticale centre ====
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new GridLayout(3, 1));
        centerPanel.add(assoScroll);
        centerPanel.add(variableScroll);
        centerPanel.add(sfScroll);

        // ==== Bas ====
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.add(computeButton, BorderLayout.NORTH);
        bottomPanel.add(resultScroll, BorderLayout.CENTER);

        // ==== Contenu global ====
        JPanel content = new JPanel();
        content.setLayout(new BorderLayout());
        content.add(topPanel, BorderLayout.NORTH);

        JPanel topCenter = new JPanel(new BorderLayout());
        topCenter.add(createAssoPanel, BorderLayout.NORTH);
        topCenter.add(centerPanel, BorderLayout.CENTER);

        content.add(topCenter, BorderLayout.CENTER);
        content.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(content);

        // ==== Actions ====
        addAssoButton.addActionListener(e -> {
            String name = newAssoField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Le nom de l'association ne peut pas être vide.",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            addAssociation(name);
            newAssoField.setText("");
            saveAll();
        });

        removeAssoButton.addActionListener(e -> {
            int row = assoTable.getSelectedRow();
            if (row >= 0) {
                String name = assoModel.getValueAt(row, 0).toString();
                removeAssociationByName(name);
                saveAll();
            } else {
                JOptionPane.showMessageDialog(this, "Sélectionnez une association à supprimer.",
                        "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        computeButton.addActionListener(e -> computeBenefits());

        settingsButton.addActionListener(e -> {
            SettingsDialog dialog = new SettingsDialog(
                    this,
                    savoirFaireTypes,
                    pointTypes
            );
            dialog.setVisible(true);
            // Les paramètres sont sauvegardés par SettingsDialog (Save.saveParams)
        });

        // ==== Sauvegarde automatique sur édition des tables ====
        TableModelListener autoSaveListenerVar = (TableModelEvent e) -> {
            if (loading) return;
            if (e.getType() != TableModelEvent.UPDATE) return;
            int col = e.getColumn();
            if (col == 0) return; // ignorer la colonne "Type de point"
            saveAll();
        };
        variableModel.addTableModelListener(autoSaveListenerVar);

        TableModelListener autoSaveListenerSf = (TableModelEvent e) -> {
            if (loading) return;
            if (e.getType() != TableModelEvent.UPDATE) return;
            int col = e.getColumn();
            if (col == 0) return; // ignorer la colonne "Nom"
            saveAll();
        };
        sfModel.addTableModelListener(autoSaveListenerSf);
    }

    // === Gestion des associations et synchronisation avec les tableaux ===

    private void addAssociation(String name) {
        assoModel.addRow(new Object[]{name});
        variableModel.addColumn(name);

        Object[] row = new Object[1 + savoirFaireTypes.size()];
        row[0] = name;
        for (int i = 1; i < row.length; i++) {
            row[i] = Boolean.FALSE;
        }
        sfModel.addRow(row);
    }

    private void removeAssociationByName(String name) {
        removeRowByName(assoModel, name);
        removeRowByName(sfModel, name);
        removeVariableColumnByName(name);
    }

    private void removeRowByName(DefaultTableModel model, String name) {
        for (int i = model.getRowCount() - 1; i >= 0; i--) {
            Object val = model.getValueAt(i, 0);
            if (val != null && val.toString().equals(name)) {
                model.removeRow(i);
            }
        }
    }

    private void removeVariableColumnByName(String name) {
        int colCount = variableModel.getColumnCount();
        int colIndex = -1;
        for (int c = 1; c < colCount; c++) { // 0 = type
            if (variableModel.getColumnName(c).equals(name)) {
                colIndex = c;
                break;
            }
        }
        if (colIndex >= 0) {
            TableColumnRemover.removeColumn(variableModel, colIndex);
        }
    }

    // === Chargement via fichiers CSV ===

    private void loadFromFiles() {
        loading = true;

        assoModel.setRowCount(0);
        sfModel.setRowCount(0);

        // Réinit table des points à partir des types actuels
        variableModel.setColumnCount(0);
        variableModel.setRowCount(0);
        variableModel.addColumn("Type de point");
        for (PointType pt : pointTypes) {
            variableModel.addRow(new Object[]{pt.getNom()});
        }

        // 1) Assos
        File assoFile = new File(ASSO_FILE);
        List<String> assosInFile = new ArrayList<>();
        if (assoFile.exists()) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(assoFile), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String name = line.trim();
                    if (!name.isEmpty()) {
                        assosInFile.add(name);
                        addAssociation(name);
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Erreur lors du chargement des associations : " + e.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }

        // 2) Points
        File pointsFile = new File(DATA_POINTS_FILE);
        if (pointsFile.exists()) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(pointsFile), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(";");
                    if (parts.length >= 1) {
                        String typeName = parts[0];
                        int rowIndex = findRowIndexForType(typeName);
                        if (rowIndex >= 0) {
                            for (int i = 1; i < parts.length && i <= assosInFile.size(); i++) {
                                String value = parts[i];
                                variableModel.setValueAt(value, rowIndex, i);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Erreur lors du chargement des points : " + e.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }

        // 3) Savoir-faire
        File sfFile = new File(DATA_SF_FILE);
        if (sfFile.exists()) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(sfFile), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(";");
                    if (parts.length >= 1) {
                        String name = parts[0];
                        int row = findSfRowForName(name);
                        if (row >= 0) {
                            for (int i = 1; i < parts.length && i <= savoirFaireTypes.size(); i++) {
                                sfModel.setValueAt(Boolean.parseBoolean(parts[i]), row, i);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Erreur lors du chargement des savoir-faire : " + e.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }

        loading = false;
    }

    private int findRowIndexForType(String typeName) {
        for (int i = 0; i < variableModel.getRowCount(); i++) {
            Object val = variableModel.getValueAt(i, 0);
            if (val != null && val.toString().equals(typeName)) {
                return i;
            }
        }
        return -1;
    }

    private int findSfRowForName(String name) {
        for (int i = 0; i < sfModel.getRowCount(); i++) {
            Object n = sfModel.getValueAt(i, 0);
            if (n != null && n.toString().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    // === Sauvegardes via Save ===

    private void saveAll() {
        System.out.println("saveAll() appelé");
        try {
            Save.saveAssociations(assoModel);
            Save.savePoints(variableModel);
            Save.saveSavoirFaire(sfModel, savoirFaireTypes.size());
            Save.saveParams(savoirFaireTypes, pointTypes);
            System.out.println("saveAll() terminé");
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de la sauvegarde : " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    // === Calcul des bénéfices (tout le calcul dans BenefitCalculator) ===

    private void computeBenefits() {
        String totalText = totalProfitField.getText().trim().replace(',', '.');
        if (totalText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez saisir le bénéfice total.",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double totalProfit;
        try {
            totalProfit = Double.parseDouble(totalText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Le bénéfice total doit être un nombre.",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int assoCount = assoModel.getRowCount();
        if (assoCount == 0) {
            JOptionPane.showMessageDialog(this, "Veuillez ajouter au moins une association.",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Association> associations = new ArrayList<>();
        for (int a = 0; a < assoCount; a++) {
            String name = assoModel.getValueAt(a, 0).toString();
            associations.add(new Association(name, 0.0, 0.0));
        }

        // Matrice des points bruts : [type][asso]
        int typeCount = variableModel.getRowCount();
        double[][] rawPoints = new double[typeCount][assoCount];

        for (int t = 0; t < typeCount; t++) {
            for (int a = 0; a < assoCount; a++) {
                String assoName = assoModel.getValueAt(a, 0).toString();
                int colIndex = findVariableColumnForAsso(assoName);
                if (colIndex < 0) continue;
                Object v = variableModel.getValueAt(t, colIndex);
                if (v != null && !v.toString().isEmpty()) {
                    try {
                        rawPoints[t][a] = Double.parseDouble(v.toString().replace(',', '.'));
                    } catch (NumberFormatException ignored) {
                        rawPoints[t][a] = 0.0;
                    }
                }
            }
        }

        // Matrice des savoir-faire : [sfIndex][asso]
        int sfCount = savoirFaireTypes.size();
        boolean[][] sfSelected = new boolean[sfCount][assoCount];

        for (int a = 0; a < assoCount; a++) {
            String name = assoModel.getValueAt(a, 0).toString();
            int row = findSfRowForName(name);
            if (row < 0) continue;
            for (int i = 0; i < sfCount; i++) {
                Object v = sfModel.getValueAt(row, i + 1);
                sfSelected[i][a] = (v instanceof Boolean) && (Boolean) v;
            }
        }

        BenefitCalculator calculator = new BenefitCalculator();
        try {
            calculator.computeBenefits(
                    totalProfit,
                    associations,
                    pointTypes,
                    rawPoints,
                    savoirFaireTypes,
                    sfSelected
            );
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Erreur de calcul", JOptionPane.ERROR_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder();
        double totalCheck = 0.0;
        sb.append("=== Résultats de la répartition ===\n");
        for (Association asso : associations) {
            sb.append("\nAssociation : ").append(asso.getName()).append("\n");
            sb.append("  Part fixe         : ").append(asso.getPartFixe()).append(" €\n");
            sb.append("  Part variable     : ").append(asso.getPartVariable()).append(" €\n");
            sb.append("  Part savoir-faire : ").append(asso.getPartSavoirFaire()).append(" €\n");
            sb.append("  TOTAL             : ").append(asso.getTotal()).append(" €\n");
            totalCheck += asso.getTotal();
        }
        sb.append("\nVérification : somme totale répartie = ")
                .append(Math.round(totalCheck * 100.0) / 100.0)
                .append(" €\n");

        resultArea.setText(sb.toString());

        // Sauvegarde complète après calcul
        saveAll();
    }

    private int findVariableColumnForAsso(String name) {
        int colCount = variableModel.getColumnCount();
        for (int c = 1; c < colCount; c++) {
            if (variableModel.getColumnName(c).equals(name)) {
                return c;
            }
        }
        return -1;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            App app = new App();
            app.setVisible(true);
        });
    }

    // === Utilitaire pour supprimer une colonne d'un DefaultTableModel ===
    private static class TableColumnRemover {
        static void removeColumn(DefaultTableModel model, int colIndex) {
            int colCount = model.getColumnCount();
            int rowCount = model.getRowCount();
            if (colIndex < 0 || colIndex >= colCount) return;

            String[] newColumnNames = new String[colCount - 1];
            int idx = 0;
            for (int c = 0; c < colCount; c++) {
                if (c == colIndex) continue;
                newColumnNames[idx++] = model.getColumnName(c);
            }

            DefaultTableModel newModel = new DefaultTableModel(newColumnNames, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return column > 0;
                }

                @Override
                public Class<?> getColumnClass(int columnIndex) {
                    return Object.class;
                }
            };

            for (int r = 0; r < rowCount; r++) {
                Object[] newRow = new Object[colCount - 1];
                idx = 0;
                for (int c = 0; c < colCount; c++) {
                    if (c == colIndex) continue;
                    newRow[idx++] = model.getValueAt(r, c);
                }
                newModel.addRow(newRow);
            }

            model.setDataVector(newModel.getDataVector(), convertToVector(newColumnNames));
        }

        @SuppressWarnings("rawtypes")
        private static java.util.Vector convertToVector(Object[] data) {
            java.util.Vector<Object> v = new java.util.Vector<>();
            for (Object o : data) {
                v.add(o);
            }
            return v;
        }
    }
}
