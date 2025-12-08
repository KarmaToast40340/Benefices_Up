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

        // ===== Onglet Savoir-faire (ajout / suppression / édition nom + poids) =====
        JPanel sfPanel = new JPanel(new BorderLayout());
        sfModel = new DefaultTableModel(new String[]{"Nom", "Poids"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // nom et poids éditables
                return true;
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

        JPanel sfButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addSfBtn = new JButton("Ajouter un savoir-faire");
        JButton removeSfBtn = new JButton("Supprimer le savoir-faire sélectionné");
        sfButtons.add(addSfBtn);
        sfButtons.add(removeSfBtn);
        sfPanel.add(sfButtons, BorderLayout.SOUTH);

        tabs.addTab("Savoir-faire", sfPanel);

        // ===== Onglet Types de points (ajout / suppression / édition nom + poids) =====
        JPanel ptPanel = new JPanel(new BorderLayout());
        pointsTypeModel = new DefaultTableModel(new String[]{"Type de point", "Poids"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
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

        JPanel ptButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addPtBtn = new JButton("Ajouter un type");
        JButton removePtBtn = new JButton("Supprimer le type sélectionné");
        ptButtons.add(addPtBtn);
        ptButtons.add(removePtBtn);
        ptPanel.add(ptButtons, BorderLayout.SOUTH);

        tabs.addTab("Part variable", ptPanel);

        // ===== Bas de fenêtre : bouton Fermer =====
        JButton closeButton = new JButton("Fermer");
        closeButton.addActionListener(e -> dispose());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(closeButton);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(tabs, BorderLayout.CENTER);
        getContentPane().add(bottom, BorderLayout.SOUTH);

        // === Actions sur Savoir-faire ===
        addSfBtn.addActionListener(e -> {
            sfModel.addRow(new Object[]{"Nouveau savoir-faire", 0.0});
            applyChanges();
        });

        removeSfBtn.addActionListener(e -> {
            int row = sfTable.getSelectedRow();
            if (row >= 0) {
                sfModel.removeRow(row);
                applyChanges();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Sélectionnez un savoir-faire à supprimer.",
                        "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // === Actions sur Types de points ===
        addPtBtn.addActionListener(e -> {
            pointsTypeModel.addRow(new Object[]{"Nouveau type", 1.0});
            applyChanges();
        });

        removePtBtn.addActionListener(e -> {
            int row = ptTable.getSelectedRow();
            if (row >= 0) {
                pointsTypeModel.removeRow(row);
                applyChanges();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Sélectionnez un type de point à supprimer.",
                        "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // === Sauvegarde auto sur modification de cellule ===
        TableModelListener sfListener = (TableModelEvent e) -> {
            if (loading) return;
            if (e.getType() != TableModelEvent.UPDATE) return;
            applyChanges();
        };
        sfModel.addTableModelListener(sfListener);

        TableModelListener ptListener = (TableModelEvent e) -> {
            if (loading) return;
            if (e.getType() != TableModelEvent.UPDATE) return;
            applyChanges();
        };
        pointsTypeModel.addTableModelListener(ptListener);

        loading = false;
    }

    private void stopTableEditing() {
        for (Window w : Window.getWindows()) {
            if (w.isFocused()) {
                for (Component c : w.getComponents()) {
                    if (c instanceof JTable) {
                        JTable t = (JTable) c;
                        if (t.isEditing()) {
                            t.getCellEditor().stopCellEditing();
                        }
                    }
                }
            }
        }
    }

    private boolean applyChanges() {
        stopTableEditing();
        // 1) Re-synchroniser complètement savoirFaireTypes à partir du modèle
        savoirFaireTypes.clear();
        for (int row = 0; row < sfModel.getRowCount(); row++) {
            Object nomObj = sfModel.getValueAt(row, 0);
            Object poidsObj = sfModel.getValueAt(row, 1);
            String nom = nomObj == null ? "" : nomObj.toString().trim();
            if (nom.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Le nom d'un savoir-faire ne peut pas être vide (ligne " + (row + 1) + ").",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            double poids;
            try {
                poids = Double.parseDouble(poidsObj.toString());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Poids invalide pour le savoir-faire \"" + nom + "\" (ligne " + (row + 1) + ").",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            savoirFaireTypes.add(new SavoirFaire(nom, poids));
        }

        // 2) Re-synchroniser complètement pointTypes à partir du modèle
        pointTypes.clear();
        for (int row = 0; row < pointsTypeModel.getRowCount(); row++) {
            Object nomObj = pointsTypeModel.getValueAt(row, 0);
            Object poidsObj = pointsTypeModel.getValueAt(row, 1);
            String nom = nomObj == null ? "" : nomObj.toString().trim();
            if (nom.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Le nom d'un type de point ne peut pas être vide (ligne " + (row + 1) + ").",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            double poids;
            try {
                poids = Double.parseDouble(poidsObj.toString());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Poids invalide pour le type de point \"" + nom + "\" (ligne " + (row + 1) + ").",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            pointTypes.add(new PointType(nom, poids));
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
