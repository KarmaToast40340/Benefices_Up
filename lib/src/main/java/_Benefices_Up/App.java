package _Benefices_Up;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
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

    // Savoir-faire de la charte avec leurs poids (modifiable)
    private final List<SavoirFaire> savoirFaireTypes = new ArrayList<>(Arrays.asList(
            new SavoirFaire("Prog artistique", 11.0),
            new SavoirFaire("Malle de prévention", 3.0),
            new SavoirFaire("Visuel", 3.0),
            new SavoirFaire("Déco", 3.0)
    ));

    // Pour ne pas sauvegarder pendant le chargement / refresh
    private boolean loading = false;

    public App() {
        Locale.setDefault(Locale.FRENCH);
        initPointTypes();
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

        // ==== Haut : création d'asso + bouton paramètres ====
        JPanel topBar = new JPanel(new BorderLayout());

        JPanel createAssoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        createAssoPanel.setBorder(BorderFactory.createTitledBorder("Créer une association"));

        JTextField newAssoField = new JTextField(15);
        JButton addAssoButton = new JButton("Ajouter l'association");
        JButton removeAssoButton = new JButton("Supprimer sélection");

        createAssoPanel.add(new JLabel("Nom :"));
        createAssoPanel.add(newAssoField);
        createAssoPanel.add(addAssoButton);
        createAssoPanel.add(removeAssoButton);

        JPanel settingsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton settingsButton = new JButton("Paramètres");
        settingsPanel.add(settingsButton);

        topBar.add(createAssoPanel, BorderLayout.CENTER);
        topBar.add(settingsPanel, BorderLayout.EAST);

        // ==== Table 1 : associations ====
        assoModel = new DefaultTableModel(new String[]{"Nom"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable assoTable = new JTable(assoModel);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        assoTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        JScrollPane assoScroll = new JScrollPane(assoTable);
        assoScroll.setBorder(BorderFactory.createTitledBorder("Associations"));

        // ==== Table 2 : part variable ====
        variableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
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

        // ==== Table 3 : part savoir-faire ====
        sfModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
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

        // ==== Centre (3 tables) ====
        JPanel centerPanel = new JPanel(new GridLayout(3, 1));
        centerPanel.add(assoScroll);
        centerPanel.add(variableScroll);
        centerPanel.add(sfScroll);

        // ==== Bas : bénéfice total + bouton calcul + résultats ====
        JPanel bottomPanel = new JPanel(new BorderLayout());

        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        totalPanel.add(new JLabel("Bénéfice total (€) :"));
        totalProfitField = new JTextField(10);
        totalPanel.add(totalProfitField);

        JButton computeButton = new JButton("Calculer les bénéfices par Asso");

        JPanel bottomTop = new JPanel(new BorderLayout());
        bottomTop.add(totalPanel, BorderLayout.NORTH);
        bottomTop.add(computeButton, BorderLayout.SOUTH);

        resultArea = new JTextArea(10, 60);
        resultArea.setEditable(false);
        JScrollPane resultScroll = new JScrollPane(resultArea);
        resultScroll.setBorder(BorderFactory.createTitledBorder("Résultats"));

        bottomPanel.add(bottomTop, BorderLayout.NORTH);
        bottomPanel.add(resultScroll, BorderLayout.CENTER);

        JPanel content = new JPanel(new BorderLayout());
        content.add(topBar, BorderLayout.NORTH);
        content.add(centerPanel, BorderLayout.CENTER);
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
            dialog.setVisible(true);        // modal
            refreshPointTypesFromModel();   // met à jour les lignes types de points
            refreshSavoirFaireFromModel();  // met à jour les colonnes savoir-faire
            saveAll();
        });

        // Sauvegarde auto sur édition des tableaux
        TableModelListener autoSaveListenerVar = (TableModelEvent e) -> {
            if (loading) return;
            if (e.getType() != TableModelEvent.UPDATE) return;
            int col = e.getColumn();
            if (col == 0) return;
            saveAll();
        };
        variableModel.addTableModelListener(autoSaveListenerVar);

        TableModelListener autoSaveListenerSf = (TableModelEvent e) -> {
            if (loading) return;
            if (e.getType() != TableModelEvent.UPDATE) return;
            int col = e.getColumn();
            if (col == 0) return;
            saveAll();
        };
        sfModel.addTableModelListener(autoSaveListenerSf);
    }

    // === Refresh des types de points après SettingsDialog ===
    public void refreshPointTypesFromModel() {
        loading = true;

        int rowCount = variableModel.getRowCount();
        int colCount = variableModel.getColumnCount();
        java.util.Map<String, java.util.Map<String, String>> saved = new java.util.HashMap<>();

        for (int r = 0; r < rowCount; r++) {
            Object typeNameObj = variableModel.getValueAt(r, 0);
            if (typeNameObj == null) continue;
            String typeName = typeNameObj.toString();
            java.util.Map<String, String> perAsso = new java.util.HashMap<>();
            for (int c = 1; c < colCount; c++) {
                String assoName = variableModel.getColumnName(c);
                Object v = variableModel.getValueAt(r, c);
                if (v != null && !v.toString().isEmpty()) {
                    perAsso.put(assoName, v.toString());
                }
            }
            saved.put(typeName, perAsso);
        }

        variableModel.setRowCount(0);
        variableModel.setColumnCount(0);
        variableModel.addColumn("Type de point");

        int assoCount = assoModel.getRowCount();
        for (int i = 0; i < assoCount; i++) {
            String assoName = assoModel.getValueAt(i, 0).toString();
            variableModel.addColumn(assoName);
        }

        for (PointType pt : pointTypes) {
            String typeName = pt.getNom();
            Object[] row = new Object[1 + assoCount];
            row[0] = typeName;
            java.util.Map<String, String> perAsso = saved.get(typeName);
            for (int i = 0; i < assoCount; i++) {
                String assoName = assoModel.getValueAt(i, 0).toString();
                if (perAsso != null && perAsso.containsKey(assoName)) {
                    row[i + 1] = perAsso.get(assoName);
                } else {
                    row[i + 1] = "";
                }
            }
            variableModel.addRow(row);
        }

        loading = false;
    }

    // === Refresh des savoir-faire après SettingsDialog ===
    public void refreshSavoirFaireFromModel() {
        loading = true;

        // sauvegarde des cases cochées : [asso][sfName] -> boolean
        java.util.Map<String, java.util.Map<String, Boolean>> saved = new java.util.HashMap<>();

        int rowCount = sfModel.getRowCount();
        int colCount = sfModel.getColumnCount();

        for (int r = 0; r < rowCount; r++) {
            Object assoNameObj = sfModel.getValueAt(r, 0);
            if (assoNameObj == null) continue;
            String assoName = assoNameObj.toString();
            java.util.Map<String, Boolean> perSf = new java.util.HashMap<>();
            for (int c = 1; c < colCount; c++) {
                String sfName = sfModel.getColumnName(c);
                Object v = sfModel.getValueAt(r, c);
                if (v instanceof Boolean) {
                    perSf.put(sfName, (Boolean) v);
                }
            }
            saved.put(assoName, perSf);
        }

        sfModel.setRowCount(0);
        sfModel.setColumnCount(0);
        sfModel.addColumn("Nom");
        for (SavoirFaire sf : savoirFaireTypes) {
            sfModel.addColumn(sf.getNom());
        }

        int assoCount = assoModel.getRowCount();
        for (int i = 0; i < assoCount; i++) {
            String assoName = assoModel.getValueAt(i, 0).toString();
            Object[] row = new Object[1 + savoirFaireTypes.size()];
            row[0] = assoName;
            java.util.Map<String, Boolean> perSf = saved.get(assoName);
            for (int s = 0; s < savoirFaireTypes.size(); s++) {
                String sfName = savoirFaireTypes.get(s).getNom();
                Boolean val = perSf != null ? perSf.get(sfName) : null;
                row[s + 1] = val != null ? val : Boolean.FALSE;
            }
            sfModel.addRow(row);
        }

        loading = false;
    }

    // === Gestion des associations ===

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
        for (int c = 1; c < colCount; c++) {
            if (variableModel.getColumnName(c).equals(name)) {
                colIndex = c;
                break;
            }
        }
        if (colIndex >= 0) {
            TableColumnRemover.removeColumn(variableModel, colIndex);
        }
    }

    // === Chargement des fichiers CSV ===

    private void loadFromFiles() {
        loading = true;

        assoModel.setRowCount(0);
        sfModel.setRowCount(0);

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

    // === Sauvegardes ===

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

    // === Calcul des bénéfices ===

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

    // === Utilitaire : suppression de colonne ===
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
