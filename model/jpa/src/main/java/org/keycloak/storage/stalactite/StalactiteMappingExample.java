/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.storage.stalactite;

import org.codefilarete.stalactite.engine.EntityPersister;
import org.codefilarete.stalactite.engine.PersistenceContext;
import org.codefilarete.stalactite.sql.Dialect;
import org.codefilarete.stalactite.sql.PostgreSQLDialectBuilder;
import org.codefilarete.stalactite.sql.ddl.DDLDeployer;
import org.codefilarete.stalactite.sql.ddl.structure.Table;
import org.codefilarete.tool.collection.CaseInsensitiveMap;
import org.codefilarete.tool.collection.Iterables;
import org.keycloak.models.jpa.entities.RealmEntity;
import org.keycloak.models.jpa.entities.RealmAttributeEntity;
import org.keycloak.models.jpa.entities.AuthenticationFlowEntity;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.UUID;

/**
 * Example demonstrating how to use the Stalactite ORM mapping for RealmEntity.
 *
 * This class shows various CRUD operations and query patterns using the Stalactite
 * persistence configuration.
 *
 * @author Keycloak Team
 */
public class StalactiteMappingExample {

    private final PersistenceContext persistenceContext;
    private final EntityPersister<RealmEntity, String> realmPersister;

    /**
     * Initialize the Stalactite persistence context with a DataSource
     *
     * @param dataSource JDBC DataSource
     */
    public StalactiteMappingExample(DataSource dataSource) {
        // Create connection provider

        // Create dialect (use appropriate dialect for your database)
        Dialect dialect = PostgreSQLDialectBuilder.defaultPostgreSQLDialect();

        // Create persistence context
        this.persistenceContext = new PersistenceContext(dataSource, dialect);

        // Initialize RealmEntity mapping
        RealmEntityPersistenceConfiguration.initializePersistence(persistenceContext);
        UserEntityPersistenceConfiguration.initializePersistence(persistenceContext);
		ClientEntityPersistenceConfiguration.initializePersistence(persistenceContext);
		ClientScopePersistenceConfiguration.initializePersistence(persistenceContext);
		
		Collection<Table<?>> tables = DDLDeployer.collectTables(persistenceContext);
		CaseInsensitiveMap<Table<?>> tablePerName = Iterables.map(tables, Table::getName, () -> new CaseInsensitiveMap<>());
		// Fixing column type that can't be fixed through DSL
		dialect.getSqlTypeRegistry().put(tablePerName.get("realm_attribute").getColumn("value"), "TEXT");
		dialect.getSqlTypeRegistry().put(tablePerName.get("realm_localizations").getColumn("texts"), "TEXT");
		dialect.getSqlTypeRegistry().put(tablePerName.get("credential").getColumn("credential_data"), "TEXT");
		dialect.getSqlTypeRegistry().put(tablePerName.get("credential").getColumn("secret_data"), "TEXT");
		dialect.getSqlTypeRegistry().put(tablePerName.get("federated_identity").getColumn("token"), "TEXT");
		dialect.getSqlTypeRegistry().put(tablePerName.get("user_attribute").getColumn("long_value"), "TEXT");
		dialect.getSqlTypeRegistry().put(tablePerName.get("client_attributes").getColumn("value"), "TEXT");
		dialect.getSqlTypeRegistry().put(tablePerName.get("protocol_mapper_config").getColumn("value"), "TEXT");
		
		// Get the persister for RealmEntity
        this.realmPersister = persistenceContext.findPersister(RealmEntity.class);
    }

    /**
     * Example 1: Create a new realm with basic configuration
     */
    public RealmEntity createBasicRealm(String realmName) {
        RealmEntity realm = new RealmEntity();

        // Set required fields
        realm.setId(UUID.randomUUID().toString());
        realm.setName(realmName);
        realm.setEnabled(true);

        // Set basic configuration
        realm.setSslRequired("EXTERNAL");
        realm.setRegistrationAllowed(true);
        realm.setRegistrationEmailAsUsername(false);
        realm.setVerifyEmail(true);
        realm.setResetPasswordAllowed(true);
        realm.setLoginWithEmailAllowed(true);
        realm.setDuplicateEmailsAllowed(false);
        realm.setRememberMe(true);
        realm.setEditUsernameAllowed(false);

        // Set token and session timeouts (in seconds)
        realm.setSsoSessionIdleTimeout(1800);  // 30 minutes
        realm.setSsoSessionMaxLifespan(36000); // 10 hours
        realm.setAccessTokenLifespan(300);     // 5 minutes
        realm.setAccessCodeLifespan(60);       // 1 minute
        realm.setAccessCodeLifespanUserAction(300); // 5 minutes
        realm.setAccessCodeLifespanLogin(1800); // 30 minutes

        // Set themes
        realm.setLoginTheme("keycloak");
        realm.setAccountTheme("keycloak");
        realm.setAdminTheme("keycloak");
        realm.setEmailTheme("keycloak");

        // Enable events
        realm.setEventsEnabled(true);
        realm.setEventsExpiration(604800); // 7 days
        realm.setAdminEventsEnabled(true);
        realm.setAdminEventsDetailsEnabled(false);

        // Insert into database
        realmPersister.insert(realm);

        return realm;
    }

    /**
     * Example 2: Create a realm with custom attributes
     */
    public RealmEntity createRealmWithAttributes(String realmName) {
        RealmEntity realm = createBasicRealm(realmName);

        // Add custom attributes
        RealmAttributeEntity displayNameAttr = new RealmAttributeEntity();
        displayNameAttr.setId(UUID.randomUUID());
        displayNameAttr.setName("displayName");
        displayNameAttr.setValue("My Custom Realm");
        displayNameAttr.setRealm(realm);

        RealmAttributeEntity logoUrlAttr = new RealmAttributeEntity();
        logoUrlAttr.setId(UUID.randomUUID());
        logoUrlAttr.setName("logoUrl");
        logoUrlAttr.setValue("https://example.com/logo.png");
        logoUrlAttr.setRealm(realm);

        realm.getAttributes().add(displayNameAttr);
        realm.getAttributes().add(logoUrlAttr);

        // Update realm (cascade will save attributes)
        realmPersister.update(realm);

        return realm;
    }

    /**
     * Example 3: Configure SMTP settings for a realm
     */
    public void configureSmtp(String realmId, String host, String port,
                             String from, String username, String password) {
        RealmEntity realm = realmPersister.select(realmId);

        if (realm != null) {
            // Configure SMTP settings
            realm.getSmtpConfig().put("host", host);
            realm.getSmtpConfig().put("port", port);
            realm.getSmtpConfig().put("from", from);
            realm.getSmtpConfig().put("fromDisplayName", realm.getName());
            realm.getSmtpConfig().put("auth", "true");
            realm.getSmtpConfig().put("user", username);
            realm.getSmtpConfig().put("password", password);
            realm.getSmtpConfig().put("starttls", "true");

            realmPersister.update(realm);
        }
    }

    /**
     * Example 4: Configure event listeners
     */
    public void configureEventListeners(String realmId) {
        RealmEntity realm = realmPersister.select(realmId);

        if (realm != null) {
            // Add event listeners
            realm.getEventsListeners().add("jboss-logging");
            realm.getEventsListeners().add("email");

            // Add enabled event types
            realm.getEnabledEventTypes().add("LOGIN");
            realm.getEnabledEventTypes().add("LOGIN_ERROR");
            realm.getEnabledEventTypes().add("REGISTER");
            realm.getEnabledEventTypes().add("LOGOUT");
            realm.getEnabledEventTypes().add("CODE_TO_TOKEN");

            realmPersister.update(realm);
        }
    }

    /**
     * Example 5: Configure internationalization
     */
    public void configureInternationalization(String realmId) {
        RealmEntity realm = realmPersister.select(realmId);

        if (realm != null) {
            // Enable internationalization
            realm.setInternationalizationEnabled(true);
            realm.setDefaultLocale("en");

            // Add supported locales
            realm.getSupportedLocales().add("en");
            realm.getSupportedLocales().add("de");
            realm.getSupportedLocales().add("fr");
            realm.getSupportedLocales().add("es");
            realm.getSupportedLocales().add("ja");

            realmPersister.update(realm);
        }
    }

    /**
     * Example 6: Add authentication flow
     */
    public void addAuthenticationFlow(String realmId) {
        RealmEntity realm = realmPersister.select(realmId);

        if (realm != null) {
            // Create browser authentication flow
            AuthenticationFlowEntity browserFlow = new AuthenticationFlowEntity();
            browserFlow.setId(UUID.randomUUID().toString());
            browserFlow.setAlias("browser");
            browserFlow.setDescription("Browser based authentication");
            browserFlow.setProviderId("basic-flow");
            browserFlow.setTopLevel(true);
            browserFlow.setBuiltIn(false);
            browserFlow.setRealm(realm);

            realm.getAuthenticationFlows().add(browserFlow);

            // Set as default browser flow
            realm.setBrowserFlow(browserFlow.getId());

            realmPersister.update(realm);
        }
    }

    /**
     * Example 7: Find realm by name
     */
//    public RealmEntity findRealmByName(String name) {
//        // Using criteria query
//        List<RealmEntity> realms = realmPersister.selectWhere(
//            RealmEntity::getName, "=", name
//        );
//
//        return realms.isEmpty() ? null : realms.get(0);
//    }
//
//    /**
//     * Example 8: Find all enabled realms
//     */
//    public List<RealmEntity> findEnabledRealms() {
//        return realmPersister.selectWhere(
//            RealmEntity::isEnabled, "=", true
//        );
//    }

    /**
     * Example 9: Update realm settings
     */
    public void updateRealmSettings(String realmId) {
        RealmEntity realm = realmPersister.select(realmId);

        if (realm != null) {
            // Update various settings
            realm.setAccessTokenLifespan(600); // Change to 10 minutes
            realm.setSsoSessionIdleTimeout(3600); // Change to 1 hour
            realm.setLoginTheme("custom-theme");

            // Update password policy
            realm.setPasswordPolicy("length(8) and upperCase(1) and lowerCase(1) and digits(1)");

            // Update OTP policy
            realm.setOtpPolicyType("totp");
            realm.setOtpPolicyAlgorithm("HmacSHA1");
            realm.setOtpPolicyDigits(6);
            realm.setOtpPolicyPeriod(30);
            realm.setOtpPolicyLookAheadWindow(1);

            realmPersister.update(realm);
        }
    }

    /**
     * Example 10: Delete a realm
     */
    public void deleteRealm(String realmId) {
        RealmEntity realm = realmPersister.select(realmId);

        if (realm != null) {
            // Delete will cascade to all related entities (attributes, flows, etc.)
            realmPersister.delete(realm);
        }
    }

    /**
     * Example 11: Count all realms
     */
    public long countRealms() {
        return realmPersister.selectAll().size();
    }

    /**
     * Example 12: Disable all realms except master
     */
//    public void disableAllNonMasterRealms() {
//        List<RealmEntity> realms = realmPersister.selectAll();
//
//        for (RealmEntity realm : realms) {
//            if (!"master".equals(realm.getName()) && realm.isEnabled()) {
//                realm.setEnabled(false);
//                realmPersister.update(realm);
//            }
//        }
//    }

    /**
     * Example 13: Transaction example
     */
//    public void transactionalOperation(String realmName) {
//        persistenceContext.getConnectionProvider().giveConnection().transact(connection -> {
//            // All operations in this block are part of a single transaction
//            RealmEntity realm = createBasicRealm(realmName);
//            configureEventListeners(realm.getId());
//            configureInternationalization(realm.getId());
//
//            // Transaction commits automatically if no exception is thrown
//            // If an exception occurs, transaction is rolled back
//        });
//    }

    /**
     * Example 14: Batch operation - create multiple realms
     */
    public void createMultipleRealms(String... realmNames) {
        for (String name : realmNames) {
            createBasicRealm(name);
        }
    }

    /**
     * Example 15: Find realms with events enabled
     */
//    public List<RealmEntity> findRealmsWithEventsEnabled() {
//        return realmPersister.selectWhere(
//            RealmEntity::isEventsEnabled, "=", true
//        );
//    }

    /**
     * Get the persistence context
     */
    public PersistenceContext getPersistenceContext() {
        return persistenceContext;
    }

    /**
     * Get the realm persister
     */
    public EntityPersister<RealmEntity, String> getRealmPersister() {
        return realmPersister;
    }
}

