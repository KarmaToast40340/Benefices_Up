package _Benefices_Up;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.TableModelListener;
import javax.swing.event.TableModelEvent;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class SettingsDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private DefaultTableModel sfModel;
    private DefaultTableModel pointsTypeModel;

    private final List<SavoirFaire> savoirFaireTypes;
    private final List<PointType> pointTypes;

    // pour éviter de sauvegarder pendant l'initialisation des modèles
    private boolean loading = false;

    public SettingsDialog(JFrame owner,
                          List<SavoirFaire> savoirFaireTypes,
                          List<PointType> pointTypes) {
        super(owner, "Paramètres de répartition", true);
        this.savoirFaireTypes = savoirFaireTypes;
        this.pointTypes = pointTypes;
        initUI();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        loading = true;

        JTabbedPane tabs = new JTabbedPane();

        // Onglet Savoir-faire
        JPanel sfPanel = new JPanel(new BorderLayout());
        sfModel = new DefaultTableModel(new String[]{"Nom", "Poids"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Nom non éditable, poids éditable
                return column == 1;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 1 ? Double.class : String.class;
            }
        };
        for (SavoirFaire sf : savoirFaireTypes) {
            sfModel.addRow(new Object[]{sf.getNom(), sf.getPoids()});
        }
        JTable sfTable = new JTable(sfModel);
        sfPanel.add(new JScrollPane(sfTable), BorderLayout.CENTER);
        tabs.addTab("Savoir-faire", sfPanel);

        // Onglet Types de points
        JPanel ptPanel = new JPanel(new BorderLayout());
        pointsTypeModel = new DefaultTableModel(new String[]{"Type de point", "Poids"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Nom non éditable, poids éditable
                return column == 1;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 1 ? Double.class : String.class;
            }
        };
        for (PointType pt : pointTypes) {
            pointsTypeModel.addRow(new Object[]{pt.getNom(), pt.getPoids()});
        }
        JTable ptTable = new JTable(pointsTypeModel);
        ptPanel.add(new JScrollPane(ptTable), BorderLayout.CENTER);
        tabs.addTab("Part variable", ptPanel);

        // Bouton fermer (les sauvegardes se font automatiquement)
        JButton closeButton = new JButton("Fermer");
        closeButton.addActionListener(e -> dispose());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(closeButton);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(tabs, BorderLayout.CENTER);
        getContentPane().add(bottom, BorderLayout.SOUTH);

        // === Sauvegarde automatique sur modification des poids ===
        TableModelListener sfListener = (TableModelEvent e) -> {
            if (loading) return;
            if (e.getType() != TableModelEvent.UPDATE) return;
            int col = e.getColumn();
            if (col != 1) return; // on ne s'intéresse qu'à la colonne Poids
            if (!applyChanges()) {
                // en cas d'erreur, on ne ferme pas, on laisse le message d'erreur d'applyChanges
            }
        };
        sfModel.addTableModelListener(sfListener);

        TableModelListener ptListener = (TableModelEvent e) -> {
            if (loading) return;
            if (e.getType() != TableModelEvent.UPDATE) return;
            int col = e.getColumn();
            if (col != 1) return;
            if (!applyChanges()) {
                // idem
            }
        };
        pointsTypeModel.addTableModelListener(ptListener);

        loading = false;
    }

    private boolean applyChanges() {
        // 1) Mettre à jour les poids des savoir-faire
        for (int i = 0; i < savoirFaireTypes.size(); i++) {
            Object val = sfModel.getValueAt(i, 1);
            try {
                double poids = Double.parseDouble(val.toString());
                savoirFaireTypes.get(i).setPoids(poids);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Poids invalide pour le savoir-faire " + savoirFaireTypes.get(i).getNom(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        // 2) Mettre à jour les poids des types de points
        for (int i = 0; i < pointTypes.size(); i++) {
            Object val = pointsTypeModel.getValueAt(i, 1);
            try {
                double poids = Double.parseDouble(val.toString());
                pointTypes.get(i).setPoids(poids);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Poids invalide pour le type de point " + pointTypes.get(i).getNom(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        // 3) Sauvegarder les paramètres
        try {
            Save.saveParams(savoirFaireTypes, pointTypes);
            System.out.println("Params sauvegardés depuis SettingsDialog");
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de la sauvegarde des paramètres : " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }
}
