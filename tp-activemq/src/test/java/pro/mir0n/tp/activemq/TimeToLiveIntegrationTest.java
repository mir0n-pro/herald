/*
 *  Herald Messaging Framework (tm)
 *  tp-activemq -- transport provider (ActiveMQ), tests
 *
 *  Copyright(c) 2001, 2026 mir0n&co www.mir0n.pro
 *  mailto:mir0n.the.programmer@gmail.com
 */
package pro.mir0n.tp.activemq;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.activemq.broker.BrokerService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pro.mir0n.messaging.BusConstants;
import pro.mir0n.messaging.transport.BusIdentity;
import pro.mir0n.messaging.transport.ConsumeSettings;
import pro.mir0n.messaging.transport.PublishSettings;
import pro.mir0n.messaging.transport.TransportConsumer;
import pro.mir0n.messaging.transport.TransportMessage;
import pro.mir0n.messaging.transport.TransportPublisher;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class TimeToLiveIntegrationTest {

    private static final TransportProvider PROVIDER = new TransportProvider();
    private static final ObjectMapper OM = new ObjectMapper();

    private static BrokerService broker;
    private static String url;

    @BeforeAll
    static void startBroker() throws Exception {
        broker = new BrokerService();
        broker.setPersistent(false);
        broker.setUseJmx(false);
        broker.setBrokerName("tpAmqTimeToLiveTest");
        broker.addConnector("tcp://localhost:0");
        broker.start();
        broker.waitUntilStarted();
        url = broker.getTransportConnectors().get(0).getPublishableConnectString();
    }

    @AfterAll
    static void stopBroker() throws Exception {
        if (broker != null) {
            broker.stop();
            broker.waitUntilStopped();
        }
    }

    private static BusIdentity id() {
        return new BusIdentity("test-bus", "test-slot", "test.0");
    }

    private static TransportMessage msg() {
        return new TransportMessage(
                Map.of(BusConstants.FIELD_ROD_ID, "test.0", BusConstants.FIELD_EVENT_TYPE, BusConstants.EVENT_CREATE), "e1");
    }

    /** Sends one message to a queue nobody consumes, waits {@code waitMs}, then starts a consumer and waits for it. */
    private static boolean deliveredAfterWait(String queue, Map<String, String> params, long waitMs) throws Exception {
        CountDownLatch got = new CountDownLatch(1);
        TransportPublisher pub = PROVIDER.openPublisher(queue, new PublishSettings(OM, url, id(), params, 0));
        pub.accept(msg());
        Thread.sleep(waitMs);

        TransportConsumer con = PROVIDER.openConsumer(queue, new ConsumeSettings(OM, url, id(), Map.of(), 1, null),
                m -> got.countDown());
        con.start();
        boolean ret = got.await(3, TimeUnit.SECONDS);
        con.close();
        pub.close();
        return ret;
    }

    @Test
    @DisplayName("a message past its time-to-live is dropped by the broker, not delivered to a late consumer")
    void expiredMessageIsDropped() throws Exception {
        assertThat(deliveredAfterWait("test.ttl.expired", Map.of("timeToLiveMs", "300"), 1000)).isFalse();
    }

    @Test
    @DisplayName("without a time-to-live the same message waits and reaches the late consumer")
    void noTimeToLiveWaits() throws Exception {
        assertThat(deliveredAfterWait("test.ttl.none", Map.of(), 1000)).isTrue();
    }
}
