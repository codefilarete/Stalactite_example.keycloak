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

import java.util.Map;
import java.util.Objects;

import org.codefilarete.stalactite.dsl.entity.FluentEntityMappingBuilder;
import org.codefilarete.stalactite.dsl.idpolicy.IdentifierPolicy;
import org.codefilarete.stalactite.dsl.naming.ForeignKeyNamingStrategy;
import org.codefilarete.stalactite.dsl.property.CascadeOptions.RelationMode;
import org.codefilarete.stalactite.engine.PersistenceContext;
import org.codefilarete.stalactite.sql.ddl.Length;
import org.codefilarete.stalactite.sql.ddl.Size;
import org.codefilarete.stalactite.sql.ddl.structure.Table;
import org.codefilarete.stalactite.sql.statement.binder.DefaultParameterBinders;
import org.codefilarete.tool.function.Converter.NullAwareConverter;
import org.keycloak.models.jpa.converter.MapStringConverter;
import org.keycloak.models.jpa.entities.AuthenticationFlowEntity;
import org.keycloak.models.jpa.entities.AuthenticatorConfigEntity;
import org.keycloak.models.jpa.entities.ComponentConfigEntity;
import org.keycloak.models.jpa.entities.ComponentEntity;
import org.keycloak.models.jpa.entities.RealmAttributeEntity;
import org.keycloak.models.jpa.entities.RealmAttributeEntity.Key;
import org.keycloak.models.jpa.entities.RealmEntity;
import org.keycloak.models.jpa.entities.RealmLocalizationTextsEntity;
import org.keycloak.models.jpa.entities.RequiredActionProviderEntity;
import org.keycloak.models.jpa.entities.RequiredCredentialEntity;
import org.keycloak.models.jpa.entities.UserFederationMapperEntity;
import org.keycloak.models.jpa.entities.UserFederationProviderEntity;

import static org.codefilarete.stalactite.dsl.MappingEase.compositeKeyBuilder;
import static org.codefilarete.stalactite.dsl.MappingEase.embeddableBuilder;
import static org.codefilarete.stalactite.dsl.MappingEase.entityBuilder;

/**
 * Stalactite ORM persistence configuration for RealmEntity
 * <p>
 * This configuration defines the complete mapping between RealmEntity and the database schema,
 * including all relationships, collections, and element collections.
 *
 * @author Keycloak Team
 */
public class RealmEntityPersistenceConfiguration {
	
	private static final Length UUID_LENGTH = Size.length(36);
	
	/**
	 * Creates and configures the persistence mapping for RealmEntity
	 *
	 * @return configured FluentEntityMappingBuilder for RealmEntity
	 */
	public static FluentEntityMappingBuilder<RealmEntity, String> buildRealmEntityMapping() {
		
		Table userFederationMapperTable = new Table("USER_FEDERATION_MAPPER");
		// we fix Stalactite non capability of providing nullity on back-references
		userFederationMapperTable.addColumn("REALM_ID", String.class).notNull();
		// the federation_provider_id is not mapped in the below persistence definition because it's part of another aggregate, nevertheless we add
		// it to the schema to fill the gap between Keycloak and Stalactite
		userFederationMapperTable.addColumn("federation_provider_id", String.class, UUID_LENGTH).notNull();
		
		Table realmAttributeTable = new Table("REALM_ATTRIBUTE");
		// we fix Stalactite non capability of providing nullity on back-references
		realmAttributeTable.addColumn("REALM_ID", String.class).notNull();
		
		return entityBuilder(RealmEntity.class, String.class)
				// Table configuration
				.mapKey(RealmEntity::getId, IdentifierPolicy.alreadyAssigned(o -> {}, Objects::isNull))
					.columnName("ID").columnSize(UUID_LENGTH)
				.onTable("REALM")
				.withForeignKeyNaming(ForeignKeyNamingStrategy.HIBERNATE_7)
				
				// Simple columns
				.map(RealmEntity::getName).columnName("NAME")
					.unique()
				.map(RealmEntity::isEnabled).columnName("ENABLED")
				.map(RealmEntity::getSslRequired).columnName("SSL_REQUIRED")
				.map(RealmEntity::isRegistrationAllowed).columnName("REGISTRATION_ALLOWED")
				.map(RealmEntity::isRegistrationEmailAsUsername).columnName("REG_EMAIL_AS_USERNAME")
				.map(RealmEntity::isVerifyEmail).columnName("VERIFY_EMAIL")
				.map(RealmEntity::isResetPasswordAllowed).columnName("RESET_PASSWORD_ALLOWED")
				.map(RealmEntity::isLoginWithEmailAllowed).columnName("LOGIN_WITH_EMAIL_ALLOWED")
				.map(RealmEntity::isDuplicateEmailsAllowed).columnName("DUPLICATE_EMAILS_ALLOWED")
				.map(RealmEntity::isRememberMe).columnName("REMEMBER_ME")
				.map(RealmEntity::getPasswordPolicy).columnName("PASSWORD_POLICY").columnSize(Size.length(2550))
				
				// OTP Policy columns
				.map(RealmEntity::getOtpPolicyType).columnName("OTP_POLICY_TYPE").columnSize(UUID_LENGTH)
				.map(RealmEntity::getOtpPolicyAlgorithm).columnName("OTP_POLICY_ALG").columnSize(UUID_LENGTH)
				.map(RealmEntity::getOtpPolicyInitialCounter).columnName("OTP_POLICY_COUNTER").nullable()
				.map(RealmEntity::getOtpPolicyDigits).columnName("OTP_POLICY_DIGITS").nullable()
				.map(RealmEntity::getOtpPolicyLookAheadWindow).columnName("OTP_POLICY_WINDOW").nullable()
				.map(RealmEntity::getOtpPolicyPeriod).columnName("OTP_POLICY_PERIOD").nullable()
				
				// User management columns
				.map(RealmEntity::isEditUsernameAllowed).columnName("EDIT_USERNAME_ALLOWED")
				
				// Token and session configuration
				.map(RealmEntity::isRevokeRefreshToken).columnName("REVOKE_REFRESH_TOKEN")
				.map(RealmEntity::getRefreshTokenMaxReuse).columnName("REFRESH_TOKEN_MAX_REUSE").nullable()
				.map(RealmEntity::getSsoSessionIdleTimeout).columnName("SSO_IDLE_TIMEOUT").nullable()
				.map(RealmEntity::getSsoSessionMaxLifespan).columnName("SSO_MAX_LIFESPAN").nullable()
				.map(RealmEntity::getSsoSessionIdleTimeoutRememberMe).columnName("SSO_IDLE_TIMEOUT_REMEMBER_ME")
				.map(RealmEntity::getSsoSessionMaxLifespanRememberMe).columnName("SSO_MAX_LIFESPAN_REMEMBER_ME")
				.map(RealmEntity::getOfflineSessionIdleTimeout).columnName("OFFLINE_SESSION_IDLE_TIMEOUT").nullable()
				.map(RealmEntity::getAccessTokenLifespan).columnName("ACCESS_TOKEN_LIFESPAN").nullable()
				.map(RealmEntity::getAccessTokenLifespanForImplicitFlow).columnName("ACCESS_TOKEN_LIFE_IMPLICIT").nullable()
				.map(RealmEntity::getAccessCodeLifespan).columnName("ACCESS_CODE_LIFESPAN").nullable()
				.map(RealmEntity::getAccessCodeLifespanUserAction).columnName("USER_ACTION_LIFESPAN").nullable()
				.map(RealmEntity::getAccessCodeLifespanLogin).columnName("LOGIN_LIFESPAN").nullable()
				.map(RealmEntity::getNotBefore).columnName("NOT_BEFORE").nullable()
				
				// Theme columns
				.map(RealmEntity::getLoginTheme).columnName("LOGIN_THEME")
				.map(RealmEntity::getAccountTheme).columnName("ACCOUNT_THEME")
				.map(RealmEntity::getAdminTheme).columnName("ADMIN_THEME")
				.map(RealmEntity::getEmailTheme).columnName("EMAIL_THEME")
				
				// Event configuration
				.map(RealmEntity::isEventsEnabled).columnName("EVENTS_ENABLED")
				.map(RealmEntity::getEventsExpiration).columnName("EVENTS_EXPIRATION").nullable()
				.map(RealmEntity::isAdminEventsEnabled).columnName("ADMIN_EVENTS_ENABLED")
				.map(RealmEntity::isAdminEventsDetailsEnabled).columnName("ADMIN_EVENTS_DETAILS_ENABLED")
				
				// Admin and role configuration
				.map(RealmEntity::getMasterAdminClient).columnName("MASTER_ADMIN_CLIENT").columnSize(UUID_LENGTH)
				.map(RealmEntity::getDefaultRoleId).columnName("DEFAULT_ROLE")
				
				// Authentication flow configuration
				.map(RealmEntity::getBrowserFlow).columnName("BROWSER_FLOW").columnSize(UUID_LENGTH)
				.map(RealmEntity::getRegistrationFlow).columnName("REGISTRATION_FLOW").columnSize(UUID_LENGTH)
				.map(RealmEntity::getDirectGrantFlow).columnName("DIRECT_GRANT_FLOW").columnSize(UUID_LENGTH)
				.map(RealmEntity::getResetCredentialsFlow).columnName("RESET_CREDENTIALS_FLOW").columnSize(UUID_LENGTH)
				.map(RealmEntity::getClientAuthenticationFlow).columnName("CLIENT_AUTH_FLOW").columnSize(UUID_LENGTH)
				.map(RealmEntity::getDockerAuthenticationFlow).columnName("DOCKER_AUTH_FLOW").columnSize(UUID_LENGTH)
				
				// Internationalization
				.map(RealmEntity::isInternationalizationEnabled).columnName("INTERNATIONALIZATION_ENABLED")
				.map(RealmEntity::getDefaultLocale).columnName("DEFAULT_LOCALE")
				
				// User managed access
				.map(RealmEntity::isAllowUserManagedAccess).columnName("ALLOW_USER_MANAGED_ACCESS")
				
				// Element Collections - Simple Sets
				.mapCollection(RealmEntity::getEventsListeners, String.class)
				.reverseJoinColumn("REALM_ID")
				.elementColumnName("VALUE")
				.onTable("REALM_EVENTS_LISTENERS")
//                    .cascading()
				
				.mapCollection(RealmEntity::getEnabledEventTypes, String.class)
					.onTable("REALM_ENABLED_EVENT_TYPES")
					.reverseJoinColumn("REALM_ID")
					.elementColumnName("VALUE")
				
				.mapCollection(RealmEntity::getSupportedLocales, String.class)
					.onTable("REALM_SUPPORTED_LOCALES")
					.reverseJoinColumn("REALM_ID")
					.elementColumnName("VALUE")
				
				.mapCollection(RealmEntity::getDefaultGroupIds, String.class)
					.onTable("REALM_DEFAULT_GROUPS")
					.reverseJoinColumn("REALM_ID")
					.elementColumnName("GROUP_ID")
					.elementColumnSize(UUID_LENGTH)
				
				// Element Collection - Map (SMTP Config)
				.mapMap(RealmEntity::getSmtpConfig, String.class, String.class)
					.onTable("REALM_SMTP_CONFIG")
					.reverseJoinColumn("REALM_ID")
					.keyColumn("NAME")
					.valueColumn("VALUE")
				
				// One-to-Many Relationships with eager loading
				.mapOneToMany(RealmEntity::getAttributes, entityBuilder(RealmAttributeEntity.class, Key.class)
								.onTable(realmAttributeTable)
								.mapCompositeKey(RealmAttributeEntity::getKey, compositeKeyBuilder(Key.class)
										.map(Key::getRealmId).columnName("REALM_ID").columnSize(UUID_LENGTH)
										.map(Key::getName), o -> {}, Objects::isNull)
								.map(RealmAttributeEntity::getValue).columnName("VALUE")//.columnSize(Size.length(2147483647))
				)
				.reverseJoinColumn("REALM_ID")
				.cascading(RelationMode.ALL_ORPHAN_REMOVAL)
				.fetchSeparately() // EAGER in JPA
				
				.mapOneToMany(RealmEntity::getRequiredCredentials,
						entityBuilder(RequiredCredentialEntity.class, RequiredCredentialEntity.Key.class)
								.onTable("REALM_REQUIRED_CREDENTIAL")
								.mapCompositeKey(RequiredCredentialEntity::getKey, compositeKeyBuilder(RequiredCredentialEntity.Key.class)
												.map(RequiredCredentialEntity.Key::getType)
												.map(RequiredCredentialEntity.Key::getRealmId).columnName("REALM_ID").columnSize(UUID_LENGTH), o -> {}, Objects::isNull)
								.map(RequiredCredentialEntity::isInput).columnName("INPUT")
								.map(RequiredCredentialEntity::isSecret).columnName("SECRET")
								.map(RequiredCredentialEntity::getFormLabel).columnName("FORM_LABEL")
				)
				.reverseJoinColumn("REALM_ID")
				.cascading(RelationMode.ALL_ORPHAN_REMOVAL)
				
				.mapOneToMany(RealmEntity::getUserFederationProviders,
						entityBuilder(UserFederationProviderEntity.class, String.class)
								.onTable("USER_FEDERATION_PROVIDER")
                                .withForeignKeyNaming(ForeignKeyNamingStrategy.HIBERNATE_7)
								.mapKey(UserFederationProviderEntity::getId, IdentifierPolicy.alreadyAssigned(e -> {}, Objects::nonNull)).columnSize(UUID_LENGTH)
								.map(UserFederationProviderEntity::getProviderName).columnName("PROVIDER_NAME")
								.map(UserFederationProviderEntity::getPriority).columnName("PRIORITY").nullable()
								.map(UserFederationProviderEntity::getDisplayName).columnName("DISPLAY_NAME")
								.map(UserFederationProviderEntity::getFullSyncPeriod).columnName("FULL_SYNC_PERIOD").nullable()
								.map(UserFederationProviderEntity::getChangedSyncPeriod).columnName("CHANGED_SYNC_PERIOD").nullable()
								.map(UserFederationProviderEntity::getLastSync).columnName("LAST_SYNC").nullable()
								.mapMap(UserFederationProviderEntity::getConfig, String.class, String.class)
                                    .onTable("USER_FEDERATION_CONFIG")
                                    .reverseJoinColumn("USER_FEDERATION_PROVIDER_ID")
                                    .keyColumn("NAME")
                                    .valueColumn("VALUE")
				)
				.mappedBy(UserFederationProviderEntity::getRealm)
				.reverseJoinColumn("REALM_ID")
				.cascading(RelationMode.ALL_ORPHAN_REMOVAL)
				
				.mapOneToMany(RealmEntity::getUserFederationMappers,
						entityBuilder(UserFederationMapperEntity.class, String.class)
								.onTable(userFederationMapperTable)
                                .withForeignKeyNaming(ForeignKeyNamingStrategy.HIBERNATE_7)
								.mapKey(UserFederationMapperEntity::getId, IdentifierPolicy.alreadyAssigned(e -> {}, Objects::nonNull)).columnSize(UUID_LENGTH)
								.map(UserFederationMapperEntity::getName).mandatory()
								.map(UserFederationMapperEntity::getFederationMapperType).columnName("FEDERATION_MAPPER_TYPE").mandatory()
                                .mapMap(UserFederationMapperEntity::getConfig, String.class, String.class)
                                    .onTable("USER_FEDERATION_MAPPER_CONFIG")
                                    .reverseJoinColumn("USER_FEDERATION_MAPPER_ID")
                                    .keyColumn("NAME")
                                    .valueColumn("VALUE")
				)
				.mappedBy(UserFederationMapperEntity::getRealm)
				.reverseJoinColumn("REALM_ID")
				.cascading(RelationMode.ALL_ORPHAN_REMOVAL)
				
				.mapOneToMany(RealmEntity::getAuthenticatorConfigs,
						entityBuilder(AuthenticatorConfigEntity.class, String.class)
								.onTable("AUTHENTICATOR_CONFIG")
								.mapKey(AuthenticatorConfigEntity::getId, IdentifierPolicy.alreadyAssigned(e -> {}, Objects::nonNull)).columnSize(UUID_LENGTH)
								.map(AuthenticatorConfigEntity::getAlias).columnName("ALIAS")
				)
				.mappedBy(AuthenticatorConfigEntity::getRealm)
				.reverseJoinColumn("REALM_ID")
				.cascading(RelationMode.ALL_ORPHAN_REMOVAL)
				
				.mapOneToMany(RealmEntity::getRequiredActionProviders,
						entityBuilder(RequiredActionProviderEntity.class, String.class)
								.onTable("REQUIRED_ACTION_PROVIDER")
								.mapKey(RequiredActionProviderEntity::getId, IdentifierPolicy.alreadyAssigned(e -> {}, Objects::nonNull)).columnSize(UUID_LENGTH)
								.map(RequiredActionProviderEntity::getAlias).columnName("ALIAS")
								.map(RequiredActionProviderEntity::getName).columnName("NAME")
								.map(RequiredActionProviderEntity::getProviderId).columnName("PROVIDER_ID")
								.map(RequiredActionProviderEntity::isEnabled).columnName("ENABLED")
								.map(RequiredActionProviderEntity::isDefaultAction).columnName("DEFAULT_ACTION")
								.map(RequiredActionProviderEntity::getPriority).columnName("PRIORITY").nullable())
				.mappedBy(RequiredActionProviderEntity::getRealm)
				.reverseJoinColumn("REALM_ID")
				.cascading(RelationMode.ALL_ORPHAN_REMOVAL)
				
				.mapOneToMany(RealmEntity::getAuthenticationFlows,
						entityBuilder(AuthenticationFlowEntity.class, String.class)
								.onTable("AUTHENTICATION_FLOW")
								.mapKey(AuthenticationFlowEntity::getId, IdentifierPolicy.alreadyAssigned(e -> {}, Objects::nonNull)).columnSize(UUID_LENGTH)
								.map(AuthenticationFlowEntity::getAlias).columnName("ALIAS")
								.map(AuthenticationFlowEntity::getDescription).columnName("DESCRIPTION")
								.map(AuthenticationFlowEntity::getProviderId).columnName("PROVIDER_ID").columnSize(UUID_LENGTH).mandatory()
								.map(AuthenticationFlowEntity::isTopLevel).columnName("TOP_LEVEL")
								.map(AuthenticationFlowEntity::isBuiltIn).columnName("BUILT_IN"))
				.mappedBy(AuthenticationFlowEntity::getRealm)
				.reverseJoinColumn("REALM_ID")
				.cascading(RelationMode.ALL_ORPHAN_REMOVAL)
				
				.mapOneToMany(RealmEntity::getComponents,
						entityBuilder(ComponentEntity.class, String.class)
								.onTable("COMPONENT")
                                .withForeignKeyNaming(ForeignKeyNamingStrategy.HIBERNATE_7)
								.mapKey(ComponentEntity::getId, IdentifierPolicy.alreadyAssigned(e -> {}, Objects::nonNull))
								.columnSize(UUID_LENGTH)
								.map(ComponentEntity::getName).columnName("NAME")
								.map(ComponentEntity::getProviderType).columnName("PROVIDER_TYPE")
								.map(ComponentEntity::getProviderId).columnName("PROVIDER_ID").columnSize(UUID_LENGTH)
								.map(ComponentEntity::getParentId).columnName("PARENT_ID").columnSize(UUID_LENGTH)
								.map(ComponentEntity::getSubType).columnName("SUB_TYPE")
								.mapOneToMany(ComponentEntity::getComponentConfigs, entityBuilder(ComponentConfigEntity.class, String.class)
                                        .onTable("COMPONENT_CONFIG")
                                        .withForeignKeyNaming(ForeignKeyNamingStrategy.HIBERNATE_7)
                                        .mapKey(ComponentConfigEntity::getId, IdentifierPolicy.alreadyAssigned(e -> {}, Objects::nonNull)).columnSize(UUID_LENGTH)
                                        .map(ComponentConfigEntity::getName).columnName("NAME").mandatory()
                                        .map(ComponentConfigEntity::getValue).columnName("VALUE"))
                                    .mappedBy(ComponentConfigEntity::getComponent)
                                    .reverseJoinColumn("COMPONENT_ID")
                                    .mandatory())
				.mappedBy(ComponentEntity::getRealm)
				.reverseJoinColumn("REALM_ID")
				.cascading(RelationMode.ALL_ORPHAN_REMOVAL)
				
				// One-to-Many Map relationship for localization texts
				.mapMap(RealmEntity::getRealmLocalizationTexts, String.class, RealmLocalizationTextsEntity.class)
					.onTable("REALM_LOCALIZATIONS")
					.reverseJoinColumn("REALM_ID")
					.keyColumn("LOCALE")
				.withValueMapping(embeddableBuilder(RealmLocalizationTextsEntity.class)
						.map(RealmLocalizationTextsEntity::getTexts).mandatory()
						.readConverter(new NullAwareConverter<String, Map<String, String>>() {
							@Override
							protected Map<String, String> convertNotNull(String databaseValue) {
								return new MapStringConverter().convertToEntityAttribute(databaseValue);
							}
						})
						.writeConverter(new NullAwareConverter<Map<String, String>, String>() {
							@Override
							protected String convertNotNull(Map<String, String> stringStringMap) {
								return new MapStringConverter().convertToDatabaseColumn(stringStringMap);
							}
						})
						.sqlBinder(DefaultParameterBinders.STRING_BINDER)
				)
				;
	}
	
	/**
	 * Initializes the persistence mapping and registers it with the persistence context
	 *
	 * @param persistenceContext the Stalactite persistence context
	 */
	public static void initializePersistence(PersistenceContext persistenceContext) {
		FluentEntityMappingBuilder<RealmEntity, String> mapping = buildRealmEntityMapping();
		mapping.build(persistenceContext);
	}
}

