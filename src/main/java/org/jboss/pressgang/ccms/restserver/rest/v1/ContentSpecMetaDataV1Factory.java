package org.jboss.pressgang.ccms.restserver.rest.v1;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.RESTCSTranslatedStringCollectionV1;
import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.items.RESTCSTranslatedStringCollectionItemV1;
import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.items.join.RESTAssignedCSMetaDataCollectionItemV1;
import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.join.RESTAssignedCSMetaDataCollectionV1;
import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTBaseEntityV1;
import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTCSNodeV1;
import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTCSTranslatedStringV1;
import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.join.RESTAssignedCSMetaDataV1;
import org.jboss.pressgang.ccms.rest.v1.exceptions.InvalidParameterException;
import org.jboss.pressgang.ccms.rest.v1.expansion.ExpandDataTrunk;
import org.jboss.pressgang.ccms.restserver.entity.contentspec.CSTranslatedString;
import org.jboss.pressgang.ccms.restserver.entity.contentspec.ContentSpecToCSMetaData;
import org.jboss.pressgang.ccms.restserver.rest.v1.base.BaseRESTv1;
import org.jboss.pressgang.ccms.restserver.rest.v1.base.RESTDataObjectCollectionFactory;
import org.jboss.pressgang.ccms.restserver.rest.v1.base.RESTDataObjectFactory;

public class ContentSpecMetaDataV1Factory
        extends
        RESTDataObjectFactory<RESTAssignedCSMetaDataV1, ContentSpecToCSMetaData, RESTAssignedCSMetaDataCollectionV1, RESTAssignedCSMetaDataCollectionItemV1> {

    public ContentSpecMetaDataV1Factory() {
        super(ContentSpecToCSMetaData.class);
    }

    @Override
    public RESTAssignedCSMetaDataV1 createRESTEntityFromDBEntity(final ContentSpecToCSMetaData entity, final String baseUrl,
            final String dataType, final ExpandDataTrunk expand, final Number revision, final boolean expandParentReferences,
            final EntityManager entityManager) {
        assert entity != null : "Parameter entity can not be null";
        assert baseUrl != null : "Parameter baseUrl can not be null";

        final RESTAssignedCSMetaDataV1 retValue = new RESTAssignedCSMetaDataV1();

        final List<String> expandOptions = new ArrayList<String>();
        expandOptions.add(RESTBaseEntityV1.LOG_DETAILS_NAME);
        expandOptions.add(RESTAssignedCSMetaDataV1.TRANSLATED_STRINGS_NAME);
        if (revision == null)
            expandOptions.add(RESTBaseEntityV1.REVISIONS_NAME);
        retValue.setExpand(expandOptions);

        retValue.setId(entity.getCSMetaData().getId());
        retValue.setRelationshipId(entity.getId());
        retValue.setTitle(entity.getCSMetaData().getCSMetaDataTitle());
        retValue.setDescription(entity.getCSMetaData().getCSMetaDataDescription());
        retValue.setValue(entity.getValue());

        // REVISIONS
        if (revision == null) {
            retValue.setRevisions(new RESTDataObjectCollectionFactory<RESTAssignedCSMetaDataV1, ContentSpecToCSMetaData, RESTAssignedCSMetaDataCollectionV1, RESTAssignedCSMetaDataCollectionItemV1>()
                    .create(RESTAssignedCSMetaDataCollectionV1.class, new ContentSpecMetaDataV1Factory(), entity,
                            entity.getRevisions(entityManager), RESTBaseEntityV1.REVISIONS_NAME, dataType, expand, baseUrl,
                            entityManager));
        }

        // TRANSLATED STRINGS
        retValue.setTranslatedStrings_OTM(new RESTDataObjectCollectionFactory<RESTCSTranslatedStringV1, CSTranslatedString, RESTCSTranslatedStringCollectionV1, RESTCSTranslatedStringCollectionItemV1>()
                .create(RESTCSTranslatedStringCollectionV1.class, new CSTranslatedStringV1Factory(),
                        entity.getCSTranslatedStringsList(), RESTAssignedCSMetaDataV1.TRANSLATED_STRINGS_NAME, dataType,
                        expand, baseUrl, false, entityManager));

        retValue.setLinks(baseUrl, BaseRESTv1.CONTENT_SPEC_META_DATA_URL_NAME, dataType, retValue.getId());
        retValue.setLogDetails(new LogDetailsV1Factory().create(entity, revision, RESTBaseEntityV1.LOG_DETAILS_NAME, expand,
                dataType, baseUrl, entityManager));

        return retValue;
    }

    @Override
    public void syncDBEntityWithRESTEntity(final EntityManager entityManager, final ContentSpecToCSMetaData entity,
            final RESTAssignedCSMetaDataV1 dataObject) throws InvalidParameterException {
        if (dataObject.hasParameterSet(RESTAssignedCSMetaDataV1.VALUE_NAME))
            entity.setValue(dataObject.getValue());

        entityManager.persist(entity);

        /* One To Many - Add will create a child entity */
        if (dataObject.hasParameterSet(RESTCSNodeV1.TRANSLATED_STRINGS_NAME) && dataObject.getTranslatedStrings_OTM() != null
                && dataObject.getTranslatedStrings_OTM().getItems() != null) {
            dataObject.getTranslatedStrings_OTM().removeInvalidChangeItemRequests();

            /* remove any items first */
            for (final RESTCSTranslatedStringCollectionItemV1 restEntityItem : dataObject.getTranslatedStrings_OTM().getItems()) {
                final RESTCSTranslatedStringV1 restEntity = restEntityItem.getItem();

                if (restEntityItem.returnIsRemoveItem()) {
                    final CSTranslatedString dbEntity = entityManager.find(CSTranslatedString.class, restEntity.getId());
                    if (dbEntity == null)
                        throw new InvalidParameterException("No CSTranslatedString entity was found with the primary key "
                                + restEntity.getId());

                    entity.removeTranslatedString(dbEntity);
                } else if (restEntityItem.returnIsAddItem()) {
                    final CSTranslatedString dbEntity = new CSTranslatedStringV1Factory().createDBEntityFromRESTEntity(
                            entityManager, restEntity);
                    entityManager.persist(dbEntity);
                    entity.addTranslatedString(dbEntity);
                } else if (restEntityItem.returnIsUpdateItem()) {
                    final CSTranslatedString dbEntity = entityManager.find(CSTranslatedString.class, restEntity.getId());
                    if (dbEntity == null)
                        throw new InvalidParameterException("No CSTranslatedString entity was found with the primary key "
                                + restEntity.getId());

                    new CSTranslatedStringV1Factory().syncDBEntityWithRESTEntity(entityManager, dbEntity, restEntity);
                }
            }
        }
    }
}
