# Stalactite ORM Quick Start Guide

## What is Stalactite ORM?

Stalactite is a lightweight, type-safe ORM for Java that provides:
- **Fluent API** for mapping configuration
- **Compile-time safety** using method references
- **Code-first approach** without XML or heavy annotations
- **Fine-grained control** over queries and fetching
- **Performance** similar to JDBC with ORM convenience

## Quick Start

### 1. Add Dependencies

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>org.codefilarete.stalactite</groupId>
    <artifactId>stalactite-core</artifactId>
    <version>2.1.0</version>
</dependency>
<dependency>
    <groupId>org.codefilarete.stalactite</groupId>
    <artifactId>stalactite-engine</artifactId>
    <version>2.1.0</version>
</dependency>
```

### 2. Setup Database Connection

```java
// Create DataSource (example with HikariCP)
HikariConfig config = new HikariConfig();
config.setJdbcUrl("jdbc:postgresql://localhost:5432/keycloak");
config.setUsername("keycloak");
config.setPassword("password");
HikariDataSource dataSource = new HikariDataSource(config);

// Create Stalactite components
ConnectionProvider connectionProvider = new ConnectionProvider(dataSource);
Dialect dialect = new PostgreSQLDialect();
PersistenceContext persistenceContext = new PersistenceContext(connectionProvider, dialect);
```

### 3. Initialize RealmEntity Mapping

```java
// Initialize the mapping
RealmEntityPersistenceConfiguration.initializePersistence(persistenceContext);

// Get persister
EntityPersister<RealmEntity, String> persister = 
    persistenceContext.getPersister(RealmEntity.class);
```

### 4. Perform CRUD Operations

```java
// CREATE
RealmEntity realm = new RealmEntity();
realm.setId(UUID.randomUUID().toString());
realm.setName("my-realm");
realm.setEnabled(true);
persister.insert(realm);

// READ
RealmEntity found = persister.select(realm.getId());

// UPDATE
found.setEnabled(false);
persister.update(found);

// DELETE
persister.delete(found);
```

## Common Operations

### Find by Criteria

```java
// Find by name
List<RealmEntity> realms = persister.selectWhere(
    RealmEntity::getName, "=", "my-realm"
);

// Find enabled realms
List<RealmEntity> enabled = persister.selectWhere(
    RealmEntity::isEnabled, "=", true
);
```

### Working with Collections

```java
RealmEntity realm = persister.select(realmId);

// Add to element collection
realm.getEventsListeners().add("jboss-logging");

// Add to map collection
realm.getSmtpConfig().put("host", "smtp.gmail.com");

// Update (cascades to collections)
persister.update(realm);
```

### Working with Relationships

```java
RealmEntity realm = persister.select(realmId);

// Add child entity
RealmAttributeEntity attr = new RealmAttributeEntity();
attr.setId(UUID.randomUUID().toString());
attr.setName("theme");
attr.setValue("custom");
attr.setRealm(realm);

realm.getAttributes().add(attr);

// Update (cascades to relationships with orphan removal)
persister.update(realm);
```

### Transactions

```java
persistenceContext.getConnectionProvider().giveConnection().transact(conn -> {
    // All operations here are in one transaction
    RealmEntity realm = createRealm();
    persister.insert(realm);
    
    configureRealm(realm);
    persister.update(realm);
    
    // Auto-commit on success, auto-rollback on exception
});
```

## Comparison: JPA vs Stalactite

### JPA Entity (Annotation-based)

```java
@Entity
@Table(name = "REALM")
public class RealmEntity {
    @Id
    @Column(name = "ID")
    private String id;
    
    @Column(name = "NAME")
    private String name;
    
    @OneToMany(mappedBy = "realm", cascade = CascadeType.ALL)
    private Collection<RealmAttributeEntity> attributes;
}
```

### Stalactite Mapping (Code-based)

```java
entityBuilder(RealmEntity.class, String.class)
    .mapKey(RealmEntity::getId, "ID")
    .map(RealmEntity::getName, "NAME")
    .mapOneToMany(RealmEntity::getAttributes, ...)
        .mappedBy(RealmAttributeEntity::getRealm)
        .cascading();
```

## Key Differences

| Feature | JPA/Hibernate | Stalactite |
|---------|---------------|------------|
| Configuration | Annotations | Fluent API |
| Type Safety | Runtime | Compile-time |
| Learning Curve | Steep | Gentle |
| Performance | Good | Better |
| Control | Abstract | Fine-grained |
| Size | Large | Lightweight |

## Benefits for Keycloak

1. **Type Safety**: Method references catch errors at compile time
2. **Clarity**: Mapping is explicit and visible in code
3. **Performance**: No proxy overhead, lazy loading when needed
4. **Flexibility**: Easy to customize query strategies
5. **Debugging**: Simpler to understand and debug

## Database Support

Stalactite supports multiple databases:

```java
// PostgreSQL
Dialect dialect = new PostgreSQLDialect();

// MySQL
Dialect dialect = new MySQLDialect();

// Oracle
Dialect dialect = new OracleDialect();

// SQL Server
Dialect dialect = new SQLServerDialect();

// H2
Dialect dialect = new H2Dialect();
```

## Production Considerations

### Connection Pooling

Always use a connection pool in production:

```java
HikariConfig config = new HikariConfig();
config.setJdbcUrl("jdbc:postgresql://localhost:5432/keycloak");
config.setUsername("keycloak");
config.setPassword("password");
config.setMaximumPoolSize(10);
config.setMinimumIdle(2);
config.setConnectionTimeout(30000);
config.setIdleTimeout(600000);
config.setMaxLifetime(1800000);

HikariDataSource dataSource = new HikariDataSource(config);
```

### Transaction Management

For complex operations, use transactions:

```java
try {
    persistenceContext.getConnectionProvider().giveConnection().transact(conn -> {
        // Multiple operations
        persister.insert(realm);
        persister.update(otherRealm);
    });
} catch (Exception e) {
    log.error("Transaction failed", e);
    // Handle error
}
```

### Error Handling

```java
try {
    persister.insert(realm);
} catch (DuplicateKeyException e) {
    // Handle duplicate realm name
} catch (SQLException e) {
    // Handle database errors
}
```

## Testing

### H2 In-Memory Database

```java
@Before
public void setup() {
    DataSource dataSource = new JdbcDataSource();
    ((JdbcDataSource) dataSource).setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
    
    ConnectionProvider connectionProvider = new ConnectionProvider(dataSource);
    Dialect dialect = new H2Dialect();
    PersistenceContext persistenceContext = 
        new PersistenceContext(connectionProvider, dialect);
    
    RealmEntityPersistenceConfiguration.initializePersistence(persistenceContext);
    persister = persistenceContext.getPersister(RealmEntity.class);
}

@Test
public void testCreateRealm() {
    RealmEntity realm = new RealmEntity();
    realm.setId(UUID.randomUUID().toString());
    realm.setName("test-realm");
    realm.setEnabled(true);
    
    persister.insert(realm);
    
    RealmEntity found = persister.select(realm.getId());
    assertNotNull(found);
    assertEquals("test-realm", found.getName());
}
```

## Migration from JPA

If migrating from JPA to Stalactite:

1. **Keep Entity Classes**: No need to change `@Entity` classes
2. **Create Mappings**: Add Stalactite configuration classes
3. **Replace EntityManager**: Use `EntityPersister` instead
4. **Update Queries**: Replace JPQL with Stalactite criteria
5. **Test Thoroughly**: Verify all operations work correctly

## Resources

- **Stalactite GitHub**: https://github.com/codefilarete/stalactite
- **JavaDoc**: https://javadoc.io/doc/org.codefilarete/stalactite-core
- **Examples**: See `RealmEntityStalactiteExample.java` in this directory

## Support

For questions about this mapping:
1. Check the README.md in this directory
2. Review the example code
3. Consult Stalactite documentation
4. Ask on Keycloak dev mailing list

## License

Apache License 2.0 (same as Keycloak)

