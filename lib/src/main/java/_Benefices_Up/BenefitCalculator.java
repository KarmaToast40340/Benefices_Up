package _Benefices_Up;

import java.util.List;

public class BenefitCalculator {

    private double fixedPartRatio = 0.5;        // 50 %
    private double variablePartRatio = 0.3;     // 30 %
    private double savoirFairePartRatio = 0.2;  // 20 %

    public BenefitCalculator() {}

    public BenefitCalculator(double fixedPartRatio, double variablePartRatio, double savoirFairePartRatio) {
        this.fixedPartRatio = fixedPartRatio;
        this.variablePartRatio = variablePartRatio;
        this.savoirFairePartRatio = savoirFairePartRatio;
    }

    /**
     * @param totalProfit        bénéfice total
     * @param associations       liste des assos
     * @param pointTypes         liste des types de points (avec poids)
     * @param rawPoints          rawPoints[typeIndex][assoIndex] = valeur saisie dans l'UI
     * @param savoirFaireTypes   liste des savoir-faire (avec poids) [file:2]
     * @param sfSelected         sfSelected[sfIndex][assoIndex] (cases cochées)
     */
    public void computeBenefits(
            double totalProfit,
            List<Association> associations,
            List<PointType> pointTypes,
            double[][] rawPoints,
            List<SavoirFaire> savoirFaireTypes,
            boolean[][] sfSelected
    ) {
        if (associations == null || associations.isEmpty()) {
            throw new IllegalArgumentException("Il doit y avoir au moins une association.");
        }

        int n = associations.size();

        // 1) Points variables par asso (lignes pondérées par le poids du PointType)
        double[] pointsPerAsso = computePointsPerAssociation(pointTypes, rawPoints, n);

        // 2) Scores de savoir-faire par asso
        double[] sfScoresPerAsso = computeSavoirFaireScoresPerAssociation(
                savoirFaireTypes, sfSelected, n
        );

        // 3) Répartition
        distributeBenefits(totalProfit, associations, pointsPerAsso, sfScoresPerAsso);
    }

    private double[] computePointsPerAssociation(
            List<PointType> pointTypes,
            double[][] rawPoints,
            int assoCount
    ) {
        double[] pointsPerAsso = new double[assoCount];

        if (rawPoints == null || pointTypes == null) {
            return pointsPerAsso;
        }

        int typeCount = Math.min(rawPoints.length, pointTypes.size());
        for (int typeIndex = 0; typeIndex < typeCount; typeIndex++) {
            double[] row = rawPoints[typeIndex];
            if (row == null) continue;
            double poids = pointTypes.get(typeIndex).getPoids();
            for (int a = 0; a < assoCount && a < row.length; a++) {
                pointsPerAsso[a] += safeDouble(row[a]) * poids;
            }
        }
        return pointsPerAsso;
    }

    private double[] computeSavoirFaireScoresPerAssociation(
            List<SavoirFaire> savoirFaireTypes,
            boolean[][] sfSelected,
            int assoCount
    ) {
        double[] scores = new double[assoCount];

        if (savoirFaireTypes == null || sfSelected == null) {
            return scores;
        }

        int sfCount = Math.min(savoirFaireTypes.size(), sfSelected.length);
        for (int sfIndex = 0; sfIndex < sfCount; sfIndex++) {
            boolean[] row = sfSelected[sfIndex];
            if (row == null) continue;
            double poids = savoirFaireTypes.get(sfIndex).getPoids(); // ex 11,3,3,3 [file:2]
            for (int a = 0; a < assoCount && a < row.length; a++) {
                if (row[a]) {
                    scores[a] += poids;
                }
            }
        }
        return scores;
    }

    private void distributeBenefits(
            double totalProfit,
            List<Association> associations,
            double[] pointsPerAsso,
            double[] sfScoresPerAsso
    ) {
        int n = associations.size();

        double totalFixed = totalProfit * fixedPartRatio;
        double totalVariable = totalProfit * variablePartRatio;
        double totalSavoirFaire = totalProfit * savoirFairePartRatio;

        double fixedPerAsso = totalFixed / n;

        double totalPoints = 0.0;
        for (double p : pointsPerAsso) totalPoints += p;

        if (totalPoints <= 0 && totalVariable > 0) {
            throw new IllegalArgumentException("La somme des points doit être > 0 pour répartir la part variable.");
        }

        double totalSfScore = 0.0;
        for (double s : sfScoresPerAsso) totalSfScore += s;

        if (totalSfScore <= 0 && totalSavoirFaire > 0) {
            throw new IllegalArgumentException("Aucun savoir-faire attribué, impossible de répartir la part savoir-faire.");
        }

        for (int i = 0; i < n; i++) {
            Association asso = associations.get(i);

            asso.setPartFixe(round2(fixedPerAsso));

            double varAmount = 0.0;
            if (totalPoints > 0) {
                varAmount = totalVariable * (pointsPerAsso[i] / totalPoints);
            }
            asso.setPartVariable(round2(varAmount));

            double sfAmount = 0.0;
            if (totalSfScore > 0) {
                sfAmount = totalSavoirFaire * (sfScoresPerAsso[i] / totalSfScore);
            }
            asso.setPartSavoirFaire(round2(sfAmount));
        }
    }

    private double safeDouble(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) return 0.0;
        return v;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
