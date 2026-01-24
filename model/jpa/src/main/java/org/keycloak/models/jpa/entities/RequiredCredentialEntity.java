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

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.io.Serializable;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@Table(name="REALM_REQUIRED_CREDENTIAL")
@Entity
public class RequiredCredentialEntity {

    @EmbeddedId
    private Key key = new Key();
    @Column(name = "INPUT", nullable = false)
    protected boolean input;
    @Column(name = "SECRET", nullable = false)
    protected boolean secret;
    @Column(name = "FORM_LABEL")
    protected String formLabel;

    public Key getKey() {
        return key;
    }

    public void setRealm(RealmEntity realm) {
        this.key.setRealm(realm);
    }

    public String getType() {
        return key.type;
    }

    public void setType(String type) {
        this.key.setType(type);
    }

    public boolean isInput() {
        return input;
    }

    public void setInput(boolean input) {
        this.input = input;
    }

    public boolean isSecret() {
        return secret;
    }

    public void setSecret(boolean secret) {
        this.secret = secret;
    }

    public String getFormLabel() {
        return formLabel;
    }

    public void setFormLabel(String formLabel) {
        this.formLabel = formLabel;
    }

    @Embeddable
    public static class Key implements Serializable {

        @Column(name="REALM_ID", length = 36)
        private String realmId;

        private String type;

        public Key() {
        }

        public String getRealmId() {
            return this.realmId;
        }

        public void setRealm(RealmEntity realm) {
            this.realmId = realm.id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key key = (Key) o;

            if (realmId != null ? !realmId.equals(key.realmId) : key.realmId != null) return false;
            if (type != null ? !type.equals(key.type) : key.type != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = realmId != null ? realmId.hashCode() : 0;
            result = 31 * result + (type != null ? type.hashCode() : 0);
            return result;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof RequiredCredentialEntity that)) return false;

        return this.key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }


}
