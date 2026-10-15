/*
 *  Herald Messaging Framework (tm)
 *  messaging -- the bus
 *
 *  Copyright(c) 2001, 2026 mir0n&co www.mir0n.pro
 *  mailto:mir0n.the.programmer@gmail.com
 *
 *  History:
 * 06/15/2026 mir0n  created: XRodInfo's own param layer (named after its impl), bound from the leg's x-rod.custom
 *                   via XRodParams.custom(XRodInfoParams.class). One field: the directive that leads each logged
 *                   line in the dir slot (where TX|RX go for a real send) -- e.g. "Skipped".
 * 10/13/2026 mir0n  v2.0.0 -- moved into Herald: package pro.mir0n.esquire.messaging -> pro.mir0n.messaging
 */
package pro.mir0n.messaging.xrod.impl;

/** XRodInfo's own params (bound from the leg's x-rod.custom): the directive logged in place of a TX|RX send. */
public record XRodInfoParams(String dir) {

    public String dirOr(String def) { return dir != null && !dir.isBlank() ? dir : def; }
}
