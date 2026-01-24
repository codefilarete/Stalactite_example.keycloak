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
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import org.hibernate.annotations.Nationalized;


/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
@Table(name="CLIENT_ATTRIBUTES",
		indexes = {
				// Note that in Liquibase the index is more complex than this by expressing it with a substring of the value column
				// (depending on the database)
				@Index(name = "IDX_CLIENT_ATT_BY_NAME_VALUE", columnList = "NAME, VALUE")
		}
)
@Entity
public class ClientAttributeEntity {

	@EmbeddedId
	protected ClientAttributeEntity.Key key = new Key();

    @Nationalized
    @Column(name = "VALUE", columnDefinition = "TEXT")
    protected String value;

	public void getKey(Key key) {
		this.key = key;
    }

    public void setClient(ClientEntity client) {
        this.key.clientId = client.getId();
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

		@Column(name = "CLIENT_ID", length = 36)
        protected String clientId;

		@Column(name = "NAME")
        protected String name;

        public Key() {
        }

        public Key(ClientEntity client, String name) {
            this.clientId = client.getId();
            this.name = name;
        }

        public String getClientId() {
            return clientId;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ClientAttributeEntity.Key key = (ClientAttributeEntity.Key) o;

            if (clientId != null ? !clientId.equals(key.clientId != null ? key.clientId : null) : key.clientId != null) return false;
            if (name != null ? !name.equals(key.name != null ? key.name : null) : key.name != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = clientId != null ? clientId.hashCode() : 0;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            return result;
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof ClientAttributeEntity that)) return false;

        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
