package org.jboss.pressgang.ccms.restserver.rest.v1;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.pressgang.ccms.rest.v1.collections.items.join.RESTPropertyCategoryInPropertyTagCollectionItemV1;
import org.jboss.pressgang.ccms.rest.v1.collections.items.join.RESTPropertyTagInPropertyCategoryCollectionItemV1;
import org.jboss.pressgang.ccms.rest.v1.collections.join.RESTPropertyCategoryInPropertyTagCollectionV1;
import org.jboss.pressgang.ccms.rest.v1.collections.join.RESTPropertyTagInPropertyCategoryCollectionV1;
import org.jboss.pressgang.ccms.rest.v1.entities.RESTPropertyTagV1;
import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTBaseEntityV1;
import org.jboss.pressgang.ccms.rest.v1.entities.join.RESTPropertyCategoryInPropertyTagV1;
import org.jboss.pressgang.ccms.rest.v1.entities.join.RESTPropertyTagInPropertyCategoryV1;
import org.jboss.pressgang.ccms.rest.v1.expansion.ExpandDataTrunk;
import org.jboss.pressgang.ccms.restserver.entity.PropertyTagToPropertyTagCategory;
import org.jboss.pressgang.ccms.restserver.rest.v1.base.BaseRESTv1;
import org.jboss.pressgang.ccms.restserver.rest.v1.base.RESTDataObjectCollectionFactory;
import org.jboss.pressgang.ccms.restserver.rest.v1.base.RESTDataObjectFactory;


public class PropertyTagInPropertyCategoryV1Factory
        extends
        RESTDataObjectFactory<RESTPropertyTagInPropertyCategoryV1, PropertyTagToPropertyTagCategory, RESTPropertyTagInPropertyCategoryCollectionV1, RESTPropertyTagInPropertyCategoryCollectionItemV1> {
    public PropertyTagInPropertyCategoryV1Factory() {
        super(PropertyTagToPropertyTagCategory.class);
    }

    @Override
    public RESTPropertyTagInPropertyCategoryV1 createRESTEntityFromDBEntity(final PropertyTagToPropertyTagCategory entity,
            final String baseUrl, final String dataType, final ExpandDataTrunk expand, final Number revision,
            final boolean expandParentReferences, final EntityManager entityManager) {
        assert entity != null : "Parameter entity can not be null";
        assert baseUrl != null : "Parameter baseUrl can not be null";

        final RESTPropertyTagInPropertyCategoryV1 retValue = new RESTPropertyTagInPropertyCategoryV1();

        final List<String> expandOptions = new ArrayList<String>();
        expandOptions.add(RESTPropertyTagInPropertyCategoryV1.PROPERTY_CATEGORIES_NAME);
        expandOptions.add(RESTBaseEntityV1.LOG_DETAILS_NAME);
        if (revision == null)
            expandOptions.add(RESTBaseEntityV1.REVISIONS_NAME);
        retValue.setExpand(expandOptions);

        retValue.setId(entity.getPropertyTag().getId());
        retValue.setName(entity.getPropertyTag().getPropertyTagName());
        retValue.setRegex(entity.getPropertyTag().getPropertyTagRegex());
        retValue.setIsUnique(entity.getPropertyTag().getPropertyTagIsUnique());
        retValue.setRelationshipId(entity.getId());
        retValue.setRelationshipSort(entity.getSorting());

        // REVISIONS
        if (revision == null) {
            retValue.setRevisions(new RESTDataObjectCollectionFactory<RESTPropertyTagInPropertyCategoryV1, PropertyTagToPropertyTagCategory, RESTPropertyTagInPropertyCategoryCollectionV1, RESTPropertyTagInPropertyCategoryCollectionItemV1>()
                    .create(RESTPropertyTagInPropertyCategoryCollectionV1.class, new PropertyTagInPropertyCategoryV1Factory(),
                            entity, entity.getRevisions(entityManager), RESTBaseEntityV1.REVISIONS_NAME, dataType, expand, baseUrl,
                            entityManager));
        }

        // PROPERTY CATEGORIES
        retValue.setPropertyCategories(new RESTDataObjectCollectionFactory<RESTPropertyCategoryInPropertyTagV1, PropertyTagToPropertyTagCategory, RESTPropertyCategoryInPropertyTagCollectionV1, RESTPropertyCategoryInPropertyTagCollectionItemV1>()
                .create(RESTPropertyCategoryInPropertyTagCollectionV1.class, new PropertyCategoryInPropertyTagV1Factory(),
                        entity.getPropertyTag().getPropertyTagToPropertyTagCategoriesList(),
                        RESTPropertyTagV1.PROPERTY_CATEGORIES_NAME, dataType, expand, baseUrl, revision, false, entityManager));

        retValue.setLinks(baseUrl, BaseRESTv1.PROPERTYTAG_URL_NAME, dataType, retValue.getId());
        retValue.setLogDetails(new LogDetailsV1Factory().create(entity, revision, RESTBaseEntityV1.LOG_DETAILS_NAME, expand,
                dataType, baseUrl, entityManager));

        return retValue;
    }

    @Override
    public void syncDBEntityWithRESTEntity(final EntityManager entityManager, final PropertyTagToPropertyTagCategory entity,
            final RESTPropertyTagInPropertyCategoryV1 dataObject) {
        if (dataObject.hasParameterSet(RESTPropertyTagInPropertyCategoryV1.RELATIONSHIP_SORT_NAME))
            entity.setSorting(dataObject.getRelationshipSort());

        entityManager.persist(entity);
    }
}