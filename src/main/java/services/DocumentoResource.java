/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import client.EthDocumento;
import dao.DAODocumento;
import pojo.Documento;
import pojo.ErrorObjectConv;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import dao.BusquedasIdsBlockchain;
import java.util.UUID;
import org.apache.commons.codec.DecoderException;
import org.codehaus.jettison.json.JSONException;
import pojo.ErrorClass;

/**
 * REST Web Service
 *
 * @author aldair
 */
@Path("documentos")
public class DocumentoResource {

    @Context
    private UriInfo context;
    
    private final String validationMessagge = "Validacion";
    //Retorna la lista de documentos asociados a un expediente
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDocumentos(@QueryParam("id_expediente") String id_expediente, @QueryParam("id_usuario") String id_usuario) {
        try {
            DAODocumento dao = DAODocumento.getDAODocumento();
            GenericEntity<List<Documento>> documentos;
            documentos = new GenericEntity<List<Documento>>(Lists.newArrayList(dao.getDocumentosByExpediente(id_expediente, id_usuario))) {
            };
            return Response.ok(documentos).build();
        } catch (SQLException ex) {
            Logger.getLogger(DocumentoResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ErrorObjectConv.getErrorObject(ex)).build();
        }
    }

    //Borra un documento 
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@QueryParam("id_documento") String id_documento) {
        DAODocumento dd = DAODocumento.getDAODocumento();
        try {
            dd.delete(id_documento);
            return Response.status(200).build();

        } catch (SQLException | IOException ex) {
            Logger.getLogger(DocumentoResource.class
                    .getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ErrorObjectConv.getErrorObject(ex)).build();
        } catch (ErrorClass ex) {
            Logger.getLogger(DocumentoResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ex).build();
        }
    }

    @PUT
    @Path("enable")
    @Produces(MediaType.APPLICATION_JSON)
    public Response upDocumento(@QueryParam("id_documento") String id_documento) {

        try {
            DAODocumento dao = DAODocumento.getDAODocumento();
            dao.upDoc(id_documento);
            return Response.ok().build();
        } catch (SQLException ex) {
            Logger.getLogger(UsuariosResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ErrorObjectConv.getErrorObject(ex)).build();
        } catch (ErrorClass ex) {
            Logger.getLogger(DocumentoResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ex).build();
        }
    }

    //Retorna un sólo documento consultado por su id
    @GET
    @Path("id")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDocumento(@QueryParam("id_documento") String id_documento) {
        try {
            DAODocumento dd = DAODocumento.getDAODocumento();
            return Response.ok(dd.getOneEth(id_documento)).build();
        } catch (IOException | SQLException ex) {
            Logger.getLogger(DocumentoResource.class
                    .getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ErrorObjectConv.getErrorObject(ex)).build();
        } catch (ErrorClass ex) {
            Logger.getLogger(DocumentoResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ex).build();
        }
    }

    @GET
    @Path("/download")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadDoc(@QueryParam("id_documento") String id) throws IOException, SQLException {
        EthDocumento eth = EthDocumento.getEthDocumento();
        String dir = BusquedasIdsBlockchain.getDireccionBlockchain(id, BusquedasIdsBlockchain.IdToBlock.DOCUMENTO);
        DAODocumento daodoc = DAODocumento.getDAODocumento();
        try {
            Documento doc = daodoc.getOneEth(id);

            byte[] i = eth.downloadDoc(dir);
            return Response.ok(i, MediaType.APPLICATION_OCTET_STREAM)
                    .header("content-disposition", "attachment; filename = " + UUID.randomUUID().toString().substring(0, 5) + "." + doc.getFormato().toLowerCase())
                    .build();
        } catch (JSONException ex) {
            return Response.status(409).entity(ex).build();
        } catch (ErrorClass ex) {
            Logger.getLogger(DocumentoResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ex).build();
        } catch (IOException | SQLException | DecoderException ex) {
            Logger.getLogger(DocumentoResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(404).build();
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response put(@QueryParam("id_documento") String id_documento, Documento documento) {
        DAODocumento dc = DAODocumento.getDAODocumento();
        try {
            dc.put(id_documento, documento);
            return Response.status(200).build();
        } catch (SQLException ex) {
            Logger.getLogger(DocumentoResource.class
                    .getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ex).build();
        }
    }

    @POST
    @Consumes(value = MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postie(final Documento documento, @QueryParam(value = "id_expediente") final String id_expediente) {
        DAODocumento dd = DAODocumento.getDAODocumento();
        if (documento.getNom_documento().isEmpty()) {
            ErrorClass ex = new ErrorClass();
            ex.setCode(validationMessagge);
            ex.setMessage("El nombre del documento no puede ir vacío");
            return Response.status(409).entity(ex).build();
        }
        if (documento.getNom_documento().length() > 15) {
            ErrorClass ex = new ErrorClass();
            ex.setCode(validationMessagge);
            ex.setMessage("El nombre del documento no puede exceder los 15 caracteres");
            return Response.status(409).entity(ex).build();
        }
        if (documento.getDescripcion().isEmpty()) {
            ErrorClass ex = new ErrorClass();
            ex.setCode(validationMessagge);
            ex.setMessage("La descripción del documento no puede ir vacío");
            return Response.status(409).entity(ex).build();
        }
        if (documento.getDescripcion().length() > 50) {
            ErrorClass ex = new ErrorClass();
            ex.setCode(validationMessagge);
            ex.setMessage("La descripción del documento no puede exceder los 50 caracteres");
            return Response.status(409).entity(ex).build();
        }
        try {
            dd.post(documento, id_expediente);
            return Response.ok().build();
        } catch (SQLException | IOException | JSONException ex) {
            Logger.getLogger(DocumentoResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ErrorObjectConv.getErrorObject(ex)).build();
        } catch (ErrorClass ex) {
            Logger.getLogger(DocumentoResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ex).build();
        }
    }
}
