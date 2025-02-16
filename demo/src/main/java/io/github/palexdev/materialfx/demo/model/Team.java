package io.github.palexdev.materialfx.demo.model;

import java.util.ArrayList;
import java.util.List;

public class Team {
    private int id;
    private String nom;
    private String categorie;
    private ModeJeu modeJeu;
    private int nombreJoueurs;
    private List<User> membres;
    public Team() {
    }
    public Team( String nom, String categorie, ModeJeu modeJeu) {
        this.nom = nom;
        this.categorie = categorie;
        this.modeJeu = modeJeu;
        this.nombreJoueurs = 0;
        this.membres = new ArrayList<>();
    }
    public Team(int id, String nom, String categorie, ModeJeu modeJeu) {
        this.id = id;
        this.nom = nom;
        this.categorie = categorie;
        this.modeJeu = modeJeu;
        this.nombreJoueurs = 0;
        this.membres = new ArrayList<>();
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public ModeJeu getModeJeu() {
        return modeJeu;
    }

    public void setModeJeu(ModeJeu modeJeu) {
        this.modeJeu = modeJeu;
    }

    public int getNombreJoueurs() {
        return nombreJoueurs;
    }

    public void setNombreJoueurs(int nombreJoueurs) {
        this.nombreJoueurs = nombreJoueurs;
    }
    @java.lang.Override
    public java.lang.String toString() {
        return "Person{" +
                "id=" + id +
                ", nom=" + nom +
                ", categorie=" + categorie +
                ", ModeJeu=" + modeJeu +
                ", nombreJoueurs"+nombreJoueurs+
                '}';
    }
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Team equipe)) return false;
        if (!super.equals(object)) return false;

        if (id != equipe.id) return false;
        if (!java.util.Objects.equals(nom, equipe.nom)) return false;
        return java.util.Objects.equals(nom, equipe.nom);
    }
}
