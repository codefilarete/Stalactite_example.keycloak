package org.keycloak.storage.stalactite;

import java.util.Objects;

import org.codefilarete.stalactite.dsl.entity.FluentEntityMappingBuilder;
import org.codefilarete.stalactite.dsl.idpolicy.IdentifierPolicy;
import org.codefilarete.stalactite.sql.ddl.Length;
import org.codefilarete.stalactite.sql.ddl.Size;
import org.keycloak.models.jpa.entities.ProtocolMapperEntity;

import static org.codefilarete.stalactite.dsl.MappingEase.entityBuilder;

public class ProtocolMapperEntityPersistenceConfiguration {
	
	private static final Length UUID_LENGTH = Size.length(36);
	
	public static FluentEntityMappingBuilder<ProtocolMapperEntity, String> buildEntityMapping() {
		return entityBuilder(ProtocolMapperEntity.class, String.class)
				.onTable("PROTOCOL_MAPPER")
				.mapKey(ProtocolMapperEntity::getId, IdentifierPolicy.alreadyAssigned(o -> {}, Objects::isNull)).columnSize(UUID_LENGTH)
				.map(ProtocolMapperEntity::getName).mandatory()
				.map(ProtocolMapperEntity::getProtocol).mandatory()
				.map(ProtocolMapperEntity::getProtocolMapper).columnName("PROTOCOL_MAPPER_NAME").mandatory()
				.mapMap(ProtocolMapperEntity::getConfig, String.class, String.class)
					.keyColumn("NAME")
					.valueColumn("VALUE")
					.onTable("PROTOCOL_MAPPER_CONFIG")
					.reverseJoinColumn("PROTOCOL_MAPPER_ID");
	}
}
