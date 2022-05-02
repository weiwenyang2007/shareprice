package org.easystogu.portal;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.sf.json.JSONObject;

public class TestControler {
	@POST
	@Path("/holle")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response holle(final String body, @Context HttpServletRequest request) {
		System.out.println(request.getHeader("Content-Type"));
		JSONObject json = JSONObject.fromObject(body);
		System.out.println(json.get("subscriberId"));
		return Response.ok().build();
	}
}
