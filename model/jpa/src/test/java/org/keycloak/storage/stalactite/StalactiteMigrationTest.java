package org.keycloak.storage.stalactite;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Set;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.codefilarete.jumper.schema.DefaultSchemaElementCollector;
import org.codefilarete.jumper.schema.DefaultSchemaElementCollector.Schema;
import org.codefilarete.jumper.schema.PostgreSQLSchemaElementCollector;
import org.codefilarete.jumper.schema.difference.PostgreSQLSchemaDiffer;
import org.codefilarete.stalactite.sql.ddl.DDLDeployer;
import org.codefilarete.tool.collection.CaseInsensitiveSet;
import org.junit.BeforeClass;
import org.junit.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class StalactiteMigrationTest {
	
	private static final String LIQUIBASE_SCHEMA_NAME = "liquibase";
	private static final String STALACTITE_SCHEMA_NAME = "stalactite";
	private static final String LIQUIBASE_CHANGELOG_PATH = "META-INF/jpa-changelog-master.xml";
	private static JdbcDatabaseContainer POSTGRESQL_CONTAINER;
	
	@BeforeClass
	public static void startContainers() {
		// Fix for TestContainers 1.21.3 which is not compatible with local Docker environment
		// The best fix would be to upgrade TestContainers to the 1.21.4 but we're stuck on the old one due to the Keycloak dependency onto quarkus-bom
		// which manages it. Though we fix it with this short line of code.
		// see https://github.com/testcontainers/testcontainers-java/issues/11212
		System.setProperty("api.version", "1.44");
		System.out.println("Starting containers");
		POSTGRESQL_CONTAINER = buildContainer();
		POSTGRESQL_CONTAINER.start();
	}
	
	private static JdbcDatabaseContainer buildContainer() {
		JdbcDatabaseContainer result = new PostgreSQLContainer<>(DockerImageName.parse("postgres:14.11"))
				.withReuse(true);
		result
				.withConnectTimeoutSeconds(20)
				.setPortBindings(Arrays.asList(5432 + ":" + 5432));
		return result;
	}
	
	@Test
	public void compareSchemas() throws LiquibaseException, SQLException {
		Connection liquibaseConnection = dataSource(LIQUIBASE_SCHEMA_NAME).getConnection();
		
		Liquibase liquibase = new Liquibase(
				LIQUIBASE_CHANGELOG_PATH,
				new ClassLoaderResourceAccessor(),
				new JdbcConnection(liquibaseConnection));
		liquibase.update();
		
		DefaultSchemaElementCollector liquibaseSchemaElementCollector = new PostgreSQLSchemaElementCollector(liquibaseConnection.getMetaData());
		liquibaseSchemaElementCollector
				.withSchema(LIQUIBASE_SCHEMA_NAME)
				.withTableNamePattern("%");
		
		PGSimpleDataSource stalactiteDataSource = dataSource(STALACTITE_SCHEMA_NAME);
		deployStalactiteSchema(stalactiteDataSource);
		Connection stalactiteConnection = stalactiteDataSource.getConnection();
		
		DefaultSchemaElementCollector stalactiteSchemaElementCollector = new PostgreSQLSchemaElementCollector(stalactiteConnection.getMetaData());
		stalactiteSchemaElementCollector
				.withSchema(STALACTITE_SCHEMA_NAME)
				.withTableNamePattern("%");
		
		PostgreSQLSchemaDiffer schemaDiffer = new PostgreSQLSchemaDiffer();
		Schema liquibaseSchema = liquibaseSchemaElementCollector.collect();
		// we remove particular tables
		Set<String> unmanagedTables = new CaseInsensitiveSet(
				// liquibase changelog management
				"databasechangelog", "databasechangeloglock",
				// this table appeared recently in Keycloak without JPA entity, but still used in code with native access
				"jgroups_ping");
		liquibaseSchema.getTables().removeIf(table -> unmanagedTables.contains(table.getName()));
		Schema stalactiteSchema = stalactiteSchemaElementCollector.collect();
		schemaDiffer.compareAndPrint(liquibaseSchema, stalactiteSchema);
	}
	
	private static PGSimpleDataSource dataSource(String schemaName) {
		JdbcDatabaseContainer jdbcDatabaseContainer = POSTGRESQL_CONTAINER;
		PGSimpleDataSource pgDataSource = new PGSimpleDataSource();
		pgDataSource.setUrl(jdbcDatabaseContainer.getJdbcUrl());
		pgDataSource.setCurrentSchema(schemaName);
		pgDataSource.setUser(jdbcDatabaseContainer.getUsername());
		pgDataSource.setPassword(jdbcDatabaseContainer.getPassword());
		
		try (Connection connection = pgDataSource.getConnection()) {
			PreparedStatement preparedStatement = connection.prepareStatement("create schema if not exists " + schemaName);
			preparedStatement.execute();
			preparedStatement.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return pgDataSource;
	}
	
	public void deployStalactiteSchema(DataSource stalactiteDataSource) throws SQLException {
		Connection stalactiteConnection = stalactiteDataSource.getConnection();
		
		RealmEntityStalactiteExample realmEntityStalactiteExample = new RealmEntityStalactiteExample(stalactiteDataSource);
		
		// Because Stalactite sets the connection in a transaction mode, we must disable it to make DDLDeployer commit the schema changes
		// (PostgreSQL is a rare database that make schema changes transactional)
		realmEntityStalactiteExample.getPersistenceContext().getConnectionProvider().giveConnection().setAutoCommit(true);
		DDLDeployer ddlDeployer = new DDLDeployer(realmEntityStalactiteExample.getPersistenceContext());
		ddlDeployer.getCreationScripts().forEach(System.out::println);
		ddlDeployer.deployDDL();
	}
}
