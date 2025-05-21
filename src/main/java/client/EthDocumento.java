/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import config.ConfigAccess;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import com.seguridata.wseth.cipher.SgDataCrypto;
import com.seguridata.wseth.cipher.beans.CryptoParameters;
import com.seguridata.wseth.cipher.utilities.Utilities;
import dao.Conexion;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import pojo.Documento;
import pojo.ErrorClass;
import services.ExpedientedocumentosResource;

/**
 *
 * @author aldair
 */
public class EthDocumento {

    private static EthDocumento ethDoc = null;
    private final HttpAuthenticationFeature feature;
    private final javax.ws.rs.client.Client client;
    private final String basepath;
    private final javax.ws.rs.client.Client clientrq;
    private final String DOCUMENTOENTRY;

    private EthDocumento() throws IOException {
        this.DOCUMENTOENTRY = "documentos";
        this.basepath = ConfigAccess.getRecurso().getValue("eth.basepath");
        String usuario = ConfigAccess.getRecurso().getValue("eth.user");
        String password = ConfigAccess.getRecurso().getValue("eth.password");
        feature = HttpAuthenticationFeature.basic(usuario, password);
        clientrq = ClientBuilder.newClient();
        clientrq.register(feature);
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.register(MultiPartFeature.class);
        client = ClientBuilder.newClient(clientConfig);
        client.register(feature);
    }

    public static EthDocumento getEthDocumento() throws IOException {
        if (ethDoc == null) {
            ethDoc = new EthDocumento();
        }
        return ethDoc;
    }

    public void uploadDoc(byte[] filetoupload, String direccionBlockchain) throws JSONException, ErrorClass, SQLException {

        byte[] certificate = null;
        try {
            ConfigAccess cnf = ConfigAccess.getRecurso();
            certificate = cnf.getFile("usuario.cer");
        } catch (IOException ex) {
            Logger.getLogger(EthDocumento.class.getName()).log(Level.SEVERE, null, ex);
        }

        InputStream fis = new ByteArrayInputStream(filetoupload);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SgDataCrypto sgDataCipher = new SgDataCrypto();
        CryptoParameters cryptoParameters0 = null;
        try {
            cryptoParameters0 = sgDataCipher.cipher(certificate, fis, baos);
        } catch (Exception ex) {
            Logger.getLogger(EthDocumento.class.getName()).log(Level.SEVERE, null, ex);
            ErrorClass error = new ErrorClass();
            error.setCode("409");
            error.setMessage("No se ha podido cargar el archivo");
            throw error;
        }

        byte[] salidaCifrada = baos.toByteArray();

        long size = salidaCifrada.length;

        String hash = bytesToHex(cryptoParameters0.getHash());
        String ivy = bytesToHex(cryptoParameters0.getIv());
        String key = bytesToHex(cryptoParameters0.getEnvelopedKey());
        WebTarget target = client.target(basepath).path(DOCUMENTOENTRY).path(direccionBlockchain).path("contentDetails");

        JSONObject json = new JSONObject();
        json.put("tamanoContenido", size);
        json.put("hash", hash);
        json.put("vectorInicializacion", ivy);
        json.put("llave", key);

        String sql = "insert into criptoparametros values(?,?,?)";
        PreparedStatement pr = null;
        Connection cx = Conexion.getConnection();
        try {
            pr = cx.prepareStatement(sql);
            pr.setString(1, direccionBlockchain);
            pr.setString(2, key);
            pr.setString(3, ivy);
            pr.execute();
        } catch (SQLException ex) {
            ErrorClass error = new ErrorClass();
            error.setCode("300");
            error.setMessage("No se ha podido subir el archivo");
            throw error;
        } finally {
            if (pr != null) {
                pr.close();
            }
        }

        Response response = target.request().post(Entity.entity(json.toString(), MediaType.APPLICATION_JSON));
        String body = response.readEntity(String.class);
        try {
            validator(response, body);
        } catch (ErrorClass ex) {
            Logger.getLogger(EthDocumento.class.getName()).log(Level.SEVERE, null, ex);
            ex.setCode("001");
            ex.setMessage("No se ha podido subir los detalles del documento");
            throw ex;
        }
        JSONObject jsonresponse = new JSONObject(body);

        int noIteraciones = jsonresponse.getInt("noIteraciones");
        byte[] range = null;
        int blockSize = 1024;
        int limite = noIteraciones;

        int idx = 0;
        int res = salidaCifrada.length % 1024;
        int vueltas1024 = salidaCifrada.length / 1024;

        for (int i = 1; i <= limite; i++) {

            if (i <= vueltas1024) {
                range = Arrays.copyOfRange(salidaCifrada, idx, idx + blockSize);
            } else {
                range = Arrays.copyOfRange(salidaCifrada, idx, idx + res);
            }
            FormDataContentDisposition fdc = FormDataContentDisposition//
                    .name("file")//
                    .fileName(direccionBlockchain + i + ".bin")//
                    .size(range.length)//
                    .build();
            FormDataBodyPart fdbp = new FormDataBodyPart(fdc, new ByteArrayInputStream(range), MediaType.APPLICATION_OCTET_STREAM_TYPE);

            FormDataMultiPart formdata = new FormDataMultiPart();
            formdata.bodyPart(fdbp);
            formdata.field("iteracion", String.valueOf(i));
            WebTarget target2 = client.target(basepath).path(DOCUMENTOENTRY).path(direccionBlockchain).path("subeContenido");
            Response r = target2.request().post(Entity.entity(formdata, formdata.getMediaType()));
            try {
                validator(response, r.readEntity(String.class));
            } catch (ErrorClass error) {
                error.setMessage("No se ha podido subir el documento, intente de nuevo más tarde");
                dao.DAOExpedienteDocumento.getDaoExpedienteDocumento().deleteOnError(direccionBlockchain);
                dao.DAODocumento.getDAODocumento().deleteOnError(direccionBlockchain);
                throw error;
            }
            idx = (i) * blockSize;
        }
    }

    public byte[] downloadDoc(String direccionBlockchain) throws JSONException, ErrorClass, IOException, DecoderException, SQLException {

        ConfigAccess con = ConfigAccess.getRecurso();

        byte[] privateKey = con.getFile("usuario.key");
        String password = con.getValue("eth.doc.password");
        WebTarget target = client.target(basepath).path(DOCUMENTOENTRY).path(direccionBlockchain).path("contentDetails");
        Response response = target.request().get();
        String body = response.readEntity(String.class);
        validator(response, body);
        JSONObject json = new JSONObject(body);
        int noIt = Integer.parseInt(json.getString("noIteraciones"));
        ByteArrayOutputStream bytea = new ByteArrayOutputStream();

        for (int i = 1; i <= noIt; i++) {
            WebTarget target2 = client.target(basepath).path(DOCUMENTOENTRY).path(direccionBlockchain).path("descargaContenido").path(String.valueOf(i));
            Response response2 = target2.request().get();
            File is = response2.readEntity(File.class);
            byte[] range = Utilities.readBytesFromFile(is);
            bytea.write(range);
        }
        ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
        SgDataCrypto sgDataCipher = new SgDataCrypto();
        String sql = "select * from criptoparametros where direccion_blockchain_documento=?";
        PreparedStatement pr = null;
        try {
            pr = Conexion.getConnection().prepareStatement(sql);
            pr.setString(1, direccionBlockchain);
            try (ResultSet rs = pr.executeQuery()) {
                byte[] key;
                byte[] ivy;
                if (rs.next()) {
                    key = Hex.decodeHex(rs.getString(2).toCharArray());
                    ivy = Hex.decodeHex(rs.getString(3).toCharArray());
                    InputStream is = new ByteArrayInputStream(bytea.toByteArray());
                    try {
                        sgDataCipher.decipher(
                                privateKey,
                                password.toCharArray(),
                                key,
                                ivy,
                                is,
                                baos1);
                    } catch (Exception ex) {
                        Logger.getLogger(EthDocumento.class.getName()).log(Level.SEVERE, null, ex);
                        ErrorClass error = new ErrorClass();
                        error.setCode("409");
                        error.setMessage("No se pudo descifrar el documento");
                    }
                } else {
                    ErrorClass error = new ErrorClass();
                    error.setCode("400");
                    error.setMessage("No se ha podido descargar");
                    throw error;
                }
            }
            return baos1.toByteArray();
        } catch (SQLException ex) {
            Logger.getLogger(EthDocumento.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        } finally {
            if (pr != null) {
                pr.close();
            }
        }
    }

    public String post(Documento doc) throws JSONException, ErrorClass {
        WebTarget target = client.target(basepath).path(DOCUMENTOENTRY);
        Response response = target.request().post(Entity.entity(jsontify(doc), MediaType.APPLICATION_JSON));
        String body = response.readEntity(String.class
        );
        validator(response, body);
        JSONObject json = new JSONObject(body);
        return json.getString("direccion");
    }

    public void enableDoc(String direccionBlockchain) throws ErrorClass {
        WebTarget target = client.target(basepath).path(DOCUMENTOENTRY).path(direccionBlockchain).path("enable");
        Response r = target.request().put(Entity.entity("{}", MediaType.APPLICATION_JSON));
        String res = r.readEntity(String.class
        );
        validator(r, res);
    }

    public void disableDoc(String direccionBlockchain) throws ErrorClass {
        WebTarget target = client.target(basepath).path(DOCUMENTOENTRY).path(direccionBlockchain).path("disable");
        JSONObject json = new JSONObject();
        try {
            json.put("key", "value");

        } catch (JSONException ex) {
            Logger.getLogger(EthDocumento.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        Response r = target.request().put(Entity.entity(json.toString(), MediaType.APPLICATION_JSON));
        String res = r.readEntity(String.class
        );
        validator(r, res);
    }

    public Documento get(String direccionBlockchain) throws ErrorClass {
        WebTarget target = clientrq.target(basepath).path(DOCUMENTOENTRY).path(direccionBlockchain);
        Response response = target.request().get();
        String body = response.readEntity(String.class);
        validator(response, body);
        return objectify(body);
    }

    private String jsontify(Documento doc) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", doc.getId_documento());
        json.put("nombre", doc.getNom_documento());
        json.put("fechaCreacion", new SimpleDateFormat("dd-MM-yyyy").format(doc.getFecha_creacion()));
        json.put("descripcion", doc.getDescripcion());
        json.put("fechaIncorporacion", Long.toString(doc.getFecha_incorporacion().toInstant().toEpochMilli()));
        json.put("formato", doc.getFormato().toLowerCase());
        json.put("noPaginas", doc.getNo_paginas());
        json.put("nivConfidencialidad", Integer.toString(doc.getNivel_confidencialidad()));
        json.put("activo", "1");
        return json.toString();
    }

    private Documento objectify(String json) {
        try {
            JSONObject json2 = new JSONObject(json);
            Documento doc = new Documento();
            doc.setId_documento(json2.getString("id"));
            doc.setNom_documento(json2.getString("nombre"));
            doc.setFecha_creacion(new SimpleDateFormat("dd-MM-yyyy").parse(json2.getString("fechaCreacion")));

            //Convertir fecha de milisegundos a timestamp en utc
            Calendar calendar = Calendar.getInstance();
            long date = Long.parseLong(json2.getString("fechaIncorporacion"));
            calendar.setTimeInMillis(date);
            LocalDateTime ldt = LocalDateTime.ofInstant(calendar.toInstant(), ZoneOffset.UTC);
            Timestamp tsutcfi = Timestamp.valueOf(ldt);
            doc.setFecha_incorporacion(tsutcfi);
            //Fin del timestamp en utc

            doc.setDescripcion(json2.getString("descripcion"));
            doc.setFormato(json2.getString("formato"));
            doc.setTamano(0);
            doc.setNo_paginas(json2.getInt("noPaginas"));
            doc.setNivel_confidencialidad(json2.getInt("nivConfidencialidad"));
            int activo = Integer.parseInt(json2.getString("activo"));
            doc.setActivo((activo == 1));
            return doc;

        } catch (JSONException | ParseException ex) {
            Logger.getLogger(EthDocumento.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    private final ErrorClass erroreth = new ErrorClass();

    private void validator(Response response, String responsestr) throws ErrorClass {
        JSONObject json = null;
        try {
            json = new JSONObject(responsestr);
            switch (response.getStatus()) {
                case 200:
                    if (!json.has("errors")) {
                        return;
                    }
                    JSONArray jsonarray = json.getJSONArray("errors");
                    if (jsonarray.length() > 0) {
                        JSONObject json2 = jsonarray.getJSONObject(0);
                        erroreth.setCode(json2.getString("code"));
                        erroreth.setMessage(json2.getString("description"));
                        throw erroreth;
                    } else {
                        return;
                    }
                case 400:
                    try {
                        erroreth.setCode(json.getString("code"));
                        erroreth.setMessage(json.getString("description"));

                    } catch (JSONException ex) {
                        Logger.getLogger(EthDocumento.class
                                .getName()).log(Level.SEVERE, null, ex);
                    }

                    throw erroreth;
                default:
                    erroreth.setCode("unknown");
                    erroreth.setMessage("Error no esperado");
                    throw erroreth;
            }
        } catch (JSONException ex) {
            switch (response.getStatus()) {
                case 401:
                    erroreth.setCode("Fallo autenticación");
                    erroreth.setMessage("Requiere autenticación");
                    throw erroreth;
                case 404:
                    erroreth.setCode("404");
                    erroreth.setMessage("Documento no encontrado");
                    throw erroreth;
                case 405:
                    erroreth.setCode("405");
                    erroreth.setMessage("Bad Request");
                    throw erroreth;
                default:
                    erroreth.setCode("unknown");
                    erroreth.setMessage("Error no esperado");
                    throw erroreth;
            }
        }

    }

    private String bytesToHex(byte[] hashInBytes) {

        StringBuilder sb = new StringBuilder();
        for (byte b : hashInBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();

    }

}
