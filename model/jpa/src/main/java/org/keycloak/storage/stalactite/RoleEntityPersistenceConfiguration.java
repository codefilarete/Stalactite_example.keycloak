package org.keycloak.storage.stalactite;

import org.codefilarete.stalactite.dsl.MappingEase;
import org.codefilarete.stalactite.dsl.entity.EntityMappingConfigurationProvider;
import org.codefilarete.stalactite.dsl.entity.FluentEntityMappingBuilder;
import org.codefilarete.stalactite.dsl.idpolicy.IdentifierPolicy;
import org.codefilarete.stalactite.dsl.naming.ForeignKeyNamingStrategy;
import org.codefilarete.stalactite.engine.PersistenceContext;
import org.codefilarete.stalactite.sql.ddl.Length;
import org.codefilarete.stalactite.sql.ddl.Size;
import org.keycloak.models.jpa.entities.RoleEntity;

public class RoleEntityPersistenceConfiguration {

    private static final Length UUID_LENGTH = Size.length(36);

    public static FluentEntityMappingBuilder<RoleEntity, String> buildEntityMapping() {
        EntityMappingConfigurationProvider.EntityMappingConfigurationProviderHolder<RoleEntity, String> mappingConfigurationProviderHolder = new EntityMappingConfigurationProvider.EntityMappingConfigurationProviderHolder<>();

        mappingConfigurationProviderHolder.setProvider(MappingEase.entityBuilder(RoleEntity.class, String.class)
                .onTable("KEYCLOAK_ROLE")
                .withForeignKeyNaming(ForeignKeyNamingStrategy.HIBERNATE_7)
                .mapKey(RoleEntity::getId, IdentifierPolicy.alreadyAssigned(o -> {
                }, o -> true)).columnSize(UUID_LENGTH)
                .map(RoleEntity::getName).columnName("NAME")
                .map(RoleEntity::getDescription).columnName("DESCRIPTION")
                .map(RoleEntity::getRealmId).columnName("REALM_ID")
                .map(RoleEntity::isClientRole).columnName("CLIENT_ROLE")
                .map(RoleEntity::getClientId).columnName("CLIENT").columnSize(UUID_LENGTH)
                .map(RoleEntity::getClientRealmConstraint).columnName("CLIENT_REALM_CONSTRAINT").columnSize(UUID_LENGTH)
                .mapManyToMany(RoleEntity::getCompositeRoles, mappingConfigurationProviderHolder)
                        .joinTable("COMPOSITE_ROLE")
                        .sourceJoinColumn("COMPOSITE")
                        .targetJoinColumn("CHILD_ROLE")
                        .reverseCollection(RoleEntity::getParentRoles)
        );
        return mappingConfigurationProviderHolder.getProvider();
    }

    /**
     * Initializes the persistence mapping and registers it with the persistence context
     *
     * @param persistenceContext the Stalactite persistence context
     */
    public static void initializePersistence(PersistenceContext persistenceContext) {
        FluentEntityMappingBuilder<RoleEntity, String> mapping = buildEntityMapping();
        mapping.build(persistenceContext);
    }
}
