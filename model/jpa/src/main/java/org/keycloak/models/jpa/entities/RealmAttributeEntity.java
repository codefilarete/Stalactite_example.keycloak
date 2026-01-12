/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
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

package org.keycloak.models.jpa.entities;

import jakarta.persistence.Index;
import org.hibernate.annotations.Nationalized;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@NamedQueries({
        @NamedQuery(name="deleteRealmAttributesByRealm", query="delete from RealmAttributeEntity attr where attr.realm = :realm"),
        @NamedQuery(name="selectRealmAttributesNotEmptyByName", query="select ra from RealmAttributeEntity ra WHERE ra.name = :name and length(ra.value) > 0")
})
@Table(name="REALM_ATTRIBUTE",
        indexes = {
                @Index(name = "IDX_REALM_ATTR_REALM", columnList = "REALM_ID")
        })
@Entity
@IdClass(RealmAttributeEntity.Key.class)
public class RealmAttributeEntity {

    @Id
    private Key key;

//    @Id
//    @ManyToOne(fetch= FetchType.LAZY)
//    @JoinColumn(name = "REALM_ID")
//    protected RealmEntity realm;
//
//    @Id
//    @Column(name = "NAME")
//    protected String name;

    @Nationalized
    @Column(name = "VALUE", columnDefinition = "TEXT")
    protected String value;

    public void setId(UUID uuid) {
        this.key = new Key();
        this.key.setRealmId(uuid.toString());
    }

    public Key getKey() {
        return key;
    }

    public String getName() {
        return key.name;
    }

    public void setName(String name) {
        this.key.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getRealmId() {
        return key.getRealmId();
    }

    public void setRealm(RealmEntity realm) {
        this.key.setRealm(realm);
    }

    public static class Key implements Serializable {

        protected String realmId;

        protected String name;

        public Key() {
        }

        public Key(RealmEntity realm, String name) {
            this.realmId = realm.getId();
            this.name = name;
        }

        public void setRealm(RealmEntity realm) {
            this.realmId = realm.getId();
        }

        public String getRealmId() {
            return realmId;
        }

        public Key setRealmId(String realmId) {
            this.realmId = realmId;
            return this;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key that = (Key) o;

            return Objects.equals(realmId, that.realmId) &&
                    Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            int result = realmId != null ? realmId.hashCode() : 0;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            return result;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof RealmAttributeEntity that)) return false;

        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }


}
