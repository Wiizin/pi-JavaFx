package io.github.palexdev.materialfx.demo.model;

import java.util.ArrayList;
import java.util.List;

public class Team {
    private int id;
    private String nom;
    private String categorie;
    private ModeJeu modeJeu;
    private int nombreJoueurs;



    private int idtournoi;
    private List<User> membres;


    private String logoPath;
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
    public Team(int id, String nom, String categorie, ModeJeu modeJeu, int nombreJoueurs,String logoPath) {
        this.id = id;
        this.nom = nom;
        this.categorie = categorie;
        this.modeJeu = modeJeu;
        this.nombreJoueurs = nombreJoueurs;
        this.logoPath = logoPath;
        this.membres = new ArrayList<>();
    }
    public Team(int id, String nom, String categorie, ModeJeu modeJeu, int nombreJoueurs,String logoPath,int idtournoi) {
        this.id = id;
        this.nom = nom;
        this.categorie = categorie;
        this.modeJeu = modeJeu;
        this.nombreJoueurs = nombreJoueurs;
        this.logoPath = logoPath;
        this.membres = new ArrayList<>();
        this.idtournoi = idtournoi;
    }
    public int getIdtournoi() {
        return idtournoi;
    }

    public void setIdtournoi(int idtournoi) {
        this.idtournoi = idtournoi;
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
    public String getLogoPath() {
        return logoPath;
    }

    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }

    public List<User> getMembres() {
        return membres;
    }

    public void setMembres(List<User> membres) {
        this.membres = membres;
    }
    public void setNombreJoueurs(int nombreJoueurs) {
        this.nombreJoueurs = nombreJoueurs;
    }
    @java.lang.Override
    public java.lang.String toString() {
        return "Team{" +
                "id=" + id +
                ", nom=" + nom +
                ", categorie=" + categorie +
                ", ModeJeu=" + modeJeu +
                ", nombreJoueurs"+nombreJoueurs+
                ", logoPath"+logoPath+
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
