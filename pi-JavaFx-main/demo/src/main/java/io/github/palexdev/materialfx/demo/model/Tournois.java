package io.github.palexdev.materialfx.demo.model;


import java.time.LocalDate;

public class Tournois {
    private String reglements;

    private int id;
    private String nom;
    private String format;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private int nbEquipe;
    private String tournoisLocation;
    private int idorganiser;
    public int getIdorganiser() {
        return idorganiser;
    }

    public void setIdorganiser(int idorganiser) {
        this.idorganiser = idorganiser;
    }



    public Tournois() {
    }

    // Getters and Setters
    public int getId() { return id; }
  

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public int getNbEquipe() { return nbEquipe; }
    public void setNbEquipe(int nbEquipe) { this.nbEquipe = nbEquipe; }

    public String getTournoisLocation() { return tournoisLocation; }
    public void setTournoisLocation(String tournoisLocation) { this.tournoisLocation = tournoisLocation; }


    public void setId(int id) {
        this.id = id;
    }
    public String getReglements() {
        return reglements;
    }

    public void setReglements(String reglements) {
        this.reglements = reglements;
    }


}