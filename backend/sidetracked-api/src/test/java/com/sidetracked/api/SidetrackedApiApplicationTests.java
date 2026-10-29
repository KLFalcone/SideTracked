package com.sidetracked.api;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

/*
 * Starts the whole application against the separate
 * sidetracked_test database (see application-test.properties).
 *
 * Needs MySQL running and DB_PASSWORD set, like the app itself.
 */
@SpringBootTest
@ActiveProfiles("test")
class SidetrackedApiApplicationTests {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testsNeverUseTheRealDatabase() throws Exception {

        try (var connection = dataSource.getConnection()) {

            assertThat(connection.getCatalog())
                    .isEqualTo("sidetracked_test");
        }
    }

    @Test
    void contextLoadsAndAllMigrationsApply() {

        Integer pending =
                jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM flyway_schema_history "
                                + "WHERE success = 0",
                        Integer.class
                );

        assertThat(pending).isZero();
    }

    /*
     * A brand-new database (like production) must start with no
     * users and no personal data. Shared curated topics are fine.
     */
    @Test
    void aFreshDatabaseHasNoPersonalData() {

        for (String table : new String[] {
                "users", "players", "quest",
                "user_rabbit_holes", "xp_events", "quest_completions" }) {

            assertThat(count(table))
                    .as("rows in %s", table)
                    .isZero();
        }

        assertThat(count("topics"))
                .as("curated topics")
                .isGreaterThanOrEqualTo(12);
    }

    private int count(String table) {

        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + table,
                Integer.class
        );
    }
}
