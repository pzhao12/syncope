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
package org.apache.syncope.core.persistence.validation.entity;

import javax.validation.ConstraintValidatorContext;
import org.apache.syncope.common.types.EntityViolationType;
import org.apache.syncope.core.persistence.beans.AbstractSyncTask;
import org.apache.syncope.core.persistence.beans.PushTask;
import org.apache.syncope.core.persistence.beans.SyncTask;
import org.apache.syncope.core.sync.PushActions;
import org.apache.syncope.core.sync.SyncActions;

public class AbstractSyncTaskValidator extends AbstractValidator<AbstractSyncTaskCheck, AbstractSyncTask> {

    private final SchedTaskValidator schedV;

    public AbstractSyncTaskValidator() {
        super();

        schedV = new SchedTaskValidator();
    }

    @Override
    public boolean isValid(final AbstractSyncTask object, final ConstraintValidatorContext context) {
        boolean isValid = schedV.isValid(object, context);

        if (isValid) {
            isValid = object.getResource() != null;
            if (!isValid) {
                LOG.error("Resource is null");

                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        getTemplate(EntityViolationType.InvalidSyncTask, "Resource cannot be null")).
                        addPropertyNode("resource").addConstraintViolation();
            }

            if (!object.getActionsClassNames().isEmpty()) {
                for (String className : object.getActionsClassNames()) {
                    Class<?> actionsClass = null;
                    boolean isAssignable = false;
                    try {
                        actionsClass = Class.forName(className);
                        isAssignable = object instanceof SyncTask
                                ? SyncActions.class.isAssignableFrom(actionsClass)
                                : object instanceof PushTask
                                        ? PushActions.class.isAssignableFrom(actionsClass)
                                        : false;
                    } catch (Exception e) {
                        LOG.error("Invalid SyncActions specified", e);
                        isValid = false;
                    }

                    if (actionsClass == null || !isAssignable) {
                        isValid = false;

                        context.disableDefaultConstraintViolation();
                        context.buildConstraintViolationWithTemplate(
                                getTemplate(EntityViolationType.InvalidSyncTask, "Invalid class name")).
                                addPropertyNode("actionsClassName").addConstraintViolation();
                    }
                }
            }
        }

        return isValid;
    }
}
