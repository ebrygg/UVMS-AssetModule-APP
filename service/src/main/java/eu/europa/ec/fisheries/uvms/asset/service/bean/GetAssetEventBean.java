package eu.europa.ec.fisheries.uvms.asset.service.bean;

import eu.europa.ec.fisheries.uvms.asset.types.AssetDTO;
import eu.europa.ec.fisheries.uvms.asset.types.AssetId;
import eu.europa.ec.fisheries.uvms.asset.enums.AssetIdTypeEnum;
import eu.europa.ec.fisheries.uvms.asset.exception.AssetServiceException;
import eu.europa.ec.fisheries.uvms.asset.message.AssetDataSourceQueue;
import eu.europa.ec.fisheries.uvms.asset.message.event.AssetMessageErrorEvent;
import eu.europa.ec.fisheries.uvms.asset.message.event.AssetMessageEvent;
import eu.europa.ec.fisheries.uvms.asset.message.producer.MessageProducer;
import eu.europa.ec.fisheries.uvms.asset.service.AssetService;
import eu.europa.ec.fisheries.uvms.asset.service.constants.ParameterKey;
import eu.europa.ec.fisheries.uvms.config.exception.ConfigServiceException;
import eu.europa.ec.fisheries.uvms.config.service.ParameterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.jms.TextMessage;

@Stateless
@LocalBean
public class GetAssetEventBean {

    final static Logger LOG = LoggerFactory.getLogger(GetAssetEventBean.class);

    @EJB
    private AssetService service;

    @EJB
    private ParameterService parameters;

    @EJB
    private MessageProducer messageProducer;

    @Inject
    @AssetMessageErrorEvent
    Event<AssetMessageEvent> assetErrorEvent;

    public void getAsset(TextMessage textMessage, AssetId assetId) {
        LOG.info("Getting asset.");
        AssetDataSourceQueue dataSource = null;
        AssetDTO asset = null;
        boolean messageSent = false;


        /*

        try {
            dataSource = decideDataflow(assetId);
            LOG.debug("Got message to AssetModule, Executing Get asset from datasource {}", dataSource.name());
            asset = service.getAssetById(assetId, dataSource);
        } catch (AssetException e) {
            LOG.error("[ Error when getting asset from source {}. ] ", dataSource.name());
            assetErrorEvent.fire(new AssetMessageEvent(textMessage, AssetModuleResponseMapper.createFaultMessage(FaultCode.ASSET_MESSAGE, "Exception when getting asset from source : " + dataSource.name() + " Error message: " + e.getMessage())));
            messageSent = true;
            asset = null;
        }

        if (asset != null && !dataSource.equals(AssetDataSourceQueue.INTERNAL)) {
            try {
                AssetDTO upsertedAsset = service.upsertAsset(asset, dataSource.name());
                asset.getAssetId().setGuid(upsertedAsset.getAssetId().getGuid());
            } catch (AssetException e) {
                LOG.error("[ Couldn't upsert asset in internal ]");
                assetErrorEvent.fire(new AssetMessageEvent(textMessage, AssetModuleResponseMapper.createFaultMessage(FaultCode.ASSET_MESSAGE, e.getMessage())));
                messageSent = true;
            }
        }

        if (!messageSent) {
            try {
                messageProducer.sendModuleResponseMessage(textMessage, AssetModuleResponseMapper.mapAssetModuleResponse(asset));
            } catch (AssetModelMapperException e) {
                LOG.error("[ Error when mapping asset ] ");
                assetErrorEvent.fire(new AssetMessageEvent(textMessage, AssetModuleResponseMapper.createFaultMessage(FaultCode.ASSET_MESSAGE, "Exception when mapping asset" + e.getMessage())));
            }
        }
        */
    }

    private AssetDataSourceQueue decideDataflow(AssetId assetId) throws AssetServiceException {

        try {
            // If search is made by guid, no other source is relevant
            if (AssetIdTypeEnum.GUID.equals(assetId.getType())) {
                return AssetDataSourceQueue.INTERNAL;
            }

            Boolean xeu = parameters.getBooleanValue(ParameterKey.EU_USE.getKey());
            Boolean national = parameters.getBooleanValue(ParameterKey.NATIONAL_USE.getKey());
            LOG.debug("Settings for dataflow are: XEU: {0} NATIONAL: {1}", new Object[]{xeu, national});
            if (xeu && national) {
                return AssetDataSourceQueue.NATIONAL;
            }
            if (national) {
                return AssetDataSourceQueue.NATIONAL;
            } else if (xeu) {
                return AssetDataSourceQueue.XEU;
            } else {
                return AssetDataSourceQueue.INTERNAL;
            }
        } catch (ConfigServiceException e) {
            LOG.error("[ Error when deciding data flow. ] ");
            throw new AssetServiceException(e.getMessage());
        }

    }


}
