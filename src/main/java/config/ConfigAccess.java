/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package config;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author aldair
 */
public class ConfigAccess {

    private static ConfigAccess recurso;
    private static final String PROPERTIES = "config/config.properties";
    private final Properties property = new Properties();
    private static InputStream stream;

    private ConfigAccess() throws FileNotFoundException, IOException {

        // this is how we load file within editor (eg eclipse)
        stream = ConfigAccess.class.getClassLoader().getResourceAsStream(PROPERTIES);

        property.load(stream);
    }

    public static ConfigAccess getRecurso() throws IOException {
        if (recurso == null) {
            recurso = new ConfigAccess();
        }
        return recurso;
    }

    public String getValue(String key) throws FileNotFoundException, IOException {

        return property.getProperty(key);
    }

    public byte[] getFile(String value) throws IOException {
        InputStream is = ConfigAccess.class.getClassLoader().getResourceAsStream("private/" + value);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] bytes = new byte[1024];
        int read;
        while ((read = is.read(bytes)) != -1) {
            baos.write(bytes, 0, read);
        }
        return baos.toByteArray();
    }
}
