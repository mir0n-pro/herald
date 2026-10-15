package pro.mir0n.messaging;

import pro.mir0n.messaging.transport.ConsumeSettings;
import pro.mir0n.messaging.transport.ITransportProvider;
import pro.mir0n.messaging.transport.PublishSettings;
import pro.mir0n.messaging.transport.TransportConsumer;
import pro.mir0n.messaging.transport.TransportMessage;
import pro.mir0n.messaging.transport.TransportPublisher;

import java.util.function.Consumer;

/** Test-only transport provider, resolved by its full class name (the TransportProviders escape hatch). */
public class FakeTransportProvider implements ITransportProvider {

    @Override
    public TransportPublisher openPublisher(String destination, PublishSettings s) {
        return TransportPublisher.of(msg -> { }, () -> { });
    }

    @Override
    public TransportConsumer openConsumer(String destination, ConsumeSettings s, Consumer<TransportMessage> handler) {
        return TransportConsumer.of(() -> { }, () -> { });
    }
}
