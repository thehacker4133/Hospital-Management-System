package com.example.hospital.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Configuration
@Profile("replit")
public class ReplitDataSourceConfiguration {
    @Bean
    public DataSource dataSource(@Value("${DATABASE_URL}") String databaseUrl) {
        return new HikariDataSource(toHikariConfig(databaseUrl));
    }

    static HikariConfig toHikariConfig(String databaseUrl) {
        final URI uri;
        try {
            uri = URI.create(databaseUrl);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("DATABASE_URL must be a valid PostgreSQL URI");
        }

        String scheme = uri.getScheme();
        if (!"postgresql".equals(scheme) && !"postgres".equals(scheme)) {
            throw new IllegalArgumentException("DATABASE_URL must use PostgreSQL");
        }
        if (uri.getHost() == null || uri.getRawPath() == null || uri.getRawPath().length() <= 1
                || uri.getRawUserInfo() == null || uri.getRawFragment() != null) {
            throw new IllegalArgumentException("DATABASE_URL must include a PostgreSQL host, database, and user");
        }

        String[] credentials = uri.getRawUserInfo().split(":", 2);
        String username = decodeUriPart(credentials[0]);
        String password = credentials.length == 2 ? decodeUriPart(credentials[1]) : "";
        String host = uri.getHost();
        if (host.contains(":") && !host.startsWith("[")) host = "[" + host + "]";

        StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://").append(host);
        if (uri.getPort() >= 0) jdbcUrl.append(':').append(uri.getPort());
        jdbcUrl.append(uri.getRawPath());
        if (uri.getRawQuery() != null) jdbcUrl.append('?').append(uri.getRawQuery());

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl.toString());
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");
        return config;
    }

    private static String decodeUriPart(String value) {
        return URLDecoder.decode(value.replace("+", "%2B"), StandardCharsets.UTF_8);
    }
}
