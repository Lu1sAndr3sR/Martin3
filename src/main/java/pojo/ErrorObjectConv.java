/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pojo;

import org.postgresql.util.PSQLException;

/**
 *
 * @author aldair
 */
public class ErrorObjectConv {
    
    private static final ErrorClass ERROR = new ErrorClass();
    
    private ErrorObjectConv(){
        
    }
    
    public static ErrorClass getErrorObject(Exception ex) {

        if (ex.getClass() == PSQLException.class) {
            ERROR.setMessage("Error");
            ERROR.setMessage("Elemento o llave duplicada");
        }
        else if(ex.getClass() == ErrorClass.class){
            ERROR.setCode("Error");
            ERROR.setMessage(ex.getMessage());
        }
        else{
            ERROR.setCode("409");
            ERROR.setMessage("Error: Error en el sevidor");
            ERROR.setDetail("Algo salío mal dentro del servidor");
        }
        return ERROR;
    }
}
