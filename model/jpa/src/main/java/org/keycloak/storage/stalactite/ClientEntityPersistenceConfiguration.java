package org.keycloak.storage.stalactite;

import java.util.Objects;

import org.codefilarete.stalactite.dsl.entity.FluentEntityMappingBuilder;
import org.codefilarete.stalactite.dsl.idpolicy.IdentifierPolicy;
import org.codefilarete.stalactite.engine.PersistenceContext;
import org.codefilarete.stalactite.sql.ddl.Length;
import org.codefilarete.stalactite.sql.ddl.Size;
import org.keycloak.models.jpa.entities.ClientAttributeEntity;
import org.keycloak.models.jpa.entities.ClientEntity;
import org.keycloak.models.jpa.entities.ProtocolMapperEntity;

import static org.codefilarete.stalactite.dsl.MappingEase.embeddableBuilder;
import static org.codefilarete.stalactite.dsl.MappingEase.entityBuilder;

public class ClientEntityPersistenceConfiguration {
	
	private static final Length UUID_LENGTH = Size.length(36);
	
	public static FluentEntityMappingBuilder<ClientEntity, String> buildEntityMapping() {
		
		FluentEntityMappingBuilder<ClientEntity, String> result = entityBuilder(ClientEntity.class, String.class)
				.mapKey(ClientEntity::getId, IdentifierPolicy.<ClientEntity, String>alreadyAssigned(o -> {}, Objects::isNull)).columnSize(UUID_LENGTH)
				.onTable("CLIENT")
				.map(ClientEntity::getName)
				.map(ClientEntity::getDescription)
				.map(ClientEntity::getClientId).columnName("CLIENT_ID")
				.map(ClientEntity::isEnabled)
				.map(ClientEntity::isAlwaysDisplayInConsole).columnName("ALWAYS_DISPLAY_IN_CONSOLE")
				.map(ClientEntity::getSecret)
				.map(ClientEntity::setRegistrationToken).columnName("REGISTRATION_TOKEN")
				.map(ClientEntity::getClientAuthenticatorType).columnName("CLIENT_AUTHENTICATOR_TYPE")
				.map(ClientEntity::getNotBefore).columnName("NOT_BEFORE").nullable()
				.map(ClientEntity::isPublicClient).columnName("PUBLIC_CLIENT")
				.map(ClientEntity::getProtocol)
				.map(ClientEntity::isFrontchannelLogout).columnName("FRONTCHANNEL_LOGOUT")
				.map(ClientEntity::isFullScopeAllowed).columnName("FULL_SCOPE_ALLOWED")
				.map(ClientEntity::getRealmId).columnName("REALM_ID").columnSize(UUID_LENGTH)
				.mapCollection(ClientEntity::getWebOrigins, String.class)
					.onTable("WEB_ORIGINS")
					.elementColumnName("VALUE")
					.reverseJoinColumn("CLIENT_ID")
				.mapCollection(ClientEntity::getRedirectUris, String.class)
					.onTable("REDIRECT_URIS")
					.elementColumnName("VALUE")
					.reverseJoinColumn("CLIENT_ID")
				.mapCollection(ClientEntity::getAttributes, ClientAttributeEntity.class, embeddableBuilder(ClientAttributeEntity.class)
						.map(ClientAttributeEntity::getName).mandatory()
						.map(ClientAttributeEntity::getValue))
					.onTable("CLIENT_ATTRIBUTES")
					.reverseJoinColumn("CLIENT_ID")
				.mapMap(ClientEntity::getAuthFlowBindings, String.class, String.class)
					.onTable("CLIENT_AUTH_FLOW_BINDINGS")
					.keyColumn("BINDING_NAME")
					.valueColumn("FLOW_ID").valueSize(UUID_LENGTH)
					.reverseJoinColumn("CLIENT_ID")
				.mapOneToMany(ClientEntity::getProtocolMappers, ProtocolMapperPersistenceConfiguration.buildEntityMapping())
					// We don't map ProtocolMapperEntity::getClientScope as a reverse relation because it's not in the ClientEntity aggregate
					.mappedBy(ProtocolMapperEntity::getClient)
					.reverseJoinColumn("CLIENT_ID")
				.map(ClientEntity::isSurrogateAuthRequired).columnName("SURROGATE_AUTH_REQUIRED")
				.map(ClientEntity::getRootUrl).columnName("ROOT_URL")
				.map(ClientEntity::getBaseUrl).columnName("BASE_URL")
				.map(ClientEntity::getManagementUrl).columnName("MANAGEMENT_URL")
				.map(ClientEntity::isBearerOnly).columnName("BEARER_ONLY")
				.map(ClientEntity::isConsentRequired).columnName("CONSENT_REQUIRED")
				.map(ClientEntity::isStandardFlowEnabled).columnName("STANDARD_FLOW_ENABLED")
				.map(ClientEntity::isImplicitFlowEnabled).columnName("IMPLICIT_FLOW_ENABLED")
				.map(ClientEntity::isDirectAccessGrantsEnabled).columnName("DIRECT_ACCESS_GRANTS_ENABLED")
				.map(ClientEntity::isServiceAccountsEnabled).columnName("SERVICE_ACCOUNTS_ENABLED")
				.map(ClientEntity::getNodeReRegistrationTimeout).columnName("NODE_REREG_TIMEOUT").nullable()
				.mapCollection(ClientEntity::getScopeMappingIds, String.class)
					.onTable("SCOPE_MAPPING")
					.elementColumnName("ROLE_ID").elementColumnSize(UUID_LENGTH)
					.reverseJoinColumn("CLIENT_ID")
				.mapMap(ClientEntity::getRegisteredNodes, String.class, Integer.class)
					.onTable("CLIENT_NODE_REGISTRATIONS")
					.reverseJoinColumn("CLIENT_ID")
					.keyColumn("NAME")
					.valueColumn("VALUE")
				;
		
		return result;
	}
	
	/**
	 * Initializes the persistence mapping and registers it with the persistence context
	 *
	 * @param persistenceContext the Stalactite persistence context
	 */
	public static void initializePersistence(PersistenceContext persistenceContext) {
		FluentEntityMappingBuilder<ClientEntity, String> mapping = buildEntityMapping();
		mapping.build(persistenceContext);
	}
}
