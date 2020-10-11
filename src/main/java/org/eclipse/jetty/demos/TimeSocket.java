//
// ========================================================================
// Copyright (c) Webtide LLC and others.
//
// This program and the accompanying materials are made available under
// the terms of the Eclipse Public License 2.0 which is available at
// https://www.eclipse.org/legal/epl-2.0
//
// This Source Code may also be made available under the following
// Secondary Licenses when the conditions for such availability set
// forth in the Eclipse Public License, v. 2.0 are satisfied:
// the Apache License v2.0 which is available at
// https://www.apache.org/licenses/LICENSE-2.0
//
// SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
// ========================================================================
//

package org.eclipse.jetty.demos;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/time/")
public class TimeSocket implements Runnable
{
    @Inject
    public Logger logger;

    private TimeZone timezone;
    private Session session;

    @OnOpen
    public void onOpen(Session session)
    {
        logger.info("onOpen() session:" + session);
        this.session = session;
        this.timezone = TimeZone.getTimeZone("UTC");
        new Thread(this).start();
    }

    @OnClose
    public void onClose(CloseReason close)
    {
        logger.info("onClose() close:" + close);
        this.session = null;
    }

    @Override
    public void run()
    {
        while (this.session != null)
        {
            try
            {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
                dateFormat.setTimeZone(timezone);

                String timestamp = dateFormat.format(new Date());
                this.session.getBasicRemote().sendText(timestamp);
                TimeUnit.SECONDS.sleep(1);
            }
            catch (InterruptedException | IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
