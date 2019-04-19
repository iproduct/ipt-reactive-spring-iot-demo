package org.iproduct.iot.demo.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.File;

@Path("/")
public class StaticResource {
    private static final Logger log = LoggerFactory.getLogger(StaticResource.class);

    @GET
    @Path("/{docPath:.*}.{ext}")
    public Response getHtml(@PathParam("docPath") String docPath, @PathParam("ext") String ext, @HeaderParam("accept") String accept)
    {
        File file = new File(this.getClass().getResource("/static/" + cleanDocPath(docPath) + "." + ext).getFile());
        return Response.ok(file).build();
    }

    @GET
    @Path("{docPath:.*}")
    public Response getFolder(@PathParam("docPath") String docPath)
    {
        File file = null;
        if ("".equals(docPath) || "/".equals(docPath))
        {
            file = new File(this.getClass().getResource("/static/index.html").getFile());
        }
        else
        {
            file = new File(this.getClass().getResource("/static/" + cleanDocPath(docPath) + "/index.html").getFile());
        }
        return Response.ok(file).build();
    }

    private String cleanDocPath(String docPath)
    {
        if (docPath.startsWith("/"))
        {
            return docPath.substring(1);
        }
        else
        {
            return docPath;
        }
    }
}