# Stalactite ORM Persistence Mapping for RealmEntity

## Overview

This directory contains the Stalactite ORM persistence configuration for `RealmEntity`. Stalactite ORM is a lightweight, code-first Java ORM that provides a fluent API for mapping Java objects to database tables.

## Files

- **RealmEntityPersistenceConfiguration.java**: Complete persistence mapping configuration for `RealmEntity`

## Prerequisites

To use this Stalactite mapping, you need to add the following dependencies to your project's `pom.xml`:

```xml
<dependencies>
    <!-- Stalactite Core -->
    <dependency>
        <groupId>org.codefilarete.stalactite</groupId>
        <artifactId>core</artifactId>
        <version>2.1.0</version>
    </dependency>
    
    <!-- Stalactite Engine -->
    <dependency>
        <groupId>org.codefilarete.stalactite</groupId>
        <artifactId>engine</artifactId>
        <version>2.1.0</version>
    </dependency>
    
    <!-- Stalactite SQL -->
    <dependency>
        <groupId>org.codefilarete.stalactite</groupId>
        <artifactId>sql</artifactId>
        <version>2.1.0</version>
    </dependency>
</dependencies>
```

## Features

The `RealmEntityPersistenceConfiguration` provides a comprehensive mapping that includes:

### 1. **Primary Key Mapping**
- Maps `id` field to `ID` column (VARCHAR(36))
- Configured with `ALREADY_ASSIGNED` identifier strategy (UUID assigned before persistence)

### 2. **Simple Column Mappings**
All realm configuration fields including:
- Basic settings (enabled, name, SSL requirements)
- Registration and authentication settings
- OTP policy configuration
- Session timeout settings
- Token lifespan configuration
- Theme configuration
- Event configuration
- Internationalization settings

### 3. **Element Collections**
Four element collections for simple value sets:
- **Event Listeners** → `REALM_EVENTS_LISTENERS` table
- **Enabled Event Types** → `REALM_ENABLED_EVENT_TYPES` table
- **Supported Locales** → `REALM_SUPPORTED_LOCALES` table
- **Default Group IDs** → `REALM_DEFAULT_GROUPS` table

### 4. **Element Collection Map**
- **SMTP Configuration** → `REALM_SMTP_CONFIG` table (key-value pairs)

### 5. **One-to-Many Relationships**
The mapping handles all complex relationships with proper cascading and orphan removal:

| Relationship | Target Entity | Cascade | Orphan Removal | Fetch Strategy |
|--------------|---------------|---------|----------------|----------------|
| attributes | RealmAttributeEntity | Yes | Yes | Eager (fetchSeparately) |
| requiredCredentials | RequiredCredentialEntity | Yes | Yes | Lazy |
| userFederationProviders | UserFederationProviderEntity | Yes | Yes | Lazy |
| userFederationMappers | UserFederationMapperEntity | Yes | Yes | Lazy |
| authenticatorConfigs | AuthenticatorConfigEntity | Yes | Yes | Lazy |
| requiredActionProviders | RequiredActionProviderEntity | Yes | Yes | Lazy |
| authenticationFlows | AuthenticationFlowEntity | Yes | Yes | Lazy |
| components | ComponentEntity | Yes | Yes | Lazy |
| realmLocalizationTexts | RealmLocalizationTextsEntity (Map) | Yes | Yes | Lazy |

## Usage Example

### Initialize Persistence Context

```java
import org.codefilarete.stalactite.engine.PersistenceContext;
import org.codefilarete.stalactite.sql.ConnectionProvider;
import org.codefilarete.stalactite.sql.Dialect;
import org.codefilarete.stalactite.sql.dialect.PostgreSQLDialect;

// Create a connection provider (example with PostgreSQL)
ConnectionProvider connectionProvider = new ConnectionProvider(dataSource);

// Create persistence context with dialect
Dialect dialect = new PostgreSQLDialect();
PersistenceContext persistenceContext = new PersistenceContext(connectionProvider, dialect);

// Initialize the RealmEntity mapping
RealmEntityPersistenceConfiguration.initializePersistence(persistenceContext);
```

### CRUD Operations

```java
import org.codefilarete.stalactite.engine.EntityPersister;

// Get the persister for RealmEntity
EntityPersister<RealmEntity, String> realmPersister = 
    persistenceContext.getPersister(RealmEntity.class);

// CREATE - Insert a new realm
RealmEntity newRealm = new RealmEntity();
newRealm.setId("realm-id-123");
newRealm.setName("MyRealm");
newRealm.setEnabled(true);
realmPersister.insert(newRealm);

// READ - Find by ID
RealmEntity realm = realmPersister.select(newRealm.getId());

// UPDATE - Update an existing realm
realm.setEnabled(false);
realmPersister.update(realm);

// DELETE - Remove a realm
realmPersister.delete(realm);

// QUERY - Find with criteria
List<RealmEntity> enabledRealms = realmPersister.selectWhere(
    RealmEntity::isEnabled, "=", true
);
```

### Working with Collections

```java
// Add event listeners
RealmEntity realm = realmPersister.select("realm-id-123");
realm.getEventsListeners().add("jboss-logging");
realm.getEventsListeners().add("email");

// Add SMTP configuration
realm.getSmtpConfig().put("host", "smtp.example.com");
realm.getSmtpConfig().put("port", "587");

// Persist changes (will cascade to collections)
realmPersister.update(realm);
```

### Working with Relationships

```java
// Add realm attributes
RealmAttributeEntity attribute = new RealmAttributeEntity();
attribute.setId("attr-id-123");
attribute.setName("displayName");
attribute.setValue("My Awesome Realm");
attribute.setRealm(realm);

realm.getAttributes().add(attribute);

// Add authentication flow
AuthenticationFlowEntity flow = new AuthenticationFlowEntity();
flow.setId("flow-id-123");
flow.setAlias("browser");
flow.setDescription("Browser based authentication");
flow.setProviderId("basic-flow");
flow.setTopLevel(true);
flow.setBuiltIn(true);
flow.setRealm(realm);

realm.getAuthenticationFlows().add(flow);

// Update will cascade to all relationships
realmPersister.update(realm);
```

## Mapping Details

### Cascade Behavior
All relationships are configured with:
- **Cascading**: Changes to parent (RealmEntity) cascade to children
- **Orphan Removal**: Removing a child from collection automatically deletes it from database

### Fetch Strategy
- **Eager Loading**: Only `attributes` collection (matches JPA mapping)
- **Lazy Loading**: All other relationships load on-demand

### Identifier Strategy
Uses `Identifier.ALREADY_ASSIGNED` for all entities, meaning:
- IDs must be assigned before calling `insert()`
- Typically UUIDs generated by application code
- Matches existing Keycloak JPA behavior

## Comparison with JPA

| Feature | JPA Annotation | Stalactite Equivalent |
|---------|----------------|----------------------|
| Table name | `@Table(name="REALM")` | `.table("REALM")` |
| Primary key | `@Id` + `@Column` | `.mapKey(...)` |
| Column mapping | `@Column(name="...")` | `.map(getter, "COLUMN")` |
| One-to-Many | `@OneToMany(mappedBy=...)` | `.mapOneToMany(...).mappedBy(...)` |
| Element Collection | `@ElementCollection` | `.mapCollection(...)` |
| Cascade | `cascade = {CascadeType.REMOVE}` | `.cascading()` |
| Orphan removal | `orphanRemoval = true` | `.orphanRemoval()` |
| Fetch type | `fetch = FetchType.EAGER` | `.fetchSeparately()` |

## Database Schema

The mapping creates/uses the following database tables:

### Main Table
- `REALM` - Main realm entity table

### Collection Tables
- `REALM_EVENTS_LISTENERS` - Event listener configurations
- `REALM_ENABLED_EVENT_TYPES` - Enabled event types
- `REALM_SUPPORTED_LOCALES` - Internationalization locales
- `REALM_DEFAULT_GROUPS` - Default group IDs
- `REALM_SMTP_CONFIG` - SMTP configuration map

### Related Entity Tables
- `REALM_ATTRIBUTE` - Custom realm attributes
- `REQUIRED_CREDENTIAL` - Required credential configurations
- `USER_FEDERATION_PROVIDER` - User federation provider configurations
- `USER_FEDERATION_MAPPER` - User federation mapper configurations
- `AUTHENTICATOR_CONFIG` - Authenticator configurations
- `REQUIRED_ACTION_PROVIDER` - Required action provider configurations
- `AUTHENTICATION_FLOW` - Authentication flow definitions
- `COMPONENT` - Component configurations
- `REALM_LOCALIZATIONS` - Localization texts by locale

## Notes

1. **Not a Drop-in Replacement**: This is a reference implementation showing how to map RealmEntity using Stalactite ORM. The actual Keycloak codebase uses JPA/Hibernate.

2. **Related Entities**: The nested `entityBuilder()` calls in the one-to-many mappings are simplified. In production, each related entity should have its own complete persistence configuration file.

3. **Transactions**: Stalactite requires explicit transaction management. Wrap CRUD operations in transactions:
   ```java
   persistenceContext.getConnectionProvider().giveConnection().transact(connection -> {
       // Perform operations
       realmPersister.insert(realm);
   });
   ```

4. **Connection Pooling**: In production, use a proper connection pool (HikariCP, Apache DBCP, etc.) with the ConnectionProvider.

5. **Dialect Selection**: Choose the appropriate SQL dialect for your database:
   - PostgreSQL: `PostgreSQLDialect`
   - MySQL: `MySQLDialect`
   - Oracle: `OracleDialect`
   - H2: `H2Dialect`
   - SQL Server: `SQLServerDialect`

## Benefits of Stalactite ORM

1. **Type-safe**: Compile-time checking of mappings using method references
2. **Code-first**: Define schema through code, not annotations
3. **Fluent API**: Readable, chainable configuration
4. **Lightweight**: Minimal overhead compared to JPA/Hibernate
5. **Flexible**: Easy to customize behavior at any level
6. **Performance**: Fine-grained control over fetch strategies and queries

## Further Reading

- [Stalactite ORM Documentation](https://github.com/codefilarete/stalactite)
- [Stalactite Examples](https://github.com/codefilarete/stalactite/tree/master/examples)
- [Fluent API Reference](https://javadoc.io/doc/org.codefilarete/stalactite-core/latest/index.html)

## License

This code follows the same Apache License 2.0 as the Keycloak project.

