package org.keycloak.storage.stalactite;

import org.codefilarete.stalactite.engine.EntityCriteria.CriteriaPath;
import org.codefilarete.stalactite.engine.EntityPersister;
import org.codefilarete.stalactite.query.model.Operators;
import org.codefilarete.stalactite.sql.result.Accumulators;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RealmProvider;
import org.keycloak.models.jpa.entities.ComponentEntity;
import org.keycloak.models.jpa.entities.RealmEntity;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.codefilarete.stalactite.query.model.Operators.*;

public class StalactiteRealmProvider implements RealmProvider {

    private final EntityPersister<RealmEntity, String> realmPersister;

    public StalactiteRealmProvider(EntityPersister<RealmEntity, String> realmPersister) {
        this.realmPersister = realmPersister;
    }

    @Override
    public RealmModel getRealm(String id) {
        RealmEntity select = realmPersister.select(id);
        return null;
    }

    @Override
    public RealmModel getRealmByName(String name) {
        RealmEntity foundRealm = realmPersister.selectWhere(RealmEntity::getName, contains(name).ignoringCase())
                .execute(Accumulators.getFirstUnique());
        return null;
    }

    @Override
    public Stream<RealmModel> getRealmsStream() {
        Stream<RealmEntity> result = realmPersister.selectAll().stream();
        return Stream.empty();
    }

    @Override
    public Stream<RealmModel> getRealmsWithProviderTypeStream(Class<?> type) {
        Set<RealmEntity> foundRealms = realmPersister.selectWhere(RealmEntity::getComponents, ComponentEntity::getProviderType, eq(type.getName()))
                .execute(Accumulators.toSet());
        return Stream.empty();
    }

    @Override
    public RealmModel createRealm(String name) {
        return null;
    }

    @Override
    public RealmModel createRealm(String id, String name) {
        return null;
    }

    @Override
    public boolean removeRealm(String id) {
        return false;
    }

    @Override
    public void removeExpiredClientInitialAccess() {

    }

    @Override
    public void saveLocalizationText(RealmModel realm, String locale, String key, String text) {

    }

    @Override
    public void saveLocalizationTexts(RealmModel realm, String locale, Map<String, String> localizationTexts) {

    }

    @Override
    public boolean updateLocalizationText(RealmModel realm, String locale, String key, String text) {
        return false;
    }

    @Override
    public boolean deleteLocalizationTextsByLocale(RealmModel realm, String locale) {
        return false;
    }

    @Override
    public boolean deleteLocalizationText(RealmModel realm, String locale, String key) {
        return false;
    }

    @Override
    public String getLocalizationTextsById(RealmModel realm, String locale, String key) {
        return "";
    }

    @Override
    public void close() {

    }
}
