package com.telus.credit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.cloud.gcp.pubsub.integration.AckMode;
import org.springframework.cloud.gcp.pubsub.integration.inbound.PubSubInboundChannelAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.messaging.MessageChannel;

@Configuration
public class PubSubConfig {

    public static final String XCONV_SUBSCRIPTION_NAME_PROPERTY_KEY = "${creditmgmt.pubsub.xconv.subscriptionName}";
    public static final String CAPI_SUBSCRIPTION_NAME_PROPERTY_KEY  = "${creditmgmt.pubsub.capi.subscriptionName}";
    public static final String MERGECP_SUBSCRIPTION_NAME_PROPERTY_KEY  = "${creditmgmt.pubsub.mergecp.subscriptionName}";

    @Bean
    public MessageChannel xConvPubSubInputChannel() {
        return new PublishSubscribeChannel();
    }

    @Bean
    @ConditionalOnProperty("creditmgmt.pubsub.xconv.subscriptionName")
    public PubSubInboundChannelAdapter xConvMessageChannelAdapter(PubSubTemplate pubSubTemplate, @Value(XCONV_SUBSCRIPTION_NAME_PROPERTY_KEY) String subscriptionName) {
        PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(pubSubTemplate, subscriptionName);
        adapter.setOutputChannel(xConvPubSubInputChannel());
        adapter.setAckMode(AckMode.MANUAL);
        adapter.setPayloadType(String.class);
        return adapter;
    }

    @Bean
    public MessageChannel capiPubSubInputChannel() {
        return new PublishSubscribeChannel();
    }

    @Bean
    @ConditionalOnProperty("creditmgmt.pubsub.capi.subscriptionName")
    public PubSubInboundChannelAdapter accountSyncMessageChannelAdapter(PubSubTemplate pubSubTemplate, @Value(CAPI_SUBSCRIPTION_NAME_PROPERTY_KEY) String subscriptionName) {
        PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(pubSubTemplate, subscriptionName);
        adapter.setOutputChannel(capiPubSubInputChannel());
        adapter.setAckMode(AckMode.MANUAL);
        adapter.setPayloadType(String.class);
        return adapter;
    }

    
    @Bean
    public MessageChannel mergecpPubSubInputChannel() {
        return new PublishSubscribeChannel();
    }
    @Bean
    @ConditionalOnProperty("creditmgmt.pubsub.mergecp.subscriptionName")
    public PubSubInboundChannelAdapter mergeCPMessageChannelAdapter(PubSubTemplate pubSubTemplate, @Value(MERGECP_SUBSCRIPTION_NAME_PROPERTY_KEY) String subscriptionName) {
        PubSubInboundChannelAdapter adapter = new PubSubInboundChannelAdapter(pubSubTemplate, subscriptionName);
        adapter.setOutputChannel(mergecpPubSubInputChannel());
        adapter.setAckMode(AckMode.MANUAL);
        adapter.setPayloadType(String.class);
        return adapter;
    }    

}
