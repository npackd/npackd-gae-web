/**
 * Copyright 2012 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.npackdweb;

import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GuestbookServletTest {

    @Before
    public void setupGuestBookServlet() {
    }

    @After
    public void tearDownHelper() {
    }

    @Test
    public void testDoGet() throws IOException {
        /*
		 * HttpServletRequest request = mock(HttpServletRequest.class);
		 * HttpServletResponse response = mock(HttpServletResponse.class);
		 *
		 * StringWriter stringWriter = new StringWriter();
		 *
		 * when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));
		 *
		 * guestbookServlet.doGet(request, response);
		 *
		 * User currentUser =
		 * UserServiceFactory.getUserService().getCurrentUser();
		 *
		 * assertEquals("Hello, " + currentUser.getNickname() +
		 * System.getProperty("line.separator"), stringWriter.toString()); TODO
         */
    }

}
