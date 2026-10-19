# Release Report: 0.0.0 → HEAD

**Repo:** `herald/develop`  
**Top commit:** `b6a1b7b`

---

## Release Notes

### doc/release_notes.txt

Herald 2.0  
Herald -- messaging, dataKeep, tp-activemq, tp-kafka, tp-redis, tp-sqns, tp-kinesis  

**v2.0.0-2610.1720**  v2.0.0 -- the jars are published on GitHub Packages  
&nbsp;: Config:      the jars are published to GitHub Packages, on a version tag or by hand  
&nbsp;                 (.github/workflows/publish.yml)  
&nbsp;: Config:      the keep's Maven artifact is named herald-datakeep, all lowercase  
&nbsp;: Doc:         README.md  
&nbsp;                 doc\Herald.MessagingBus.ContinuingDev.md  
&nbsp;   Components:   dataKeep  

**v2.0.0-2610.1419**  v2.0.0 -- the first release: the messaging framework leaves Esquire  
&nbsp;: Refactoring: herald-messaging, the framework, comes from Esquire services, where it was  
&nbsp;                 messaging  
&nbsp;: Refactoring: herald-dataKeep, the keep that writes bus messages into a database, comes from  
&nbsp;                 Esquire services; it no longer depends on the Esquire common library  
&nbsp;: Refactoring: the transports come from Esquire services: herald-tp-activemq, herald-tp-kafka,  
&nbsp;                 herald-tp-redis, herald-tp-sqns, herald-tp-kinesis  
&nbsp;: Doc:         README.md  
&nbsp;                 doc\Herald.MessagingBus.md  
&nbsp;                 doc\Herald.MessagingBus.Guides.md  
&nbsp;                 doc\Herald.MessagingBus.MessageStructure.md  
&nbsp;                 doc\Herald.MessagingBus.Q&A.md  
&nbsp;                 doc\Herald.MessagingBus.ContinuingDev.md  
&nbsp;   Components:   messaging (new),  
&nbsp;                 dataKeep (new),  
&nbsp;                 tp-activemq (new),  
&nbsp;                 tp-kafka (new),  
&nbsp;                 tp-redis (new),  
&nbsp;                 tp-sqns (new),  
&nbsp;                 tp-kinesis (new)  
&nbsp;   The history before v2.0.0 is in the Esquire release notes, release v1.2.16:  
&nbsp;     https://github.com/mir0n-pro/esquire.services/blob/release/v1.2.16/doc/release_notes.txt  

---

## Code Changes

### dataKeep/src/main/java/pro/mir0n/dataKeep/changes.txt

herald dataKeep library -- pro.mir0n.dataKeep  

**10/13/2026** mir0n  v2.0.0 -- the keep library moves into Herald, and its last edge onto the Esquire domain goes with it  
**keep.RodEventDbWriter**  
&nbsp;- EsqBizMeters / EsqTraceMark -> pro.mir0n.o11y.BizMeters / TraceMark. These were the ONE thing that  
&nbsp;   stopped the keep from travelling with the bus: they lived in the Esquire common library, so a keep  
&nbsp;   inside Herald meant herald -> esquire.services -> herald. Both import Micrometer and the JDK and  
&nbsp;   nothing else, so they moved down into Armoury instead. The meter and span NAMES are unchanged  
&nbsp;   (esq.keep.apply, esq.biz.keep.write.*), so no dashboard moves  
(every file)  
&nbsp;- package pro.mir0n.esquire.dataKeep -> pro.mir0n.dataKeep; the masthead is the Herald Messaging Framework;  
&nbsp;   one history line per file records the move  
**pom.xml**  
&nbsp;- artifactId herald-dataKeep, parent herald-parent 2.0.0, groupId pro.mir0n. The esquire-common dependency is  
&nbsp;   GONE -- that was the cycle. In its place armoury-utils, which carries pro.mir0n.o11y, plus micrometer-core and  
&nbsp;   micrometer-observation at provided scope, since an actuator-enabled service carries the implementation. The  
&nbsp;   Spring starters and the two JDBC drivers are as they were.  


### messaging/src/main/java/pro/mir0n/messaging/changes.txt

herald messaging framework classes -- pro.mir0n.messaging  

**10/13/2026** mir0n  v2.0.0 -- the bus becomes the Herald Messaging Framework  
BusConstants  
&nbsp;- CONFIG_ROOT: the root of the bus CONFIGURATION namespace, which used to be the literal "esquire"  
&nbsp;   written out at four call sites. The value is unchanged, so every existing properties file and topology  
&nbsp;   file reads as it did; it is one constant now so that naming the namespace after this product instead  
&nbsp;   is a one-line decision  
&nbsp;- CATALOG_KEY: CONFIG_ROOT + ".messaging-bus", the key the shared topology binds at  
&nbsp;- four U+2014 em dashes in the field-list comment replaced with -- , the house ASCII rule  
**catalog.MessagingBusCatalog**  
&nbsp;- binds BusConstants.CATALOG_KEY instead of the literal "esquire.messaging-bus"  
MessagingBus  
&nbsp;- ROLE_KEY is built from BusConstants.CONFIG_ROOT; the bus-ref bind and the no-role message with it  
(every file)  
&nbsp;- package pro.mir0n.esquire.messaging -> pro.mir0n.messaging; the masthead is the Herald Messaging Framework;  
&nbsp;   one history line per file records the move  
**pom.xml**  
&nbsp;- artifactId herald-messaging, parent herald-parent 2.0.0, groupId pro.mir0n. The utils dependency is  
&nbsp;   armoury-utils, version pinned in herald-parent. Everything else is as it was: jackson at compile, slf4j and  
&nbsp;   the three spring-boot artifacts provided, spring-test for the catalog binding test.  
(not moved -- left for the Esquire side)  
&nbsp;- catalog.TopologyDriftGuardTest: it reads the three per-environment topology FILES out of the services  
&nbsp;   repo (compose, k8s charts, k8s-oci) and fails the build when an ActiveMQ endpoint in one of them is  
&nbsp;   missing failover:. That guards an Esquire deployment artifact, not this framework, so it belongs with  
&nbsp;   those files. The 1.2 copy is untouched  

### tp-activemq/src/main/java/pro/mir0n/messaging/changes.txt

herald messaging helper classes -- pro.mir0n.messaging.jms  

**10/13/2026** mir0n  v2.0.0 -- moved into Herald with tp-activemq  
**jms.Utils**  
&nbsp;- package pro.mir0n.esquire.messaging.jms -> pro.mir0n.messaging.jms; the masthead is the Herald  
&nbsp;   Messaging Framework. One U+2014 em dash in a javadoc line replaced with -- , the house ASCII rule.  
&nbsp;   It still ships in the tp-activemq jar, in a messaging package: that split is as it was  

### tp-activemq/src/main/java/pro/mir0n/tp/activemq/changes.txt

herald ActiveMQ transport provider classes -- pro.mir0n.tp.activemq  

**10/13/2026** mir0n  v2.0.0 -- the transport provider moves into Herald  
**META-INF/spring.factories**  
&nbsp;- the AutoConfigurationImportFilter class name follows the package: pro.mir0n.tp.activemq.TpActiveMqAutoConfigFilter  
(every file)  
&nbsp;- package pro.mir0n.esquire.tp.activemq -> pro.mir0n.tp.activemq; the masthead is the Herald Messaging Framework;  
&nbsp;   one history line per file records the move  
**pom.xml**  
&nbsp;- artifactId herald-tp-activemq, parent herald-parent 2.0.0, groupId pro.mir0n. The messaging dependency is  
&nbsp;   herald-messaging. The embedded-broker test dependencies are unchanged.  

### tp-kafka/src/main/java/pro/mir0n/tp/kafka/changes.txt

herald Kafka transport provider classes -- pro.mir0n.tp.kafka  

**10/13/2026** mir0n  v2.0.0 -- the transport provider moves into Herald  
**META-INF/spring.factories**  
&nbsp;- the AutoConfigurationImportFilter class name follows the package: pro.mir0n.tp.kafka.TpKafkaAutoConfigFilter  
TransportProvider  
&nbsp;- the default consumer group id is left as it was (esquire-xxrod-audit): it is a WIRE value, and changing  
&nbsp;   it would reset a live deployment's offsets  
(every file)  
&nbsp;- package pro.mir0n.esquire.tp.kafka -> pro.mir0n.tp.kafka; the masthead is the Herald Messaging Framework;  
&nbsp;   one history line per file records the move  
**pom.xml**  
&nbsp;- artifactId herald-tp-kafka, parent herald-parent 2.0.0, groupId pro.mir0n. The messaging dependency is  
&nbsp;   herald-messaging.  

### tp-kinesis/src/main/java/pro/mir0n/tp/kinesis/changes.txt

herald Amazon Kinesis transport provider classes -- pro.mir0n.tp.kinesis  

**10/13/2026** mir0n  v2.0.0 -- the transport provider moves into Herald  
(every file)  
&nbsp;- package pro.mir0n.esquire.tp.kinesis -> pro.mir0n.tp.kinesis; the masthead is the Herald Messaging Framework;  
&nbsp;   one history line per file records the move  
**pom.xml**  
&nbsp;- artifactId herald-tp-kinesis, parent herald-parent 2.0.0, groupId pro.mir0n. The messaging dependency is  
&nbsp;   herald-messaging, still provided, so the aws-lib folder stays the AWS client and nothing else. lombok drops  
&nbsp;   out of the copy-dependencies exclude list, because Herald has none.  

### tp-redis/src/main/java/pro/mir0n/tp/redis/changes.txt

herald Redis transport provider classes -- pro.mir0n.tp.redis  

**10/13/2026** mir0n  v2.0.0 -- the transport provider moves into Herald  
**META-INF/spring.factories**  
&nbsp;- the AutoConfigurationImportFilter class name follows the package: pro.mir0n.tp.redis.TpRedisAutoConfigFilter  
(every file)  
&nbsp;- package pro.mir0n.esquire.tp.redis -> pro.mir0n.tp.redis; the masthead is the Herald Messaging Framework;  
&nbsp;   one history line per file records the move  
**pom.xml**  
&nbsp;- artifactId herald-tp-redis, parent herald-parent 2.0.0, groupId pro.mir0n. The messaging dependency is  
&nbsp;   herald-messaging.  

### tp-sqns/src/main/java/pro/mir0n/tp/sqns/changes.txt

herald Amazon SQS / Amazon SNS transport provider classes -- pro.mir0n.tp.sqns  

**10/13/2026** mir0n  v2.0.0 -- the transport provider moves into Herald  
(every file)  
&nbsp;- package pro.mir0n.esquire.tp.sqns -> pro.mir0n.tp.sqns; the masthead is the Herald Messaging Framework;  
&nbsp;   one history line per file records the move  
**pom.xml**  
&nbsp;- artifactId herald-tp-sqns, parent herald-parent 2.0.0, groupId pro.mir0n. The messaging dependency is  
&nbsp;   herald-messaging, still provided, so the aws-lib folder stays the AWS client and nothing else. lombok drops  
&nbsp;   out of the copy-dependencies exclude list, because Herald has none.  

---

## Commits

```

-- 2026-10-18 | commit: b6a1b7b | mir0n.the.programmer | v2.0.0 finalization --
A	.github/workflows/publish.yml
M	README.md
M	dataKeep/pom.xml
M	doc/Herald.MessagingBus.ContinuingDev.md
M	doc/Herald.MessagingBus.Guides.md
M	doc/Herald.MessagingBus.MessageStructure.md
M	doc/Herald.MessagingBus.Q&A.md
M	doc/Herald.MessagingBus.md
A	doc/logo/MQ-cd.svg
A	doc/logo/activemq.svg
A	doc/logo/dataKeep.svg
A	doc/logo/herald.3.svg
M	doc/logo/herald.svg
A	doc/logo/kafka.svg
A	doc/logo/kinesis-cd.svg
A	doc/logo/messaging.svg
A	doc/logo/msk-cd.svg
A	doc/logo/redis.svg
A	doc/logo/sns-cd.svg
A	doc/logo/sqs-cd.svg
M	doc/release_notes.txt
A	doc/reports/report_v2.0.0.md
M	pom.xml
 23 files changed, 1783 insertions(+), 117 deletions(-)

-- 2026-10-14 | commit: 2216a46 | mir0n.the.programmer | v2.0 initial version --
A	.gitignore
A	LICENSE
M	README.md
A	dataKeep/pom.xml
A	dataKeep/src/main/java/pro/mir0n/dataKeep/changes.txt
A	dataKeep/src/main/java/pro/mir0n/dataKeep/director/IKeepDirector.java
A	dataKeep/src/main/java/pro/mir0n/dataKeep/keep/KeepApplier.java
A	dataKeep/src/main/java/pro/mir0n/dataKeep/keep/KeepDataSourceParams.java
A	dataKeep/src/main/java/pro/mir0n/dataKeep/keep/KeepSqlStore.java
A	dataKeep/src/main/java/pro/mir0n/dataKeep/keep/RodEventDbWriter.java
A	dataKeep/src/main/java/pro/mir0n/dataKeep/keep/XRodInProcessKeep.java
A	dataKeep/src/main/resources/spring.properties
A	dataKeep/src/test/java/pro/mir0n/dataKeep/keep/RodEventDbWriterTest.java
A	doc/Herald.MessagingBus.ContinuingDev.md
A	doc/Herald.MessagingBus.Guides.md
A	doc/Herald.MessagingBus.MessageStructure.md
A	doc/Herald.MessagingBus.Q&A.md
A	doc/Herald.MessagingBus.md
A	doc/img/messaging-bus-architecture.svg
A	doc/img/messaging-bus-classes.svg
A	doc/img/messaging-bus-params.svg
A	doc/logo/herald.svg
A	doc/release_notes.txt
A	messaging/pom.xml
A	messaging/src/main/java/pro/mir0n/messaging/BusConstants.java
A	messaging/src/main/java/pro/mir0n/messaging/BusHealthIndicator.java
A	messaging/src/main/java/pro/mir0n/messaging/IRodEventRepo.java
A	messaging/src/main/java/pro/mir0n/messaging/IXRod.java
A	messaging/src/main/java/pro/mir0n/messaging/MessagingBus.java
A	messaging/src/main/java/pro/mir0n/messaging/RodEvent.java
A	messaging/src/main/java/pro/mir0n/messaging/RodEventRepoRegistry.java
A	messaging/src/main/java/pro/mir0n/messaging/TransportHealthIndicator.java
A	messaging/src/main/java/pro/mir0n/messaging/catalog/BusNode.java
A	messaging/src/main/java/pro/mir0n/messaging/catalog/BusRef.java
A	messaging/src/main/java/pro/mir0n/messaging/catalog/BusSlot.java
A	messaging/src/main/java/pro/mir0n/messaging/catalog/BusTransport.java
A	messaging/src/main/java/pro/mir0n/messaging/catalog/MessagingBus.java
A	messaging/src/main/java/pro/mir0n/messaging/catalog/MessagingBusCatalog.java
A	messaging/src/main/java/pro/mir0n/messaging/catalog/Role.java
A	messaging/src/main/java/pro/mir0n/messaging/catalog/XRodParams.java
A	messaging/src/main/java/pro/mir0n/messaging/changes.txt
A	messaging/src/main/java/pro/mir0n/messaging/o11y/IRodMeters.java
A	messaging/src/main/java/pro/mir0n/messaging/o11y/IRodObserver.java
A	messaging/src/main/java/pro/mir0n/messaging/o11y/IRodTracer.java
A	messaging/src/main/java/pro/mir0n/messaging/o11y/RodObserverHolder.java
A	messaging/src/main/java/pro/mir0n/messaging/transport/BusIdentity.java
A	messaging/src/main/java/pro/mir0n/messaging/transport/ConsumeSettings.java
A	messaging/src/main/java/pro/mir0n/messaging/transport/ITransportProvider.java
A	messaging/src/main/java/pro/mir0n/messaging/transport/OwnExcluding.java
A	messaging/src/main/java/pro/mir0n/messaging/transport/PublishSettings.java
A	messaging/src/main/java/pro/mir0n/messaging/transport/SelectingReceiver.java
A	messaging/src/main/java/pro/mir0n/messaging/transport/TransportConsumer.java
A	messaging/src/main/java/pro/mir0n/messaging/transport/TransportHealth.java
A	messaging/src/main/java/pro/mir0n/messaging/transport/TransportMessage.java
A	messaging/src/main/java/pro/mir0n/messaging/transport/TransportProviders.java
A	messaging/src/main/java/pro/mir0n/messaging/transport/TransportPublisher.java
A	messaging/src/main/java/pro/mir0n/messaging/transport/TransportSettings.java
A	messaging/src/main/java/pro/mir0n/messaging/xrod/RodEventCodec.java
A	messaging/src/main/java/pro/mir0n/messaging/xrod/RodPublisher.java
A	messaging/src/main/java/pro/mir0n/messaging/xrod/RodTransportAdapter.java
A	messaging/src/main/java/pro/mir0n/messaging/xrod/impl/AXRod.java
A	messaging/src/main/java/pro/mir0n/messaging/xrod/impl/ISessionSublayer.java
A	messaging/src/main/java/pro/mir0n/messaging/xrod/impl/MsgAudit.java
A	messaging/src/main/java/pro/mir0n/messaging/xrod/impl/XRod.java
A	messaging/src/main/java/pro/mir0n/messaging/xrod/impl/XRodDisabled.java
A	messaging/src/main/java/pro/mir0n/messaging/xrod/impl/XRodInProcess.java
A	messaging/src/main/java/pro/mir0n/messaging/xrod/impl/XRodInfo.java
A	messaging/src/main/java/pro/mir0n/messaging/xrod/impl/XRodInfoParams.java
A	messaging/src/main/java/pro/mir0n/messaging/xrod/impl/XRodRR.java
A	messaging/src/main/java/pro/mir0n/messaging/xrod/impl/sublayer/AliveSession.java
A	messaging/src/main/java/pro/mir0n/messaging/xrod/impl/sublayer/AliveSessionRR.java
A	messaging/src/main/java/pro/mir0n/messaging/xrod/impl/sublayer/SendRetrySublayer.java
A	messaging/src/main/java/pro/mir0n/messaging/xrod/impl/sublayer/SessionSublayerFactory.java
A	messaging/src/test/java/pro/mir0n/messaging/BrokerDownTransportProvider.java
A	messaging/src/test/java/pro/mir0n/messaging/BusHealthIndicatorTest.java
A	messaging/src/test/java/pro/mir0n/messaging/BusRefBindTest.java
A	messaging/src/test/java/pro/mir0n/messaging/CapturingTransportProvider.java
A	messaging/src/test/java/pro/mir0n/messaging/EncodeFailingTransportProvider.java
A	messaging/src/test/java/pro/mir0n/messaging/FakeTransportProvider.java
A	messaging/src/test/java/pro/mir0n/messaging/MessagingBusTest.java
A	messaging/src/test/java/pro/mir0n/messaging/ProducerOnlyTransportProvider.java
A	messaging/src/test/java/pro/mir0n/messaging/TransportHealthIndicatorTest.java
A	messaging/src/test/java/pro/mir0n/messaging/XRodParamsTest.java
A	messaging/src/test/java/pro/mir0n/messaging/catalog/MessagingBusCatalogTest.java
A	messaging/src/test/java/pro/mir0n/messaging/o11y/RodObserverHolderTest.java
A	messaging/src/test/java/pro/mir0n/messaging/transport/BusIdentityTest.java
A	messaging/src/test/java/pro/mir0n/messaging/transport/OwnExcludingTest.java
A	messaging/src/test/java/pro/mir0n/messaging/transport/SelectingReceiverTest.java
A	messaging/src/test/java/pro/mir0n/messaging/transport/TransportHealthTest.java
A	messaging/src/test/java/pro/mir0n/messaging/transport/TransportProvidersTest.java
A	messaging/src/test/java/pro/mir0n/messaging/xrod/RodEventCodecTest.java
A	messaging/src/test/java/pro/mir0n/messaging/xrod/RodEventRepoRegistryTest.java
A	messaging/src/test/java/pro/mir0n/messaging/xrod/RodTransportAdapterTest.java
A	messaging/src/test/java/pro/mir0n/messaging/xrod/XRodBrokerDownTest.java
A	messaging/src/test/java/pro/mir0n/messaging/xrod/XRodRoleSupportTest.java
A	messaging/src/test/java/pro/mir0n/messaging/xrod/XRodSendFailureTest.java
A	messaging/src/test/java/pro/mir0n/messaging/xrod/XRodSubscriptionSelectorTest.java
A	messaging/src/test/java/pro/mir0n/messaging/xrod/XRodTest.java
A	messaging/src/test/java/pro/mir0n/messaging/xrod/XRodValidateTest.java
A	messaging/src/test/java/pro/mir0n/messaging/xrod/impl/XRodDisabledTest.java
A	messaging/src/test/java/pro/mir0n/messaging/xrod/impl/XRodInfoTest.java
A	messaging/src/test/java/pro/mir0n/messaging/xrod/impl/sublayer/AliveSessionTest.java
A	messaging/src/test/java/pro/mir0n/messaging/xrod/impl/sublayer/SendRetrySublayerTest.java
A	pom.xml
A	tp-activemq/pom.xml
A	tp-activemq/src/main/java/pro/mir0n/messaging/changes.txt
A	tp-activemq/src/main/java/pro/mir0n/messaging/jms/Utils.java
A	tp-activemq/src/main/java/pro/mir0n/tp/activemq/TpActiveMqAutoConfigFilter.java
A	tp-activemq/src/main/java/pro/mir0n/tp/activemq/TransportProvider.java
A	tp-activemq/src/main/java/pro/mir0n/tp/activemq/changes.txt
A	tp-activemq/src/main/resources/META-INF/spring.factories
A	tp-activemq/src/test/java/pro/mir0n/tp/activemq/CredentialParamsIntegrationTest.java
A	tp-activemq/src/test/java/pro/mir0n/tp/activemq/NoLocalIntegrationTest.java
A	tp-kafka/pom.xml
A	tp-kafka/src/main/java/pro/mir0n/tp/kafka/TpKafkaAutoConfigFilter.java
A	tp-kafka/src/main/java/pro/mir0n/tp/kafka/TransportProvider.java
A	tp-kafka/src/main/java/pro/mir0n/tp/kafka/changes.txt
A	tp-kafka/src/main/resources/META-INF/spring.factories
A	tp-kafka/src/test/java/pro/mir0n/tp/kafka/TransportProviderTest.java
A	tp-kinesis/pom.xml
A	tp-kinesis/src/main/java/pro/mir0n/tp/kinesis/KinesisConsumer.java
A	tp-kinesis/src/main/java/pro/mir0n/tp/kinesis/TransportProvider.java
A	tp-kinesis/src/main/java/pro/mir0n/tp/kinesis/changes.txt
A	tp-kinesis/src/test/java/pro/mir0n/tp/kinesis/StreamModeParamsTest.java
A	tp-redis/pom.xml
A	tp-redis/src/main/java/pro/mir0n/tp/redis/TpRedisAutoConfigFilter.java
A	tp-redis/src/main/java/pro/mir0n/tp/redis/TransportProvider.java
A	tp-redis/src/main/java/pro/mir0n/tp/redis/changes.txt
A	tp-redis/src/main/resources/META-INF/spring.factories
A	tp-redis/src/test/java/pro/mir0n/tp/redis/TransportProviderTest.java
A	tp-sqns/pom.xml
A	tp-sqns/src/main/java/pro/mir0n/tp/sns/TransportProvider.java
A	tp-sqns/src/main/java/pro/mir0n/tp/sqns/SnsSupport.java
A	tp-sqns/src/main/java/pro/mir0n/tp/sqns/SqsConsumer.java
A	tp-sqns/src/main/java/pro/mir0n/tp/sqns/SqsSupport.java
A	tp-sqns/src/main/java/pro/mir0n/tp/sqns/changes.txt
A	tp-sqns/src/main/java/pro/mir0n/tp/sqs/TransportProvider.java
A	tp-sqns/src/test/java/pro/mir0n/tp/sqns/SqsSupportParamGroupTest.java
A	tp-sqns/src/test/java/pro/mir0n/tp/sqns/SqsSupportParamsTest.java
A	tp-sqns/src/test/java/pro/mir0n/tp/sqns/SqsSupportTest.java
 140 files changed, 18102 insertions(+), 2 deletions(-)
```

---

## Files Modified

```
A	.github/workflows/publish.yml
A	.gitignore
A	LICENSE
M	README.md
A	dataKeep/pom.xml
A	dataKeep/src/main/java/pro/mir0n/dataKeep/changes.txt
A	dataKeep/src/main/java/pro/mir0n/dataKeep/director/IKeepDirector.java
A	dataKeep/src/main/java/pro/mir0n/dataKeep/keep/KeepApplier.java
A	dataKeep/src/main/java/pro/mir0n/dataKeep/keep/KeepDataSourceParams.java
A	dataKeep/src/main/java/pro/mir0n/dataKeep/keep/KeepSqlStore.java
A	dataKeep/src/main/java/pro/mir0n/dataKeep/keep/RodEventDbWriter.java
A	dataKeep/src/main/java/pro/mir0n/dataKeep/keep/XRodInProcessKeep.java
A	dataKeep/src/main/resources/spring.properties
A	dataKeep/src/test/java/pro/mir0n/dataKeep/keep/RodEventDbWriterTest.java
A	doc/Herald.MessagingBus.ContinuingDev.md
A	doc/Herald.MessagingBus.Guides.md
A	doc/Herald.MessagingBus.MessageStructure.md
A	doc/Herald.MessagingBus.Q&A.md
A	doc/Herald.MessagingBus.md
A	doc/img/messaging-bus-architecture.svg
A	doc/img/messaging-bus-classes.svg
A	doc/img/messaging-bus-params.svg
A	doc/logo/MQ-cd.svg
A	doc/logo/activemq.svg
A	doc/logo/dataKeep.svg
A	doc/logo/herald.3.svg
A	doc/logo/herald.svg
A	doc/logo/kafka.svg
A	doc/logo/kinesis-cd.svg
A	doc/logo/messaging.svg
A	doc/logo/msk-cd.svg
A	doc/logo/redis.svg
A	doc/logo/sns-cd.svg
A	doc/logo/sqs-cd.svg
A	doc/release_notes.txt
A	doc/reports/report_v2.0.0.md
A	messaging/pom.xml
A	messaging/src/main/java/pro/mir0n/messaging/BusConstants.java
A	messaging/src/main/java/pro/mir0n/messaging/BusHealthIndicator.java
A	messaging/src/main/java/pro/mir0n/messaging/IRodEventRepo.java
A	messaging/src/main/java/pro/mir0n/messaging/IXRod.java
A	messaging/src/main/java/pro/mir0n/messaging/MessagingBus.java
A	messaging/src/main/java/pro/mir0n/messaging/RodEvent.java
A	messaging/src/main/java/pro/mir0n/messaging/RodEventRepoRegistry.java
A	messaging/src/main/java/pro/mir0n/messaging/TransportHealthIndicator.java
A	messaging/src/main/java/pro/mir0n/messaging/catalog/BusNode.java
A	messaging/src/main/java/pro/mir0n/messaging/catalog/BusRef.java
A	messaging/src/main/java/pro/mir0n/messaging/catalog/BusSlot.java
A	messaging/src/main/java/pro/mir0n/messaging/catalog/BusTransport.java
A	messaging/src/main/java/pro/mir0n/messaging/catalog/MessagingBus.java
A	messaging/src/main/java/pro/mir0n/messaging/catalog/MessagingBusCatalog.java
A	messaging/src/main/java/pro/mir0n/messaging/catalog/Role.java
A	messaging/src/main/java/pro/mir0n/messaging/catalog/XRodParams.java
A	messaging/src/main/java/pro/mir0n/messaging/changes.txt
A	messaging/src/main/java/pro/mir0n/messaging/o11y/IRodMeters.java
A	messaging/src/main/java/pro/mir0n/messaging/o11y/IRodObserver.java
A	messaging/src/main/java/pro/mir0n/messaging/o11y/IRodTracer.java
A	messaging/src/main/java/pro/mir0n/messaging/o11y/RodObserverHolder.java
A	messaging/src/main/java/pro/mir0n/messaging/transport/BusIdentity.java
A	messaging/src/main/java/pro/mir0n/messaging/transport/ConsumeSettings.java
A	messaging/src/main/java/pro/mir0n/messaging/transport/ITransportProvider.java
A	messaging/src/main/java/pro/mir0n/messaging/transport/OwnExcluding.java
A	messaging/src/main/java/pro/mir0n/messaging/transport/PublishSettings.java
A	messaging/src/main/java/pro/mir0n/messaging/transport/SelectingReceiver.java
A	messaging/src/main/java/pro/mir0n/messaging/transport/TransportConsumer.java
A	messaging/src/main/java/pro/mir0n/messaging/transport/TransportHealth.java
A	messaging/src/main/java/pro/mir0n/messaging/transport/TransportMessage.java
A	messaging/src/main/java/pro/mir0n/messaging/transport/TransportProviders.java
A	messaging/src/main/java/pro/mir0n/messaging/transport/TransportPublisher.java
A	messaging/src/main/java/pro/mir0n/messaging/transport/TransportSettings.java
A	messaging/src/main/java/pro/mir0n/messaging/xrod/RodEventCodec.java
A	messaging/src/main/java/pro/mir0n/messaging/xrod/RodPublisher.java
A	messaging/src/main/java/pro/mir0n/messaging/xrod/RodTransportAdapter.java
A	messaging/src/main/java/pro/mir0n/messaging/xrod/impl/AXRod.java
A	messaging/src/main/java/pro/mir0n/messaging/xrod/impl/ISessionSublayer.java
A	messaging/src/main/java/pro/mir0n/messaging/xrod/impl/MsgAudit.java
A	messaging/src/main/java/pro/mir0n/messaging/xrod/impl/XRod.java
A	messaging/src/main/java/pro/mir0n/messaging/xrod/impl/XRodDisabled.java
A	messaging/src/main/java/pro/mir0n/messaging/xrod/impl/XRodInProcess.java
A	messaging/src/main/java/pro/mir0n/messaging/xrod/impl/XRodInfo.java
A	messaging/src/main/java/pro/mir0n/messaging/xrod/impl/XRodInfoParams.java
A	messaging/src/main/java/pro/mir0n/messaging/xrod/impl/XRodRR.java
A	messaging/src/main/java/pro/mir0n/messaging/xrod/impl/sublayer/AliveSession.java
A	messaging/src/main/java/pro/mir0n/messaging/xrod/impl/sublayer/AliveSessionRR.java
A	messaging/src/main/java/pro/mir0n/messaging/xrod/impl/sublayer/SendRetrySublayer.java
A	messaging/src/main/java/pro/mir0n/messaging/xrod/impl/sublayer/SessionSublayerFactory.java
A	messaging/src/test/java/pro/mir0n/messaging/BrokerDownTransportProvider.java
A	messaging/src/test/java/pro/mir0n/messaging/BusHealthIndicatorTest.java
A	messaging/src/test/java/pro/mir0n/messaging/BusRefBindTest.java
A	messaging/src/test/java/pro/mir0n/messaging/CapturingTransportProvider.java
A	messaging/src/test/java/pro/mir0n/messaging/EncodeFailingTransportProvider.java
A	messaging/src/test/java/pro/mir0n/messaging/FakeTransportProvider.java
A	messaging/src/test/java/pro/mir0n/messaging/MessagingBusTest.java
A	messaging/src/test/java/pro/mir0n/messaging/ProducerOnlyTransportProvider.java
A	messaging/src/test/java/pro/mir0n/messaging/TransportHealthIndicatorTest.java
A	messaging/src/test/java/pro/mir0n/messaging/XRodParamsTest.java
A	messaging/src/test/java/pro/mir0n/messaging/catalog/MessagingBusCatalogTest.java
A	messaging/src/test/java/pro/mir0n/messaging/o11y/RodObserverHolderTest.java
A	messaging/src/test/java/pro/mir0n/messaging/transport/BusIdentityTest.java
A	messaging/src/test/java/pro/mir0n/messaging/transport/OwnExcludingTest.java
A	messaging/src/test/java/pro/mir0n/messaging/transport/SelectingReceiverTest.java
A	messaging/src/test/java/pro/mir0n/messaging/transport/TransportHealthTest.java
A	messaging/src/test/java/pro/mir0n/messaging/transport/TransportProvidersTest.java
A	messaging/src/test/java/pro/mir0n/messaging/xrod/RodEventCodecTest.java
A	messaging/src/test/java/pro/mir0n/messaging/xrod/RodEventRepoRegistryTest.java
A	messaging/src/test/java/pro/mir0n/messaging/xrod/RodTransportAdapterTest.java
A	messaging/src/test/java/pro/mir0n/messaging/xrod/XRodBrokerDownTest.java
A	messaging/src/test/java/pro/mir0n/messaging/xrod/XRodRoleSupportTest.java
A	messaging/src/test/java/pro/mir0n/messaging/xrod/XRodSendFailureTest.java
A	messaging/src/test/java/pro/mir0n/messaging/xrod/XRodSubscriptionSelectorTest.java
A	messaging/src/test/java/pro/mir0n/messaging/xrod/XRodTest.java
A	messaging/src/test/java/pro/mir0n/messaging/xrod/XRodValidateTest.java
A	messaging/src/test/java/pro/mir0n/messaging/xrod/impl/XRodDisabledTest.java
A	messaging/src/test/java/pro/mir0n/messaging/xrod/impl/XRodInfoTest.java
A	messaging/src/test/java/pro/mir0n/messaging/xrod/impl/sublayer/AliveSessionTest.java
A	messaging/src/test/java/pro/mir0n/messaging/xrod/impl/sublayer/SendRetrySublayerTest.java
A	pom.xml
A	tp-activemq/pom.xml
A	tp-activemq/src/main/java/pro/mir0n/messaging/changes.txt
A	tp-activemq/src/main/java/pro/mir0n/messaging/jms/Utils.java
A	tp-activemq/src/main/java/pro/mir0n/tp/activemq/TpActiveMqAutoConfigFilter.java
A	tp-activemq/src/main/java/pro/mir0n/tp/activemq/TransportProvider.java
A	tp-activemq/src/main/java/pro/mir0n/tp/activemq/changes.txt
A	tp-activemq/src/main/resources/META-INF/spring.factories
A	tp-activemq/src/test/java/pro/mir0n/tp/activemq/CredentialParamsIntegrationTest.java
A	tp-activemq/src/test/java/pro/mir0n/tp/activemq/NoLocalIntegrationTest.java
A	tp-kafka/pom.xml
A	tp-kafka/src/main/java/pro/mir0n/tp/kafka/TpKafkaAutoConfigFilter.java
A	tp-kafka/src/main/java/pro/mir0n/tp/kafka/TransportProvider.java
A	tp-kafka/src/main/java/pro/mir0n/tp/kafka/changes.txt
A	tp-kafka/src/main/resources/META-INF/spring.factories
A	tp-kafka/src/test/java/pro/mir0n/tp/kafka/TransportProviderTest.java
A	tp-kinesis/pom.xml
A	tp-kinesis/src/main/java/pro/mir0n/tp/kinesis/KinesisConsumer.java
A	tp-kinesis/src/main/java/pro/mir0n/tp/kinesis/TransportProvider.java
A	tp-kinesis/src/main/java/pro/mir0n/tp/kinesis/changes.txt
A	tp-kinesis/src/test/java/pro/mir0n/tp/kinesis/StreamModeParamsTest.java
A	tp-redis/pom.xml
A	tp-redis/src/main/java/pro/mir0n/tp/redis/TpRedisAutoConfigFilter.java
A	tp-redis/src/main/java/pro/mir0n/tp/redis/TransportProvider.java
A	tp-redis/src/main/java/pro/mir0n/tp/redis/changes.txt
A	tp-redis/src/main/resources/META-INF/spring.factories
A	tp-redis/src/test/java/pro/mir0n/tp/redis/TransportProviderTest.java
A	tp-sqns/pom.xml
A	tp-sqns/src/main/java/pro/mir0n/tp/sns/TransportProvider.java
A	tp-sqns/src/main/java/pro/mir0n/tp/sqns/SnsSupport.java
A	tp-sqns/src/main/java/pro/mir0n/tp/sqns/SqsConsumer.java
A	tp-sqns/src/main/java/pro/mir0n/tp/sqns/SqsSupport.java
A	tp-sqns/src/main/java/pro/mir0n/tp/sqns/changes.txt
A	tp-sqns/src/main/java/pro/mir0n/tp/sqs/TransportProvider.java
A	tp-sqns/src/test/java/pro/mir0n/tp/sqns/SqsSupportParamGroupTest.java
A	tp-sqns/src/test/java/pro/mir0n/tp/sqns/SqsSupportParamsTest.java
A	tp-sqns/src/test/java/pro/mir0n/tp/sqns/SqsSupportTest.java
 153 files changed, 19768 insertions(+), 2 deletions(-)
```

---

*From `0.0.0` till `HEAD`*
