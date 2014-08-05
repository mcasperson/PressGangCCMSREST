/*
  Copyright 2011-2014 Red Hat, Inc

  This file is part of PressGang CCMS.

  PressGang CCMS is free software: you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  PressGang CCMS is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with PressGang CCMS.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.jboss.pressgang.ccms.server.rest.v1.interceptor;

import org.jboss.pressgang.ccms.rest.v1.constants.RESTv1Constants;
import org.jboss.pressgang.ccms.rest.v1.jaxrsinterfaces.RESTInterfaceV1;
import org.jboss.pressgang.ccms.server.constants.Constants;
import org.jboss.pressgang.ccms.server.rest.v1.RESTv1;
import org.jboss.pressgang.ccms.utils.common.VersionUtilities;
import org.jboss.resteasy.annotations.interception.ServerInterceptor;
import org.jboss.resteasy.core.Headers;
import org.jboss.resteasy.core.ResourceMethodInvoker;
import org.jboss.resteasy.core.ServerResponse;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.util.List;

@Provider
@ServerInterceptor
public class RESTv1VersionInterceptor implements ContainerRequestFilter {


    private static final int UPGRADE_STATUS_CODE = 426;
    private static final String RESTv1_VERSION = VersionUtilities.getAPIVersion(RESTInterfaceV1.class);
    private static final boolean IS_RESTv1_SNAPSHOT = isSnapshotVersion(RESTv1_VERSION);
    private static final int MIN_CSP_MAJOR_VERSION = 1;
    private static final int MIN_CSP_MINOR_VERSION = 8;

    private static final String REST_VERSION_ERROR_MSG = "The REST Client Implementation is out of date, " +
            "and no longer supported. Please update the REST Client library.";
    private static final String CSP_VERSION_ERROR_MSG = "The csprocessor application is out of date, " +
            "and no longer compatible. Please update the csprocessor application.";

    @Override
    public void filter(final ContainerRequestContext requestContext) {
        final ResourceMethodInvoker resourceMethodInvoker =  (ResourceMethodInvoker)requestContext.getProperty(Constants.RESOURCE_METHOD_INVOKER_PROPERTY);

        if (RESTv1.class.equals(resourceMethodInvoker.getResourceClass())) {
            if (requestContext != null && requestContext.getHeaders() != null) {
                final MultivaluedMap<String, String> headers = requestContext.getHeaders();

                // REST API Version Check
                final List<String> RESTVersions = headers.get(RESTv1Constants.X_VERSION_HEADER);
                if (RESTVersions != null) {
                    for (final String version : RESTVersions) {
                        if (!isValidVersion(version)) {
                            requestContext.abortWith(new ServerResponse(REST_VERSION_ERROR_MSG, UPGRADE_STATUS_CODE, new Headers<Object>()));
                        }
                    }
                }

                // CSP Version Check
                final List<String> CSPVersions = headers.get(RESTv1Constants.X_CSP_VERSION_HEADER);
                if (CSPVersions != null) {
                    for (final String version : CSPVersions) {
                        if (!isValidCSPVersion(version)) {
                            requestContext.abortWith(new ServerResponse(CSP_VERSION_ERROR_MSG, UPGRADE_STATUS_CODE, new Headers<Object>()));
                        }
                    }
                }

            }
        }
    }

    /**
     * Checks to see if the REST Version is valid and compatible with the server.
     *
     * @param version The REST Version from the "X-Version" HTTP Header.
     * @return True if the REST Version is compatible, otherwise false.
     */
    protected boolean isValidVersion(final String version) {
        if (isUnknownVersion(version)) return true;

        final Integer majorVersion = getMajorVersion(version);
        final Integer minorVersion = getMinorVersion(version);
        final boolean isSnapshot = isSnapshotVersion(version);
        final Integer MIN_MINOR_RESTv1_VERSION = 0;

        switch (majorVersion) {
            case 1:
                if (minorVersion == null) {
                    return true;
                } else {
                    // Check that the minor version is 0 or higher
                    if (minorVersion < MIN_MINOR_RESTv1_VERSION) {
                        return false;
                    } else if (minorVersion.equals(MIN_MINOR_RESTv1_VERSION)) {
                        if (!IS_RESTv1_SNAPSHOT && isSnapshot) {
                            return false;
                        } else {
                            return true;
                        }
                    } else {
                        return minorVersion > MIN_MINOR_RESTv1_VERSION;
                    }
                }
            default:
                return false;
        }
    }

    protected boolean isValidCSPVersion(final String version) {
        if (isUnknownVersion(version)) return false;

        final Integer majorVersion = getMajorVersion(version);
        final Integer minorVersion = getMinorVersion(version);
        final boolean isSnapshotVersion = isSnapshotVersion(version);

        // Check that the version is 1.6.0 or higher (allowing for snapshots).
        return (majorVersion != null && majorVersion >= MIN_CSP_MAJOR_VERSION) && (minorVersion == null || (minorVersion >= MIN_CSP_MINOR_VERSION &&
                !isSnapshotVersion) || minorVersion > MIN_CSP_MINOR_VERSION);
    }

    protected static Integer getMajorVersion(final String version) {
        // Remove any extra information, ie -SNAPSHOT
        final String cleanedVersion = version.replaceAll("-.*", "");
        String[] tmp = cleanedVersion.split("\\.");
        return Integer.parseInt(tmp[0]);
    }

    protected static Integer getMinorVersion(final String version) {
        // Remove any extra information, ie -SNAPSHOT
        final String cleanedVersion = version.replaceAll("-.*", "");
        String[] tmp = cleanedVersion.split("\\.");
        return tmp.length >= 2 ? Integer.parseInt(tmp[1]) : null;
    }

    protected static Integer getZStreamVersion(final String version) {
        // Remove any extra information, ie -SNAPSHOT
        final String cleanedVersion = version.replaceAll("-.*", "");
        String[] tmp = cleanedVersion.split("\\.");
        return tmp.length >= 3 ? Integer.parseInt(tmp[2]) : null;
    }

    protected static boolean isSnapshotVersion(final String version) {
        return version.contains("-SNAPSHOT");
    }

    protected static boolean isUnknownVersion(final String version) {
        return version.toLowerCase().equals("unknown");
    }
}
