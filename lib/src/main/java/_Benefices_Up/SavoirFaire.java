package _Benefices_Up;

public class SavoirFaire {

    private String nom;
    private double poids;

    public SavoirFaire(String nom, double poids) {
        this.nom = nom;
        this.poids = poids;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public double getPoids() {
        return poids;
    }

    public void setPoids(double poids) {
        this.poids = poids;
    }
}
