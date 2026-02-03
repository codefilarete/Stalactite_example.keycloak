package org.keycloak.storage.stalactite;

import org.codefilarete.stalactite.engine.EntityPersister;
import org.codefilarete.stalactite.engine.ExecutableProjection;
import org.codefilarete.stalactite.sql.result.Accumulators;
import org.codefilarete.tool.collection.Iterables;
import org.keycloak.models.ClientModel;
import org.keycloak.models.ClientProvider;
import org.keycloak.models.ClientScopeModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.jpa.entities.ClientEntity;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.codefilarete.stalactite.query.model.Operators.count;
import static org.codefilarete.stalactite.query.model.Operators.eq;

public class StalactiteClientProvider implements ClientProvider {

    private final EntityPersister<ClientEntity, String> clientPersister;

    public StalactiteClientProvider(EntityPersister<ClientEntity, String> clientPersister) {
        this.clientPersister = clientPersister;
    }

    @Override
    public Stream<ClientModel> getClientsStream(RealmModel realm, Integer firstResult, Integer maxResults) {
        Stream<ClientEntity> result = clientPersister.selectWhere(ClientEntity::getRealmId, eq(realm.getId()))
                .limit(firstResult, maxResults)
                .execute(Accumulators.toSet()).stream();
        return Stream.empty();
    }

    @Override
    public long getClientsCount(RealmModel realm) {
        ExecutableProjection.ProjectionDataProvider queryResult = clientPersister.selectProjectionWhere(selectAdapter -> {
                    selectAdapter.add(count(selectAdapter.giveColumn(ClientEntity::getId)), "count");
                }, ClientEntity::getRealmId, eq(realm.getId()))
                .execute(Accumulators.getFirst());
        return queryResult.getValue("count", long.class);
    }

    @Override
    public Stream<ClientModel> getAlwaysDisplayInConsoleClientsStream(RealmModel realm) {
        // we mimic the "getAlwaysDisplayInConsoleClients" named query
        // "select client.id from ClientEntity client where client.alwaysDisplayInConsole = true and client.realmId = :realm order by client.clientId"
        Set<ClientEntity> queryResult = clientPersister.selectWhere(ClientEntity::getRealmId, eq(realm.getId()))
                .and(ClientEntity::isAlwaysDisplayInConsole, eq(true))
                .orderBy(ClientEntity::getClientId)
                .execute(Accumulators.toSet());
        return Stream.empty();
    }

    @Override
    public Map<ClientModel, Set<String>> getAllRedirectUrisOfEnabledClients(RealmModel realm) {
        // we mimic the "getAllRedirectUrisOfEnabledClients" named query
        // "select new map(client as client, r as redirectUri) from ClientEntity client join client.redirectUris r where client.realmId = :realm and client.enabled = true"
        // => I don't understand why the JpaRealmProvider.getAllRedirectUrisOfEnabledClients(..) is so complex: getting
        // ClientEntities with their redirect URIs seems pretty trivial, even in JPA: using a query that maps
        // ClientEntity to redirect URIs seems useless
        Set<ClientEntity> queryResult = clientPersister.selectWhere(ClientEntity::getRealmId, eq(realm.getId()))
                .and(ClientEntity::isEnabled, eq(true))
                .execute(Accumulators.toSet());
        Map<ClientEntity, Set<String>> result = Iterables.map(queryResult, Function.identity(), ClientEntity::getRedirectUris);
        return Map.of();
    }

    @Override
    public ClientModel addClient(RealmModel realm, String id, String clientId) {
        return null;
    }

    @Override
    public boolean removeClient(RealmModel realm, String id) {
        return false;
    }

    @Override
    public void removeClients(RealmModel realm) {

    }

    @Override
    public void addClientScopes(RealmModel realm, ClientModel client, Set<ClientScopeModel> clientScopes, boolean defaultScope) {

    }

    @Override
    public void removeClientScope(RealmModel realm, ClientModel client, ClientScopeModel clientScope) {

    }

    @Override
    public void addClientScopeToAllClients(RealmModel realm, ClientScopeModel clientScope, boolean defaultClientScope) {

    }

    @Override
    public void close() {

    }

    @Override
    public ClientModel getClientById(RealmModel realm, String id) {
        return null;
    }

    @Override
    public ClientModel getClientByClientId(RealmModel realm, String clientId) {
        return null;
    }

    @Override
    public Stream<ClientModel> searchClientsByClientIdStream(RealmModel realm, String clientId, Integer firstResult, Integer maxResults) {
        return Stream.empty();
    }

    @Override
    public Stream<ClientModel> searchClientsByAttributes(RealmModel realm, Map<String, String> attributes, Integer firstResult, Integer maxResults) {
        return Stream.empty();
    }

    @Override
    public Map<String, ClientScopeModel> getClientScopes(RealmModel realm, ClientModel client, boolean defaultScopes) {
        return Map.of();
    }
}
