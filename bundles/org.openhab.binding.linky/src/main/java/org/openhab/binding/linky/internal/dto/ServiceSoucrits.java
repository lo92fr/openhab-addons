package org.openhab.binding.linky.internal.dto;

public class ServiceSoucrits {

    public int nbTotalServices;
    public ServiceSouscrit[] serviceSouscrit;

    public class ServiceSouscrit {
        public int id;
        public boolean injection;
        public boolean soutirage;
        public String serviceCode;
        public String dateDebut;
        public String dateFin;
        public String etatCode;
        public String etatLibelle;
        public String pointId;
        public String sirentBeneficiaire;
        public String mesuresTypeCode;
        public String mesurePas;
        public boolean publicationDonnees;

        public Autorisation autorisation;

        public class Autorisation {
            public int autorisationId;
            public String autorisationLibelle;
            public String autorisationType;
            public String autorisationStatut;
        }
    }
}
