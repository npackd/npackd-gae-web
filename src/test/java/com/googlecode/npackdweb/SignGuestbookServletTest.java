/**
 * Copyright 2012 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.googlecode.npackdweb;

import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SignGuestbookServletTest {

    // TODO: private SignGuestbookServlet signGuestbookServlet;
    //private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
    //		new LocalDatastoreServiceTestConfig()).setEnvIsLoggedIn(true)
    //		.setEnvAuthDomain("localhost").setEnvEmail("test@localhost");
    @Before
    public void setupSignGuestBookServlet() {
        //	helper.setUp();
        // TODO: signGuestbookServlet = new SignGuestbookServlet();
    }

    @After
    public void tearDownHelper() {
        //	helper.tearDown();
    }

    @Test
    public void testDoPost() throws IOException {
        /*
         * // TODO: HttpServletRequest request = mock(HttpServletRequest.class);
         * HttpServletResponse response = mock(HttpServletResponse.class);
         *
         * String guestbookName = "TestGuestbook"; String testContent =
         * "Test Content";
         *
         * when(request.getParameter("guestbookName")).thenReturn(guestbookName);
         * when(request.getParameter("content")).thenReturn(testContent);
         *
         * Date priorToRequest = new Date();
         *
         * signGuestbookServlet.doPost(request, response);
         *
         * Date afterRequest = new Date();
         *
         * verify(response).sendRedirect(
         * "/guestbook.jsp?guestbookName=TestGuestbook");
         *
         * User currentUser =
         * UserServiceFactory.getUserService().getCurrentUser();
         *
         * Entity greeting =
         * DatastoreServiceFactory.getDatastoreService().prepare(new
         * Query()).asSingleEntity();
         *
         * assertEquals(guestbookName, greeting.getKey().getParent().getName());
         * assertEquals(testContent, greeting.getProperty("content"));
         * assertEquals(currentUser, greeting.getProperty("user"));
         *
         * Date date = (Date) greeting.getProperty("date");
         * assertTrue("The date in the entity [" + date +
         * "] is prior to the request being performed",
         * priorToRequest.before(date) || priorToRequest.equals(date));
         * assertTrue("The date in the entity [" + date +
         * "] is after to the request completed", afterRequest.after(date) ||
         * afterRequest.equals(date));
         */
    }
}
