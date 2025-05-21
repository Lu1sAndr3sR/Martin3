/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

/**
 *
 * @author aldair
 */
public enum TipoDato {
    NUMERIC("NUMERIC"),
    STRING("STRING"),
    DATE("DATE");
    private final String tipo;

    TipoDato(String ad) {
        this.tipo = ad;
    }

    public String getTipo() {
        return tipo;
    }

    public static TipoDato toTipoDato(String tipo) {
        switch (tipo.toUpperCase()) {
            case "NUMERIC":
                return NUMERIC;
            case "STRING":
                return STRING;
            case "DATE":
                return DATE;
            default:
                return null;
        }
    }
}
