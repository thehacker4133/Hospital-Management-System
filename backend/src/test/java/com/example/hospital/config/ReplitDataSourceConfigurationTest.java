package com.example.hospital.config;

import com.zaxxer.hikari.HikariConfig;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReplitDataSourceConfigurationTest {
    @Test
    void convertsReplitUriIntoJdbcUrlAndSeparateCredentials() {
        HikariConfig config = ReplitDataSourceConfiguration.toHikariConfig(
                "postgresql://carepoint-user:p%3Ass+word@db.internal:5432/carepoint?sslmode=disable");

        assertThat(config.getJdbcUrl())
                .isEqualTo("jdbc:postgresql://db.internal:5432/carepoint?sslmode=disable");
        assertThat(config.getUsername()).isEqualTo("carepoint-user");
        assertThat(config.getPassword()).isEqualTo("p:ss+word");
    }

    @Test
    void invalidDatabaseUriFailsWithoutEchoingCredentials() {
        assertThatThrownBy(() -> ReplitDataSourceConfiguration.toHikariConfig("mysql://db-user:secret@db.internal/carepoint"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("DATABASE_URL must use PostgreSQL")
                .hasMessageNotContaining("secret");
    }
}
