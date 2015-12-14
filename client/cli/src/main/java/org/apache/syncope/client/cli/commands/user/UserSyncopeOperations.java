/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.syncope.client.cli.commands.user;

import java.util.List;
import java.util.Map;
import org.apache.syncope.client.cli.SyncopeServices;
import org.apache.syncope.client.lib.SyncopeClient;
import org.apache.syncope.common.lib.to.BulkAction;
import org.apache.syncope.common.lib.to.BulkActionResult;
import org.apache.syncope.common.lib.to.PagedResult;
import org.apache.syncope.common.lib.to.UserTO;
import org.apache.syncope.common.rest.api.RESTHeaders;
import org.apache.syncope.common.rest.api.beans.AnyListQuery;
import org.apache.syncope.common.rest.api.beans.AnySearchQuery;
import org.apache.syncope.common.rest.api.service.UserService;

public class UserSyncopeOperations {

    private final UserService userService = SyncopeServices.get(UserService.class);

    public boolean auth(final String username, final String password) {
        try {
            SyncopeServices.testUsernameAndPassword(username, password);
            return true;
        } catch (final Exception e) {
            return false;
        }
    }

    public List<UserTO> searchByRole(final String realm, final String role) {
        return userService.search(
                new AnySearchQuery.Builder().realm(realm).
                fiql(SyncopeClient.getUserSearchConditionBuilder().inRoles(role)
                        .query()).build()).getResult();
    }

    public List<UserTO> searchByResource(final String realm, final String resource) {
        return userService.search(
                new AnySearchQuery.Builder().realm(realm).
                fiql(SyncopeClient.getUserSearchConditionBuilder().hasResources(resource)
                        .query()).build()).getResult();
    }

    public List<UserTO> searchByAttribute(final String realm, final String attributeName, final String attributeValue) {
        return userService.search(
                new AnySearchQuery.Builder().realm(realm).
                fiql(SyncopeClient.getUserSearchConditionBuilder().is(attributeName).equalTo(attributeValue)
                        .query()).build()).getResult();
    }

    public PagedResult<UserTO> list() {
        return userService.list(new AnyListQuery());
    }

    public UserTO read(final String userId) {
        return userService.read(Long.valueOf(userId));
    }

    public String getUsernameFromId(final String userId) {
        return userService.getUsername(Long.valueOf(userId)).getHeaderString(RESTHeaders.USERNAME);
    }

    public String getIdFromUsername(final String username) {
        return userService.getUserKey(username).getHeaderString(RESTHeaders.USER_KEY);
    }

    public void delete(final String userId) {
        userService.delete(Long.valueOf(userId));
    }

    public Map<String, BulkActionResult.Status> deleteByAttribute(
            final String realm, final String attributeName, final String attributeValue) {
        final List<UserTO> users = userService.search(
                new AnySearchQuery.Builder().realm(realm).
                fiql(SyncopeClient.getUserSearchConditionBuilder().is(attributeName).equalTo(attributeValue)
                        .query()).build()).getResult();
        return deleteBulk(users);
    }

    public Map<String, BulkActionResult.Status> deleteAll(final String realm) {
        return deleteBulk(userService.list(new AnyListQuery.Builder().realm(realm).details(false).build()).getResult());
    }

    private Map<String, BulkActionResult.Status> deleteBulk(final List<UserTO> users) {
        final BulkAction bulkAction = new BulkAction();
        bulkAction.setType(BulkAction.Type.DELETE);
        for (UserTO user : users) {
            bulkAction.getTargets().add(String.valueOf(user.getKey()));
        }
        final BulkActionResult bulkResult = userService.bulk(bulkAction);
        return bulkResult.getResults();
    }
}