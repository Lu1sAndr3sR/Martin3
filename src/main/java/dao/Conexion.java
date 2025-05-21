package dao;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import config.ConfigAccess;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author aldair
 */
public class Conexion implements Serializable {

    private static Connection cx;
    private static Conexion c;
    private Conexion() {
        try {
            String ip = ConfigAccess.getRecurso().getValue("app.db.ip");
            String dbname = ConfigAccess.getRecurso().getValue("app.db.nombre");
            String user = ConfigAccess.getRecurso().getValue("app.db.usuario");
            String password = ConfigAccess.getRecurso().getValue("app.db.password");
            String port = ConfigAccess.getRecurso().getValue("app.db.port");
            String ruta = "jdbc:postgresql://"
                    + ip+":"+port+"/"+dbname;
            Class.forName("org.postgresql.Driver");
            cx = DriverManager.getConnection(ruta, user,password);
        } catch (ClassNotFoundException | SQLException | IOException ex) {
            Logger.getLogger(Conexion.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    public static Connection getConnection() {
        if (c == null) {
            c = new Conexion();
        }
        return cx;
    }
}
