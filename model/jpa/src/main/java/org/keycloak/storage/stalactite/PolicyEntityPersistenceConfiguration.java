package org.keycloak.storage.stalactite;

import org.codefilarete.stalactite.dsl.entity.EntityMappingConfigurationProvider.EntityMappingConfigurationProviderHolder;
import org.codefilarete.stalactite.dsl.entity.FluentEntityMappingBuilder;
import org.codefilarete.stalactite.dsl.idpolicy.IdentifierPolicy;
import org.codefilarete.stalactite.dsl.naming.ForeignKeyNamingStrategy;
import org.codefilarete.stalactite.engine.PersistenceContext;
import org.codefilarete.stalactite.sql.ddl.Length;
import org.codefilarete.stalactite.sql.ddl.Size;
import org.keycloak.authorization.jpa.entities.PolicyEntity;
import org.keycloak.authorization.jpa.entities.ResourceEntity;
import org.keycloak.authorization.jpa.entities.ScopeEntity;

import java.util.Objects;

import static org.codefilarete.stalactite.dsl.MappingEase.entityBuilder;

public class PolicyEntityPersistenceConfiguration {

    private static final Length UUID_LENGTH = Size.length(36);

    public static FluentEntityMappingBuilder<PolicyEntity, String> buildEntityMapping() {
        EntityMappingConfigurationProviderHolder<PolicyEntity, String> result = new EntityMappingConfigurationProviderHolder<>();
        result.setProvider(entityBuilder(PolicyEntity.class, String.class)
                .onTable("RESOURCE_SERVER_POLICY")
                .withForeignKeyNaming(ForeignKeyNamingStrategy.HIBERNATE_7)
                .mapKey(PolicyEntity::getId, IdentifierPolicy.alreadyAssigned(o -> {}, Objects::isNull)).columnName("ID").columnSize(UUID_LENGTH)
                .map(PolicyEntity::getName).columnName("NAME").mandatory()
                .map(PolicyEntity::getDescription).columnName("DESCRIPTION")
                .map(PolicyEntity::getType).columnName("TYPE").mandatory()
                .mapEnum(PolicyEntity::getDecisionStrategy).columnName("DECISION_STRATEGY")
                .mapEnum(PolicyEntity::getLogic).columnName("LOGIC")
                .map(PolicyEntity::getOwner).columnName("OWNER")
                .mapMap(PolicyEntity::getConfig, String.class, String.class)
                    .onTable("POLICY_CONFIG")
                    .reverseJoinColumn("POLICY_ID")
                    .keyColumn("NAME")
                    .valueColumn("VALUE")
                .mapManyToOne(PolicyEntity::getResourceServer, ResourceServerEntityPersistenceConfiguration.buildEntityMapping())
                    .columnName("RESOURCE_SERVER_ID")
                    .mandatory()
                .mapOneToMany(PolicyEntity::getAssociatedPolicies, result)
                    .joinTable("ASSOCIATED_POLICY")
                    .sourceJoinColumn("POLICY_ID")
                    .targetJoinColumn("ASSOCIATED_POLICY_ID")
                // for simplicity, we don't map again the ResourceEntity class (or make a reference to it)
                .mapOneToMany(PolicyEntity::getResources, entityBuilder(ResourceEntity.class, String.class)
                        .mapKey(ResourceEntity::getId, IdentifierPolicy.alreadyAssigned(o -> {}, Objects::isNull)).columnSize(UUID_LENGTH)
                        .onTable("RESOURCE_SERVER_RESOURCE"))
                    .joinTable("RESOURCE_POLICY")
                    .sourceJoinColumn("POLICY_ID")
                    .targetJoinColumn("RESOURCE_ID")
                // for simplicity, we don't map again the ScopeEntity class (or make a reference to it)
                .mapOneToMany(PolicyEntity::getScopes, entityBuilder(ScopeEntity.class, String.class)
                        .mapKey(ScopeEntity::getId, IdentifierPolicy.alreadyAssigned(o -> {}, Objects::isNull)).columnSize(UUID_LENGTH)
                        .onTable("RESOURCE_SERVER_SCOPE"))
                    .joinTable("SCOPE_POLICY")
                    .sourceJoinColumn("POLICY_ID")
                    .targetJoinColumn("SCOPE_ID")
        );
        return result.getProvider();
    }


    /**
     * Initializes the persistence mapping and registers it with the persistence context
     *
     * @param persistenceContext the Stalactite persistence context
     */
    public static void initializePersistence(PersistenceContext persistenceContext) {
        FluentEntityMappingBuilder<PolicyEntity, String> mapping = buildEntityMapping();
        mapping.build(persistenceContext);
    }
}
