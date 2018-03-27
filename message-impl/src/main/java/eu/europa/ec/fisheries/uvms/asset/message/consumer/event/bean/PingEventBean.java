package eu.europa.ec.fisheries.uvms.asset.message.consumer.event.bean;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europa.ec.fisheries.uvms.asset.message.event.AssetMessageErrorEvent;
import eu.europa.ec.fisheries.uvms.asset.message.event.AssetMessageEvent;
import eu.europa.ec.fisheries.uvms.asset.message.producer.MessageProducer;
import eu.europa.ec.fisheries.uvms.asset.model.exception.AssetModelMarshallException;
import eu.europa.ec.fisheries.uvms.asset.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.wsdl.asset.module.PingResponse;

@Stateless
public class PingEventBean {

    private static final Logger LOG = LoggerFactory.getLogger(PingEventBean.class);

    @Inject
    private MessageProducer messageProducer;

    @Inject
    @AssetMessageErrorEvent
    Event<AssetMessageEvent> assetErrorEvent;

    public void ping(AssetMessageEvent message) {
        try {
            PingResponse pingResponse = new PingResponse();
            pingResponse.setResponse("pong");
            messageProducer.sendModuleResponseMessage(message.getMessage(), JAXBMarshaller.marshallJaxBObjectToString(pingResponse));
        } catch (AssetModelMarshallException e) {
            LOG.error("[ Error when marshalling ping response ]");
        }
    }
}
