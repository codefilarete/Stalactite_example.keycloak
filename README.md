This is a [Stalactite](https://github.com/codefilarete/Stalactite) example for [Keycloak](https://github.com/keycloak/).

The goal is to demonstrate how to use Stalactite with a concrete example.
Keycloak is a huge project which is a perfect candidate for this: it contains [a lot of entities](model/jpa/src/main/java/org/keycloak/models/jpa/entities) and relations, as well as JPA repositories and named queries.

This will also be a good compliance test between JPA (Hibernate here) and Stalactite, and eventually reveal some usage differences.

There are several goals here:
1. generating the same database schema with Stalactite
2. mapping the same (named) queries with Stalactite
3. having some equivalent Stalactite Spring Data repositories than the existing ones

Maybe not all the entities will be mapped.
Don't expect a fully operational Keycloak with Stalactite here.
No performance benchmark will be done here.

Work will take place in the [JPA Model module](model/jpa) and in particular in [its test directory](model/jpa/src/test/java/org/keycloak/storage).

Stalactite 3, still under development, will be used.
Keycloak 26.4 is used as a reference because, at that time, the official branch doesn't change so much.

Note that a full project build (simple `clean install`, skipping tests) is required to generate some Keycloak classes. Then, newly added test classes can be run.