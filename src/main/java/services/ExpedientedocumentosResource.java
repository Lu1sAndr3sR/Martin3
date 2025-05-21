/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;


import dao.DAOExpedienteDocumento;
import java.io.IOException;
import pojo.ErrorObjectConv;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.DELETE;
import javax.ws.rs.Produces;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.codehaus.jettison.json.JSONException;
import pojo.ErrorClass;

/**
 * REST Web Service
 *
 * @author aldair
 */
@Path("expedientedocumentos")
public class ExpedientedocumentosResource {

    @Context
    private UriInfo context;   
    
    //Asocia un documento existente a un expediente existente
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response agregaDocumentoExpediente(@QueryParam("id_expediente") String id_expediente, 
            @QueryParam("id_documento") String id_documento) throws JSONException, IOException{
        try {
            DAOExpedienteDocumento ded = DAOExpedienteDocumento.getDaoExpedienteDocumento();
            if(ded.agregarDocumento(id_expediente, id_documento))
                return Response.status(200).build();
            else
                return Response.status(408).build();
        } catch (SQLException ex) {
            Logger.getLogger(ExpedientedocumentosResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ErrorObjectConv.getErrorObject(ex)).build();
        } catch (ErrorClass ex) {
            Logger.getLogger(ExpedientedocumentosResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ex).build();
        }
    }
    
    //Desvincula un documento de algún expediente sin darlo de baja física
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response desvincularExpediente(@QueryParam("id_expediente") String id_expediente,@QueryParam("id_documento") String id_documento ){
        try {
            DAOExpedienteDocumento.getDaoExpedienteDocumento().quitarDocumento(id_expediente, id_documento);
            return Response.status(200).build();
        } catch (SQLException ex) {
            Logger.getLogger(ExpedientedocumentosResource.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(409).entity(ErrorObjectConv.getErrorObject(ex)).build();
        }
        
    }
    
}
