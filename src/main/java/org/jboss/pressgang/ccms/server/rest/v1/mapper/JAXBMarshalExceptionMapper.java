package org.jboss.pressgang.ccms.server.rest.v1.mapper;

import org.jboss.pressgang.ccms.rest.v1.constants.RESTv1Constants;
import org.jboss.pressgang.ccms.server.rest.interceptor.VersionHeaderInterceptor;
import org.jboss.pressgang.ccms.server.utils.Constants;
import org.jboss.pressgang.ccms.utils.common.VersionUtilities;
import org.jboss.resteasy.plugins.providers.jaxb.JAXBMarshalException;
import org.jboss.resteasy.spi.InternalServerErrorException;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class JAXBMarshalExceptionMapper  implements ExceptionMapper<JAXBMarshalException> {

    @Override
    public Response toResponse(final JAXBMarshalException exception) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(exception.getMessage() + "\n")
                .header("Content-Type", MediaType.TEXT_PLAIN)
                .header(RESTv1Constants.X_PRESSGANG_VERSION_HEADER, VersionUtilities.getAPIVersion(VersionHeaderInterceptor.class))
                .header(RESTv1Constants.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, Constants.CORS_ALLOW_ORIGIN_HEADER)
                .header(RESTv1Constants.ACCESS_CONTROL_EXPOSE_HEADERS, RESTv1Constants.X_PRESSGANG_VERSION_HEADER)
                .build();
    }
}