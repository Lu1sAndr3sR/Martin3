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
public enum Criterio {
    IGUAL("="),
    MAYORQUE(">"),
    MENORQUE("<"),
    LIKE("like ");
    
    private final String critrio;
    
    private Criterio(String criterio){
        this.critrio = criterio;
    }
    
    public String getCriterio(){
        return critrio;
    }
    
}
