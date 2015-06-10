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
package org.apache.syncope.core.persistence.jpa.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;
import org.apache.syncope.common.lib.types.AnyTypeKind;
import org.apache.syncope.core.persistence.api.attrvalue.validation.InvalidEntityException;
import org.apache.syncope.core.persistence.api.dao.AnyTypeClassDAO;
import org.apache.syncope.core.persistence.api.dao.AnyTypeDAO;
import org.apache.syncope.core.persistence.api.entity.AnyType;
import org.apache.syncope.core.persistence.jpa.AbstractTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class AnyTypeTest extends AbstractTest {

    @Autowired
    private AnyTypeDAO anyTypeDAO;

    @Autowired
    private AnyTypeClassDAO anyTypeClassDAO;

    @Test
    public void find() {
        AnyType userType = anyTypeDAO.findUser();
        assertNotNull(userType);
        assertEquals(AnyTypeKind.USER, userType.getKind());
        assertEquals(AnyTypeKind.USER.name(), userType.getKey());
        assertFalse(userType.getClasses().isEmpty());

        AnyType groupType = anyTypeDAO.findGroup();
        assertNotNull(groupType);
        assertEquals(AnyTypeKind.GROUP, groupType.getKind());
        assertEquals(AnyTypeKind.GROUP.name(), groupType.getKey());
        assertFalse(groupType.getClasses().isEmpty());

        AnyType otherType = anyTypeDAO.find("PRINTER");
        assertNotNull(otherType);
        assertEquals(AnyTypeKind.ANY_OBJECT, otherType.getKind());
        assertEquals("PRINTER", otherType.getKey());
    }

    @Test
    public void findAll() {
        List<AnyType> list = anyTypeDAO.findAll();
        assertFalse(list.isEmpty());
    }

    @Test
    public void save() {
        AnyType newType = entityFactory.newEntity(AnyType.class);
        newType.setKey("new type");
        newType.setKind(AnyTypeKind.ANY_OBJECT);
        newType.add(anyTypeClassDAO.find("generic membership"));
        newType.add(anyTypeClassDAO.find("csv"));

        newType = anyTypeDAO.save(newType);
        assertNotNull(newType);
        assertFalse(newType.getClasses().isEmpty());
    }

    @Test(expected = InvalidEntityException.class)
    public void saveInvalidKind() {
        AnyType newType = entityFactory.newEntity(AnyType.class);
        newType.setKey("new type");
        newType.setKind(AnyTypeKind.USER);
        anyTypeDAO.save(newType);
    }

    @Test(expected = InvalidEntityException.class)
    public void saveInvalidName() {
        AnyType newType = entityFactory.newEntity(AnyType.class);
        newType.setKey("group");
        newType.setKind(AnyTypeKind.ANY_OBJECT);
        anyTypeDAO.save(newType);
    }

    @Test
    public void delete() {
        AnyType otherType = anyTypeDAO.find("PRINTER");
        assertNotNull(otherType);

        anyTypeDAO.delete(otherType.getKey());
        assertNull(anyTypeDAO.find("PRINTER"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteInvalid() {
        anyTypeDAO.delete(anyTypeDAO.findUser().getKey());
    }
}