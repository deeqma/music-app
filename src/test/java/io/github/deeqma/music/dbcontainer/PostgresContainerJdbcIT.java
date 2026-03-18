package io.github.deeqma.music.dbcontainer;


import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PostgresContainerJdbcIT extends AbstractPostgresContainer {

    @Test
    void testDatabaseIsRunning() throws Exception {
        Connection connection = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(),
                POSTGRES.getUsername(),
                POSTGRES.getPassword()
        );

        Statement statement = connection.createStatement();

        statement.execute("CREATE TABLE demo (id SERIAL PRIMARY KEY, value TEXT)");
        statement.execute("INSERT INTO demo(value) VALUES ('hello-test')");

        ResultSet result = statement.executeQuery("SELECT value FROM demo");

        result.next();
        String fetched = result.getString("value");
        System.out.println(fetched);
        assertEquals("hello-test", fetched);

        connection.close();
    }

}