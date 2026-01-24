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

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.hibernate.annotations.Nationalized;
import org.keycloak.models.jpa.converter.MapStringConverter;

@Entity
@Table(name = "REALM_LOCALIZATIONS")
public class RealmLocalizationTextsEntity {

    @Embeddable
    static public class RealmLocalizationTextEntityKey implements Serializable {
        @Column(name = "REALM_ID", length = 36)
        private String realmId;
        @Column(name = "LOCALE")
        private String locale;

        public String getRealm() {
            return realmId;
        }

        public void setRealm(RealmEntity realm) {
            this.realmId = realm.getId();
        }

        public String getLocale() {
            return locale;
        }

        public void setLocale(String locale) {
            this.locale = locale;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RealmLocalizationTextEntityKey that = (RealmLocalizationTextEntityKey) o;
            return Objects.equals(realmId, that.realmId) &&
                    Objects.equals(locale, that.locale);
        }

        @Override
        public int hashCode() {
            return Objects.hash(realmId, locale);
        }
    }

    @EmbeddedId
    private RealmLocalizationTextEntityKey key;

    @Nationalized
    @Column(name = "TEXTS", columnDefinition = "TEXT", nullable = false) // can't set it to CLOB as in Liquibase scripts, because Liquibase converts it to TEXT (see ClobType)
    @Convert(converter = MapStringConverter.class)
    private Map<String,String> texts;

    public Map<String,String> getTexts() {
        return texts;
    }

    public void setTexts(Map<String,String> texts) {
        this.texts = texts;
    }

    @Override
    public String toString() {
        return "LocalizationTextEntity{" +
                "text='" + texts + '\'' +
                ", locale='" + key.locale + '\'' +
                ", realm='" + key.realmId + '\'' +
                '}';
    }

    public RealmLocalizationTextEntityKey getKey() {
        return key;
    }

    public void setKey(RealmLocalizationTextEntityKey key) {
        this.key = key;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RealmLocalizationTextsEntity that = (RealmLocalizationTextsEntity) o;
        return Objects.equals(key, that.key) && Objects.equals(texts, that.texts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, texts);
    }
}
