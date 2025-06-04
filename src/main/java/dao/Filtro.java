/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import pojo.ErrorClass;

/**
 *
 * @author aldair
 */
public class Filtro {

    private String nombreCampo;
    private Criterio criterio;
    private TipoDato tipo;
    private Object value;

    public Filtro() {
        this.value2 = "value";
    }

    public enum Criterio {
        IGUAL("="),
        MAYORQUE(">"),
        MENORQUE("<"),
        LIKE("ilike");

        private final String tipoCriterio;

        private Criterio(String tipoCriterio) {
            this.tipoCriterio = tipoCriterio;
        }

        public String getTipoCriterio() {
            return tipoCriterio;
        }

        public static Criterio toCriterio(String tipoCriterio) {
            switch (tipoCriterio) {
                case "=":
                    return IGUAL;
                case ">":
                    return MAYORQUE;

                case "<":
                    return MENORQUE;
                case "like":
                    return LIKE;
                default:
                    return null;
            }
        }
    }

    public TipoDato getTipo() {
        return tipo;
    }

    public void setTipo(TipoDato tipo) {
        this.tipo = tipo;
    }

    public String getNombreCampo() {
        return nombreCampo;
    }

    public void setNombreCampo(String nombreCampo) {
        this.nombreCampo = nombreCampo;
    }

    public Criterio getCriterio() {
        return criterio;
    }

    public void setCriterio(Criterio criterio) {
        this.criterio = criterio;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(nombreCampo)
                .append(" ")
                .append(criterio.getTipoCriterio())
                .append(" ? ");
        return str.toString();
    }

    private final String value2;
    public void objectify(JSONObject json) throws JSONException {

        String temp = json.getString("nombre_campo");
        this.setNombreCampo(temp);
        temp = json.getString("criterio");
        this.setCriterio(Filtro.Criterio.toCriterio(temp));
        temp = json.getString("tipo");
        this.setTipo(TipoDato.toTipoDato(temp));
        switch (this.getTipo()) {
            case STRING:
                temp = json.getString(value2);
                this.setValue(temp);
                break;
            case NUMERIC:
                this.setValue(json.getInt(value2));
                break;
            case DATE:
                temp = json.getString(value2);
                this.setValue(Date.valueOf(temp));
                break;
        }

    }

    public static List<Filtro> getFiltrosList(String filtros) throws ErrorClass {
        List<Filtro> f = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(filtros);
            for (int i = 0; i < array.length(); i++) {
                Filtro filtro = new Filtro();
                filtro.objectify(array.getJSONObject(i));
                f.add(filtro);
            }
        } catch (JSONException ex) {
            throw new ErrorClass("Bad Request", null, "409");
        }
        return f;
    }
}
