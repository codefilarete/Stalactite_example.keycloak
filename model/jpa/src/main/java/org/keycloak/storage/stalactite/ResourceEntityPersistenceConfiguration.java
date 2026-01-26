package org.keycloak.storage.stalactite;

import org.codefilarete.stalactite.dsl.MappingEase;
import org.codefilarete.stalactite.dsl.entity.FluentEntityMappingBuilder;
import org.codefilarete.stalactite.dsl.idpolicy.IdentifierPolicy;
import org.codefilarete.stalactite.dsl.naming.ForeignKeyNamingStrategy;
import org.codefilarete.stalactite.engine.PersistenceContext;
import org.codefilarete.stalactite.sql.ddl.Length;
import org.codefilarete.stalactite.sql.ddl.Size;
import org.keycloak.authorization.jpa.entities.ResourceAttributeEntity;
import org.keycloak.authorization.jpa.entities.ResourceEntity;
import org.keycloak.authorization.jpa.entities.ResourceServerEntity;
import org.keycloak.authorization.jpa.entities.ScopeEntity;

public class ResourceEntityPersistenceConfiguration {

    private static final Length UUID_LENGTH = Size.length(36);

    public static FluentEntityMappingBuilder<ResourceEntity, String> buildEntityMapping() {
        FluentEntityMappingBuilder<ResourceEntity, String> result = MappingEase.entityBuilder(ResourceEntity.class, String.class)
                .onTable("RESOURCE_SERVER_RESOURCE")
                .withForeignKeyNaming(ForeignKeyNamingStrategy.HIBERNATE_7)
                .mapKey(ResourceEntity::getId, IdentifierPolicy.alreadyAssigned(o -> {}, o -> true)).columnSize(UUID_LENGTH)
                .map(ResourceEntity::getName).columnName("NAME").mandatory()
                .map(ResourceEntity::getDisplayName).columnName("DISPLAY_NAME")
                .mapCollection(ResourceEntity::getUris, String.class)
                    .onTable("RESOURCE_URIS")
                    .reverseJoinColumn("RESOURCE_ID")
                    .elementColumnName("VALUE")
                .map(ResourceEntity::getType).columnName("TYPE")
                .map(ResourceEntity::getIconUri).columnName("ICON_URI")
                .map(ResourceEntity::getOwner).mandatory().columnName("OWNER")
                .map(ResourceEntity::isOwnerManagedAccess).columnName("OWNER_MANAGED_ACCESS")
                .map(ResourceEntity::getResourceServer).columnName("RESOURCE_SERVER_ID").mandatory().columnSize(UUID_LENGTH)
                .mapOneToMany(ResourceEntity::getScopes, MappingEase.entityBuilder(ScopeEntity.class, String.class)
                        .onTable("RESOURCE_SERVER_SCOPE")
                        .mapKey(ScopeEntity::getId, IdentifierPolicy.alreadyAssigned(o -> {}, o -> true)).columnSize(UUID_LENGTH)
                        .map(ScopeEntity::getName).columnName("NAME").mandatory()
                        .map(ScopeEntity::getDisplayName).columnName("DISPLAY_NAME")
                        .map(ScopeEntity::getIconUri).columnName("ICON_URI")
                        .mapManyToOne(ScopeEntity::getResourceServer, MappingEase.entityBuilder(ResourceServerEntity.class, String.class)
                                .onTable("RESOURCE_SERVER")
                                .mapKey(ResourceServerEntity::getId, IdentifierPolicy.alreadyAssigned(o -> {}, o -> true)).columnSize(UUID_LENGTH)
                                .map(ResourceServerEntity::isAllowRemoteResourceManagement).columnName("ALLOW_RS_REMOTE_MGMT").mandatory()
                                .map(ResourceServerEntity::getPolicyEnforcementMode).columnName("POLICY_ENFORCE_MODE").mandatory()
                                .map(ResourceServerEntity::getDecisionStrategy).columnName("DECISION_STRATEGY").mandatory())
                        .columnName("RESOURCE_SERVER_ID")
                )
                .joinTable("RESOURCE_SCOPE")
                    .sourceJoinColumn("RESOURCE_ID")
                    .targetJoinColumn("SCOPE_ID")
                .mapOneToMany(ResourceEntity::getAttributes, MappingEase.entityBuilder(ResourceAttributeEntity.class, String.class)
                        .onTable("RESOURCE_ATTRIBUTE")
                        .mapKey(ResourceAttributeEntity::getId, IdentifierPolicy.alreadyAssigned(o -> {}, o -> true)).columnSize(UUID_LENGTH)
                        .map(ResourceAttributeEntity::getName).columnName("NAME").mandatory()
                        .map(ResourceAttributeEntity::getValue).columnName("VALUE")
                )
                    .mappedBy(ResourceAttributeEntity::getResource)
                    .reverseJoinColumn("RESOURCE_ID")
                ;

        return result;
    }

    /**
     * Initializes the persistence mapping and registers it with the persistence context
     *
     * @param persistenceContext the Stalactite persistence context
     */
    public static void initializePersistence(PersistenceContext persistenceContext) {
        FluentEntityMappingBuilder<ResourceEntity, String> mapping = buildEntityMapping();
        mapping.build(persistenceContext);
    }
}
