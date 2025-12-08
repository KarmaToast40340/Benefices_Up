package _Benefices_Up;

public class Association {

    private String name;
    private double pointsVariable;        // indice d'investissement
    private double savoirFairePercent;    // % de la part savoir-faire (0-100)

    // résultats
    private double partFixe;
    private double partVariable;
    private double partSavoirFaire;
    private double total;

    public Association(String name, double pointsVariable, double savoirFairePercent) {
        this.name = name;
        this.pointsVariable = pointsVariable;
        this.savoirFairePercent = savoirFairePercent;
    }

    public String getName() {
        return name;
    }

    public double getPointsVariable() {
        return pointsVariable;
    }

    public double getSavoirFairePercent() {
        return savoirFairePercent;
    }

    public double getPartFixe() {
        return partFixe;
    }

    public double getPartVariable() {
        return partVariable;
    }

    public double getPartSavoirFaire() {
        return partSavoirFaire;
    }

    public double getTotal() {
        return total;
    }

    public void setPartFixe(double partFixe) {
        this.partFixe = partFixe;
        updateTotal();
    }

    public void setPartVariable(double partVariable) {
        this.partVariable = partVariable;
        updateTotal();
    }

    public void setPartSavoirFaire(double partSavoirFaire) {
        this.partSavoirFaire = partSavoirFaire;
        updateTotal();
    }

    private void updateTotal() {
        this.total = partFixe + partVariable + partSavoirFaire;
    }

    @Override
    public String toString() {
        return "Association{" +
                "name='" + name + '\'' +
                ", partFixe=" + partFixe +
                ", partVariable=" + partVariable +
                ", partSavoirFaire=" + partSavoirFaire +
                ", total=" + total +
                '}';
    }
}
