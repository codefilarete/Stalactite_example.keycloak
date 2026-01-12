# JPA vs Stalactite Mapping Comparison for RealmEntity

This document provides a side-by-side comparison of how RealmEntity is mapped using JPA annotations versus Stalactite ORM configuration.

## Table of Contents
1. [Entity Definition](#entity-definition)
2. [Column Mappings](#column-mappings)
3. [Element Collections](#element-collections)
4. [One-to-Many Relationships](#one-to-many-relationships)
5. [Fetch Strategies](#fetch-strategies)
6. [Cascade Operations](#cascade-operations)
7. [Queries](#queries)

## Entity Definition

### JPA Approach
```java
@Entity
@Table(name="REALM")
@NamedQueries({
    @NamedQuery(name="getAllRealmIds", 
                query="select realm.id from RealmEntity realm")
})
public class RealmEntity {
    // ... fields and methods
}
```

### Stalactite Approach
```java
public class RealmEntityPersistenceConfiguration {
    public static FluentEntityMappingBuilder<RealmEntity, String> 
        buildRealmEntityMapping(PersistenceContext context) {
        
        return entityBuilder(RealmEntity.class, String.class)
            // ... mapping configuration
    }
}
```

**Comparison:**
- JPA: Annotations on entity class (intrusive)
- Stalactite: Separate configuration class (non-intrusive)

## Column Mappings

### Primary Key

#### JPA
```java
@Id
@Column(name="ID", length = 36)
@Access(AccessType.PROPERTY)
protected String id;
```

#### Stalactite
```java
.mapKey(RealmEntity::getId, "ID")
    .identifier(Identifier.ALREADY_ASSIGNED)
    .size(36)
```

### Simple Columns

#### JPA
```java
@Column(name="NAME", unique = true)
protected String name;

@Column(name="ENABLED")
protected boolean enabled;

@Column(name="SSL_REQUIRED")
protected String sslRequired;
```

#### Stalactite
```java
.map(RealmEntity::getName, "NAME")
    .mandatory()
    .unique()
.map(RealmEntity::isEnabled, "ENABLED")
.map(RealmEntity::getSslRequired, "SSL_REQUIRED")
```

**Comparison:**
| Feature | JPA | Stalactite |
|---------|-----|------------|
| Type Safety | No | Yes (method reference) |
| Refactoring | Risky | Safe |
| IDE Support | Limited | Full |
| Readability | Good | Excellent |

## Element Collections

### Simple Set Collection

#### JPA
```java
@ElementCollection
@Column(name="VALUE")
@CollectionTable(name="REALM_EVENTS_LISTENERS", 
                 joinColumns={ @JoinColumn(name="REALM_ID") })
protected Set<String> eventsListeners;
```

#### Stalactite
```java
.mapCollection(RealmEntity::getEventsListeners, String.class)
    .table("REALM_EVENTS_LISTENERS")
    .reverseJoinColumn("REALM_ID")
    .elementColumn("VALUE")
    .cascading()
```

### Map Collection

#### JPA
```java
@ElementCollection
@MapKeyColumn(name="NAME")
@Column(name="VALUE")
@CollectionTable(name="REALM_SMTP_CONFIG", 
                 joinColumns={ @JoinColumn(name="REALM_ID") })
protected Map<String, String> smtpConfig;
```

#### Stalactite
```java
.mapMap(RealmEntity::getSmtpConfig, String.class, String.class)
    .table("REALM_SMTP_CONFIG")
    .reverseJoinColumn("REALM_ID")
    .keyColumn("NAME")
    .valueColumn("VALUE")
    .cascading()
```

**Comparison:**
| Aspect | JPA | Stalactite |
|--------|-----|------------|
| Verbosity | High | Lower |
| Clarity | Moderate | High |
| Configuration | Scattered | Centralized |

## One-to-Many Relationships

### Basic Relationship

#### JPA
```java
@OneToMany(cascade = {CascadeType.REMOVE}, 
           orphanRemoval = true, 
           mappedBy = "realm")
Collection<RequiredCredentialEntity> requiredCredentials = new LinkedList<>();
```

#### Stalactite
```java
.mapOneToMany(RealmEntity::getRequiredCredentials,
              entityBuilder(RequiredCredentialEntity.class, String.class)
                  .mapKey(RequiredCredentialEntity::getId, "ID")
                      .identifier(Identifier.ALREADY_ASSIGNED)
                  .map(RequiredCredentialEntity::getType, "TYPE")
                  .map(RequiredCredentialEntity::isInput, "INPUT")
                  .map(RequiredCredentialEntity::isSecret, "SECRET")
                  .map(RequiredCredentialEntity::getFormLabel, "FORM_LABEL"))
    .mappedBy(RequiredCredentialEntity::getRealm)
    .cascading()
    .orphanRemoval()
```

### Eager Loading Relationship

#### JPA
```java
@OneToMany(cascade = {CascadeType.REMOVE}, 
           orphanRemoval = true, 
           mappedBy = "realm", 
           fetch = FetchType.EAGER)
Collection<RealmAttributeEntity> attributes = new LinkedList<>();
```

#### Stalactite
```java
.mapOneToMany(RealmEntity::getAttributes, 
              entityBuilder(RealmAttributeEntity.class, String.class)
                  .mapKey(RealmAttributeEntity::getId, "ID")
                      .identifier(Identifier.ALREADY_ASSIGNED)
                  .map(RealmAttributeEntity::getName, "NAME")
                  .map(RealmAttributeEntity::getValue, "VALUE"))
    .mappedBy(RealmAttributeEntity::getRealm)
    .cascading()
    .orphanRemoval()
    .fetchSeparately()  // EAGER
```

**Comparison:**
| Feature | JPA | Stalactite |
|---------|-----|------------|
| Nested Mapping | Separate class | Inline or separate |
| Cascade Control | Enum-based | Method-based |
| Fetch Strategy | Limited options | Fine-grained control |

## Fetch Strategies

### JPA Options
```java
// Eager loading
@OneToMany(fetch = FetchType.EAGER)

// Lazy loading (default)
@OneToMany(fetch = FetchType.LAZY)

// Batch fetching
@BatchSize(size = 10)
```

### Stalactite Options
```java
// Eager loading - separate query per parent
.fetchSeparately()

// Lazy loading (default)
// No annotation needed

// Batch fetching
.fetchSeparately().batchSize(10)

// Join fetch
.withJoinFetch()
```

**Comparison:**
| Strategy | JPA | Stalactite | Notes |
|----------|-----|------------|-------|
| Default | Lazy | Lazy | Both lazy by default |
| Eager | `EAGER` | `fetchSeparately()` | Stalactite clearer |
| N+1 Control | `@BatchSize` | `.batchSize(n)` | Similar |
| Join Fetch | JPQL only | `.withJoinFetch()` | Stalactite more flexible |

## Cascade Operations

### JPA Cascading
```java
@OneToMany(cascade = {
    CascadeType.PERSIST,
    CascadeType.MERGE,
    CascadeType.REMOVE,
    CascadeType.REFRESH
})
```

### Stalactite Cascading
```java
.mapOneToMany(...)
    .cascading()  // All operations cascade
    .orphanRemoval()
```

**Operations Comparison:**

| Operation | JPA | Stalactite | Behavior |
|-----------|-----|------------|----------|
| Insert | `PERSIST` | `.cascading()` | Save parent + children |
| Update | `MERGE` | `.cascading()` | Update parent + children |
| Delete | `REMOVE` | `.cascading()` | Delete parent + children |
| Orphan Removal | `orphanRemoval=true` | `.orphanRemoval()` | Auto-delete removed children |

## Queries

### Find by ID

#### JPA
```java
EntityManager em = ...;
RealmEntity realm = em.find(RealmEntity.class, realmId);
```

#### Stalactite
```java
EntityPersister<RealmEntity, String> persister = ...;
RealmEntity realm = persister.select(realmId);
```

### Find by Name (Named Query)

#### JPA
```java
@NamedQuery(
    name="getRealmIdByName", 
    query="select realm.id from RealmEntity realm where realm.name = :name"
)

// Usage
TypedQuery<String> query = em.createNamedQuery("getRealmIdByName", String.class);
query.setParameter("name", "my-realm");
String id = query.getSingleResult();
```

#### Stalactite
```java
List<RealmEntity> realms = persister.selectWhere(
    RealmEntity::getName, "=", "my-realm"
);
RealmEntity realm = realms.isEmpty() ? null : realms.get(0);
```

### Find with Criteria

#### JPA (Criteria API)
```java
CriteriaBuilder cb = em.getCriteriaBuilder();
CriteriaQuery<RealmEntity> cq = cb.createQuery(RealmEntity.class);
Root<RealmEntity> root = cq.from(RealmEntity.class);
cq.select(root).where(cb.equal(root.get("enabled"), true));
List<RealmEntity> results = em.createQuery(cq).getResultList();
```

#### Stalactite
```java
List<RealmEntity> results = persister.selectWhere(
    RealmEntity::isEnabled, "=", true
);
```

**Query Comparison:**

| Feature | JPA | Stalactite | Winner |
|---------|-----|------------|--------|
| Type Safety | Criteria API | Method refs | Stalactite |
| Verbosity | High (Criteria) | Low | Stalactite |
| Readability | Low (Criteria) | High | Stalactite |
| JPQL Option | Yes | No | JPA |
| Flexibility | Very high | Moderate | JPA |
| Learning Curve | Steep | Gentle | Stalactite |

## CRUD Operations Comparison

### CREATE

#### JPA
```java
RealmEntity realm = new RealmEntity();
realm.setId(UUID.randomUUID().toString());
realm.setName("my-realm");
realm.setEnabled(true);

em.persist(realm);
em.flush();  // Optional
```

#### Stalactite
```java
RealmEntity realm = new RealmEntity();
realm.setId(UUID.randomUUID().toString());
realm.setName("my-realm");
realm.setEnabled(true);

persister.insert(realm);
```

### READ

#### JPA
```java
RealmEntity realm = em.find(RealmEntity.class, realmId);
```

#### Stalactite
```java
RealmEntity realm = persister.select(realmId);
```

### UPDATE

#### JPA
```java
RealmEntity realm = em.find(RealmEntity.class, realmId);
realm.setEnabled(false);
em.merge(realm);
```

#### Stalactite
```java
RealmEntity realm = persister.select(realmId);
realm.setEnabled(false);
persister.update(realm);
```

### DELETE

#### JPA
```java
RealmEntity realm = em.find(RealmEntity.class, realmId);
em.remove(realm);
```

#### Stalactite
```java
RealmEntity realm = persister.select(realmId);
persister.delete(realm);
```

**Winner:** Tie - both are equally simple

## Transaction Management

### JPA
```java
EntityTransaction tx = em.getTransaction();
try {
    tx.begin();
    
    // Operations
    em.persist(realm);
    em.merge(otherRealm);
    
    tx.commit();
} catch (Exception e) {
    tx.rollback();
    throw e;
}
```

### Stalactite
```java
persistenceContext.getConnectionProvider().giveConnection().transact(conn -> {
    // Operations
    persister.insert(realm);
    persister.update(otherRealm);
    
    // Auto-commit on success, auto-rollback on exception
});
```

**Winner:** Stalactite (less boilerplate, automatic rollback)

## Performance Characteristics

| Aspect | JPA/Hibernate | Stalactite | Notes |
|--------|---------------|------------|-------|
| Startup Time | Slow | Fast | Hibernate scans entities |
| Memory Usage | Higher | Lower | No proxy overhead |
| Query Performance | Good | Excellent | Direct JDBC mapping |
| Lazy Loading | Proxy-based | Direct | Stalactite simpler |
| N+1 Problem | Common | Less common | Explicit control |
| Caching | First & Second level | Manual | JPA has more options |

## Code Size Comparison

### Full Mapping for 5 Fields

#### JPA (in entity)
```java
@Column(name="NAME", unique = true)
protected String name;

@Column(name="ENABLED")
protected boolean enabled;

@Column(name="SSL_REQUIRED")
protected String sslRequired;

@Column(name="REGISTRATION_ALLOWED")
protected boolean registrationAllowed;

@Column(name = "REG_EMAIL_AS_USERNAME")
protected boolean registrationEmailAsUsername;
```
**Lines:** ~15

#### Stalactite (in config)
```java
.map(RealmEntity::getName, "NAME").mandatory().unique()
.map(RealmEntity::isEnabled, "ENABLED")
.map(RealmEntity::getSslRequired, "SSL_REQUIRED")
.map(RealmEntity::isRegistrationAllowed, "REGISTRATION_ALLOWED")
.map(RealmEntity::isRegistrationEmailAsUsername, "REG_EMAIL_AS_USERNAME")
```
**Lines:** 5

**Winner:** Stalactite (3x more concise)

## Summary: When to Use Each

### Use JPA/Hibernate when:
- ✅ You need extensive caching strategies
- ✅ Complex JPQL queries are required
- ✅ Team is already familiar with JPA
- ✅ Using existing JPA ecosystem/tools
- ✅ Need lazy loading with proxies

### Use Stalactite when:
- ✅ You want compile-time type safety
- ✅ Performance is critical
- ✅ You prefer code over annotations
- ✅ Need fine-grained control
- ✅ Want simpler debugging
- ✅ Prefer lightweight solutions

## Migration Effort

To migrate from JPA to Stalactite:

1. **Keep Entity Classes** - No changes needed ⭐
2. **Create Mapping Configs** - New files (~1 per entity)
3. **Replace Repository Layer** - Change from EntityManager to Persister
4. **Update Queries** - Convert JPQL to Stalactite criteria
5. **Test Thoroughly** - Verify all operations

**Estimated effort:** 2-5 days per 10 entities

## Conclusion

Both JPA and Stalactite are excellent ORMs with different philosophies:

- **JPA**: Mature, feature-rich, annotation-based, widely adopted
- **Stalactite**: Modern, type-safe, code-first, lightweight

The choice depends on your project's specific needs, team expertise, and preferences for configuration style.

