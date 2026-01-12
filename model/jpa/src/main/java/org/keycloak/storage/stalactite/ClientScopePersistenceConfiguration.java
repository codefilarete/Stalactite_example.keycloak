package org.keycloak.storage.stalactite;

import java.util.Objects;

import org.codefilarete.stalactite.dsl.MappingEase;
import org.codefilarete.stalactite.dsl.entity.FluentEntityMappingBuilder;
import org.codefilarete.stalactite.dsl.idpolicy.IdentifierPolicy;
import org.codefilarete.stalactite.engine.PersistenceContext;
import org.codefilarete.stalactite.sql.ddl.Length;
import org.codefilarete.stalactite.sql.ddl.Size;
import org.keycloak.models.jpa.entities.ClientScopeAttributeEntity;
import org.keycloak.models.jpa.entities.ClientScopeEntity;
import org.keycloak.models.jpa.entities.ProtocolMapperEntity;

import static org.codefilarete.stalactite.dsl.MappingEase.embeddableBuilder;

public class ClientScopePersistenceConfiguration {
	
	private static final Length UUID_LENGTH = Size.length(36);
	
	public static FluentEntityMappingBuilder<ClientScopeEntity, String> buildEntityMapping() {
		
		FluentEntityMappingBuilder<ClientScopeEntity, String> result = MappingEase.entityBuilder(ClientScopeEntity.class, String.class)
				.onTable("CLIENT_SCOPE")
				.mapKey(ClientScopeEntity::getId, IdentifierPolicy.alreadyAssigned(O -> {}, Objects::isNull)).columnSize(UUID_LENGTH)
				.map(ClientScopeEntity::getName)
				.map(ClientScopeEntity::getDescription)
				.mapOneToMany(ClientScopeEntity::getProtocolMappers, ProtocolMapperPersistenceConfiguration.buildEntityMapping())
					// We don't map ProtocolMapperEntity::getClient as a reverse relation because it's not in the ClientScopeEntity aggregate
					.mappedBy(ProtocolMapperEntity::getClientScope)
					.reverseJoinColumn("CLIENT_SCOPE_ID")
				.map(ClientScopeEntity::getRealmId).columnName("REALM_ID").columnSize(UUID_LENGTH)
				.map(ClientScopeEntity::getProtocol)
				.mapCollection(ClientScopeEntity::getAttributes, ClientScopeAttributeEntity.class, embeddableBuilder(ClientScopeAttributeEntity.class)
						.map(ClientScopeAttributeEntity::getName).mandatory()
						.map(ClientScopeAttributeEntity::getValue).columnSize(Size.length(2048)
					))
					.onTable("CLIENT_SCOPE_ATTRIBUTES")
					.reverseJoinColumn("SCOPE_ID")
				.mapCollection(ClientScopeEntity::getScopeMappingIds, String.class)
					.elementColumnName("ROLE_ID")
					.elementColumnSize(UUID_LENGTH)
					.onTable("CLIENT_SCOPE_ROLE_MAPPING")
					.reverseJoinColumn("SCOPE_ID")
				;
		
		return result;
	}
	
	/**
	 * Initializes the persistence mapping and registers it with the persistence context
	 *
	 * @param persistenceContext the Stalactite persistence context
	 */
	public static void initializePersistence(PersistenceContext persistenceContext) {
		FluentEntityMappingBuilder<ClientScopeEntity, String> mapping = buildEntityMapping();
		mapping.build(persistenceContext);
	}
}
