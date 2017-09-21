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

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.syncope.core.persistence.api.entity.AnyType;
import org.apache.syncope.core.persistence.api.entity.DynRealm;
import org.apache.syncope.core.persistence.api.entity.DynRealmMembership;
import org.apache.syncope.core.persistence.jpa.validation.entity.DynRealmCheck;

@Entity
@Table(name = JPADynRealm.TABLE)
@Cacheable
@DynRealmCheck
public class JPADynRealm extends AbstractProvidedKeyEntity implements DynRealm {

    private static final long serialVersionUID = -6851035842423560341L;

    public static final String TABLE = "DynRealm";

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER, mappedBy = "dynRealm")
    private List<JPADynRealmMembership> dynMemberships = new ArrayList<>();

    @Override
    public boolean add(final DynRealmMembership dynRealmMembership) {
        checkType(dynRealmMembership, JPADynRealmMembership.class);
        return this.dynMemberships.add((JPADynRealmMembership) dynRealmMembership);
    }

    @Override
    public DynRealmMembership getDynMembership(final AnyType anyType) {
        return IterableUtils.find(dynMemberships, new Predicate<DynRealmMembership>() {

            @Override
            public boolean evaluate(final DynRealmMembership dynRealmMembership) {
                return anyType != null && anyType.equals(dynRealmMembership.getAnyType());
            }
        });
    }

    @Override
    public List<? extends DynRealmMembership> getDynMemberships() {
        return dynMemberships;
    }

}