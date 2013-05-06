package org.jboss.pressgang.ccms.restserver.webdav.topics.topic;

import net.java.dev.webdav.jaxrs.methods.PROPFIND;
import static net.java.dev.webdav.jaxrs.xml.properties.ResourceType.COLLECTION;
import net.java.dev.webdav.jaxrs.xml.elements.*;
import net.java.dev.webdav.jaxrs.xml.elements.Response;
import net.java.dev.webdav.jaxrs.xml.properties.*;
import org.jboss.pressgang.ccms.model.Topic;
import org.jboss.pressgang.ccms.restserver.webdav.WebDavConstants;
import org.jboss.pressgang.ccms.restserver.webdav.WebDavResource;
import org.jboss.pressgang.ccms.restserver.webdav.topics.topic.fields.WebDavTopicContent;
import org.jboss.pressgang.ccms.restserver.webdav.WebDavUtils;

import static javax.ws.rs.core.HttpHeaders.CONTENT_LENGTH;
import static net.java.dev.webdav.jaxrs.Headers.DEPTH;
import static javax.ws.rs.core.Response.Status.OK;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.*;

import javax.persistence.EntityManager;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Providers;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
    The virtual folder that holds all the topics
 */
@Path("{var:.*}/TOPICS/{topicId: \\d+}")
public class WebDavTopic extends WebDavResource {

    private static final Logger LOGGER = Logger.getLogger(WebDavTopic.class.getName());

    @PathParam("topicId") int topicId;

    @Override
    @Produces(MediaType.APPLICATION_XML)
    @PROPFIND
    public javax.ws.rs.core.Response propfind(@Context final UriInfo uriInfo, @HeaderParam(DEPTH) final int depth, final InputStream entityStream,
                                              @HeaderParam(CONTENT_LENGTH) final long contentLength, @Context final Providers providers,
                                              @Context final HttpHeaders httpHeaders) throws URISyntaxException, IOException {
        try {
            LOGGER.info("ENTER TopicVirtualFolder.propfind()");

            if (depth == 0) {
                LOGGER.info("Depth == 0");
                /* A depth of zero means we are returning information about this item only */
                return javax.ws.rs.core.Response.status(207).entity(new MultiStatus(getProperties(uriInfo, topicId))).type(WebDavConstants.XML_MIME).build();
            } else {
                LOGGER.info("Depth != 0");

                try {
                    final EntityManager entityManager = WebDavUtils.getEntityManager(false);

                    final Topic topic = entityManager.find(Topic.class, topicId);

                    if (topic != null) {
                        /* Otherwise we are retuning info on the children in this collection */
                        final List<Response> responses = new ArrayList<Response>();
                        responses.add(WebDavTopicContent.getProperties(uriInfo, topic));
                        final MultiStatus st = new MultiStatus(responses.toArray(new Response[responses.size()]));
                        return javax.ws.rs.core.Response.status(207).entity(st).type(WebDavConstants.XML_MIME).build();
                    } else {
                        return javax.ws.rs.core.Response.status(404).build();
                    }

                } catch (final NumberFormatException ex) {
                    return javax.ws.rs.core.Response.status(404).build();
                }


            }

        } catch (final Exception ex) {
            LOGGER.severe(ex.toString());
            ex.printStackTrace();
            return javax.ws.rs.core.Response.status(500).build();
        }
    }

    public static Response getProperties(final UriInfo uriInfo, final int topicId) {
        final URI uri = uriInfo.getRequestUriBuilder().path(topicId + "").build();
        final HRef hRef = new HRef(uri);
        final Date lastModified = new Date();
        final CreationDate creationDate = new CreationDate(lastModified);
        final GetLastModified getLastModified = new GetLastModified(lastModified);
        final Status status = new Status((javax.ws.rs.core.Response.StatusType) OK);
        final Prop prop = new Prop(creationDate, getLastModified, COLLECTION);
        final PropStat propStat = new PropStat(prop, status);

        final Response folder = new Response(hRef, null, null, null, propStat);

        return folder;
    }

}
