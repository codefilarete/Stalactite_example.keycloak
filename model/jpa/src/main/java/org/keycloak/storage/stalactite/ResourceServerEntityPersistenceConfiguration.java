package org.keycloak.storage.stalactite;

import org.codefilarete.stalactite.dsl.MappingEase;
import org.codefilarete.stalactite.dsl.entity.FluentEntityMappingBuilder;
import org.codefilarete.stalactite.dsl.idpolicy.IdentifierPolicy;
import org.codefilarete.stalactite.dsl.naming.ForeignKeyNamingStrategy;
import org.codefilarete.stalactite.sql.ddl.Length;
import org.codefilarete.stalactite.sql.ddl.Size;
import org.keycloak.authorization.jpa.entities.ResourceServerEntity;

public class ResourceServerEntityPersistenceConfiguration {

    private static final Length UUID_LENGTH = Size.length(36);

    public static FluentEntityMappingBuilder<ResourceServerEntity, String> buildEntityMapping() {
        return MappingEase.entityBuilder(ResourceServerEntity.class, String.class)
                .onTable("RESOURCE_SERVER")
                .withForeignKeyNaming(ForeignKeyNamingStrategy.HIBERNATE_7)
                .mapKey(ResourceServerEntity::getId, IdentifierPolicy.alreadyAssigned(o -> {}, o -> true)).columnSize(UUID_LENGTH)
                .map(ResourceServerEntity::isAllowRemoteResourceManagement).columnName("ALLOW_RS_REMOTE_MGMT").mandatory()
                .map(ResourceServerEntity::getPolicyEnforcementMode).columnName("POLICY_ENFORCE_MODE").mandatory()
                .map(ResourceServerEntity::getDecisionStrategy).columnName("DECISION_STRATEGY").mandatory();
    }
}
