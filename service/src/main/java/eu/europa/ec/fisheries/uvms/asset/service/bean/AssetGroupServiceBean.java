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
package eu.europa.ec.fisheries.uvms.asset.service.bean;

import eu.europa.ec.fisheries.uvms.asset.message.ModuleQueue;
import eu.europa.ec.fisheries.uvms.asset.message.consumer.AssetQueueConsumer;
import eu.europa.ec.fisheries.uvms.asset.message.mapper.AuditModuleRequestMapper;
import eu.europa.ec.fisheries.uvms.asset.message.producer.MessageProducer;
import eu.europa.ec.fisheries.uvms.asset.model.exception.AssetException;
import eu.europa.ec.fisheries.uvms.asset.model.exception.InputArgumentException;
import eu.europa.ec.fisheries.uvms.asset.service.AssetGroupService;
import eu.europa.ec.fisheries.uvms.audit.model.exception.AuditModelMarshallException;
import eu.europa.ec.fisheries.uvms.bean.AssetGroupDomainModelBean;
import eu.europa.ec.fisheries.uvms.dao.AssetGroupDao;
import eu.europa.ec.fisheries.uvms.entity.assetgroup.AssetGroup;
import eu.europa.ec.fisheries.wsdl.asset.group.AssetGroupWSDL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.List;

@Stateless
public class AssetGroupServiceBean implements AssetGroupService {

    private static  final String GROUP_QUALIFIER_PREFIX = "Group: ";

    @EJB
    MessageProducer messageProducer;

    @EJB
    AssetQueueConsumer receiver;

    @EJB
    private AssetGroupDomainModelBean assetGroupDomainModel;
    private AssetGroupDao assetGroupDao;

    final static Logger LOG = LoggerFactory.getLogger(AssetGroupServiceBean.class);

    @Override
    public List<AssetGroupWSDL> getAssetGroupList(String user) throws AssetException {
        LOG.info("Getting asset group list by user: {}.", user);
        if (user == null || user.isEmpty()) {
            throw new InputArgumentException("Invalid user");
        }

        List<AssetGroupWSDL> assetGroupList = assetGroupDomainModel.getAssetGroupListByUser(user);
        return assetGroupList;
    }

    @Override
    public List<AssetGroupWSDL> getAssetGroupListByAssetGuid(String assetGuid) throws AssetException {
        LOG.info("Getting asset group list by asset guid: {}.", assetGuid);
        if (assetGuid == null || assetGuid.isEmpty()) {
            throw new InputArgumentException("Invalid asset");
        }

        List<AssetGroupWSDL> assetGroups = assetGroupDomainModel.getAssetGroupsByAssetGuid(assetGuid);
        return assetGroups;
    }

    @Override
    public AssetGroupWSDL getAssetGroupById(String guid) throws AssetException {
        LOG.info("Getting asset group by id: {}.", guid);
        if (guid == null) {
            throw new InputArgumentException("No asset group to get");
        }

        AssetGroupWSDL assetGroup = assetGroupDomainModel.getAssetGroup(guid);
        return assetGroup;
    }

    @Override
    public AssetGroupWSDL createAssetGroup(AssetGroupWSDL assetGroup, String username) throws AssetException {
        if (assetGroup == null) {
            throw new InputArgumentException("No asset group to create");
        }
        AssetGroupWSDL createdAssetGroup = assetGroupDomainModel.createAssetGroup(assetGroup, username);
        try {
            String auditData = AuditModuleRequestMapper.mapAuditLogAssetGroupCreated(createdAssetGroup.getGuid(), username, GROUP_QUALIFIER_PREFIX + createdAssetGroup.getName());
            messageProducer.sendModuleMessage(auditData, ModuleQueue.AUDIT);
        } catch (AuditModelMarshallException e) {
            LOG.error("Failed to send audit log message! Asset Group with id {} was created", createdAssetGroup.getGuid());
        }
        return createdAssetGroup;
    }

    @Override
    public AssetGroupWSDL updateAssetGroup(AssetGroupWSDL assetGroup, String username) throws AssetException {
        if (assetGroup == null) {
            throw new InputArgumentException("No asset group to update");
        }
        if (assetGroup.getGuid() == null) {
            throw new InputArgumentException("No id on asset group to update");
        }
        AssetGroupWSDL updatedAssetGroup = assetGroupDomainModel.updateAssetGroup(assetGroup, username);
        try {
            String auditData = AuditModuleRequestMapper.mapAuditLogAssetGroupUpdated(updatedAssetGroup.getGuid(), username, GROUP_QUALIFIER_PREFIX + updatedAssetGroup.getName());
            messageProducer.sendModuleMessage(auditData, ModuleQueue.AUDIT);
        } catch (AuditModelMarshallException e) {
            LOG.error("Failed to send audit log message! Asset Group with id {} was updated", updatedAssetGroup.getGuid());
        }

        return updatedAssetGroup;
    }

    @Override
    public AssetGroupWSDL deleteAssetGroupById(String guid, String username) throws AssetException {
        LOG.info("Deleting asset group by id: {}.", guid);
        if (guid == null) {
            throw new InputArgumentException("No asset group to remove");
        }

        AssetGroupWSDL deletedAssetGroup = assetGroupDomainModel.deleteAssetGroup(guid, username);
        try {
            String auditData = AuditModuleRequestMapper.mapAuditLogAssetGroupDeleted(deletedAssetGroup.getGuid(),  username, GROUP_QUALIFIER_PREFIX  + deletedAssetGroup.getName() );
            messageProducer.sendModuleMessage(auditData, ModuleQueue.AUDIT);
        } catch (AuditModelMarshallException e) {
            LOG.error("Failed to send audit log message! Asset Group with id {} was deleted", deletedAssetGroup.getGuid());
        }
        return deletedAssetGroup;
    }

}