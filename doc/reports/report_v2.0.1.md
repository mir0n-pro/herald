# Release Report: v2.0.0 → HEAD

**Repo:** `herald/develop`  
**Top commit:** `70a63e8`

---

## Release Notes

### doc/release_notes.txt


**v2.0.1-2610.2221**  v2.0.1 -- Herald's own configuration names; a time limit for a message on ActiveMQ  
&nbsp;: Config:      the bus configuration is read under herald (was esquire): the shared topology at  
&nbsp;                 herald.messaging-bus, a service's bus at herald..messaging-bus  
&nbsp;: Config:      the keep's meters and trace are named herald.keep.*; the default Kafka consumer group  
&nbsp;                 is herald-xxrod-audit  
&nbsp;: Feature:     an ActiveMQ leg can set how long a message may wait for a consumer (timeToLiveMs);  
&nbsp;                 after that the broker drops it  
&nbsp;: Doc:         doc\Herald.MessagingBus.md  
&nbsp;                 doc\Herald.MessagingBus.Guides.md  
&nbsp;                 doc\Herald.MessagingBus.MessageStructure.md  
&nbsp;                 doc\Herald.MessagingBus.Q&A.md  
&nbsp;                 doc\Herald.MessagingBus.ContinuingDev.md  
&nbsp;                 doc\img\messaging-bus-params.svg  
&nbsp;                 the Herald logo  
&nbsp;   Components:   messaging,  
&nbsp;                 dataKeep,  
&nbsp;                 tp-activemq,  
&nbsp;                 tp-kafka  

---

## Code Changes

### dataKeep/src/main/java/pro/mir0n/dataKeep/changes.txt


**10/22/2026** mir0n  v2.0.1 -- the keep meters and trace mark named after Herald  
**keep.RodEventDbWriter**  
&nbsp;- the meters herald.keep.write.total / herald.keep.write.duration (were esq.biz.keep.write.*), the trace  
&nbsp;   mark herald.keep.apply (was esq.keep.apply)  

### messaging/src/main/java/pro/mir0n/messaging/changes.txt


**10/22/2026** mir0n  v2.0.1 -- the configuration keys named after Herald  
BusConstants  
&nbsp;- CONFIG_ROOT = "herald" (was "esquire"): the shared catalog binds at herald.messaging-bus, a service's  
&nbsp;   bus ref at herald..messaging-bus  
MessagingBus  
&nbsp;- the javadoc and the comments name the bus refs herald..messaging-bus  
**catalog.MessagingBusCatalog**  
&nbsp;- the javadoc names the catalog key herald.messaging-bus and the bus refs herald..messaging-bus  

### tp-activemq/src/main/java/pro/mir0n/tp/activemq/changes.txt


**10/22/2026** mir0n  v2.0.1 -- the message time-to-live as a declared bus param  
TransportProvider  
&nbsp;- PARAM_TIME_TO_LIVE_MS ("timeToLiveMs") added: how long a sent message may wait for a consumer, read from  
&nbsp;   transport.params; > 0 is applied with setTimeToLive on the publisher's JmsTemplate, absent or 0 = no  
&nbsp;   expiry. Excluded from withParams (a setter, not a broker-URI option); the publisher-opened devLog line  
&nbsp;   carries timeToLiveMs=  

### tp-kafka/src/main/java/pro/mir0n/tp/kafka/changes.txt


**10/22/2026** mir0n  v2.0.1 -- the default consumer group named after Herald  
TransportProvider  
&nbsp;- DEFAULT_GROUP_ID = "herald-xxrod-audit" (was "esquire-xxrod-audit")  

---

## Commits

```

-- 2026-10-22 | commit: 70a63e8 | mir0n.the.programmer | v2.0.1 configuration root - herald --
M	README.md
A	Releases.md
M	dataKeep/src/main/java/pro/mir0n/dataKeep/changes.txt
M	dataKeep/src/main/java/pro/mir0n/dataKeep/keep/RodEventDbWriter.java
M	doc/Herald.MessagingBus.ContinuingDev.md
M	doc/Herald.MessagingBus.Guides.md
M	doc/Herald.MessagingBus.MessageStructure.md
M	doc/Herald.MessagingBus.Q&A.md
M	doc/Herald.MessagingBus.md
M	doc/img/messaging-bus-params.svg
M	doc/logo/herald.3.svg
A	doc/logo/herald.5.png
A	doc/logo/herald.5.svg
M	doc/logo/herald.svg
M	doc/release_notes.txt
M	messaging/src/main/java/pro/mir0n/messaging/BusConstants.java
M	messaging/src/main/java/pro/mir0n/messaging/MessagingBus.java
M	messaging/src/main/java/pro/mir0n/messaging/catalog/MessagingBusCatalog.java
M	messaging/src/main/java/pro/mir0n/messaging/changes.txt
M	messaging/src/test/java/pro/mir0n/messaging/BusRefBindTest.java
M	messaging/src/test/java/pro/mir0n/messaging/MessagingBusTest.java
M	messaging/src/test/java/pro/mir0n/messaging/catalog/MessagingBusCatalogTest.java
M	pom.xml
M	tp-activemq/src/main/java/pro/mir0n/tp/activemq/TransportProvider.java
M	tp-activemq/src/main/java/pro/mir0n/tp/activemq/changes.txt
A	tp-activemq/src/test/java/pro/mir0n/tp/activemq/TimeToLiveIntegrationTest.java
M	tp-kafka/src/main/java/pro/mir0n/tp/kafka/TransportProvider.java
M	tp-kafka/src/main/java/pro/mir0n/tp/kafka/changes.txt
 28 files changed, 494 insertions(+), 186 deletions(-)
```

