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

    private String nombre_campo;
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

        private final String criterio;

        private Criterio(String criterio) {
            this.criterio = criterio;
        }

        public String getCriterio() {
            return criterio;
        }

        public static Criterio toCriterio(String criterio) {
            switch (criterio) {
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

    public String getNombre_campo() {
        return nombre_campo;
    }

    public void setNombre_campo(String nombre_campo) {
        this.nombre_campo = nombre_campo;
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
        str.append(nombre_campo)
                .append(" ")
                .append(criterio.getCriterio())
                .append(" ? ");
        return str.toString();
    }

    private final String value2;
    public void objectify(JSONObject json) throws JSONException {

        String temp = json.getString("nombre_campo");
        this.setNombre_campo(temp);
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
            ErrorClass error = new ErrorClass();
            error.setCode("409");
            error.setMessage("Bad Request");
            throw error;
        }
        return f;
    }
}
