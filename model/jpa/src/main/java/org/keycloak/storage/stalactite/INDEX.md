# Stalactite ORM Persistence Mapping for Keycloak RealmEntity

## 📋 Overview

This directory contains a complete Stalactite ORM persistence mapping for Keycloak's `RealmEntity`, demonstrating an alternative to the traditional JPA/Hibernate approach.

## 📁 Files in This Directory

| File | Description |
|------|-------------|
| **RealmEntityPersistenceConfiguration.java** | Main persistence mapping configuration |
| **RealmEntityStalactiteExample.java** | Comprehensive usage examples (15+ scenarios) |
| **README.md** | Detailed documentation and API reference |
| **QUICKSTART.md** | Quick start guide for getting up and running |
| **COMPARISON.md** | Side-by-side comparison of JPA vs Stalactite |
| **pom-example.xml** | Maven dependencies and build configuration |
| **INDEX.md** | This file - directory overview and navigation |

## 🚀 Quick Start

1. **Add Dependencies** - See `pom-example.xml`
2. **Initialize Mapping** - See `QUICKSTART.md` 
3. **Run Examples** - See `RealmEntityStalactiteExample.java`
4. **Compare with JPA** - See `COMPARISON.md`

## 📖 Documentation

### For Beginners
Start here if you're new to Stalactite ORM:
1. Read **QUICKSTART.md** - Get started in 5 minutes
2. Review **RealmEntityStalactiteExample.java** - See practical examples
3. Read **README.md** - Learn about features and capabilities

### For JPA Users
Already familiar with JPA? Start here:
1. Read **COMPARISON.md** - See how Stalactite differs from JPA
2. Review **RealmEntityPersistenceConfiguration.java** - See mapping structure
3. Check **README.md** - Understand migration considerations

### For Advanced Users
Looking for deep details?
1. Review **RealmEntityPersistenceConfiguration.java** - Complete mapping
2. Read **README.md** - Full API documentation
3. Check **RealmEntityStalactiteExample.java** - Advanced patterns

## 🎯 What You'll Learn

### Core Concepts
- ✅ Fluent API for entity mapping
- ✅ Type-safe configuration using method references
- ✅ Element collections and relationship mapping
- ✅ Cascade operations and orphan removal
- ✅ Fetch strategies and lazy loading
- ✅ Transaction management
- ✅ CRUD operations

### RealmEntity Specific
- ✅ Mapping 30+ RealmEntity fields
- ✅ 4 element collections (events, locales, groups)
- ✅ 1 element collection map (SMTP config)
- ✅ 9 one-to-many relationships
- ✅ Eager and lazy loading strategies
- ✅ Complete cascade configuration

## 📊 Feature Mapping Coverage

### ✅ Mapped Features

| Category | Fields | Collections | Relationships |
|----------|--------|-------------|---------------|
| **Core Config** | 15 | - | - |
| **OTP Policy** | 6 | - | - |
| **Token/Session** | 10 | - | - |
| **Themes** | 4 | - | - |
| **Events** | 4 | 2 | - |
| **I18n** | 2 | 1 | 1 |
| **Auth Flows** | 6 | - | 1 |
| **Collections** | - | 4 | - |
| **Relationships** | - | - | 9 |
| **Total** | **47** | **7** | **11** |

### Database Schema

**Tables Created/Mapped:**
- 1 main table (REALM)
- 5 collection tables
- 9+ related entity tables

## 🔧 Key Technologies

- **Stalactite ORM** 2.1.0
- **Java** 11+
- **Jakarta Persistence API** 3.1.0
- **PostgreSQL/MySQL/H2** (dialect support)

## 💡 Use Cases

### 1. Learning Stalactite ORM
Perfect for developers wanting to learn Stalactite with a real-world example.

### 2. Evaluating ORM Options
Compare JPA/Hibernate vs Stalactite for your project needs.

### 3. Migration Reference
Reference implementation for migrating from JPA to Stalactite.

### 4. Code-First Design
Example of code-first ORM configuration vs annotation-based.

### 5. Performance Optimization
Learn how Stalactite provides fine-grained control over queries and fetching.

## 📈 Complexity Levels

### Basic (Start Here)
1. Simple CRUD operations
2. Basic queries (find by ID, by name)
3. Single field updates

**See:** QUICKSTART.md sections 1-4

### Intermediate
1. Working with collections
2. Managing relationships
3. Transaction handling
4. Batch operations

**See:** RealmEntityStalactiteExample.java examples 1-8

### Advanced
1. Custom fetch strategies
2. Complex cascade configurations
3. Performance optimization
4. Multi-entity transactions

**See:** README.md sections on relationships and fetch strategies

## 🎓 Learning Path

### Day 1: Basics
- [ ] Read QUICKSTART.md
- [ ] Review examples 1-5 in RealmEntityStalactiteExample.java
- [ ] Try creating and reading a realm

### Day 2: Collections & Relationships
- [ ] Review README.md sections 3-5
- [ ] Study examples 6-11 in RealmEntityStalactiteExample.java
- [ ] Try adding attributes and flows

### Day 3: Advanced Topics
- [ ] Read COMPARISON.md
- [ ] Review RealmEntityPersistenceConfiguration.java
- [ ] Experiment with transactions and batch operations

### Day 4: Deep Dive
- [ ] Study complete mapping configuration
- [ ] Compare with JPA RealmEntity
- [ ] Consider migration strategy

## 🔍 Code Examples Quick Reference

### Initialize Persistence
```java
PersistenceContext ctx = new PersistenceContext(connectionProvider, dialect);
RealmEntityPersistenceConfiguration.initializePersistence(ctx);
EntityPersister<RealmEntity, String> persister = ctx.getPersister(RealmEntity.class);
```

### Create Realm
```java
RealmEntity realm = new RealmEntity();
realm.setId(UUID.randomUUID().toString());
realm.setName("my-realm");
realm.setEnabled(true);
persister.insert(realm);
```

### Query Realm
```java
List<RealmEntity> realms = persister.selectWhere(
    RealmEntity::getName, "=", "my-realm"
);
```

### Update Realm
```java
RealmEntity realm = persister.select(realmId);
realm.setEnabled(false);
persister.update(realm);
```

### Delete Realm
```java
RealmEntity realm = persister.select(realmId);
persister.delete(realm);
```

**For more examples:** See RealmEntityStalactiteExample.java (15+ complete examples)

## 🆚 JPA vs Stalactite

| Aspect | JPA | Stalactite | Winner |
|--------|-----|------------|--------|
| Type Safety | Runtime | Compile-time | ⭐ Stalactite |
| Configuration | Annotations | Fluent API | Preference |
| Verbosity | Higher | Lower | ⭐ Stalactite |
| Learning Curve | Steep | Gentle | ⭐ Stalactite |
| Performance | Good | Better | ⭐ Stalactite |
| Maturity | Very High | Moderate | ⭐ JPA |
| Ecosystem | Large | Growing | ⭐ JPA |
| Query Power | Very High | Moderate | ⭐ JPA |
| Debugging | Complex | Simple | ⭐ Stalactite |
| Size | Heavy | Light | ⭐ Stalactite |

**Detailed comparison:** See COMPARISON.md

## 🛠️ Prerequisites

### Required
- Java 11 or higher
- Maven 3.6+
- Understanding of SQL and databases
- Basic ORM concepts

### Recommended
- Familiarity with JPA/Hibernate
- Experience with PostgreSQL or MySQL
- Understanding of Keycloak architecture

## 🎯 Benefits of Stalactite

### Developer Experience
- ✅ **Type Safety**: Compile-time checking with method references
- ✅ **Readability**: Clear, fluent API
- ✅ **Refactoring**: IDE support for renames and changes
- ✅ **Debugging**: Simpler stack traces

### Performance
- ✅ **Faster Startup**: No bytecode generation
- ✅ **Lower Memory**: No proxy overhead
- ✅ **Direct Queries**: No HQL parsing
- ✅ **Fine Control**: Explicit fetch strategies

### Maintenance
- ✅ **Code Over Annotations**: Easier to version control
- ✅ **Centralized Config**: All mappings in one place
- ✅ **Less Magic**: Explicit behavior
- ✅ **Testable**: Easy to unit test mappings

## 🚧 Limitations

### Compared to JPA
- ❌ Smaller ecosystem
- ❌ Less mature
- ❌ Fewer query options
- ❌ No second-level cache
- ❌ Limited documentation

### Mitigation
- ✅ Growing community
- ✅ Active development
- ✅ This comprehensive example
- ✅ Can implement custom caching
- ✅ Clean, readable code

## 📝 Notes

1. **Not Production Ready**: This is a reference implementation for learning purposes
2. **Keycloak Uses JPA**: The actual Keycloak codebase uses JPA/Hibernate
3. **Related Entities**: Simplified mappings for related entities (full mappings would be separate files)
4. **Testing Required**: Thorough testing needed before production use
5. **Dependencies**: Stalactite not included in Keycloak's current dependencies

## 🔗 External Resources

- [Stalactite GitHub](https://github.com/codefilarete/stalactite)
- [Stalactite JavaDoc](https://javadoc.io/doc/org.codefilarete/stalactite-core)
- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [RealmEntity Source](../entities/RealmEntity.java)

## 📞 Getting Help

### Questions About Mapping
1. Check README.md for detailed documentation
2. Review COMPARISON.md for JPA differences
3. Study RealmEntityStalactiteExample.java for patterns

### Questions About Stalactite
1. Visit [Stalactite GitHub](https://github.com/codefilarete/stalactite)
2. Check [JavaDoc](https://javadoc.io/doc/org.codefilarete/stalactite-core)
3. Open an issue on GitHub

### Questions About Keycloak
1. Visit [Keycloak Community](https://www.keycloak.org/community)
2. Check [Keycloak Docs](https://www.keycloak.org/documentation)
3. Ask on dev mailing list

## 📄 License

Apache License 2.0 (same as Keycloak)

## 🎉 Ready to Start?

1. **Absolute Beginner?** → Start with **QUICKSTART.md**
2. **JPA User?** → Start with **COMPARISON.md**
3. **Want Examples?** → Start with **RealmEntityStalactiteExample.java**
4. **Need Details?** → Start with **README.md**

---

**Happy Coding! 🚀**

