package org.jboss.pressgang.ccms.server.rest.v1;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.pressgang.ccms.model.PropertyTag;
import org.jboss.pressgang.ccms.model.PropertyTagCategory;
import org.jboss.pressgang.ccms.model.PropertyTagToPropertyTagCategory;
import org.jboss.pressgang.ccms.model.base.AuditedEntity;
import org.jboss.pressgang.ccms.rest.v1.collections.RESTPropertyTagCollectionV1;
import org.jboss.pressgang.ccms.rest.v1.collections.items.RESTPropertyTagCollectionItemV1;
import org.jboss.pressgang.ccms.rest.v1.collections.items.join.RESTPropertyCategoryInPropertyTagCollectionItemV1;
import org.jboss.pressgang.ccms.rest.v1.collections.join.RESTPropertyCategoryInPropertyTagCollectionV1;
import org.jboss.pressgang.ccms.rest.v1.constants.RESTv1Constants;
import org.jboss.pressgang.ccms.rest.v1.entities.RESTPropertyTagV1;
import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTBaseEntityV1;
import org.jboss.pressgang.ccms.rest.v1.entities.join.RESTPropertyCategoryInPropertyTagV1;
import org.jboss.pressgang.ccms.rest.v1.entities.join.RESTPropertyTagInPropertyCategoryV1;
import org.jboss.pressgang.ccms.rest.v1.expansion.ExpandDataTrunk;
import org.jboss.pressgang.ccms.server.rest.v1.base.RESTDataObjectCollectionFactory;
import org.jboss.pressgang.ccms.server.rest.v1.base.RESTDataObjectFactory;
import org.jboss.pressgang.ccms.server.rest.v1.utils.RESTv1Utilities;
import org.jboss.pressgang.ccms.server.utils.EnversUtilities;
import org.jboss.resteasy.spi.BadRequestException;

public class PropertyTagV1Factory extends RESTDataObjectFactory<RESTPropertyTagV1, PropertyTag, RESTPropertyTagCollectionV1,
        RESTPropertyTagCollectionItemV1> {
    private final PropertyCategoryInPropertyTagV1Factory propertyCategoryInPropertyTagFactory = new
            PropertyCategoryInPropertyTagV1Factory();

    public PropertyTagV1Factory() {
        super(PropertyTag.class);
    }

    @Override
    public RESTPropertyTagV1 createRESTEntityFromDBEntityInternal(final PropertyTag entity, final String baseUrl, final String dataType,
            final ExpandDataTrunk expand, final Number revision, final boolean expandParentReferences, final EntityManager entityManager) {
        assert entity != null : "Parameter entity can not be null";
        assert baseUrl != null : "Parameter baseUrl can not be null";

        final RESTPropertyTagV1 retValue = new RESTPropertyTagV1();

        final List<String> expandOptions = new ArrayList<String>();
        expandOptions.add(RESTPropertyTagInPropertyCategoryV1.PROPERTY_CATEGORIES_NAME);
        expandOptions.add(RESTBaseEntityV1.LOG_DETAILS_NAME);
        if (revision == null) expandOptions.add(RESTBaseEntityV1.REVISIONS_NAME);
        retValue.setExpand(expandOptions);

        retValue.setId(entity.getPropertyTagId());
        retValue.setName(entity.getPropertyTagName());
        retValue.setDescription(entity.getPropertyTagDescription());
        retValue.setRegex(entity.getPropertyTagRegex());
        retValue.setIsUnique(entity.getPropertyTagIsUnique());
        retValue.setCanBeNull(entity.isPropertyTagCanBeNull());

        // REVISIONS
        if (revision == null && expand != null && expand.contains(RESTBaseEntityV1.REVISIONS_NAME)) {
            retValue.setRevisions(
                    new RESTDataObjectCollectionFactory<RESTPropertyTagV1, PropertyTag, RESTPropertyTagCollectionV1,
                            RESTPropertyTagCollectionItemV1>().create(
                            RESTPropertyTagCollectionV1.class, new PropertyTagV1Factory(), entity,
                            EnversUtilities.getRevisions(entityManager, entity), RESTBaseEntityV1.REVISIONS_NAME, dataType, expand, baseUrl,
                            entityManager));
        }

        // PROPERTY CATEGORIES
        if (expand != null && expand.contains(RESTPropertyTagV1.PROPERTY_CATEGORIES_NAME)) {
            retValue.setPropertyCategories(
                    new RESTDataObjectCollectionFactory<RESTPropertyCategoryInPropertyTagV1, PropertyTagToPropertyTagCategory,
                            RESTPropertyCategoryInPropertyTagCollectionV1, RESTPropertyCategoryInPropertyTagCollectionItemV1>().create(
                            RESTPropertyCategoryInPropertyTagCollectionV1.class, new PropertyCategoryInPropertyTagV1Factory(),
                            entity.getPropertyTagToPropertyTagCategoriesList(), RESTPropertyTagV1.PROPERTY_CATEGORIES_NAME, dataType,
                            expand, baseUrl, revision, false, entityManager));
        }

        retValue.setLinks(baseUrl, RESTv1Constants.PROPERTYTAG_URL_NAME, dataType, retValue.getId());

        return retValue;
    }

    @Override
    public void syncDBEntityWithRESTEntityFirstPass(final EntityManager entityManager,
            Map<RESTBaseEntityV1<?, ?, ?>, AuditedEntity> newEntityCache, final PropertyTag entity, final RESTPropertyTagV1 dataObject) {
        if (dataObject.hasParameterSet(RESTPropertyTagV1.DESCRIPTION_NAME)) entity.setPropertyTagDescription(dataObject.getDescription());
        if (dataObject.hasParameterSet(RESTPropertyTagV1.CANBENULL_NAME)) entity.setPropertyTagCanBeNull(dataObject.getCanBeNull());
        if (dataObject.hasParameterSet(RESTPropertyTagV1.NAME_NAME)) entity.setPropertyTagName(dataObject.getName());
        if (dataObject.hasParameterSet(RESTPropertyTagV1.REGEX_NAME)) entity.setPropertyTagRegex(dataObject.getRegex());
        if (dataObject.hasParameterSet(RESTPropertyTagV1.ISUNIQUE_NAME)) entity.setPropertyTagIsUnique(dataObject.getIsUnique());

        entityManager.persist(entity);

        // Many to Many
        if (dataObject.hasParameterSet(
                RESTPropertyTagV1.PROPERTY_CATEGORIES_NAME) && dataObject.getPropertyCategories() != null && dataObject
                .getPropertyCategories().getItems() != null) {
            dataObject.getPropertyCategories().removeInvalidChangeItemRequests();

            for (final RESTPropertyCategoryInPropertyTagCollectionItemV1 restEntityItem : dataObject.getPropertyCategories().getItems()) {
                final RESTPropertyCategoryInPropertyTagV1 restEntity = restEntityItem.getItem();

                if (restEntityItem.returnIsRemoveItem()) {
                    final PropertyTagCategory dbEntity = entityManager.find(PropertyTagCategory.class, restEntity.getId());
                    if (dbEntity == null)
                        throw new BadRequestException("No PropertyTagCategory entity was found with the primary key " + restEntity.getId());

                    entity.removePropertyTagCategory(dbEntity);
                } else if (restEntityItem.returnIsUpdateItem()) {
                    final PropertyTagToPropertyTagCategory dbEntity = entityManager.find(PropertyTagToPropertyTagCategory.class,
                            restEntity.getRelationshipId());
                    if (dbEntity == null) throw new BadRequestException(
                            "No PropertyTagToPropertyTagCategory entity was found with the primary key " + restEntity.getRelationshipId());
                    if (!entity.getPropertyTagToPropertyTagCategories().contains(dbEntity)) throw new BadRequestException(
                            "No PropertyTagToPropertyTagCategory entity was found with the primary key " + restEntity.getRelationshipId()
                                    + " for PropertyTag " + entity.getId());

                    propertyCategoryInPropertyTagFactory.syncDBEntityWithRESTEntityFirstPass(entityManager, newEntityCache, dbEntity,
                            restEntity);
                }
            }
        }
    }

    @Override
    public void syncDBEntityWithRESTEntitySecondPass(EntityManager entityManager,
            Map<RESTBaseEntityV1<?, ?, ?>, AuditedEntity> newEntityCache, PropertyTag entity, RESTPropertyTagV1 dataObject) {
        // Many to Many
        if (dataObject.hasParameterSet(
                RESTPropertyTagV1.PROPERTY_CATEGORIES_NAME) && dataObject.getPropertyCategories() != null && dataObject
                .getPropertyCategories().getItems() != null) {
            dataObject.getPropertyCategories().removeInvalidChangeItemRequests();

            for (final RESTPropertyCategoryInPropertyTagCollectionItemV1 restEntityItem : dataObject.getPropertyCategories().getItems()) {
                final RESTPropertyCategoryInPropertyTagV1 restEntity = restEntityItem.getItem();

                if (restEntityItem.returnIsAddItem()) {
                    final PropertyTagCategory dbEntity = RESTv1Utilities.findEntity(entityManager, newEntityCache, restEntity,
                            PropertyTagCategory.class);
                    if (dbEntity == null)
                        throw new BadRequestException("No PropertyTagCategory entity was found with the primary key " + restEntity.getId());

                    entity.addPropertyTagCategory(dbEntity);
                } else if (restEntityItem.returnIsUpdateItem()) {
                    final PropertyTagToPropertyTagCategory dbEntity = entityManager.find(PropertyTagToPropertyTagCategory.class,
                            restEntity.getRelationshipId());
                    if (dbEntity == null) throw new BadRequestException(
                            "No PropertyTagToPropertyTagCategory entity was found with the primary key " + restEntity.getRelationshipId());

                    propertyCategoryInPropertyTagFactory.syncDBEntityWithRESTEntitySecondPass(entityManager, newEntityCache, dbEntity,
                            restEntity);
                }
            }
        }
    }
}