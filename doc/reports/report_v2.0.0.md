# Release Report: HEAD~1 → v2.0.0

**Repo:** `herald/main`  
**Top commit:** `2216a46`

---

## Release Notes

### doc/release_notes.txt

Herald 2.0  
Herald -- messaging, dataKeep, tp-activemq, tp-kafka, tp-redis, tp-sqns, tp-kinesis  

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

**08/26/2026** mir0n  v1.2.13 -- an event the keep cannot record  
**keep.RodEventDbWriter**  
&nbsp;- an op the action mapping does not cover is ignored with a named warning and counted outcome=ignored,  
&nbsp;   instead of falling through to a delete; action() answers null for it  

**08/11/2026** mir0n  v1.2.12 -- entity change number  
**keep.RodEventDbWriter**  
&nbsp;- PARAM_CHANGE_NO added and bound from the event in the header parameter map  

**07/11/2026** mir0n  v1.2.11 -- business meters (O1/T8): the DB write at the keep sink  
**keep.RodEventDbWriter**  
&nbsp;- applyEvent() counts esq.biz.keep.write.total and times esq.biz.keep.write.duration (tags op = the RodEvent op,  
&nbsp;   outcome = ok|error) around the DB write. This is the one thing the bus meters cannot see:  
&nbsp;   messaging.receive.total says the audit event ARRIVED, only this says whether the row was WRITTEN. The op tag  
&nbsp;   is null-safe -- these read from a finally, and a meter that throws there would REPLACE the real exception  

**07/10/2026** mir0n  v1.2.11 -- metrics foundation (O1): the keep pool reports hikaricp_* meters  
**keep.KeepApplier**  
&nbsp;- 5-arg constructor + buildPool(p, metricRegistry): an optional metricRegistry (nullable Object, so dataKeep  
&nbsp;   stays Micrometer-free) is handed to HikariConfig.setMetricRegistry, so this OWN (non-Spring) keep pool  
&nbsp;   reports its hikaricp_* meters alongside the services' Spring-managed pools  

**07/08/2026** mir0n  v1.2.11 -- distributed tracing (O2): trace mark on the keep apply  
**keep.RodEventDbWriter**  
&nbsp;- applyEvent() body wrapped in EsqTraceMark.around("esq.keep.apply", "keep audit log", ...) -- the writer is not a  
&nbsp;   Spring bean, so the programmatic mark stands in for @EsqTraced  

**06/29/2026** mir0n  v1.2.10 -- R6 per-apply query-timeout (keep surface)  
**keep.KeepDataSourceParams**  
&nbsp;- added queryTimeoutSeconds (Integer; null / <= 0 = no cap, pre-HA default) -- the per-apply JDBC statement cap  
**keep.RodEventDbWriter**  
&nbsp;- constructor takes queryTimeoutSeconds: when > 0 applies it via JdbcTemplate setQueryTimeout; null / <= 0 leaves  
&nbsp;   it uncapped; added queryTimeoutSeconds() accessor  
**keep.KeepApplier**  
&nbsp;- passes ds.queryTimeoutSeconds() to the RodEventDbWriter  

**06/23/2026** mir0n  keep datasource: driver connection properties (data-source-properties)  
**keep.KeepDataSourceParams**  
&nbsp;- Hikari record gained dataSourceProperties (bound from data-source-properties, like  
&nbsp;   spring.datasource.hikari.data-source-properties); import java.util.Map  
**keep.KeepApplier**  
&nbsp;- buildPool forwards hikari.data-source-properties to the JDBC driver via addDataSourceProperty -- so pgjdbc  
&nbsp;   socketTimeout / tcpKeepAlive make health() fail fast on a vanished DB instead of hanging on a half-open socket  

**06/22/2026** mir0n  v1.2.9 -- keep datasource health  
**keep.KeepApplier**  
&nbsp;- added health(): pings the keep pool -- a pooled connection that validates within 2s -> UP; any failure  
&nbsp;   (cannot reach / validate the database) -> DOWN. The keep-datasource health source.  
**keep.XRodInProcessKeep**  
&nbsp;- added health(): the in-process keep has no broker, so its "down" risk is the DB it applies to -- it  
&nbsp;   reports keepApplier.health() (the receiver-side DB the generic in-process relay otherwise hides)  

**06/22/2026** mir0n  v1.2.9 -- in-process keep x-rod; shared keep pool mode dropped  
keep.XRodInProcessKeep  (new, was audit.XRodAuditKeep, generalized)  
&nbsp;- the in-process keep x-rod: extends the generic XRodInProcess (moved to the messaging library); validate()  
&nbsp;   requires the leg's datasource.url + a director; init() builds the KeepApplier from the leg's datasource +  
&nbsp;   config-named IKeepDirector, OPENs the engine (super.init -> receive pool, paused), THEN setWorker(applier)  
&nbsp;   (the build-engine-before-setWorker order is required -- setWorker needs the pool to exist); shutdown() drains  
&nbsp;   then closes the keep pool  
**keep.KeepApplier**  
&nbsp;- dropped SHARED pool mode: removed the shared-DataSource constructor and the ownsPool field; a keep is always  
&nbsp;   a DEDICATED pool now -- close() closes its own pool unconditionally; RodEvent/RodEventRepoRegistry imports  
&nbsp;   moved to messaging.xrod  
**keep.KeepDataSourceParams**  
&nbsp;- dropped the shared field and isShared(); the record carries only url/username/password/hikari  
**keep.RodEventDbWriter**  
&nbsp;- RodEvent import moved to messaging.xrod  

**06/21/2026** mir0n  v1.2.9 -- keep dialect derived from the database URL; the datasource vendor setting removed  
**keep.KeepDataSourceParams**  
&nbsp;- removed the vendor field (the record is now url/username/password/hikari/shared) and vendorOr(); when the  
&nbsp;   keep is shared the dialect comes from the service's spring.datasource.url  
**keep.KeepApplier**  
&nbsp;- dedicated-mode dialect is KeepSqlStore.dialectOf(ds.url()) -- read from the datasource URL subprotocol  
&nbsp;   instead of the vendor/profile label  

**06/18/2026** mir0n  v1.2.8 -- created: esquire-dataKeep, the generic DB-keep engine (extracted from the audit code)  
keep.KeepApplier  (new)  
&nbsp;- builds a keep's DB applier: a RodEventDbWriter over the KeepSqlStore + a kind->statement registry from the  
&nbsp;   director's kinds; applier() is the worker an x-rod runs. DEDICATED mode builds and owns its own auto-commit  
&nbsp;   Hikari pool from the datasource group; SHARED mode reuses a provided DataSource (the service's own pool) and  
&nbsp;   does NOT close it (close() closes only an owned pool)  
keep.RodEventDbWriter  (new, was common.audit.AuditLogWriter)  
&nbsp;- applies a RodEvent to a DB sink: the uniform identity/header params + the event body bound onto the  
&nbsp;   dialect-keyed SQL via NamedParameterJdbcTemplate; one INSERT/MERGE per call; a param the body lacks binds NULL  
keep.KeepSqlStore  (new, was common.audit.AuditLogSql)  
&nbsp;- dialect-keyed SQL store: statements live in META-INF//.xml, loaded on first use;  
&nbsp;   dialectOf() normalizes a vendor/profile label to the dialect token (which is the resource name)  
keep.KeepDataSourceParams  (new)  
&nbsp;- the keep datasource config group (the database + its connection pool) in its own group, not spring.datasource;  
&nbsp;   a shared flag (isShared()) selects reuse of the service's own pool over a dedicated keep pool  
keep.XRodInProcess  (new)  
&nbsp;- the generic in-process relay: an AXRod whose transmit feed loops into its own worker pool and runs the  
&nbsp;   worker start() is handed (no transport, no codec); resolved by rod-class, starts the worker pool a producer leg lacks  
director.IKeepDirector  (new)  
&nbsp;- the keep's declaration only: the SQL resource group + the kind->statement-key map; the engine does the rest  

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

**08/29/2026** mir0n  v1.2.14 -- the two receive filters a transport applies when its vendor applies neither  
OwnExcluding  (new)  
&nbsp;- the own-exclusion filter -- what a broker does with noLocal, applied in code for a transport whose vendor  
&nbsp;   cannot. Its own filter, never folded into the subscription one: a leg carries whatever subscription its  
&nbsp;   consumer asked for, and this sits in front of any of them  
SelectingReceiver  (new)  
&nbsp;- the subscription filter -- what a broker does with a message selector, applied in code for a transport  
&nbsp;   whose vendor has none. A general filter over the header bag: any field, = and  and !=, IN and NOT IN,  
&nbsp;   joined by AND. A selector it cannot read is REFUSED when the leg opens, never treated as take-everything  

**08/27/2026** mir0n  v1.2.13 -- one alphabet for the create op  
BusConstants  
&nbsp;- EVENT_CREATE is "I" -- the wire event code and the *_log action column carry the same letters  
RodEvent  
&nbsp;- dbAction() -- the action code the keep writes; null for UPDATE_PATH and UNKNOWN, which no *_log holds  

**08/26/2026** mir0n  v1.2.13 -- the transport-state gauge, and failures that leave a record  
BusHealthIndicator  
&nbsp;- registerTransportGauges publishes each bus transport state as messaging.transport.up (1 connected,  
&nbsp;   0 not), so the state reaches Prometheus and not only /actuator/health  
**o11y.IRodMeters**  
&nbsp;- registerTransportUp(busId, IntSupplier) added -- the transport-state gauge, registered once per bus;  
&nbsp;   NOOP implementation alongside  
**o11y.IRodObserver**  
&nbsp;- registerTransportUp forwarded to the meters, and to IRodMeters.NOOP when unobserved  
RodEvent  
&nbsp;- Op gains UNKNOWN: opOf falls back to it instead of DELETE, and opCode() answers null for it  
**xrod.impl.AXRod**  
&nbsp;- implements IQueueRig.IErrorListener and registers itself on the feed, so whatever the send worker  
&nbsp;   throws is recorded as TX-ERR plus the error meter; encode THROWS instead of returning null, which was  
&nbsp;   the one send failure that left no record  
**xrod.impl.sublayer.AliveSessionRR**  
&nbsp;- an R&R SERVER emits no unsolicited keep-alive -- keepAliveEvent returns null for that role  
**xrod.impl.sublayer.SendRetrySublayer**  
&nbsp;- parseBackoff takes the BusIdentity and REFUSES the leg on a step that does not parse, naming it; a  
&nbsp;   blank spec still yields the single 1s step  

**08/15/2026** mir0n  v1.2.13 -- gateWard  
BusHealthIndicator  
&nbsp;- register() now registers into BOTH health registries: the blocking HealthContributorRegistry as before,  
&nbsp;   and the ReactiveHealthContributorRegistry via ReactiveHealthContributor.adapt() in registerReactive(),  
&nbsp;   which a WebFlux service reads  

**08/11/2026** mir0n  v1.2.12 -- entity change number  
BusConstants  
&nbsp;- FIELD_CHANGE_NO added (ChangeNo, FIX 50015), the per-row change number supplied by the producer; the  
&nbsp;   declared exception is stated at the field: C/U/D carry the entity row's number, X the path row's,  
&nbsp;   never comparable  
RodEvent  
&nbsp;- changeNo component added to the identity group and to the producer constructor as a normal argument;  
&nbsp;   the record is documented as identity / header / payload / engine-stamped tail  
**xrod.RodEventCodec**  
&nbsp;- changeNo written to and read from the wire properties; new longOrNull() keeps an absent number null  
&nbsp;   instead of 0  
**xrod.impl.MsgAudit**  
&nbsp;- changeNo added to the TX/RX leg trace, printed as "-" when the producer supplied none  

**07/23/2026** mir0n  v1.2.11 -- homework: fix/resolve issues found in the fresh-mind audit  
**xrod.impl.XRod**  
&nbsp;- setWorker(subscription) comment: set the subscription BEFORE start(); the x-rod does not support changing a  
&nbsp;   broker subscription on the fly  
**xrod.impl.sublayer.AliveSession**  
&nbsp;- comment: the tick() heartbeat uses a BLOCKING put (not tryPut) -- it fires only when the leg is idle, so the  
&nbsp;   feed is drained/empty and put() returns at once; never drops a heartbeat  
**xrod.impl.sublayer.AliveSessionRR**  
&nbsp;- comment: the SERVER HeartBeat reply uses a BLOCKING put -- a reply can be needed while the leg is busy, and  
&nbsp;   dropping it would cause a false SERVER-DOWN at the client  

**07/17/2026** mir0n  v1.2.11 -- T11 cleanup: fix/resolve issues noted during development  
**o11y.IRodTracer**  
&nbsp;- the alive-trace opt-in key is namespaced under esquire.observability.tracing.msg-bus-alive-trace (was  
&nbsp;   esquire.tracing.*)  

**07/15/2026** mir0n  v1.2.11 -- T11 cleanup: fix/resolve issues noted during development  
**o11y.RodObserverHolder**  
&nbsp;- registrar-vs-bus-start ordering tripwire (I11): a feedDepthAgainstNoop latch + a develop-channel logger.  
&nbsp;   noteFeedDepthAgainstNoop() latches when a feed-depth gauge is registered while the observer is still NOOP;  
&nbsp;   setObserver() logs an ERROR if a real observer is installed AFTER that. Silent when observability is off  
**xrod.impl.AXRod**  
&nbsp;- runEngine() calls RodObserverHolder.noteFeedDepthAgainstNoop() when the observer is still NOOP at feed-depth  
&nbsp;   registration, arming the ordering tripwire (I11)  

**07/14/2026** mir0n  v1.2.11 -- feed-await-ms: a producer leg can HOLD on a full feed instead of dropping the event (T10)  
**catalog.XRodParams**  
&nbsp;- feed-await-ms in SCALARS + feedAwaitMsOr(long) getter -- how long a producer waits on a FULL feed before the  
&nbsp;   event is DISCARDED; <= 0 = wait forever (backpressure, never drop). longOr(key, def) helper added alongside  
&nbsp;   intOr / boolOr  
**xrod.impl.AXRod**  
&nbsp;- feedAwaitMs read from params (feed-await-ms, default BoundedQueueRig.DEFAULT_AWAIT_TIMEOUT_MS) and applied to  
&nbsp;   the feed via setPutAwaitMs in buildEngine -- <= 0 holds the producer on a full feed instead of discarding the  
&nbsp;   event  

**07/11/2026** mir0n  v1.2.11 -- bus meters (O1/T5): the bus-hop meter hook, declared by the bus; trace + metrics unified into ONE bus observer  
o11y.IRodMeters  (new)  
&nbsp;- the bus-hop METER hook, DECLARED by the messaging bus. The mirror of IRodTracer for metrics: the bus deals  
&nbsp;   only in String / primitives / IntSupplier through it and imports no Micrometer (or anything above itself);  
&nbsp;   the Micrometer-backed implementation is the host application's, handed in via RodObserverHolder as part of  
&nbsp;   the ONE bus observer. sent / sendDuration / received / error / retryBackoff / retryDropped report events;  
&nbsp;   registerFeedDepth / registerRetryHeld hand over an IntSupplier the host reads as a gauge. NOOP = zero cost  
o11y.IRodObserver  (new)  
&nbsp;- the ONE bus-hop observer umbrella: joins IRodTracer + IRodMeters so a single host object covers both, is  
&nbsp;   held once in RodObserverHolder and is registered by one bean. of(IRodTracer, IRodMeters) combines two  
&nbsp;   separate hooks into one observer; NOOP. The interfaces stay separate -- only the object / holder /  
&nbsp;   registrar / umbrella switch are unified  
o11y.RodObserverHolder  (new -- was RodTracerHolder)  
&nbsp;- generalised from RodTracerHolder to the ONE bus observer umbrella: holds a single volatile IRodObserver  
&nbsp;   (default NOOP); setObserver() registers it, observer() / tracer() / meters() return it as each view. One  
&nbsp;   object, two views -- so a trace seam and a metric seam read the SAME registered observer  
**o11y.IRodTracer**  
&nbsp;- javadoc {@link} repointed RodTracerHolder -> RodObserverHolder  
**xrod.impl.AXRod**  
&nbsp;- the tracer holder is repointed to o11y.RodObserverHolder. The bus METERS are emitted from the same seams  
&nbsp;   the tracer already owns: onSendSuccess -> sent(), onSendError -> error(..,"send"), the receive worker ->  
&nbsp;   received() / error(..,"receive"), and send() times the whole dispatch into sendDuration() in a finally.  
&nbsp;   runEngine registers the feed-depth gauge (registerFeedDepth(meterBusId(), meterSlotId(), feed::size)).  
&nbsp;   Session (heartbeat) events are excluded from every meter. meterBusId() / meterSlotId() added (the identity's  
&nbsp;   bus / slot, or the rod name when it has no identity)  
**xrod.impl.sublayer.SendRetrySublayer**  
&nbsp;- retry meters over o11y.RodObserverHolder.meters(): start() registers the held-count gauge  
&nbsp;   (registerRetryHeld(.., this::heldCount)), a hold reports retryBackoff(busId, backoffMs) and drop() reports  
&nbsp;   retryDropped(busId, slotId); start() added as an ISessionSublayer override  
**xrod.impl.sublayer.AliveSessionRR**  
&nbsp;- RodTracerHolder -> RodObserverHolder at the four alive round-trip trace call sites  

**07/09/2026** mir0n  v1.2.11 -- distributed tracing (O2/T3): the bus-hop trace hook, declared by the bus; the RR liveness round-trip; the bus drops its Esquire dependency  
o11y.IRodTracer  (new)  
&nbsp;- the bus-hop trace hook, DECLARED by the messaging bus. The bus deals only in String + Runnable through it  
&nbsp;   and imports nothing above itself; the OTel-backed implementation is the host application's and is handed in  
&nbsp;   via RodTracerHolder. Legs: outbound() on the PRODUCER thread stamps the traceparent (parent span) onto the  
&nbsp;   event; inbound() on the CONSUMER (pool) thread runs the worker inside a span continuing the producer's trace;  
&nbsp;   aliveOutbound() / aliveInbound() are the same pair for the RR liveness round-trip; newTraceId() mints a trace  
&nbsp;   id in the tracer's own shape, so the bus never has to know what one looks like; aliveTrace() carries the  
&nbsp;   host's opt-in for the round-trip trace. traceId is ALWAYS the correlationId (authoritative); the traceparent  
&nbsp;   only carries the parent span id. NOOP when tracing is off  
o11y.RodTracerHolder  (new)  
&nbsp;- the single-slot hand-off for the bus-hop trace hook. The host application's tracing config sets its  
&nbsp;   IRodTracer once at startup (only when tracing is enabled); the x-rod engine reads tracer() at each bus hop.  
&nbsp;   Defaults to IRodTracer.NOOP so the bus pays nothing when tracing is off / never registered. Holds the tracer  
&nbsp;   and nothing else -- the RR liveness round-trip switch rides on the tracer (IRodTracer.aliveTrace())  
BusConstants  
&nbsp;- FIELD_TRACEPARENT ("TraceParent", FIX 50014) added  
RodEvent  
&nbsp;- the record gains a traceparent component (last); the applMsgId-shaped constructor leaves it null;  
&nbsp;   withTraceparent(String) copy added, withApplMsgId preserves it  
**xrod.RodEventCodec**  
&nbsp;- toProps() writes FIELD_TRACEPARENT on both the entity and the session branch when the event carries one;  
&nbsp;   fromProps() reads it back via withTraceparent()  
**xrod.impl.AXRod**  
&nbsp;- transmit() stamps the W3C traceparent on the caller's thread for non-session events  
&nbsp;   (o11y.RodTracerHolder.tracer().outbound()); the receive path runs the pool worker inside inbound() and, on a rod  
&nbsp;   that tracesAliveRoundTrip() with msg-bus-alive-trace on, wraps onReceiveSessn in aliveInbound();  
&nbsp;   tracesAliveRoundTrip() (false) and aliveMsgLabel(String) added  
**xrod.impl.XRodRR**  
&nbsp;- tracesAliveRoundTrip() overridden to true  
**xrod.impl.sublayer.AliveSessionRR**  
&nbsp;- when the registered tracer's aliveTrace() is on: a CLIENT keepAliveEvent() takes its correlation id from  
&nbsp;   o11y.IRodTracer.newTraceId() and opens a ROOT producer span (aliveOutbound asRoot=true), stamping the  
&nbsp;   traceparent on the TestRequest; a SERVER onReceiveSessn() stamps its HeartBeat reply from a nested producer  
&nbsp;   span (asRoot=false)  
MessagingBus  
&nbsp;- instanceId(): the per-instance token now reads pro.mir0n.utils.HostId.instanceNo() (was EsqUtils.instanceNo());  
&nbsp;   the bus no longer imports anything Esquire  

**07/03/2026** mir0n  v1.2.10 -- send-retry drop visibility; send-retry-backoff config key -sec unit suffix  
XRodParams  
&nbsp;- config key send-retry-backoff -> send-retry-backoff-sec (unit suffix): SCALARS entry + sendRetryBackoff() getter now read send-retry-backoff-sec  
SendRetrySublayer  
&nbsp;- drop() also logs to the MAIN app log (msgAudit rides the msg-log channel, OFF in prod) so a dropped (dead) message is visible in prod logs  

**07/01/2026** mir0n  v1.2.10 -- x-rod worker pool = the common WorkerPool; the pool thread model becomes per-leg config (receiver-pool / publisher-pool {size, mode}); SendRetry shutdown lifecycle  
XRodParams  
&nbsp;- the worker pool regrouped: pool-size / virtual-threads / publisher-pool-size dropped from SCALARS, replaced  
&nbsp;   by the receiver-pool / publisher-pool {size, mode} GROUPS -- getters receiverPoolSizeOr / receiverPoolMode /  
&nbsp;   publisherPoolSizeOr / publisherPoolMode; the flat-key getters + fallback removed  
AXRod  
&nbsp;- the receive/apply pool is now the common WorkerPool: the inline 3-way executor + Semaphore + drain-on-shutdown  
&nbsp;   lift out; poolSize / poolMode read from receiver-pool.size / receiver-pool.mode (WorkerPool.Mode.of); shutdown()  
&nbsp;   calls pool.shutdown(SHUTDOWN_AWAIT_SECONDS)  
XRod  
&nbsp;- the async-publish pool's thread model reads publisher-pool.mode -- init sets poolMode via WorkerPool.Mode.of(publisherPoolMode())  
SendRetrySublayer  
&nbsp;- shutdown() lifecycle: a volatile 'stopping' releases a held worker AT ONCE on service stop and refuses new  
&nbsp;   holds (re-checked under the monitor so a shutdown notify is never lost); the holds map moved to a  
&nbsp;   ConcurrentHashMap and the lock narrowed to the wait/notify monitor only  

**06/30/2026** mir0n  v1.2.10 -- x-rod session-sublayer stack (the feed worker owns the send): keep-alive + the new send-retry as session protocols on one seam  
AXRod  
&nbsp;- the feed (tx) worker OWNS the send: send() stamps the ApplMsgID once, runs beforeSend, then sendInProcess (a  
&nbsp;   non-transport outbound) or sendOut (encode once + the dispatch loop)  
&nbsp;- the alive session field replaced by a session-sublayer list (installSessionStack via SessionSublayerFactory);  
&nbsp;   the worker fans the hooks out -- beforeSend / onSendSuccess / onSendError(ev, enc, Throwable) / onReceiveSessn /  
&nbsp;   sessionHealth -- and idle() ticks them  
&nbsp;- the raw msgLog replaced by the MsgAudit module (TX / TX-ERR with the cause / RX); publisher / outbound / feed /  
&nbsp;   sendSublayers made protected  
XRod  
&nbsp;- the inline AliveSession build + the alive constants + buildKeepAlive / onSessionMsg / newCorrelationId removed  
&nbsp;- - init calls installSessionStack (the base builds the broadcast sublayer stack); health() = worst(transport  
&nbsp;   indicator, sessionHealth())  
XRodRR  
&nbsp;- installSessionStack override builds the sublayers via SessionSublayerFactory (identity + role); buildKeepAlive /  
&nbsp;   onSessionMsg removed (the role-driven keep-alive + the SERVER TestRequest echo move to AliveSessionRR)  
XRodInfo  
&nbsp;- the msgLog logger replaced by the MsgAudit module (built from the identity at init); logInfo logs via  
&nbsp;   msgAudit.info  
ISessionSublayer  (new)  
&nbsp;- the x-rod SESSION-sublayer interface + producer extension point, in the engine package so the x-rod depends on  
&nbsp;   THIS, not on a concrete sublayer (implementations in the .sublayer sub-package, built in via  
&nbsp;   SessionSublayerFactory); hooks beforeSend / onSendSuccess / onSendError / onReceiveSessn / tick / health /  
&nbsp;   start / shutdown  
MsgAudit  (new)  
&nbsp;- the x-rod MESSAGE-AUDIT module -- a null-safe wrapper over the per-leg msg.. logger, built  
&nbsp;   from the leg identity (no logger when the leg has no bus-id); centralises the leg-trace / transmit-error line  
&nbsp;   format; raw info / warn passthroughs  
SessionSublayerFactory  (new)  
&nbsp;- builds the x-rod producer session-sublayer LIST so the engine (AXRod) never names a concrete sublayer: the  
&nbsp;   alive keepalive (opt-in 'alive'; AliveSessionRR by R&R role, else the base AliveSession) and the send-retry  
&nbsp;   policy (opt-in 'send-retry', only with a transport publisher), ordered alive FIRST then send-retry  
SendRetrySublayer  (new)  
&nbsp;- the producer SEND-RETRY sublayer (an ISessionSublayer): on a dispatch throw onSendError marks the holds map  
&nbsp;   (keyed by the stable ApplMsgID) and reacts -- past the max-attempts cap DROP (null), else HOLD the worker over  
&nbsp;   the backoff ladder (a monitor wait released by tick()) and return the same encoded unit to re-dispatch;  
&nbsp;   onSendSuccess clears the hold; a SESSION event is never retried; the trail logs to MsgAudit  
&nbsp;- health() override: DOWN while any request is held (heldCount > 0), else UP -- folded into the leg session  
&nbsp;   health, so a send-retry-only leg (no alive protocol) depools on a broker outage  
AliveSession  
&nbsp;- moved to the .sublayer sub-package; now an ISessionSublayer -- an event-driven session sublayer the feed (tx)  
&nbsp;   worker drives (beforeSend / onSendSuccess / onSendError(ev, msg, Throwable) / tick / health); the keep-alive is  
&nbsp;   PUT on the feed (IQueueRig) instead of a transmit callback; ctor takes the feed + BusIdentity  
AliveSessionRR  (new)  
&nbsp;- the R&R ALIVE-PROTOCOL session -- AliveSession specialised by R&R role: a CLIENT keep-alive is a TestRequest  
&nbsp;   (its rod-id rides so the SERVER HeartBeat reply routes back), a SERVER keep-alive the base HeartBeat; on  
&nbsp;   receive a SERVER echoes an arriving TestRequest back as a HeartBeat  
RodEvent  
&nbsp;- applMsgId component (the ApplMsgID / FIX 1181 wire dedup id) + a 12-arg canonical ctor; the former 11-arg ctor  
&nbsp;   delegates with applMsgId null (stamped once on the send path); withApplMsgId() copy  
RodEventCodec  
&nbsp;- the ApplMsgID wire dedup id rides as a header when the event carries one (stamped once on the send path,  
&nbsp;   frozen across a resend); fromProps carries it back via RodEvent.withApplMsgId  
RodPublisher  
&nbsp;- send-retry seam: encode(RodEvent) throws + dispatch(Object) throws defaults (encode once, dispatch the same  
&nbsp;   unit throwing on failure), both routed through accept  
RodTransportAdapter  
&nbsp;- publisher(TransportPublisher, ObjectMapper, BusIdentity) returns a full RodPublisher -- accept / encode /  
&nbsp;   dispatch / health / close delegating to the transport publisher (the send-retry encode-once + throwing-dispatch  
&nbsp;   path); toMessage() helper for the wire codec  
TransportPublisher  
&nbsp;- send-retry seam: encode(TransportMessage) (build the concrete send unit ONCE, broker-free) + dispatch(Object)  
&nbsp;   throws (send the unit, throwing on a transport failure) defaults, both routed through accept  
XRodParams  
&nbsp;- producer send-retry knobs in SCALARS + getters: send-retry (opt-in, default off) / send-retry-backoff (seconds  
&nbsp;   ladder) / send-retry-max-attempts (0 = block, N = drop)  

**06/27/2026** mir0n  v1.2.10 -- broadcast dual-leg on one connection (broker noLocal own-exclusion) + setWorker subscription selector  
BusConstants  
&nbsp;- PARAM_NO_LOCAL ("noLocal") added -- the transport-leg vendor param key for the shared-connection  
&nbsp;   own-exclusion (broadcast only): a receive leg sharing the publisher's connection drops THIS connection's  
&nbsp;   own publications  
IXRod  
&nbsp;- setWorker(subscription, worker) default added -- a broker-side selector narrowing what the receive leg  
&nbsp;   consumes; the default ignores the subscription (R&R / non-transport rods too)  
&nbsp;- rodId() default null added -- the leg's ., null for in-process/disabled/info  
ITransportProvider  
&nbsp;- openConsumerOn(publisher, destination, settings, handler) default added -- open a consumer leg that REUSES  
&nbsp;   an already-open publisher's connection (the dual-leg, one-connection shape); the default falls back to  
&nbsp;   openConsumer (a separate connection)  
RodTransportAdapter  
&nbsp;- publisher(TransportPublisher, ObjectMapper, BusIdentity) overload added -- wrap an ALREADY-OPEN transport  
&nbsp;   publisher (so an XRod opening its consumer on the SAME connection keeps the raw publisher handle); the  
&nbsp;   original publisher(sink, settings) opens the sink and delegates here  
AXRod  
&nbsp;- name / devLog made protected (XRodRR reads them); rodId() override returns identity.rodId() (null when the  
&nbsp;   rod has no identity)  
XRod  
&nbsp;- dual-leg on ONE connection: a single-node CLIENT that shares its connection opens a producer leg too and  
&nbsp;   ADDs the consumer onto it (openConsumerOn), so the broker's noLocal drops this connection's own  
&nbsp;   publications; setWorker(subscription) re-opens the receive consumer with a broker selector  
&nbsp;   (effectiveSelector; re-open only when it CHANGES); separate-connection fallback drops own events in code  
&nbsp;   (filterOwnInCode); the raw transportPublisher is kept so the shared consumer can reuse its connection  
XRodRR  
&nbsp;- sharesConnection() = false (R&R keeps two connections on different nodes -- never the shared dual-leg path,  
&nbsp;   so never noLocal); setWorker(subscription) override warns and ignores the selector (R&R selects by rod-id /  
&nbsp;   slot-id)  

**06/24/2026** mir0n  v1.2.9 -- EsqMsgConstants split (wire -> BusConstants); alive protocol opt-in + worst-of health + supportsBothLegs role guard + Role.BOTH removed  
BusConstants  (new)  
&nbsp;- the messaging-bus FIX-JSON wire constants -- moved from common.EsqMsgConstants into  
&nbsp;   the messaging module (where the bus framework lives); the non-wire app constants  
&nbsp;   (BUS_KEY_* / TEXT_* / FLAG_OPEN / CCY_DEFAULT) split out to common.EsqConstants, so common no  
&nbsp;   longer carries any bus-wire definition.  
RodEvent  
&nbsp;- EsqMsgConstants wire constants -> messaging.BusConstants (references repointed)  
RodEventCodec  
&nbsp;- EsqMsgConstants wire constants -> messaging.BusConstants (references repointed)  
IXRod  
&nbsp;- configure() javadoc: role list CLIENT/SERVER (BOTH removed)  
Role  
&nbsp;- BOTH removed from the role list -- the constants are now CLIENT / SERVER (BOTH was unused)  
XRodParams  
&nbsp;- 'alive' on/off knob in SCALARS + aliveOr(boolean) getter -- the session is OPT-IN, default off  
ITransportProvider  
&nbsp;- supportsBothLegs() default-true SPI method -- whether a single rod can run both legs (transmit  
&nbsp;+ receive) on the transport's node; false for a produce-only transport, so the bus fails a  
&nbsp;   CLIENT role fast over one that cannot  
XRod  
&nbsp;- alive is OPT-IN: init builds the AliveSession only when the 'alive' param is set; health() =  
&nbsp;   worst(transport indicator [worst of the transmit + receive legs], alive metric when enabled);  
&nbsp;   transmits() role+alive-aware (a single-node CLIENT opens a producer leg only to self-heartbeat,  
&nbsp;   i.e. when alive on); receives() = role==CLIENT (BOTH removed); init FAILS FAST when a receiving  
&nbsp;   role hits a transport that cannot run the needed legs (supportsBothLegs / supportsConsume)  
XRodRR  
&nbsp;- buildKeepAlive() javadoc: "an R&R SERVER" (BOTH removed from the role list)  

**06/23/2026** mir0n  x-rod alive protocol (HeartBeat / TestRequest session layer)  
AliveSession  (new)  
&nbsp;- the x-rod session collaborator: producer/consumer leg timestamps + lastSendAttempt, timestamp-age health  
&nbsp;   (producer leg only; sendBroken fast-path when alive-fail-fast), the keep-alive cadence step tick(), and  
&nbsp;   receivedSession() (advance the consumer ts + run the rod's session-msg handler)  
&nbsp;- tick() driven externally (no own thread); start() seeds the timestamps; injectable-clock test seam  
RodEvent  
&nbsp;- bodyText component added (a prepared JSON Text string; null = serialize the body Map) + an 11-arg delegating  
&nbsp;   constructor so existing callers are unchanged  
&nbsp;- isSession; session(msgType,corr,req,rod,body) factory (decode); heartbeat(corr,req,rod) / testRequest(corr,rod)  
&nbsp;   build the prepared bodyText (constant HEARTBEAT_BODY + concat templates); opCode() null-safe (no op on a session event)  
RodEventCodec  
&nbsp;- toProps/fromProps branch on isSession: a session message rides the reduced field set (no EventType / EntityKind /  
&nbsp;   EntityID / SubID / ActionTime / Uid / header TestReqID; RequestID omitted on an unsolicited HeartBeat)  
&nbsp;- textOf() writes a prepared bodyText straight to Text (no Map, no Jackson), else serializes the body Map  
IXRod  
&nbsp;- idle() default no-op added: the per-rod maintenance hook the MessagingBus idle ticker fires  
AXRod  
&nbsp;- session field; sendOut marks the alive send-attempt/sent/failed; receive intercepts a session message  
&nbsp;   (receivedSession, not forwarded to the worker) and marks an app receive; runEngine seeds the session; idle()  
&nbsp;   drives session.tick()  
XRod  
&nbsp;- init builds the AliveSession (heartbeat-interval / alive-timeout / alive-fail-fast); transmits() now always  
&nbsp;   true (a broadcast CLIENT auto-opens a producer leg to self-heartbeat); health() = session.health();  
&nbsp;   buildKeepAlive() (unsolicited HeartBeat) + onSessionMsg() (no-op) + newCorrelationId() hooks; role protected  
XRodRR  
&nbsp;- buildKeepAlive() (CLIENT emits TestRequest, SERVER/BOTH an unsolicited HeartBeat); onSessionMsg() (SERVER  
&nbsp;   echoes a received TestRequest back as a HeartBeat, routing echoed)  
MessagingBus  
&nbsp;- one per-service idle ticker (scheduleWithFixedDelay, daemon "messaging-idle"): start()/close() manage it;  
&nbsp;   idleSweep() fires IXRod.idle() on every rod and catches Throwable per rod (a fixed-delay task that lets a  
&nbsp;   Throwable escape is silently cancelled), logging on the develop tier  
XRodParams  
&nbsp;- SCALARS + getters for heartbeat-interval (10s) / alive-timeout (3x) / alive-fail-fast; boolOr with a default  

**06/22/2026** mir0n  messaging-bus connection health indicator  
TransportHealth  
&nbsp;- new connection health a transport leg reports: UP (broker connection established), DOWN (dropped / failed  
&nbsp;   to connect), UNKNOWN (this transport cannot observe its connection)  
&nbsp;- worst() folds two legs (DOWN worst, then UNKNOWN, then UP)  
TransportPublisher  
&nbsp;- health() default added (UNKNOWN unless the provider can observe it)  
&nbsp;- of(sink,closer) delegates to a new of(sink,closer,healthSupplier) overload that surfaces the supplier  
TransportConsumer  
&nbsp;- health() default added (UNKNOWN unless the provider can observe it)  
&nbsp;- of(starter,closer) delegates to a new of(starter,closer,healthSupplier) overload that surfaces the supplier  
RodPublisher  
&nbsp;- health() default added; of(dispatcher,closer) surfaces the closer's TransportPublisher health (the closer's  
&nbsp;   health() iff it is a TransportPublisher, else UNKNOWN)  
IXRod  
&nbsp;- health() default added = TransportHealth.UP (an in-process / disabled / log-only rod has no broker  
&nbsp;   connection that can drop); a transport-backed rod overrides it  
XRod  
&nbsp;- health() = worst of the transmit (outboundCloser) + receive (inbound) legs, each ignored when null (a leg  
&nbsp;   the role does not run)  
&nbsp;- outboundCloser retyped AutoCloseable -> RodPublisher  
MessagingBus  
&nbsp;- health() added: a busKey -> TransportHealth map (each built rod's health()), the source the bus health  
&nbsp;   indicator forwards to /actuator/health  
BusHealthIndicator  
&nbsp;- new Actuator HealthIndicator that forwards MessagingBus.health() to /actuator/health: DOWN if any bus  
&nbsp;   connection is down; UNKNOWN buses reported but do NOT fail it  
&nbsp;- register(ctx,bus) registers it programmatically (no @Bean) into the Actuator HealthContributorRegistry under  
&nbsp;   name "messagingBus"; the per-service readiness group includes it (not liveness)  
TransportHealthIndicator  
&nbsp;- new generic Actuator HealthIndicator over a SINGLE TransportHealth source (a keep datasource, a leg, ...):  
&nbsp;   DOWN when the source is DOWN, UP otherwise (UNKNOWN reported as a detail)  
&nbsp;- registered programmatically (no @Bean) into the Actuator registry; used by auKeep for its keep-datasource  

**06/22/2026** mir0n  Messaging Bus Facade API + framework lifecycle (refactoring): MessagingBus facade replaces XRodManager; package restructure; #17 fail-fast  
MessagingBus  
&nbsp;- new per-service messaging facade: a singleton (getInstance) with a two-phase lifecycle -- init(Environment)  
&nbsp;   loads + validates the catalog and BUILDS every bus ref that declares a role  
&nbsp;   (esquire..messaging-bus.role) into a busKey -> x-rod map, PAUSED; start() runs every built rod;  
&nbsp;   getXRod(busKey) hands a rod to a publisher; close() shuts them down. Absorbs the former XRodManager  
&nbsp;- getXRod THROWS for an unbuilt bus key (a role-declared ref with no leg fails fast at init); no silent OFF  
&nbsp;- replaces XRodManager / XRods / XRodAutoConfiguration (removed)  
IXRod  
&nbsp;- moved to messaging (was messaging.xrod); lifecycle split: start(name,devLog,worker) ->  
&nbsp;   init(name,devLog) (create the legs paused) + setWorker(worker) + start(); Role is CLIENT/SERVER/BOTH  
&nbsp;   (BROADCAST removed)  
RodEvent / IRodEventRepo / RodEventRepoRegistry  
&nbsp;- moved to messaging (was messaging.xrod)  
AXRod  
&nbsp;- two-phase engine: startEngine -> buildEngine (create the pool/feed idle) + runEngine (run them); start()  
&nbsp;   runs runEngine  
&nbsp;- setWorker(worker) added (the live receive callback); throws if the rod has NO receive pool  
&nbsp;- transmit() throws if the rod has NO transmit feed and ignores a null event (the publisher-leg probe)  
&nbsp;- import Role/XRodParams from messaging.catalog, IXRod/RodEvent from messaging  
XRod  
&nbsp;- start(name,devLog,worker) split into init(name,devLog) (create the legs by role; the transport consumer  
&nbsp;   created PAUSED) + start() (runEngine, then inbound.start() begins consumer delivery)  
&nbsp;- transmits()/receives() added (legs from role); validate() now REQUIRES a complete transport  
&nbsp;- inbound is a TransportConsumer; role is CLIENT/SERVER/BOTH  
XRodRR  
&nbsp;- transmits()/receives() overridden to true (R&R runs both legs for its role); validate() now REQUIRES a  
&nbsp;   complete transport  
XRodInProcess  
&nbsp;- moved to messaging.xrod.impl (was dataKeep.keep); start(name,devLog,worker) split into init (buildEngine,  
&nbsp;   the feed loops into the pool) + start() (inherited runEngine); worker set via setWorker; no longer final  
XRodDisabled  
&nbsp;- start(name,devLog,worker) split into setWorker (no-op) + init (no-op) + start (no-op); selected ON PURPOSE  
&nbsp;   via rod-class=XRodDisabled (the facade no longer falls back to it)  
XRodInfo  
&nbsp;- start(name,devLog,worker) split into setWorker (no-op) + init (the log-line setup) + start (no-op)  
ITransportProvider  
&nbsp;- openConsumer returns a TransportConsumer (created PAUSED) instead of a bare AutoCloseable -- the listener  
&nbsp;   subscribes but delivers nothing until TransportConsumer.start()  
TransportConsumer  
&nbsp;- new consume-side handle: a listener created PAUSED plus start() (begin delivery) and close() (stop +  
&nbsp;   release the broker connection); the two-phase mirror of TransportPublisher  
catalog (package)  
&nbsp;- BusNode / BusRef / BusSlot / BusTransport / MessagingBus / MessagingBusCatalog / Role / XRodParams moved to  
&nbsp;   messaging.catalog (was messaging)  
BusRef  
&nbsp;- role component added (CLIENT/SERVER/BOTH); a ref that declares a role is one the facade builds at init  
Role  
&nbsp;- BROADCAST -> BOTH; the constants are CLIENT/SERVER/BOTH  
MessagingBusCatalog  
&nbsp;- the bind+merge moved out of the constructor into an explicit synchronized load() (buses null until load(),  
&nbsp;   buses() accessor throws if unloaded); javadoc XRodManager -> MessagingBus  
RodEventCodec / RodTransportAdapter  
&nbsp;- import RodEvent from messaging (was messaging.xrod)  
RodPublisher  
&nbsp;- import RodEvent from messaging (was the same xrod package)  

**06/21/2026** mir0n  v1.2.9 -- topology functional validation; catalog overlay merge-by-id; javadoc genericized  
MessagingBusCatalog  
&nbsp;- validate(): fail-fast at construction on a duplicate bus-id (catalog) / slot-id (within a bus) / node-id  
&nbsp;   (within an x-rod's transport.nodes), run PER SOURCE (shared catalog + overlay) before the merge  
&nbsp;- mergeOverlay / mergeSlots: the .messaging-bus overlay MERGES onto the shared esquire.messaging-bus BY  
&nbsp;   ID -- a service bus/slot REPLACES the shared one with the same id, a new one is appended (was: addAll)  
&nbsp;- find() returns the FIRST match and stops; the scan-all + take-LAST + log.warn removed  
XRodManager  
&nbsp;- a bus key that resolves to no leg logs a CONSOLE info ("...has no leg -> DISABLED...") and runs XRodDisabled  
&nbsp;   (was a develop-tier note); an explicit rod-class=XRodDisabled leg disables without the info  
framework javadoc + doc\Esquire.MessagingBus.md  
&nbsp;- genericized to describe the bus MECHANISM, not where an event is applied (audit / *_log / keep / asset / SQL  
&nbsp;   removed from IRodEventRepo, RodEventRepoRegistry, RodEvent, etc.); xx-Rod -> x-rod  

**06/21/2026** mir0n  v1.2.9 -- RodEventCodec decode hardening  
RodEventCodec  
&nbsp;- fromProps: requireSchemaVersion() gate -- a SchemaVersion present but != the codec's SCHEMA_VERSION is  
&nbsp;   rejected (logged + IllegalStateException); an absent version is tolerated  
&nbsp;- intOf / longOf take (Map, key) and guard the parse: a non-numeric value logs a warn and falls back to 0  
&nbsp;   instead of throwing NumberFormatException (which dropped the whole event); added a develop-tier logger  

**06/21/2026** mir0n  v1.2.9 -- queue-vs-topic removed from the bus (now the ActiveMQ pubSubDomain vendor param)  
TransportSettings  
&nbsp;- the topic field + topic() getter removed; the constructor drops the topic parameter  
PublishSettings / ConsumeSettings  
&nbsp;- the constructors drop the topic parameter (no longer passed to super)  
BusTransport  
&nbsp;- the topic component + topicOrFalse() removed; refinedWith(BusNode) no longer carries topic  
BusNode  
&nbsp;- the topic component removed  
XRodParams  
&nbsp;- transport() builds BusTransport without topic; bindNode() drops the topic parse  
MessagingBusCatalog  
&nbsp;- consumeLeg() builds ConsumeSettings without the topic argument  
XRod  
&nbsp;- publisher() / openConsumer() build the Publish / ConsumeSettings without the topic argument  
XRodRR  
&nbsp;- legTransport() doc: a node owns destination / params (topic dropped)  

**06/19/2026** mir0n  v1.2.8 -- bus list-config keys pluralized (slot -> slots, node -> nodes)  
MessagingBus  
&nbsp;- the slots component binds the plural config key `slots` directly; @Name("slot") dropped  
XRodParams  
&nbsp;- nodes() reads the R&R network nodes from the plural key transport.nodes[*] (was transport.node[*])  

**06/18/2026** mir0n  v1.2.8 -- extracted into its own module esquire-messaging (was inside esquire-common)  
(module)  
&nbsp;- the messaging bus framework (catalog, x-rod engine, transport SPI, codec; package  
&nbsp;   pro.mir0n.esquire.messaging, unchanged) is now the esquire-messaging module, depending on esquire-common  
&nbsp;   (one-directional, no cycle); the XRodAutoConfiguration registration (META-INF/spring) moved here too;  
&nbsp;   dependents (tp-*, dataKeep, audit, producers, bizTree, kcMaster) now declare an explicit esquire-messaging dep  

**06/18/2026** mir0n  v1.2.8 -- Messaging Bus: catalog service-overlay key moved to the service namespace  
MessagingBusCatalog  
&nbsp;- the service overlay binds .messaging-bus (was esquire.-messaging-bus);  
&nbsp;   the catalog is the shared esquire.messaging-bus topology UNION that service-namespace overlay  

**06/15/2026** mir0n  v1.2.8 -- pluggable transport layer + uniform x-Rod messaging frontend  
xrod.IXRod  (new)  
&nbsp;- the x-Rod pod SPI: a transmit/receive fan-out substrate; configure() prepares from XRodParams + Role,  
&nbsp;   start() runs, post / transmit buffer + feed out after commit, submit applies on a bounded pool  
xrod.XRodManager  (new)  
&nbsp;- the ONE per-service x-Rod frontend (one shared bean): producer / consumer build a rod in a single call  
&nbsp;   (resolve the bus key -> BusRef, merge the catalog leg with a service-level override, resolve + start the  
&nbsp;   pod); close() shuts every rod down  
&nbsp;- an unset leg rod-id defaults to the per-instance id . (EsqUtils.instanceNo()), so an  
&nbsp;   R&R CLIENT's RodID selector isolates its own instance's replies  
xrod.XRods  (new)  
&nbsp;- class-name-driven resolver for IXRod pods: x-rod.rod-class -> pro.mir0n.esquire.messaging.xrod.impl.  
&nbsp;   (or a full class name); DEFAULT = XRod, DISABLED = XRodDisabled  
xrod.XRodAutoConfiguration  (new)  
&nbsp;- @AutoConfiguration registering the one shared XRodManager bean (destroyMethod = close); imported via  
&nbsp;   META-INF/spring/...AutoConfiguration.imports  
xrod.RodEvent  (new)  
&nbsp;- record (op/kind/entityId/subId/actionTime/crl/req/uid/rodId/msgType/body): one self-contained relayed  
&nbsp;   change; opCode / opFromCode map Op  the EVENT_* wire code  
xrod.RodEventCodec  (new)  
&nbsp;- maps a RodEvent to/from the FIX-JSON envelope (header property map + body as the Text JSON field) via a  
&nbsp;   BusIdentity + the event's own msgType; fromProps tolerant of Number-or-String values  
xrod.RodTransportAdapter  (new)  
&nbsp;- codec bridge between the RodEvent and the generic transport seam: publisher() opens the provider sink and  
&nbsp;   returns a Consumer (key = entityId); handler() adapts a RodEvent sink into a TransportMessage handler  
xrod.IRodEventRepo  (new)  
&nbsp;- apply(RodEvent) contract: one per *_log table, called concurrently by the receive pool, must be thread-safe  
xrod.RodEventRepoRegistry  (new)  
&nbsp;- kind -> IRodEventRepo map (ConcurrentHashMap); applier(Logger) is a receive-leg worker that resolves each  
&nbsp;   event's repository by kind and applies it (missing repo logged + skipped)  
xrod.impl.XRod  (new)  
&nbsp;- the default x-Rod transceiver pod: both legs; post buffers in the current transaction and a feed worker  
&nbsp;   stamps actionTime + the audit triple after commit, submit applies on a Semaphore-bounded pool; builds its  
&nbsp;   own publisher / consumer from the leg transport; non-final  
xrod.impl.XRodRR  (new)  
&nbsp;- the Request/Response pod (specialised XRod): resolves the request vs response NODE by role (overlaid via  
&nbsp;   XRodParams.overlayGroups, provider / endpoint excepted) and the receive selector (CLIENT by rod-id,  
&nbsp;   SERVER by slot-id)  
xrod.impl.XRodInfo  (new)  
&nbsp;- a non-sending pod: log.info()s each event's full content to the leg's msg-audit (led by a directive from  
&nbsp;   its x-rod.info sub-block) instead of transmitting; composes the inner XRod feed  
xrod.impl.XRodInfoParams  (new)  
&nbsp;- record (dir): XRodInfo's own params (bound from the leg's x-rod.info) -- the directive logged in place of TX|RX  
xrod.impl.XRodDisabled  (new)  
&nbsp;- the OFF pod: a fully inert IXRod (both legs absent, no config, no transport); the default when a bus key  
&nbsp;   resolves to no leg, or set explicitly via rod-class = XRodDisabled  
transport.ITransportProvider  (new)  
&nbsp;- the transport-provider (tp) SPI: openPublisher / openConsumer over the neutral TransportMessage;  
&nbsp;   supportsConsume() lets a producer-only transport skip the consume leg  
transport.TransportProviders  (new)  
&nbsp;- class-name-driven resolver for ITransportProvider (a bare name -> pro.mir0n.esquire.tp..TransportProvider  
&nbsp;   by convention, or a full class name verbatim), reflectively instantiated + cached; paramKey() yields the  
&nbsp;   param-group name  
transport.TransportMessage  (new)  
&nbsp;- the transport-neutral message: a property-bag envelope (headers) + an optional routing / partition key  
transport.TransportSettings  (new)  
&nbsp;- base of the settings hierarchy: ObjectMapper, endpoint, client-id, destination kind (queue vs topic),  
&nbsp;   BusIdentity envelope, the provider's own params group; paramLong / param accessors  
transport.PublishSettings  (new)  
&nbsp;- publish-side (xy-rod) TransportSettings: adds the async publisher pool size (0 = the single feed worker)  
transport.ConsumeSettings  (new)  
&nbsp;- consume-side (xx-rod) TransportSettings: adds listener concurrency and an optional provider-specific  
&nbsp;   message selector (null = consume everything)  
transport.BusIdentity  (new)  
&nbsp;- record (busId, slotId, rodId): the x-Rod instance identity that rides the envelope  
MessagingBusCatalog  (new)  
&nbsp;- the messaging-bus catalog: the union of the shared esquire.messaging-bus topology and a service's own  
&nbsp;   esquire.-messaging-bus; resolve / find a leg by {bus-id, slot-id} -> XRodParams; publishLeg / consumeLeg  
&nbsp;   build the transport settings via the resolved provider  
MessagingBus  (new)  
&nbsp;- record (busId, slot): one bus in the catalog -- a bus-id grouping its slots  
BusSlot  (new)  
&nbsp;- record (slotId, xRod): one slot (leg) on a bus -- its slot-id + the raw x-Rod config node  
BusRef  (new)  
&nbsp;- record (busId, slotId, xRod): a service-level reference to a catalog leg, with an optional service-level  
&nbsp;   x-rod node that fully overwrites the catalog leg's x-rod when present  
BusTransport  (new)  
&nbsp;- record (provider, endpoint, destination, topic, params): the bound wire of one x-Rod leg  
XRodParams  (new)  
&nbsp;- record (busId, slotId, raw): a bound x-Rod leg -- its flattened config node plus the folded-in leg identity;  
&nbsp;   knobs read from raw by name (SCALARS), transport() binds the wire group, sub() binds a pod-owned sub-block,  
&nbsp;   merge / overlayGroups overlay an override per top-level group  
Role  (new)  
&nbsp;- enum CLIENT / SERVER / BROADCAST: a service's role on a bus (CLIENT/SERVER on a Request-Response bus,  
&nbsp;   BROADCAST on a single-node bus)  
META-INF/spring/...AutoConfiguration.imports  (new)  
&nbsp;- registers xrod.XRodAutoConfiguration as a Boot auto-configuration  

### tp-activemq/src/main/java/pro/mir0n/messaging/changes.txt

herald messaging helper classes -- pro.mir0n.messaging.jms  

**10/13/2026** mir0n  v2.0.0 -- moved into Herald with tp-activemq  
**jms.Utils**  
&nbsp;- package pro.mir0n.esquire.messaging.jms -> pro.mir0n.messaging.jms; the masthead is the Herald  
&nbsp;   Messaging Framework. One U+2014 em dash in a javadoc line replaced with -- , the house ASCII rule.  
&nbsp;   It still ships in the tp-activemq jar, in a messaging package: that split is as it was  

**06/24/2026** mir0n  v1.2.9 -- EsqMsgConstants references repointed  
**tp.activemq.TransportProvider**  
&nbsp;- EsqMsgConstants wire constants -> messaging.BusConstants (references repointed)  

**04/06/2026** mir0n  JMS Utils: sorted key output  
**messaging.jms.Utils**  
&nbsp;- formatProps(Map): keys sorted alphabetically for consistent log output  

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

**09/01/2026** mir0n  v1.2.14 -- the broker credentials as declared bus params  
**tp.activemq.TransportProvider**  
&nbsp;- PARAM_USERNAME ("userName") and PARAM_PASSWORD ("password") added: the broker credentials read from  
&nbsp;   transport.params, applied with setUserName/setPassword and BOTH excluded from withParams -- the broker  
&nbsp;   URI is written to the develop log on every open, so a credential carried as a URI option would be  
&nbsp;   written there too  

**08/26/2026** mir0n  v1.2.13 -- the connection health seed  
**tp.activemq.TransportProvider**  
&nbsp;- the connection health seeds UNKNOWN, not UP -- nothing has proved the connection at open  

**07/12/2026** mir0n  v1.2.11 -- JMS delivery mode as a declared bus param (O1/T9)  
**tp.activemq.TransportProvider**  
&nbsp;- PARAM_PERSISTENT ("persistent") added: the JMS delivery mode read from transport.params.persistent (absent =  
&nbsp;   false = NON_PERSISTENT), applied on the JmsTemplate via setExplicitQosEnabled(true) + setDeliveryPersistent(...)  
&nbsp;- - without explicit QoS the delivery mode is silently ignored. Excluded from withParams (a setter, not a  
&nbsp;   broker-URI option); the publisher-opened devLog line now carries persistent=  

**06/30/2026** mir0n  v1.2.10 -- publisher send-retry seam (encode-once / throwing dispatch)  
**tp.activemq.TransportProvider**  
&nbsp;- AmqPublisher implements the send-retry seam: encode() prepares the broker-free property bag (a stable  
&nbsp;   ApplMsgID minted ONCE, absent-only), dispatch() materializes the JMS message + sends it THROWING on a  
&nbsp;   transport failure (+ SendingTime per physical send), accept() is the best-effort (retry-off) encode+dispatch  
&nbsp;   swallowing path  
&nbsp;- the swallowing send-sink Consumer removed; AmqPublisher now holds the JmsTemplate + destination  

**06/27/2026** mir0n  v1.2.10 -- shared-connection consumer with broker noLocal own-exclusion  
**tp.activemq.TransportProvider**  
&nbsp;- openConsumerOn() added -- a consumer that SHARES the AmqPublisher's connection and sets pubSubNoLocal (the  
&nbsp;   broker drops the shared connection's own publications, the real JMS noLocal)  
&nbsp;- openPublisher returns an AmqPublisher carrying the ccf so a dual-leg consumer can reuse this connection  
&nbsp;- consumer() factored out with a noLocal flag; a separate-connection openConsumer passes false  

**06/22/2026** mir0n  v1.2.9 -- connection health (TransportListener + send outcome)  
**tp.activemq.TransportProvider**  
&nbsp;- a TransportListener set on the ActiveMQConnectionFactory flips an AtomicReference: transportInterupted /  
&nbsp;   onException -> DOWN, transportResumed -> UP; the listener propagates to the connection the factory creates  
&nbsp;- both the publisher and consumer handles report this connection state as their TransportHealth (conn::get)  
&nbsp;- a send outcome also refreshes it: a successful send -> UP, a failed send -> DOWN  
&nbsp;- imports added: org.apache.activemq.transport.TransportListener, messaging.transport.TransportHealth,  
&nbsp;   java.io.IOException, java.util.concurrent.atomic.AtomicReference  

**06/22/2026** mir0n  v1.2.9 -- two-phase consumer (created paused, started by the bus)  
**tp.activemq.TransportProvider**  
&nbsp;- openConsumer returns a TransportConsumer (start + close legs) instead of a bare AutoCloseable  
&nbsp;- the DefaultMessageListenerContainer is created PAUSED: setAutoStartup(false); afterPropertiesSet  
&nbsp;   subscribes but does not start delivery -- delivery begins only when the bus start() invokes the  
&nbsp;   returned start leg (c::start)  
&nbsp;- import added: messaging.transport.TransportConsumer  

**06/21/2026** mir0n  v1.2.9 -- queue-vs-topic read from the pubSubDomain vendor param  
**tp.activemq.TransportProvider**  
&nbsp;- reads the JMS pub/sub flag from transport.params.pubSubDomain (setPubSubDomain on the template /  
&nbsp;   listener container) instead of settings.topic(); excludes pubSubDomain from the broker-URI append in  
&nbsp;   withParams (a setter call, not a URI option)  

**06/17/2026** mir0n  v1.2.8 -- Messaging Bus cleanup: closeable publisher  
**tp.activemq.TransportProvider**  
&nbsp;- openPublisher returns a TransportPublisher (close() destroys the CachingConnectionFactory) instead of a  
&nbsp;   bare Consumer; the ccf.setClientID(...) block removed (a client id is a  
&nbsp;   transport.params.jms.clientID entry now)  

**06/15/2026** mir0n  v1.2.8 -- ActiveMQ transport provider module  
tp.activemq.TransportProvider  (new)  
&nbsp;- ActiveMQ implementation of the transport-provider SPI; resolved by name (reflectively instantiated,  
&nbsp;   no Spring bean) and owns its own audit broker connection.  
&nbsp;- openPublisher builds its own JmsTemplate over a CachingConnectionFactory (poolSize>0 -> useAsyncSend  
&nbsp;   plus sessionCacheSize); send() stamps appl-msg-id + sending-time and writes the header bag via  
&nbsp;   messaging.jms.Utils.setProps.  
&nbsp;- openConsumer runs a programmatic DefaultMessageListenerContainer (selector, concurrency, topic/queue  
&nbsp;   from settings) lifting every JMS property into a neutral TransportMessage header map.  
&nbsp;- withParams appends the leg's transport.params verbatim to the broker URI; readProps lifts all JMS  
&nbsp;   properties into the header map.  
tp.activemq.TpActiveMqAutoConfigFilter  (new)  
&nbsp;- AutoConfigurationImportFilter suppressing Boot's ActiveMQAutoConfiguration + JmsAutoConfiguration so a  
&nbsp;   service that ships this provider stays transport-agnostic.  
pom.xml  (new)  
&nbsp;- esquire-tp-activemq module; depends on esquire-common + spring-boot-starter-activemq.  
META-INF/spring.factories  (new)  
&nbsp;- registers TpActiveMqAutoConfigFilter as an AutoConfigurationImportFilter.  

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

**08/26/2026** mir0n  v1.2.13 -- the connection health seed  
**tp.kafka.TransportProvider**  
&nbsp;- the connection health seeds UNKNOWN, not UP -- nothing has proved the connection at open  

**06/24/2026** mir0n  session messages routed to a separate .admin topic  
**tp.kafka.TransportProvider**  
&nbsp;- openPublisher: a session (alive) message -- HeartBeat / TestRequest, via RodEvent.isSession(MsgType) -- is  
&nbsp;   sent to .admin instead of the log topic; topicFor() helper  
&nbsp;- ensureAdminTopic(): the .admin topic is created with a short retention (retention.ms / segment.ms /  
&nbsp;   cleanup.policy=delete) via AdminClient, best-effort -- Kafka has no per-message TTL  
&nbsp;- imports added: kafka.clients.admin Admin/AdminClientConfig/NewTopic, common.config.TopicConfig,  
&nbsp;   messaging.BusConstants, messaging.RodEvent  

**06/22/2026** mir0n  v1.2.9 -- send-outcome health on the publisher  
**tp.kafka.TransportProvider**  
&nbsp;- Kafka has no clean connection-state callback, so the publisher handle's TransportHealth is send-outcome  
&nbsp;   only (best-effort): an acked send -> UP, a failed send (callback exception / throw) -> DOWN; reported  
&nbsp;   via conn::get  
&nbsp;- imports added: messaging.transport.TransportHealth, java.util.concurrent.atomic.AtomicReference  

**06/22/2026** mir0n  v1.2.9 -- two-phase consumer (created paused, started by the bus)  
**tp.kafka.TransportProvider**  
&nbsp;- openConsumer returns a TransportConsumer (start + close legs) instead of a bare AutoCloseable  
&nbsp;- the ConcurrentMessageListenerContainer is created PAUSED: setAutoStartup(false), no container.start()  
&nbsp;- - delivery begins only when the bus start() invokes the returned start leg (container::start)  
&nbsp;- import added: messaging.transport.TransportConsumer  

**06/17/2026** mir0n  v1.2.8 -- Messaging Bus cleanup: closeable publisher  
**tp.kafka.TransportProvider**  
&nbsp;- openPublisher returns a TransportPublisher (close() destroys the DefaultKafkaProducerFactory) instead of a  
&nbsp;   bare Consumer; the clientId parameter + CLIENT_ID_CONFIG removed from buildTemplate /  
&nbsp;   buildConsumerFactory (a client id is a transport.params.client.id entry now)  

**06/15/2026** mir0n  v1.2.8 -- Kafka transport provider module  
tp.kafka.TransportProvider  (new)  
&nbsp;- Kafka implementation of the transport-provider SPI; resolved by name (reflectively instantiated,  
&nbsp;   no Spring bean) and owns its own audit producer / consumer.  
&nbsp;- openPublisher builds its own KafkaTemplate (String/String) from settings.endpoint() (bootstrap-servers)  
&nbsp;   and sends each TransportMessage as a JSON value keyed by TransportMessage.key (per-entity order).  
&nbsp;- openConsumer runs a programmatic ConcurrentMessageListenerContainer; the consumer group-id comes from  
&nbsp;   the provider param transport.kafka.group-id (PARAM_GROUP_ID, default esquire-xxrod-audit), read via  
&nbsp;   settings.params(); auto-offset-reset earliest.  
&nbsp;- buildTemplate / buildConsumerFactory apply every leg param verbatim as a Kafka config; the essentials  
&nbsp;   (bootstrap, serializers, group.id, client.id) win.  
tp.kafka.TpKafkaAutoConfigFilter  (new)  
&nbsp;- AutoConfigurationImportFilter suppressing Boot's KafkaAutoConfiguration so a service that ships this  
&nbsp;   provider stays transport-agnostic.  
pom.xml  (new)  
&nbsp;- esquire-tp-kafka module; depends on esquire-common, spring-kafka, spring-boot-autoconfigure, slf4j-api.  
META-INF/spring.factories  (new)  
&nbsp;- registers TpKafkaAutoConfigFilter as an AutoConfigurationImportFilter.  

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

**09/01/2026** mir0n  v1.2.14 -- the stream capacity mode as a declared bus param  
**tp.kinesis.TransportProvider**  
&nbsp;- stream.Mode and stream.ShardCount added as create-time attributes, read by streamMode() / shardCount()  
&nbsp;   and refused rather than guessed when unknown. A created stream is PROVISIONED at one shard instead of  
&nbsp;   ON_DEMAND: on-demand bills per STREAM-hour whether or not a record moves, provisioned per SHARD-hour.  
&nbsp;   An existing stream is left as it is, capacity mode included  

**08/29/2026** mir0n  v1.2.14 -- the Amazon Kinesis transport provider  
tp.kinesis.TransportProvider  (new)  
&nbsp;- the Amazon Kinesis ITransportProvider, both legs. An ABSENT partition-by means FIFO -- every record under  
&nbsp;   one key, one shard, one ordered sequence -- and naming a header opts in to spreading, which is safe only  
&nbsp;   where records do not depend on one another. poll-millis is the delivery latency (GetRecords is capped at  
&nbsp;   five a second per shard, so 200ms is the floor). The stream is ensured on first use, never when a leg opens  
tp.kinesis.KinesisConsumer  (new)  
&nbsp;- the Kinesis receive leg -- one poll thread per shard over GetRecords. A stream keeps nobody position, so  
&nbsp;   the leg holds its own in memory and iterator-type says where a restart begins. A failed iterator is  
&nbsp;   RETRIED, and the stream re-made if it is gone; a shard ends only when a successful GetRecords hands back  
&nbsp;   no next iterator  
pom.xml  (new)  
&nbsp;- the tp-kinesis module: esquire-messaging provided, the AWS SDK kinesis client, and copy-dependencies into  
&nbsp;   target/aws-lib so a deployment can attach the driver instead of building it into an image  
doc/tp-kinesis.md  (new)  
&nbsp;- the driver's own description: shards and what order means on a stream, why an absent partition-by is FIFO  
&nbsp;   and why FIFO also needs a receive pool of one, the position nobody keeps, the 200ms floor, configuration,  
&nbsp;   the two receive filters and health  

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

**08/26/2026** mir0n  v1.2.13 -- the connection health seed  
**tp.redis.TransportProvider**  
&nbsp;- the connection health seeds UNKNOWN, not UP -- nothing has proved the connection at open  

**06/30/2026** mir0n  v1.2.10 -- publisher send-retry seam (encode-once / throwing dispatch)  
**tp.redis.TransportProvider**  
&nbsp;- RedisPublisher (extracted class) implements the send-retry seam: encode() prepares the broker-free property  
&nbsp;   bag (a stable ApplMsgID minted ONCE, absent-only), dispatch() builds the stream record + XADDs THROWING on a  
&nbsp;   failure (+ SendingTime per physical send), accept() the best-effort (retry-off) path  
&nbsp;- health() / close() on the handle; the inline TransportPublisher.of(...) lambda replaced by RedisPublisher  

**06/24/2026** mir0n  session messages routed to a separate .admin stream  
**tp.redis.TransportProvider**  
&nbsp;- openPublisher: a session (alive) message -- HeartBeat / TestRequest, via RodEvent.isSession(MsgType) -- is  
&nbsp;   XADD'd to .admin (a capped admin stream) instead of the log stream; streamFor() helper  
&nbsp;- supportsBothLegs() override = false (XADD-only / produce-only -- no CLIENT role here)  

**06/22/2026** mir0n  v1.2.9 -- send-outcome health on the publisher  
**tp.redis.TransportProvider**  
&nbsp;- no clean connection-state callback, so the publisher handle's TransportHealth is XADD send-outcome only  
&nbsp;   (best-effort; producer-only stream): a good XADD -> UP, a failed XADD -> DOWN; reported via conn::get  
&nbsp;- imports added: messaging.transport.TransportHealth, java.util.concurrent.atomic.AtomicReference  

**06/22/2026** mir0n  v1.2.9 -- consumer SPI signature follows the two-phase change  
**tp.redis.TransportProvider**  
&nbsp;- openConsumer signature returns a TransportConsumer instead of a bare AutoCloseable; still producer-only  
&nbsp;- - the body throws UnsupportedOperationException  
&nbsp;- import added: messaging.transport.TransportConsumer  

**06/17/2026** mir0n  v1.2.8 -- Messaging Bus cleanup: closeable publisher  
**tp.redis.TransportProvider**  
&nbsp;- openPublisher returns a TransportPublisher (close() destroys the LettuceConnectionFactory) instead of a  
&nbsp;   bare Consumer  

**06/15/2026** mir0n  v1.2.8 -- Redis transport provider module  
tp.redis.TransportProvider  (new)  
&nbsp;- Redis-stream implementation of the transport-provider SPI; resolved by name (reflectively instantiated,  
&nbsp;   no Spring bean) and owns its own audit stream connection. Producer-only (supportsConsume()=false; the  
&nbsp;   stream IS the append-only log, read via XRANGE).  
&nbsp;- openPublisher XADDs each TransportMessage's header bag (plus stamped appl-msg-id + sending-time) to the  
&nbsp;   destination stream as string fields (null fields omitted); approximate-trimmed to max-len when set.  
&nbsp;- openConsumer throws UnsupportedOperationException (no consume leg).  
&nbsp;- max-len comes from the provider param transport.redis.max-len (PARAM_MAX_LEN, default unbounded), read  
&nbsp;   via settings.params(); buildTemplate builds a started StringRedisTemplate over a Lettuce connection,  
&nbsp;   appending every leg param except max-len to the redis:// URI verbatim.  
tp.redis.TpRedisAutoConfigFilter  (new)  
&nbsp;- AutoConfigurationImportFilter suppressing Boot's RedisAutoConfiguration + RedisReactiveAutoConfiguration  
&nbsp;   so a service that ships this provider stays transport-agnostic.  
pom.xml  (new)  
&nbsp;- esquire-tp-redis module; depends on esquire-common + spring-boot-starter-data-redis.  
META-INF/spring.factories  (new)  
&nbsp;- registers TpRedisAutoConfigFilter as an AutoConfigurationImportFilter.  

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

**08/29/2026** mir0n  v1.2.14 -- the Amazon SQS and Amazon SNS transport providers  
tp.sqns.SqsSupport  (new)  
&nbsp;- the pieces the sqs and sns providers share -- the client build (region, the endpoint override LocalStack  
&nbsp;   needs, the SDK default credential chain), the queue-name rules (route-by and the character set SQS allows),  
&nbsp;   the create-or-get queue URL cache, the transport.params prefix groups (client. / queue. / topic. /  
&nbsp;   subscription.) and the gate that REFUSES a param naming no AWS call rather than dropping it  
tp.sqns.SnsSupport  (new)  
&nbsp;- the SNS pieces -- CreateTopic (create-or-get, topic. attributes), and putting a queue onto a topic: the  
&nbsp;   queue policy that lets the topic write to it, raw message delivery, and the filter policy CLEARED, since  
&nbsp;   Subscribe applies attributes only when it creates a subscription and one left behind goes on dropping  
&nbsp;   messages  
tp.sqns.SqsConsumer  (new)  
&nbsp;- the long-poll receive leg both providers use -- named poll threads, the delete as the acknowledgement (sent  
&nbsp;   only after the handler returned), and a queue that went away made again on the next turn, then re-wired by  
&nbsp;   the hook the leg supplies  
tp.sqs.TransportProvider  (new)  
&nbsp;- the Amazon SQS ITransportProvider. route-by turns the filter a JMS selector used to apply into a  
&nbsp;   DESTINATION -- a queue per rod-id or per slot-id; the whole header bag rides as the message body, since SQS  
&nbsp;   allows ten attributes and the bag carries twenty. Nothing is asked of AWS when a leg opens: the queue is  
&nbsp;   made on the first poll  
tp.sns.TransportProvider  (new)  
&nbsp;- the Amazon SNS ITransportProvider. SNS delivers and holds nothing, so a consuming leg owns an SQS queue  
&nbsp;   subscribed to the topic and named from its rod-id; the subscription selector and noLocal are applied in code  
&nbsp;   by the framework filters. A topic that goes away is resolved again on both legs. Nothing is asked of AWS  
&nbsp;   when a leg opens  
pom.xml  (new)  
&nbsp;- the tp-sqns module: esquire-messaging provided, the AWS SDK sqs + sns clients, and copy-dependencies into  
&nbsp;   target/aws-lib so a deployment can attach the driver instead of building it into an image  
doc/tp-sqns.md  (new)  
&nbsp;- the driver's own description: the subscription protocol and why an sqs subscription needs no confirmation  
&nbsp;   token, the wiring when a leg opens, a broadcast end to end, request/response over SQS, the queue and topic  
&nbsp;   names, the two receive filters, what rides on the wire, configuration and health  

---

## Commits

```

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

