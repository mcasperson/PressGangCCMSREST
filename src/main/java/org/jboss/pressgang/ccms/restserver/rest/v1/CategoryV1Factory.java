package org.jboss.pressgang.ccms.restserver.rest.v1;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.pressgang.ccms.rest.v1.collections.RESTCategoryCollectionV1;
import org.jboss.pressgang.ccms.rest.v1.collections.items.RESTCategoryCollectionItemV1;
import org.jboss.pressgang.ccms.rest.v1.collections.items.join.RESTTagInCategoryCollectionItemV1;
import org.jboss.pressgang.ccms.rest.v1.collections.join.RESTTagInCategoryCollectionV1;
import org.jboss.pressgang.ccms.rest.v1.entities.RESTCategoryV1;
import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTBaseEntityV1;
import org.jboss.pressgang.ccms.rest.v1.entities.join.RESTTagInCategoryV1;
import org.jboss.pressgang.ccms.rest.v1.exceptions.InvalidParameterException;
import org.jboss.pressgang.ccms.rest.v1.expansion.ExpandDataTrunk;
import org.jboss.pressgang.ccms.restserver.entity.Category;
import org.jboss.pressgang.ccms.restserver.entity.Tag;
import org.jboss.pressgang.ccms.restserver.entity.TagToCategory;
import org.jboss.pressgang.ccms.restserver.rest.v1.base.BaseRESTv1;
import org.jboss.pressgang.ccms.restserver.rest.v1.base.RESTDataObjectCollectionFactory;
import org.jboss.pressgang.ccms.restserver.rest.v1.base.RESTDataObjectFactory;


class CategoryV1Factory extends
        RESTDataObjectFactory<RESTCategoryV1, Category, RESTCategoryCollectionV1, RESTCategoryCollectionItemV1> {
    CategoryV1Factory() {
        super(Category.class);
    }

    @Override
    public RESTCategoryV1 createRESTEntityFromDBEntity(final Category entity, final String baseUrl, final String dataType,
            final ExpandDataTrunk expand, final Number revision, final boolean expandParentReferences,
            final EntityManager entityManager) {
        assert entity != null : "Parameter topic can not be null";
        assert baseUrl != null : "Parameter baseUrl can not be null";

        final RESTCategoryV1 retValue = new RESTCategoryV1();

        final List<String> expandOptions = new ArrayList<String>();
        expandOptions.add(BaseRESTv1.TAGS_EXPANSION_NAME);
        expandOptions.add(RESTBaseEntityV1.LOG_DETAILS_NAME);
        if (revision == null)
            expandOptions.add(RESTBaseEntityV1.REVISIONS_NAME);

        retValue.setExpand(expandOptions);

        retValue.setId(entity.getCategoryId());
        retValue.setName(entity.getCategoryName());
        retValue.setDescription(entity.getCategoryDescription());
        retValue.setMutuallyExclusive(entity.isMutuallyExclusive());
        retValue.setSort(entity.getCategorySort());

        if (revision == null) {
            retValue.setRevisions(new RESTDataObjectCollectionFactory<RESTCategoryV1, Category, RESTCategoryCollectionV1, RESTCategoryCollectionItemV1>()
                    .create(RESTCategoryCollectionV1.class, new CategoryV1Factory(), entity, entity.getRevisions(entityManager),
                            RESTBaseEntityV1.REVISIONS_NAME, dataType, expand, baseUrl, entityManager));
        }
        retValue.setTags(new RESTDataObjectCollectionFactory<RESTTagInCategoryV1, TagToCategory, RESTTagInCategoryCollectionV1, RESTTagInCategoryCollectionItemV1>()
                .create(RESTTagInCategoryCollectionV1.class, new TagInCategoryV1Factory(), entity.getTagToCategoriesArray(),
                        BaseRESTv1.TAGS_EXPANSION_NAME, dataType, expand, baseUrl, entityManager));

        retValue.setLinks(baseUrl, BaseRESTv1.CATEGORY_URL_NAME, dataType, retValue.getId());
        retValue.setLogDetails(new LogDetailsV1Factory().create(entity, revision, RESTBaseEntityV1.LOG_DETAILS_NAME, expand,
                dataType, baseUrl, entityManager));

        return retValue;
    }

    @Override
    public void syncDBEntityWithRESTEntity(final EntityManager entityManager, final Category entity, final RESTCategoryV1 dataObject)
            throws InvalidParameterException {
        if (dataObject.hasParameterSet(RESTCategoryV1.DESCRIPTION_NAME))
            entity.setCategoryDescription(dataObject.getDescription());
        if (dataObject.hasParameterSet(RESTCategoryV1.MUTUALLYEXCLUSIVE_NAME))
            entity.setMutuallyExclusive(dataObject.getMutuallyExclusive());
        if (dataObject.hasParameterSet(RESTCategoryV1.NAME_NAME))
            entity.setCategoryName(dataObject.getName());
        if (dataObject.hasParameterSet(RESTCategoryV1.SORT_NAME))
            entity.setCategorySort(dataObject.getSort());

        entityManager.persist(entity);

        /* Many To Many - Add will create a mapping */
        if (dataObject.hasParameterSet(RESTCategoryV1.TAGS_NAME) && dataObject.getTags() != null
                && dataObject.getTags().getItems() != null) {
            dataObject.getTags().removeInvalidChangeItemRequests();

            for (final RESTTagInCategoryCollectionItemV1 restEntityItem : dataObject.getTags().getItems()) {
                final RESTTagInCategoryV1 restEntity = restEntityItem.getItem();

                if (restEntityItem.returnIsAddItem() || restEntityItem.returnIsRemoveItem()) {
                    final Tag dbEntity = entityManager.find(Tag.class, restEntity.getId());
                    if (dbEntity == null)
                        throw new InvalidParameterException("No Tag entity was found with the primary key "
                                + restEntity.getId());

                    if (restEntityItem.returnIsAddItem()) {
                        entity.addTagRelationship(dbEntity);
                    } else if (restEntityItem.returnIsRemoveItem()) {
                        entity.removeTagRelationship(dbEntity);
                    }
                } else if (restEntityItem.returnIsUpdateItem()) {
                    final TagToCategory dbEntity = entityManager.find(TagToCategory.class, restEntity.getRelationshipId());
                    if (dbEntity == null)
                        throw new InvalidParameterException("No TagToCategory entity was found with the primary key "
                                + restEntity.getRelationshipId());

                    new TagInCategoryV1Factory().syncDBEntityWithRESTEntity(entityManager, dbEntity, restEntity);
                }
            }
        }
    }
}
