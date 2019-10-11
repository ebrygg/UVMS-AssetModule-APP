/*
﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
© European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.mobileterminal.dao;

import eu.europa.ec.fisheries.uvms.asset.domain.constant.SearchFields;
import eu.europa.ec.fisheries.uvms.asset.domain.entity.Asset;
import eu.europa.ec.fisheries.uvms.asset.domain.mapper.SearchFieldType;
import eu.europa.ec.fisheries.uvms.asset.domain.mapper.SearchKeyValue;
import eu.europa.ec.fisheries.uvms.asset.dto.AssetMTEnrichmentRequest;
import eu.europa.ec.fisheries.uvms.mobileterminal.constants.MobileTerminalConstants;
import eu.europa.ec.fisheries.uvms.mobileterminal.entity.Channel;
import eu.europa.ec.fisheries.uvms.mobileterminal.entity.MobileTerminal;
import eu.europa.ec.fisheries.uvms.mobileterminal.entity.types.MobileTerminalTypeEnum;
import eu.europa.ec.fisheries.uvms.mobileterminal.search.MTSearchFields;
import eu.europa.ec.fisheries.uvms.mobileterminal.search.MTSearchKeyValue;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.exception.AuditException;
import org.hibernate.envers.query.AuditAssociationQuery;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.hibernate.envers.query.criteria.AuditCriterion;
import org.hibernate.envers.query.criteria.AuditDisjunction;
import org.hibernate.envers.query.criteria.ExtendableCriterion;
import org.hibernate.envers.query.criteria.MatchMode;
import org.hibernate.envers.query.internal.impl.EntitiesAtRevisionQuery;

import javax.ejb.Stateless;
import javax.persistence.*;
import javax.persistence.criteria.JoinType;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Stateless
public class TerminalDaoBean {

    @PersistenceContext
    private EntityManager em;

    public MobileTerminal getMobileTerminalById(UUID id) {
        try {
            TypedQuery<MobileTerminal> query = em.createNamedQuery(MobileTerminalConstants.MOBILE_TERMINAL_FIND_BY_ID, MobileTerminal.class);
            query.setParameter("id", id);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public MobileTerminal getMobileTerminalBySerialNo(String serialNo) {
        try {
            TypedQuery<MobileTerminal> query = em.createNamedQuery(MobileTerminalConstants.MOBILE_TERMINAL_FIND_BY_SERIAL_NO, MobileTerminal.class);
            query.setParameter("serialNo", serialNo);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public void removeMobileTerminalAfterTests(String guid) {
        MobileTerminal mobileTerminal = getMobileTerminalById(UUID.fromString(guid));
        em.remove(em.contains(mobileTerminal) ? mobileTerminal : em.merge(mobileTerminal));
    }

    public MobileTerminal createMobileTerminal(MobileTerminal terminal) {
        em.persist(terminal);
        return terminal;
    }

    public MobileTerminal updateMobileTerminal(MobileTerminal terminal) {
        if (terminal == null || terminal.getId() == null)
            throw new IllegalArgumentException("Can't update a non-persisted MobileTerminal");
        return em.merge(terminal);
    }

    @SuppressWarnings("unchecked")
    public List<MobileTerminal> getMobileTerminalsByQuery(String sql) {
            Query query = em.createQuery(sql, MobileTerminal.class);
            return query.getResultList();
    }

    public MobileTerminal findMobileTerminalByAsset(UUID assetId) {
        List<MobileTerminal> ret = em.createNamedQuery(MobileTerminalConstants.MOBILE_TERMINAL_FIND_BY_ASSET_ID, MobileTerminal.class)
                .setParameter("assetId", assetId).getResultList();
        if (ret.size() > 0) return ret.get(0);
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<MobileTerminal> getMobileTerminalRevisionByHistoryId(UUID historyId) {
        AuditReader auditReader = AuditReaderFactory.get(em);
        AuditQuery query = auditReader.createQuery().forRevisionsOfEntity(MobileTerminal.class, true, true);
        return query.add(AuditEntity.property("historyId").eq(historyId)).getResultList();
    }

    public List<MobileTerminal> getMobileTerminalRevisionById(UUID mobileTerminalId) {
        AuditReader auditReader = AuditReaderFactory.get(em);
        List<MobileTerminal> resultList = new ArrayList<>();

        List<Number> revisionNumbers = auditReader.getRevisions(MobileTerminal.class, mobileTerminalId);
        for (Number rev : revisionNumbers) {
            MobileTerminal audited = auditReader.find(MobileTerminal.class, mobileTerminalId, rev);
            resultList.add(audited);
        }
        return resultList;
    }

    @SuppressWarnings("unchecked")
    public List<MobileTerminal> getMobileTerminalRevisionByAssetId(UUID assetId) {
        AuditReader auditReader = AuditReaderFactory.get(em);
        AuditQuery query = auditReader.createQuery().forRevisionsOfEntity(MobileTerminal.class, true, true);
        return query.add(AuditEntity.property("asset_id").eq(assetId)).getResultList();
    }

    public MobileTerminal getMobileTerminalByRequest(AssetMTEnrichmentRequest request) {
        try {
            return em.createNamedQuery(MobileTerminalConstants.MOBILE_TERMINAL_FIND_BY_DNID_AND_MEMBER_NR_AND_TYPE, MobileTerminal.class)
                    .setParameter("dnid", request.getDnidValue())
                    .setParameter("memberNumber", request.getMemberNumberValue())
                    .setParameter("mobileTerminalType", MobileTerminalTypeEnum.valueOf(request.getTranspondertypeValue()))
                    .getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }


    public List<MobileTerminal> getMTListSearchPaginated(Integer pageNumber, Integer pageSize, List<MTSearchKeyValue> searchFields,
                                                   boolean isDynamic, boolean includeInactivated) {
        try {
            AuditQuery query = createAuditQuery(searchFields, isDynamic, includeInactivated);
            query.setFirstResult(pageSize * (pageNumber - 1));
            query.setMaxResults(pageSize);

            //searching for a MT involves searching for values that reside in channel, thus we need to search the db for channels and extract the MT from there
            List<Channel> channelList = query.getResultList();
            List<MobileTerminal> returnList = new ArrayList<>(channelList.size());
            for (Channel channel : channelList) {
                if(returnList.stream().noneMatch(mt -> mt.getId().equals(channel.getMobileTerminal().getId()))) {   //since several channels can be connected to the same MT
                    returnList.add(channel.getMobileTerminal());
                }
            }
            return returnList;
        } catch (AuditException e) {
            return Collections.emptyList();
        }
    }

    private AuditQuery createAuditQuery(List<MTSearchKeyValue> searchFields, boolean isDynamic, boolean includeInactivated) {
        AuditReader auditReader = AuditReaderFactory.get(em);

        //separate search fields for channel and for MT
        List<MTSearchKeyValue> channelSearchValues = new ArrayList<>();
        for (MTSearchKeyValue searchKeyValue : searchFields) {
            if(searchKeyValue.getSearchField().getFieldType().equals(SearchFieldType.CHILD)){
                channelSearchValues.add(searchKeyValue);
            }
        }
        searchFields.removeAll(channelSearchValues);


        AuditAssociationQuery aaQuery;
        MTSearchKeyValue dateSearchField = getDateSearchField(searchFields);
        if (dateSearchField != null) {
            Instant date = Instant.parse(dateSearchField.getSearchValues().get(0));
            Number revisionNumberForDate = auditReader.getRevisionNumberForDate(Date.from(date));
            aaQuery = auditReader.createQuery().forEntitiesAtRevision(Channel.class, revisionNumberForDate).traverseRelation("mobileTerminal", JoinType.INNER);
        } else {
            Number revisionNumberForDate = auditReader.getRevisionNumberForDate(new Date());
            aaQuery = auditReader.createQuery().forEntitiesAtRevision(Channel.class, revisionNumberForDate).traverseRelation("mobileTerminal", JoinType.INNER);

            if (!searchRevisions(searchFields)) {
                aaQuery.add(AuditEntity.revisionNumber().maximize().computeAggregationInInstanceContext());
            }
            if(!includeInactivated) {
                aaQuery.add(AuditEntity.property("active").eq(true));
            }
        }

        ExtendableCriterion operatorMT;
        if (isDynamic) {
            operatorMT = AuditEntity.conjunction();
        } else {
            operatorMT = AuditEntity.disjunction();
        }

        //due to how traverseRelation works, we start out in mobile terminal
        boolean operatorUsed = false;
        for (MTSearchKeyValue searchKeyValue : searchFields) {
            if (useLike(searchKeyValue)) {
                AuditDisjunction op = AuditEntity.disjunction();
                for (String value : searchKeyValue.getSearchValues()) {
                    op.add(AuditEntity.property(searchKeyValue.getSearchField().getFieldName()).ilike(value.replace("*", "%").toLowerCase(), MatchMode.ANYWHERE));
                }
                operatorUsed = true;
                operatorMT.add(op);
            } else if (searchKeyValue.getSearchField().getFieldType().equals(SearchFieldType.MIN_DECIMAL)) {
                operatorUsed = true;
                operatorMT.add(AuditEntity.property(searchKeyValue.getSearchField().getFieldName()).ge(Double.valueOf(searchKeyValue.getSearchValues().get(0))));
            } else if (searchKeyValue.getSearchField().getFieldType().equals(SearchFieldType.MAX_DECIMAL)) {
                operatorUsed = true;
                operatorMT.add(AuditEntity.property(searchKeyValue.getSearchField().getFieldName()).le(Double.valueOf(searchKeyValue.getSearchValues().get(0))));
            } else if (searchKeyValue.getSearchField().getFieldType().equals(SearchFieldType.LIST)) {
                operatorUsed = true;
                AuditDisjunction disjunctionOperator = AuditEntity.disjunction();
                for (String v : searchKeyValue.getSearchValuesAsLowerCase()) {
                    if(searchKeyValue.getSearchField().equals(MTSearchFields.ASSET_ID)){
                        disjunctionOperator.add(AuditEntity.relatedId(searchKeyValue.getSearchField().getFieldName()).eq(UUID.fromString(v)));
                    }else {
                        disjunctionOperator.add(AuditEntity.property(searchKeyValue.getSearchField().getFieldName()).ilike(v, MatchMode.ANYWHERE));
                    }
                }
                operatorMT.add(disjunctionOperator);
            } else if (searchKeyValue.getSearchField().getFieldType().equals(SearchFieldType.NUMBER)) {
                List<Integer> intValues = searchKeyValue.getSearchValues().stream().map(Integer::parseInt).collect(Collectors.toList());
                operatorUsed = true;
                operatorMT.add(AuditEntity.property(searchKeyValue.getSearchField().getFieldName()).in(intValues));
            } else if (searchKeyValue.getSearchField().getFieldType().equals(SearchFieldType.ID)) {
                List<UUID> ids = searchKeyValue.getSearchValues().stream().map(UUID::fromString).collect(Collectors.toList());
                operatorUsed = true;
                operatorMT.add(AuditEntity.property(searchKeyValue.getSearchField().getFieldName()).in(ids));
            } else if (searchKeyValue.getSearchField().getFieldType().equals(SearchFieldType.BOOLEAN) ||
                    searchKeyValue.getSearchField().getFieldType().equals(SearchFieldType.STRING)) {
                operatorUsed = true;
                operatorMT.add(AuditEntity.property(searchKeyValue.getSearchField().getFieldName()).eq(searchKeyValue.getSearchValues().get(0)));
            } else if (searchKeyValue.getSearchField().getFieldType().equals(SearchFieldType.CHILD)) {

            }
        }
        if (operatorUsed) {
            aaQuery.add((AuditCriterion) operatorMT);
        }


        AuditQuery query =  aaQuery.up();       //this moves the query to channel


        ExtendableCriterion operatorChannel;
        if (isDynamic) {
            operatorChannel = AuditEntity.conjunction();
        } else {
            operatorChannel = AuditEntity.disjunction();
        }

        boolean channelOperatorUsed = false;
        for (MTSearchKeyValue channelSearchValue : channelSearchValues) {
            if (useLike(channelSearchValue)) {
                AuditDisjunction op = AuditEntity.disjunction();
                for (String value : channelSearchValue.getSearchValues()) {
                    op.add(AuditEntity.property(channelSearchValue.getSearchField().getFieldName()).ilike(value.replace("*", "%").toLowerCase(), MatchMode.ANYWHERE));
                }
                channelOperatorUsed = true;
                operatorChannel.add(op);
            } else {
                channelOperatorUsed = true;
                AuditDisjunction disjunctionOperator = AuditEntity.disjunction();
                for (String v : channelSearchValue.getSearchValuesAsLowerCase()) {
                    disjunctionOperator.add(AuditEntity.property(channelSearchValue.getSearchField().getFieldName()).ilike(v, MatchMode.ANYWHERE));
                }
                operatorChannel.add(disjunctionOperator);
            }
        }

        if (channelOperatorUsed) {
            query.add((AuditCriterion) operatorChannel);
        }

        return query;
    }

    private boolean useLike(MTSearchKeyValue entry) {
        for (String searchValue : entry.getSearchValues()) {
            if (searchValue.contains("*")) {
                return true;
            }
        }
        return false;
    }

    private boolean searchRevisions(List<MTSearchKeyValue> searchFields) {
        for (MTSearchKeyValue searchKeyValue : searchFields) {
            if (searchKeyValue.getSearchField().equals(MTSearchFields.HIST_GUID)) {
                return true;
            }
        }
        return false;
    }

    private MTSearchKeyValue getDateSearchField(List<MTSearchKeyValue> searchFields) {
        for (MTSearchKeyValue searchKeyValue : searchFields) {
            if (searchKeyValue.getSearchField().equals(MTSearchFields.DATE)) {
                return searchKeyValue;
            }
        }
        return null;
    }


}
