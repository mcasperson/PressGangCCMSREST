package org.jboss.pressgang.ccms.server.rest.v1.factory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import org.jboss.pressgang.ccms.model.FilterLocale;
import org.jboss.pressgang.ccms.rest.v1.collections.RESTFilterLocaleCollectionV1;
import org.jboss.pressgang.ccms.rest.v1.collections.items.RESTFilterLocaleCollectionItemV1;
import org.jboss.pressgang.ccms.rest.v1.entities.RESTFilterLocaleV1;
import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTBaseEntityV1;
import org.jboss.pressgang.ccms.rest.v1.expansion.ExpandDataTrunk;
import org.jboss.pressgang.ccms.server.rest.v1.factory.base.RESTEntityCollectionFactory;
import org.jboss.pressgang.ccms.server.rest.v1.factory.base.RESTEntityFactory;
import org.jboss.pressgang.ccms.server.utils.EnversUtilities;

@ApplicationScoped
public class FilterLocaleV1Factory extends RESTEntityFactory<RESTFilterLocaleV1, FilterLocale, RESTFilterLocaleCollectionV1,
        RESTFilterLocaleCollectionItemV1> {
    @Inject FilterV1Factory filterFactory;

    @Override
    public RESTFilterLocaleV1 createRESTEntityFromDBEntityInternal(final FilterLocale entity, final String baseUrl, final String dataType,
            final ExpandDataTrunk expand, final Number revision, boolean expandParentReferences) {
        assert entity != null : "Parameter entity can not be null";
        assert baseUrl != null : "Parameter baseUrl can not be null";

        final RESTFilterLocaleV1 retValue = new RESTFilterLocaleV1();

        final List<String> expandOptions = new ArrayList<String>();
        expandOptions.add(RESTBaseEntityV1.LOG_DETAILS_NAME);
        if (revision == null) expandOptions.add(RESTBaseEntityV1.REVISIONS_NAME);

        retValue.setExpand(expandOptions);

        retValue.setId(entity.getFilterLocaleId());
        retValue.setState(entity.getLocaleState());
        retValue.setLocale(entity.getLocaleName());

        // REVISIONS
        if (revision == null && expand != null && expand.contains(RESTBaseEntityV1.REVISIONS_NAME)) {
            retValue.setRevisions(RESTEntityCollectionFactory.create(RESTFilterLocaleCollectionV1.class, this, entity,
                    EnversUtilities.getRevisions(entityManager, entity), RESTBaseEntityV1.REVISIONS_NAME, dataType, expand, baseUrl,
                    entityManager));
        }

        // PARENT
        if (expandParentReferences && expand != null && expand.contains(RESTFilterLocaleV1.FILTER_NAME) && entity.getFilter() != null) {
            retValue.setFilter(filterFactory.createRESTEntityFromDBEntity(entity.getFilter(), baseUrl, dataType,
                    expand.get(RESTFilterLocaleV1.FILTER_NAME), revision, expandParentReferences));
        }

        return retValue;
    }

    @Override
    public void syncDBEntityWithRESTEntityFirstPass(final FilterLocale entity, final RESTFilterLocaleV1 dataObject) {
        if (dataObject.hasParameterSet(RESTFilterLocaleV1.LOCALE_NAME)) entity.setLocaleName(dataObject.getLocale());
        if (dataObject.hasParameterSet(RESTFilterLocaleV1.STATE_NAME)) entity.setLocaleState(dataObject.getState());
    }

    @Override
    protected Class<FilterLocale> getDatabaseClass() {
        return FilterLocale.class;
    }
}
