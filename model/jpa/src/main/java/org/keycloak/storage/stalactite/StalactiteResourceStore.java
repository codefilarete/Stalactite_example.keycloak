package org.keycloak.storage.stalactite;

import jakarta.persistence.criteria.Expression;
import org.codefilarete.stalactite.engine.EntityCriteria.CriteriaPath;
import org.codefilarete.stalactite.engine.EntityPersister;
import org.codefilarete.stalactite.engine.EntityPersister.ExecutableProjectionQuery;
import org.codefilarete.stalactite.query.model.Operators;
import org.codefilarete.stalactite.sql.result.Accumulators;
import org.keycloak.authorization.jpa.entities.ResourceEntity;
import org.keycloak.authorization.jpa.entities.ScopeEntity;
import org.keycloak.authorization.model.Resource;
import org.keycloak.authorization.model.ResourceServer;
import org.keycloak.authorization.model.Scope;
import org.keycloak.authorization.store.ResourceStore;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static org.codefilarete.stalactite.query.model.Operators.contains;
import static org.codefilarete.stalactite.query.model.Operators.eq;
import static org.codefilarete.stalactite.query.model.Operators.in;
import static org.codefilarete.stalactite.query.model.Operators.isNull;

public class StalactiteResourceStore implements ResourceStore {

    private final EntityPersister<ResourceEntity, String> resourcePersister;

    public StalactiteResourceStore(EntityPersister<ResourceEntity, String> resourcePersister) {
        this.resourcePersister = resourcePersister;
    }


    @Override
    public void findByOwner(ResourceServer resourceServer, String ownerId, Consumer<Resource> consumer) {
        // we mimic JPAResourceStore.findByOwner(..)
        findByOwnerFilter(ownerId, resourceServer, consumer, -1, -1);
    }

    private void findByOwnerFilter(String ownerId, ResourceServer resourceServer, Consumer<Resource> consumer, int firstResult, int maxResult) {
        // we mimic JPAResourceStore.findByOwnerFilter(..)
        EntityPersister.ExecutableEntityQuery<ResourceEntity, ?> query = resourcePersister.selectWhere(ResourceEntity::getOwner, eq(ownerId));

        boolean pagination = firstResult > -1 && maxResult > -1;
        if (pagination) {
            query.orderBy(ResourceEntity::getId);
        }

        if (resourceServer == null) {
            query.orderBy(ResourceEntity::getId);
        }

        if (resourceServer != null) {
            query.and(ResourceEntity::getResourceServer, eq(resourceServer.getId()));
        }

        if (pagination) {
            query.limit(firstResult, maxResult);
        }

        Set<ResourceEntity> result = query.execute(Accumulators.toSet());
        // the next step would be to transform the result into Resources and make them consume by the given consumer in the method argument
    }

    @Override
    public Resource findById(ResourceServer resourceServer, String id) {
        if (id == null) {
            return null;
        }

        ResourceEntity entity = resourcePersister.select(id);
        return null;
    }

    @Override
    public List<Resource> findByResourceServer(ResourceServer resourceServer) {
        Set<ResourceEntity> result = resourcePersister.selectWhere(ResourceEntity::getResourceServer, resourceServer == null ? isNull() : eq(resourceServer.getId()))
                .execute(Accumulators.toSet());

        return null;
    }

    @Override
    public List<Resource> find(ResourceServer resourceServer, Map<Resource.FilterOption, String[]> attributes, Integer firstResult, Integer maxResults) {
        ExecutableProjectionQuery<ResourceEntity, ?> query = resourcePersister.selectProjectionWhere(selectAdapter -> {
            selectAdapter.add(new CriteriaPath<>(ResourceEntity::getId));
        });

        if (resourceServer != null) {
            query.and(ResourceEntity::getResourceServer, eq(resourceServer.getId()));
        }

        attributes.forEach((filterOption, value) -> {
            switch (filterOption) {
                case ID:
                    query.and(ResourceEntity::getId, in(value));
                    break;
                case OWNER:
                    query.and(ResourceEntity::getName, in(value));
                    break;
                case SCOPE_ID:
                    query.and(new CriteriaPath<>(ResourceEntity::getScopes, ScopeEntity::getId), in(value));
                    break;
                case OWNER_MANAGED_ACCESS:
                    query.and(ResourceEntity::isOwnerManagedAccess, eq(Boolean.valueOf(value[0])));
                    break;
                case URI:
                    query.and(ResourceEntity::getUris, in(value[0]).ignoringCase());
                    break;
                case URI_NOT_NULL:
                    query.and(ResourceEntity::getUris, Operators.<String>isNotNull());
                    break;
                    // predicates.add(builder.isNotEmpty(root.get("uris"))); looks like there is a bug in hibernate and this line doesn't work: https://hibernate.atlassian.net/browse/HHH-6686
                    // Workaround
//                    Expression<Integer> urisSize = builder.size(root.get("uris"));
//                    predicates.add(builder.notEqual(urisSize, 0));
//                    break;
                case NAME:
                    query.and(ResourceEntity::getName, contains(value[0]).ignoringCase());
                    break;
                case TYPE:
                    query.and(ResourceEntity::getType, contains(value[0]).ignoringCase());
                    break;
                case EXACT_NAME:
                    query.and(ResourceEntity::getName, contains(value[0]).ignoringCase());
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported filter [" + filterOption + "]");
            }
        });

        return List.of();
    }

    @Override
    public Resource create(ResourceServer resourceServer, String id, String name, String owner) {
        return null;
    }

    @Override
    public void delete(String id) {

    }

    @Override
    public void findByScopes(ResourceServer resourceServer, Set<Scope> scopes, Consumer<Resource> consumer) {

    }

    @Override
    public Resource findByName(ResourceServer resourceServer, String name, String ownerId) {
        return null;
    }

    @Override
    public void findByType(ResourceServer resourceServer, String type, Consumer<Resource> consumer) {

    }

    @Override
    public void findByType(ResourceServer resourceServer, String type, String owner, Consumer<Resource> consumer) {

    }

    @Override
    public void findByTypeInstance(ResourceServer resourceServer, String type, Consumer<Resource> consumer) {

    }
}
