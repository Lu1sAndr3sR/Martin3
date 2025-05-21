/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import config.ConfigAccess;
import java.io.IOException;
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
import pojo.ErrorClass;
import pojo.Usuario;

/**
 *
 * @author aldair
 */
public class Eth_Usuario {

    private static Eth_Usuario eth_usuario = null;
    private final HttpAuthenticationFeature feature;
    private final javax.ws.rs.client.Client client;
    private final String basepath;
    private final String password = ConfigAccess.getRecurso().getValue("eth.password");
    private final String ethUser = ConfigAccess.getRecurso().getValue("eth.user");
    private final String USUARIOPATH = "usuarios";
    
    private Eth_Usuario() throws IOException {
        this.basepath = ConfigAccess.getRecurso().getValue("eth.basepath");
        feature = HttpAuthenticationFeature.basic(ethUser, password);
        client = ClientBuilder.newClient();
        client.register(feature);
    }

    public static Eth_Usuario getEth_Usuario() throws IOException {
        if (eth_usuario == null) {
            eth_usuario = new Eth_Usuario();
        }
        return eth_usuario;
    }

    public void enableAdministrador(String direccion) throws JSONException, JSONException, ErrorClass {
        WebTarget target = client.target(basepath).path(USUARIOPATH).path(direccion).path("enableAdministrator");
        JSONObject send = new JSONObject();
        send.put("ok", "ok");
        Response response = target.request().put(Entity.entity(send.toString(), MediaType.APPLICATION_JSON));
        String body = response.readEntity(String.class);
        if (response.getStatus() != 200) {
            validator(response, body);
        }
    }

    public void enableUsuario(String direccion_blockchain) throws ErrorClass {
        WebTarget target = client.target(basepath).path(USUARIOPATH).path(direccion_blockchain).path("enable");
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

    public void disableUsuario(String direccion_blockchain) throws ErrorClass {
        WebTarget target = client.target(basepath).path(USUARIOPATH).path(direccion_blockchain).path("disable");
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

    public String createUsuario(Usuario u) throws JSONException, IOException, ErrorClass {
        WebTarget target = client.target(basepath).path(USUARIOPATH);
        Response response = target.request().post(Entity.entity(jsontify(u), MediaType.APPLICATION_JSON));
        String body = response.readEntity(String.class);
        validator(response, body);
        JSONObject json = new JSONObject(body);
        String direccion = json.getString("direccion");
        if (u.getId_perfil() == Usuario.Perfil.ADMINISTRADOR) {
            enableAdministrador(direccion);
        }
        return direccion;
    }

    public Usuario getUsuario(String direccion_blockchain) throws IOException, JSONException, ErrorClass {
        WebTarget target = client.target(ConfigAccess.getRecurso().getValue("eth.basepath"))
                .path(USUARIOPATH)
                .path(direccion_blockchain);
        Response response = target.request().get();
        String responsestr = response.readEntity(String.class);
        validator(response, responsestr);
        return objectify(responsestr);
    }

    private final ErrorClass erroreth = new ErrorClass();

    private void validator(Response response, String responsestr) throws JSONException, ErrorClass {
        JSONObject json = null;
        switch (response.getStatus()) {
            case 200:
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
                erroreth.setCode("400");
                erroreth.setMessage("Requiere autenticación");
                throw erroreth;
            case 404:
                erroreth.setCode("404");
                erroreth.setMessage("Usuario no encontrado");
                throw erroreth;
            case 405:
                erroreth.setCode("401");
                erroreth.setMessage("Bad request");
                throw erroreth;
            default:
                erroreth.setCode("unknown");
                erroreth.setMessage("Error no esperado");
                throw erroreth;
        }
    }

    private String jsontify(Usuario u) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", u.getId_usuario());
        json.put("nombre", u.getNombre());
        json.put("apellidoPaterno", u.getApellido_paterno());
        json.put("apellidoMaterno", u.getApellido_materno());
        json.put("biometria", "");
        json.put("pasw", u.getPasw());
        json.put("telefono", u.getTel_movil());
        json.put("email", u.getEmail());
        json.put("perfil", u.getId_perfil().getPerfil());
        String activo;
        if (u.isActivo()) {
            activo = "1";
        } else {
            activo = "0";
        }
        json.put("activo", activo);
        if (u.getId_perfil().equals(Usuario.Perfil.ADMINISTRADOR)) {
            json.put("administrador", "1");
        } else {
            json.put("administrador", "0");
        }

        return json.toString();
    }

    private Usuario objectify(String u) throws JSONException {
        JSONObject json = new JSONObject(u);
        Usuario user = new Usuario();
        user.setId_usuario(json.getString("id"));
        user.setNombre(json.getString("nombre"));
        user.setApellido_paterno(json.getString("apellidoPaterno"));
        user.setApellido_materno(json.getString("apellidoMaterno"));
        user.setBiometria(json.getString("biometria"));
        user.setTel_movil(json.getString("telefono"));
        user.setEmail(json.getString("email"));
        switch(json.getString("activo")){
            case "1":
                user.setActivo(true);
            case "0":
                user.setActivo(false);
        }
        user.setId_perfil(Usuario.toPerfil(json.getString("perfil")));
        return user;
    }
}
