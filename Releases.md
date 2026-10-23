<table style="width: 100%; table-layout: fixed;">
  <tr>
    <td style="width: 12%"><img src="doc/logo/herald.5.svg" alt="Herald logo" align="right" valign="middle" width="64"></td>
    <td style="width: 88%;">
       <h1>Herald Messaging Frameworks 2.0</h1>
    </td>
  </tr>
</table>


## Release Notes

### v2.0.1 -- complete (10/22/2026)

v2.0.1 gives Herald **its own names**. The framework came out of Esquire, and its configuration still said
so: every key started with `esquire`. Now every key starts with `herald`. The catalog sits at
`herald.messaging-bus`, and a service's bus at `herald.<key>.messaging-bus`. The keep's meters and trace are
`herald.keep.write.total`, `herald.keep.write.duration`, and `herald.keep.apply`. The default Kafka consumer
group is `herald-xxrod-audit`.

The rename has a price. There is no fallback to the old keys. A key left under `esquire` is not read, so a
configuration moves with this version.

The second change is a time limit. An ActiveMQ leg can set `timeToLiveMs`: how long a sent message may wait
for a consumer. A message that waits longer is dropped by the broker. On a request leg this matters. The
caller has stopped waiting, and a command run after that is a command nobody wants any more.

### v2.0.0 -- complete (10/18/2026)

v2.0.0 is the first release. The framework leaves the Esquire services repository and becomes a product of
its own, with its jars on GitHub Packages.<br>
[More Details: v2.0.0 README](https://github.com/mir0n-pro/herald/tree/release/v2.0.0?tab=readme-ov-file#project-structure)


## Milestone Reports

| Repository | Milestone Reports |
|---|---|
| **herald** | - [v2.0.1 Milestone Report](doc/reports/report_v2.0.1.md)<br/>- [v2.0.0 Milestone Report](doc/reports/report_v2.0.0.md) |
