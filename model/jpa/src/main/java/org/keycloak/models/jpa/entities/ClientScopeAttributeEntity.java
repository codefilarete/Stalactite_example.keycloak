/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@Table(name="CLIENT_SCOPE_ATTRIBUTES",
        indexes = {
                @Index(name = "IDX_CLSCOPE_ATTRS", columnList = "SCOPE_ID")
        }
)
@Entity
public class ClientScopeAttributeEntity {

    @EmbeddedId
    private Key key;

    @Column(name = "VALUE", length = 2048)
    protected String value;

    public Key getKey() {
        return this.key;
    }

    public void setClientScope(ClientScopeEntity clientScope) {
        this.key.clientScopeId = clientScope.getId();
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


    @Embeddable
    public static class Key implements Serializable {

        @Column(name = "SCOPE_ID", length = 36)
        protected String clientScopeId;

        @Column(name = "NAME")
        protected String name;

        public Key() {
        }

        public Key(ClientScopeEntity clientScope, String name) {
            this.clientScopeId = clientScope.getId();
            this.name = name;
        }

        public String getClientScopeId() {
            return clientScopeId;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ClientScopeAttributeEntity.Key key = (ClientScopeAttributeEntity.Key) o;

            if (clientScopeId != null ? !clientScopeId.equals(key.clientScopeId != null ? key.clientScopeId : null) : key.clientScopeId != null) return false;
            if (name != null ? !name.equals(key.name != null ? key.name : null) : key.name != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = clientScopeId != null ? clientScopeId.hashCode() : 0;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            return result;
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!(o instanceof ClientScopeAttributeEntity that)) return false;

        return key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
