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
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import pojo.Documento;
import pojo.ErrorClass;
import pojo.Expediente;

/**
 *
 * @author aldair
 */
public class Eth_Expediente {

    private static Eth_Expediente eth_exp = null;
    private final HttpAuthenticationFeature feature;
    private final javax.ws.rs.client.Client client;
    private final String basepath;
    private final String password = ConfigAccess.getRecurso().getValue("eth.password");
    private final String user = ConfigAccess.getRecurso().getValue("eth.user");
    private static final String PATH = "expedientes";
    
    public Eth_Expediente() throws IOException {
        this.basepath = ConfigAccess.getRecurso().getValue("eth.basepath");
        feature = HttpAuthenticationFeature.basic(user, password);
        client = ClientBuilder.newClient();
        client.register(feature);
    }

    public static Eth_Expediente getEth_Expediente() throws IOException {
        if (eth_exp == null) {
            eth_exp = new Eth_Expediente();
        }
        return eth_exp;
    }

    public void enableExpediente(String direccion_blockchain) throws ErrorClass {
        WebTarget target = client.target(basepath).path(PATH).path(direccion_blockchain).path("enable");
        Response r = target.request().put(Entity.entity("{}", MediaType.APPLICATION_JSON));
        String res = r.readEntity(String.class);
        try {
            validator(r, res);
        } catch (JSONException ex) {
            Logger.getLogger(Eth_Usuario.class.getName()).log(Level.SEVERE, null, ex);
            ErrorClass err = new ErrorClass();
            err.setCode("404");
            err.setMessage("No se ha podido deshabilitar usuario");
        }
    }

    public void disableExpediente(String direccion_blockchain) throws ErrorClass {
        WebTarget target = client.target(basepath).path(PATH).path(direccion_blockchain).path("disable");
        JSONObject json = new JSONObject();
        try {
            json.put("key", "value");
        } catch (JSONException ex) {
            Logger.getLogger(EthDocumento.class.getName()).log(Level.SEVERE, null, ex);
        }
        Response r = target.request().put(Entity.entity(json.toString(), MediaType.APPLICATION_JSON));
        String res = r.readEntity(String.class);
        try {
            validator(r, res);
        } catch (JSONException ex) {
            Logger.getLogger(Eth_Usuario.class.getName()).log(Level.SEVERE, null, ex);
            ErrorClass err = new ErrorClass();
            err.setCode("404");
            err.setMessage("No se ha podido deshabilitar usuario");
        }
    }

    public Expediente getExp(String direccion_exp) throws JSONException, ErrorClass, IOException, ParseException {
        WebTarget target = client.target(basepath).path(PATH).path(direccion_exp);
        Response response = target.request().get();
        String body = response.readEntity(String.class);
        validator(response, body);
        return objectify(body);
    }

    public String postExp(Expediente ex) throws JSONException, ErrorClass {
        WebTarget target = client.target(basepath).path(PATH);
        Response response = target.request().post(Entity.entity(jsontify(ex), MediaType.APPLICATION_JSON));
        String body = response.readEntity(String.class);
        validator(response, body);
        JSONObject json = new JSONObject(body);
        return json.getString("direccion");
    }

    public void addDoc(String direccion_exp, String direccion_doc) throws JSONException, ErrorClass {
        WebTarget target = client.target(basepath).path(PATH)
                .path(direccion_exp)
                .path(direccion_doc);
        Response response = target.request().post(Entity.entity(null, MediaType.APPLICATION_JSON));
        String body = response.readEntity(String.class);
        validator(response, body);
    }

    private String jsontify(Expediente e) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", e.getId_expediente());
        json.put("titulo", e.getTitulo());
        json.put("fechaCreacion", Long.toString(e.getFecha_creacion().toInstant().toEpochMilli()));
        json.put("tema", e.getTemas());
        json.put("activo", "1");
        json.put("direccionUsuario", e.getId_usuario());
        json.put("direccionAutor", e.getId_autor());
        return json.toString();
    }

    private Expediente objectify(String body) throws JSONException, IOException, ErrorClass, ParseException {
        JSONObject json = new JSONObject(body);
        Expediente e = new Expediente();
        e.setId_expediente(json.getString("id"));
        e.setTitulo(json.getString("titulo"));
        e.setTemas(json.getString("tema"));
        Eth_Usuario et = Eth_Usuario.getEth_Usuario();
        e.setEmisor(et.getUsuario(json.getString("direccionAutor")));
        e.setVisor(et.getUsuario(json.getString("direccionUsuario")));
        //new SimpleDateFormat("dd/MM/yyyy").parse(json2.getString("fechaCreacion"))
        e.setFecha_creacion(new SimpleDateFormat("dd/MM/yyyy").parse(json.getString("fechaCreacion")));
        JSONArray jsona = json.getJSONArray("documentos");
        ArrayList<Documento> documentos = new ArrayList<>();
        EthDocumento ethdoc = EthDocumento.getEthDocumento();
        for (int i = 0; i < jsona.length(); i++) {
            Documento doc = ethdoc.get(jsona.get(i).toString());
            documentos.add(doc);
        }
        e.setDocumentos(documentos);
        return e;
    }
    private final ErrorClass erroreth = new ErrorClass();

    private void validator(Response response, String responsestr) throws JSONException, ErrorClass {

        JSONObject json = null;
        switch (response.getStatus()) {
            case 200:
                if (responsestr.trim().isEmpty()) {
                    return;
                }
                json = new JSONObject(responsestr);
                if (json.has("errors")) {
                    JSONArray jsonarray = json.getJSONArray("errors");
                    if (jsonarray.length() > 0) {
                        JSONObject json2 = jsonarray.getJSONObject(0);
                        erroreth.setCode(json2.getString("code"));
                        erroreth.setMessage(json2.getString("description"));
                        throw erroreth;
                    }
                }

                break;

            case 400:
                json = new JSONObject(responsestr);
                erroreth.setCode(json.getString("code"));
                erroreth.setMessage(json.getString("description"));
                throw erroreth;
            case 401:
                erroreth.setCode("unknown");
                erroreth.setMessage("Requiere autenticación");
                throw erroreth;
            case 404:
                erroreth.setCode("404");
                erroreth.setMessage("Expediente no encontrado");
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
