package org.example.entities;

import java.sql.Date;

public class MarketplaceRating {
    private int idRating;
    private int idVente;
    private String customerId;
    private int note;
    private Date dateRating;

    public MarketplaceRating() {
    }

    public MarketplaceRating(int idVente, String customerId, int note, Date dateRating) {
        this.idVente = idVente;
        this.customerId = customerId;
        this.note = note;
        this.dateRating = dateRating;
    }

    public int getIdRating() {
        return idRating;
    }

    public void setIdRating(int idRating) {
        this.idRating = idRating;
    }

    public int getIdVente() {
        return idVente;
    }

    public void setIdVente(int idVente) {
        this.idVente = idVente;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public int getNote() {
        return note;
    }

    public void setNote(int note) {
        this.note = note;
    }

    public Date getDateRating() {
        return dateRating;
    }

    public void setDateRating(Date dateRating) {
        this.dateRating = dateRating;
    }
}
