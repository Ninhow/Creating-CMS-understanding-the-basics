package cms.repository.db.config;

import cms.utils.JsonConfig;
import exceptions.ConfigException;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@Configuration
@Profile("RMDBS")
@ComponentScan(basePackages = "cms.repository.db")
@EntityScan(basePackages = "cms.repository.db.domain")
@EnableJpaRepositories(basePackages = "cms.repository.db.dao")
public class RMDBSConfig{

    private DataSourceConfig dataSourceConfig;

    @PostConstruct
    public void init() throws ConfigException {
        dataSourceConfig = JsonConfig.loadConfig("config/datasource.json", DataSourceConfig.class);
    }

    @Bean
    @Primary
    public DataSource dataSource(){
        return DataSourceBuilder
                .create()
                .url(buildConnectionUrl(dataSourceConfig))
                .username(dataSourceConfig.getUsername())
                .password(dataSourceConfig.getPassword())
                .driverClassName(getDriver(dataSourceConfig.getDatabaseType()))
                .build();
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean(){
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource());
        emf.setPackagesToScan(getEmfPackagesToScan(dataSourceConfig));
        emf.setPersistenceUnitName("cms");

        HibernateJpaVendorAdapter va = new HibernateJpaVendorAdapter();
        va.setShowSql(true);
        va.setGenerateDdl(false);

        Properties jpaProperties = new Properties();
        jpaProperties.put("hibernate.dialect", getHibernateDialect(dataSourceConfig.getDatabaseType()));
        emf.setJpaVendorAdapter(va);
        emf.setJpaProperties(jpaProperties);
        emf.afterPropertiesSet();

        return emf;
    }

    private String getHibernateDialect(DatabaseType databaseType) {
        switch (type){
            case POSTGRES:
                return "org.postgres.Drive";
            case MYSQL:
                return "org.mysql.jdbc.Driver";
            default:
                return "";
        }
    }

    private String[] getEmfPackagesToScan(DataSourceConfig dataSourceConfig) {
        List<String> packagesToScan = Arrays.asList(dataSourceConfig.getPackagesToScan());
        packagesToScan.add("cms.repository.db.domain");

        return packagesToScan.toArray(new String[0]);
    }

    private String buildConnectionUrl(DataSourceConfig s){
        String connectionString = "jdbc:";
        connectionString += getConnectionUrlType(dataSourceConfig.getDatabaseType()) + "://";
        connectionString += dataSourceConfig.getHost() + ":" + dataSourceConfig.getPort();
        connectionString += "/" + dataSourceConfig.getDatabaseName();
        connectionString += "?useSSL"+ dataSourceConfig.isUseSSL();
        return connectionString;
    }

    private String getConnectionUrlType(DatabaseType type){
        switch (type){
            case POSTGRES:
                return "postgres";
            case MYSQL:
                return "mysql";
            default:
                return "";
        }
    }

    private String getDriver(DatabaseType type){
        switch (type){
            case POSTGRES:
                return "org.postgres.Drive";
            case MYSQL:
                return "org.mysql.jdbc.Driver";
            default:
                return "";
        }
    }
}
