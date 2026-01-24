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
import jakarta.persistence.Index;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import java.io.Serializable;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
@NamedQueries({
        @NamedQuery(name= "findFederatedIdentityByUser", query="select link from FederatedIdentityEntity link where link.key.userId = :user"),
        @NamedQuery(name= "findFederatedIdentityByUserAndProvider", query="select link from FederatedIdentityEntity link where link.key.userId = :user and link.key.identityProvider = :identityProvider"),
        @NamedQuery(name= "findUserByFederatedIdentityAndRealm", query="select u from UserEntity u where u.id = (select link.key.userId from FederatedIdentityEntity link where link.realmId = :realmId and link.key.identityProvider = :identityProvider and link.userId = :userId)"),
        @NamedQuery(name= "deleteFederatedIdentityByRealm", query="delete from FederatedIdentityEntity social where social.key.userId IN (select u.id from UserEntity u where realmId=:realmId)"),
        @NamedQuery(name= "deleteFederatedIdentityByProvider", query="delete from FederatedIdentityEntity fdi where fdi.realmId = :realmId and fdi.key.identityProvider = :providerAlias "),
        @NamedQuery(name= "deleteFederatedIdentityByRealmAndLink", query="delete from FederatedIdentityEntity social where social.key.userId IN (select u.id from UserEntity u where realmId=:realmId and u.federationLink=:link)"),
        @NamedQuery(name= "deleteFederatedIdentityByUser", query="delete from FederatedIdentityEntity social where social.key.userId = :user")
})
@Table(name="FEDERATED_IDENTITY",
        indexes = {
                @Index(name = "IDX_FEDIDENTITY_FEDUSER", columnList = "FEDERATED_USER_ID"),
                @Index(name = "IDX_FEDIDENTITY_USER", columnList = "USER_ID")
        }
)
@Entity
public class FederatedIdentityEntity {

	@EmbeddedId
	private FederatedIdentityEntity.Key key;
	
	public FederatedIdentityEntity.Key getKey() {
		return key;
	}
	
    @Column(name = "REALM_ID", length = 36)
    protected String realmId;

    @Column(name = "FEDERATED_USER_ID")
    protected String userId;
    @Column(name = "FEDERATED_USERNAME")
    protected String userName;

    @Column(name = "TOKEN", columnDefinition = "TEXT")
    protected String token;

    public void setUser(UserEntity user) {
        this.key.userId = user.getId();
    }

    public String getIdentityProvider() {
        return key.identityProvider;
    }

    public void setIdentityProvider(String identityProvider) {
        this.key.identityProvider = identityProvider;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRealmId() {
        return realmId;
    }

    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

	@Embeddable
    public static class Key implements Serializable {
		
		@Column(name = "USER_ID", length = 36)
        protected String userId;

		@Column(name = "IDENTITY_PROVIDER")
        protected String identityProvider;

        public Key() {
        }

        public Key(UserEntity user, String identityProvider) {
            this.userId = user.getId();
            this.identityProvider = identityProvider;
        }
		
		public String getUserId() {
			return userId;
		}
		
		//        public UserEntity getUser() {
//            return user;
//        }

        public String getIdentityProvider() {
            return identityProvider;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key key = (Key) o;

            if (identityProvider != null ? !identityProvider.equals(key.identityProvider) : key.identityProvider != null)
                return false;
            if (userId != null ? !userId.equals(key.userId != null ? key.userId : null) : key.userId != null) return false;
//            if (user != null ? !user.getId().equals(key.user != null ? key.user.getId() : null) : key.user != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = userId != null ? userId.hashCode() : 0;
            result = 31 * result + (identityProvider != null ? identityProvider.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "FederatedIdentityEntity.Key [user=" + (userId != null ? userId : null) + ", identityProvider=" + identityProvider + "]";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof FederatedIdentityEntity)) return false;

		return this.key.equals(((FederatedIdentityEntity) o).key);
		
//        FederatedIdentityEntity key = (FederatedIdentityEntity) o;
//
//        if (identityProvider != null ? !identityProvider.equals(key.identityProvider) : key.identityProvider != null)
//            return false;
//        if (user != null ? !user.getId().equals(key.user != null ? key.user.getId() : null) : key.user != null) return false;
//
//        return true;
    }

    @Override
    public int hashCode() {
		return this.key.hashCode();
//        int result = user != null ? user.getId().hashCode() : 0;
//        result = 31 * result + (identityProvider != null ? identityProvider.hashCode() : 0);
//        return result;
    }


}
