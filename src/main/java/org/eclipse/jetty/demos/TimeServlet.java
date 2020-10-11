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
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class TimeServlet extends HttpServlet {

    @Inject
    public Logger logger;

    private static final TimeZone TZ = TimeZone.getDefault();

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws
            IOException {
        logger.info(String.format("%s:%d Requested Time", req.getRemoteAddr(),
                req.getRemotePort()));
        Locale locale = req.getLocale();
        Calendar cal = Calendar.getInstance(TZ, locale);
        String dateStr = DateFormat.getDateInstance(DateFormat.DEFAULT, locale).
                format(cal.getTime());
        String timeStr = DateFormat.getTimeInstance(DateFormat.DEFAULT, locale).
                format(cal.getTime());
        String tzStr = TZ.getDisplayName(false, TimeZone.SHORT, locale);
        resp.getWriter().println(String.format("%s %s %sw", dateStr, timeStr,
                tzStr));
    }
}
