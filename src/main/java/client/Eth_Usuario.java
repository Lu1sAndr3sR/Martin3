/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import config.ConfigAccess;
import java.io.IOException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import pojo.ErrorClass;
import pojo.Usuario;

/**
 *
 * @author aldair
 */
public class EthUsuario {

    private static EthUsuario ethUsuario = null;
    private final HttpAuthenticationFeature feature;
    private final javax.ws.rs.client.Client client;
    private final String basepath;
    private final String password = ConfigAccess.getRecurso().getValue("eth.password");
    private final String ethUser = ConfigAccess.getRecurso().getValue("eth.user");
    private static final String USUARIOPATH = "usuarios";
    private static final String STRING_ACTIVO = "activo";

    private EthUsuario() throws IOException {
        this.basepath = ConfigAccess.getRecurso().getValue("eth.basepath");
        feature = HttpAuthenticationFeature.basic(ethUser, password);
        client = ClientBuilder.newClient();
        client.register(feature);
    }

    public static EthUsuario getEthUsuario() throws IOException {
        if (ethUsuario == null) {
            ethUsuario = new EthUsuario();
        }
        return ethUsuario;
    }

    public void enableAdministrador(String direccion) throws JSONException, JSONException, ErrorClass {
        WebTarget target = client.target(basepath).path(USUARIOPATH).path(direccion).path("enableAdministrator");
        JSONObject send = new JSONObject();
        send.put("ok", "ok");
        Response response = target.request().put(Entity.entity(send.toString(), MediaType.APPLICATION_JSON));
        String body = response.readEntity(String.class);
        if (response.getStatus() != 200) {
            Utils.validator(response, body);
        }
    }

    public void enableUsuario(String direccionBlockchain) {
        WebTarget target = client.target(basepath).path(USUARIOPATH).path(direccionBlockchain).path("enable");
        Utils.validacionRespuesta(target, (r, responseStr) -> {
            try {
                Utils.validator(r, responseStr);
            } catch (JSONException | ErrorClass e) {
                throw new RuntimeException("Error al habilitar usuario", e);
            }
        });
    }

    public void disableUsuario(String direccionBlockchain) throws ErrorClass {
        WebTarget target = client.target(basepath).path(USUARIOPATH).path(direccionBlockchain).path("disable");
        Utils.validacionRespuesta(target, (r, responseStr) -> {
            try {
                Utils.validator(r, responseStr);
            } catch (JSONException | ErrorClass e) {
                throw new RuntimeException("Error al deshabilitar usuario", e);
            }
        });
    }

    public String createUsuario(Usuario u) throws JSONException, IOException, ErrorClass {
        WebTarget target = client.target(basepath).path(USUARIOPATH);
        Response response = target.request().post(Entity.entity(jsontify(u), MediaType.APPLICATION_JSON));
        String body = response.readEntity(String.class);
        Utils.validator(response, body);
        JSONObject json = new JSONObject(body);
        String direccion = json.getString("direccion");
        if (u.getIdPerfil() == Usuario.Perfil.ADMINISTRADOR) {
            enableAdministrador(direccion);
        }
        return direccion;
    }

    public Usuario getUsuario(String direccionBlockchain) throws IOException, JSONException, ErrorClass {
        WebTarget target = client.target(ConfigAccess.getRecurso().getValue("eth.basepath"))
                .path(USUARIOPATH)
                .path(direccionBlockchain);
        Response response = target.request().get();
        String responsestr = response.readEntity(String.class);
        Utils.validator(response, responsestr);
        return objectify(responsestr);
    }

    

    protected String jsontify(Usuario u) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", u.getIdUsuario());
        json.put("nombre", u.getNombre());
        json.put("apellidoPaterno", u.getApellidoPaterno());
        json.put("apellidoMaterno", u.getApellidoMaterno());
        json.put("biometria", "");
        json.put("pasw", u.getPassword());
        json.put("telefono", u.getTelefonoMovil());
        json.put("email", u.getEmail());
        json.put("perfil", u.getIdPerfil().getPerfil());
        String activo;
        if (u.isActivo()) {
            activo = "1";
        } else {
            activo = "0";
        }
        json.put(STRING_ACTIVO, activo);
        if (u.getIdPerfil().equals(Usuario.Perfil.ADMINISTRADOR)) {
            json.put("administrador", "1");
        } else {
            json.put("administrador", "0");
        }

        return json.toString();
    }

    protected Usuario objectify(String u) throws JSONException {
        JSONObject json = new JSONObject(u);
        Usuario user = new Usuario();
        user.setIdUsuario(json.getString("id"));
        user.setNombre(json.getString("nombre"));
        user.setApellidoPaterno(json.getString("apellidoPaterno"));
        user.setApellidoMaterno(json.getString("apellidoMaterno"));
        user.setBiometria(json.getString("biometria"));
        user.setTelefonoMovil(json.getString("telefono"));
        user.setEmail(json.getString("email"));
        if (json.getString(STRING_ACTIVO).equals("1")) {
            user.setActivo(true);
        }
        if (json.getString(STRING_ACTIVO).equals("0")) {
            user.setActivo(false);
        }
        user.setIdPerfil(Usuario.toPerfil(json.getString("perfil")));
        return user;
    }
}
