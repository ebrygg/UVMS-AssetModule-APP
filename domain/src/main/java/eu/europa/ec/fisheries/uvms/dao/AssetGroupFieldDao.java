package eu.europa.ec.fisheries.uvms.dao;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.*;
import eu.europa.ec.fisheries.uvms.entity.AssetGroup;
import eu.europa.ec.fisheries.uvms.entity.AssetGroupField;
import java.util.List;
import java.util.UUID;

@Stateless
@Local
public class AssetGroupFieldDao {

    @PersistenceContext
    private EntityManager em;

    public AssetGroupField create(AssetGroupField field) {
        em.persist(field);
        return field;
    }

    public AssetGroupField get(UUID id) {
        try {
            TypedQuery<AssetGroupField> query = em.createNamedQuery(AssetGroupField.ASSETGROUP_FIELD_GETBYID,
                    AssetGroupField.class);
            query.setParameter("id", id);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public AssetGroupField update(AssetGroupField field) {
        return em.merge(field);
    }

    public AssetGroupField delete(AssetGroupField field) {
        em.remove(field);
        return field;
    }

    public void removeFieldsForGroup(UUID assetGroupId) {
        Query qry = em.createNamedQuery(AssetGroupField.ASSETGROUP_FIELD_CLEAR);
        qry.setParameter("assetgroup", assetGroupId);
        qry.executeUpdate();
    }

    public List<AssetGroupField> retrieveFieldsForGroup(UUID assetGroupId) {
        TypedQuery<AssetGroupField> qry = em.createNamedQuery(AssetGroupField.ASSETGROUP_RETRIEVE_FIELDS_FOR_GROUP,
                AssetGroupField.class);
        qry.setParameter("assetgroup", assetGroupId);
        return qry.getResultList();
    }

}
