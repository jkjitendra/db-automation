package org.demo.configuration;

import org.mariadb.jdbc.MariaDbDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import javax.sql.DataSource;
import java.sql.SQLException;

@Configuration
public class BeanConfiguration {

    @Value("${spring.datasource.url}")
    private String mariaCommonDataSourceUrl;
    @Value("${spring.datasource.username}")
    private String mariaCommonDataSourceUsername;
    @Value("${spring.datasource.password}")
    private String mariaCommonDataSourcePassword;

    @Bean
    public DataSource mariaDbDataSource() throws SQLException{

        MariaDbDataSource dataSource = new MariaDbDataSource();
        dataSource.setUrl(mariaCommonDataSourceUrl);
        dataSource.setUserName(mariaCommonDataSourceUsername);
        dataSource.setPassword(mariaCommonDataSourcePassword);
        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate() throws SQLException{

        JdbcTemplate jdbcTemplate = new JdbcTemplate(mariaDbDataSource());
        jdbcTemplate.setMaxRows(10000);
        return jdbcTemplate;
    }

    @Bean
    public JpaVendorAdapter jpaVendorAdapter () throws SQLException{

        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        jpaVendorAdapter.setGenerateDdl(false);
        jpaVendorAdapter.setShowSql(true);
        return jpaVendorAdapter;
    }
}
