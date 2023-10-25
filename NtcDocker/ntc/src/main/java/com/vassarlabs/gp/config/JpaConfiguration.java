package com.vassarlabs.gp.config;

import com.vassarlabs.gp.pojo.SecretValues;
import io.akeyless.client.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories("com.vassarlabs.gp.repository")
//TODO : Uncomment this for Akeyless
//@ConfigurationProperties("application-akeyless.properties")
@EnableTransactionManagement
public class JpaConfiguration  {

	//TODO : COmment this for Akeyless and Uncomment next code
	@Bean
	@ConfigurationProperties(prefix="spring.datasource")
	public DataSource dataSource() {
		DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
		return dataSourceBuilder.build();

		/*
		 * DriverManagerDataSource dataSource = new DriverManagerDataSource();
		 * dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
		 * dataSource.setUrl("jdbc:mysql://localhost:3306/test?serverTimezone=UTC");
		 * dataSource.setUsername("username"); dataSource.setPassword("password");
		 * return dataSource;
		 */
		/*
		 * return new EmbeddedDatabaseBuilder() .generateUniqueName(true)
		 * .setType(EmbeddedDatabaseType.S) .addScript("create-customer-schema.sql")
		 * .build();
		 */
		/*
		 * return DataSourceBuilder .create() .build();
		 */
		/*
		 * DriverManagerDataSource dataSource = new DriverManagerDataSource();
		 * dataSource.setDriverClassName("org.postgresql.Driver");
		 * dataSource.setUrl("jdbc:postgresql://localhost:5432/auth_gp");
		 * dataSource.setUsername("postgres"); dataSource.setPassword("test"); return
		 * dataSource;
		 */
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory(
			DataSource dataSource, JpaVendorAdapter jpaVendorAdapter) {
		LocalContainerEntityManagerFactoryBean lef = new LocalContainerEntityManagerFactoryBean();
		lef.setDataSource(dataSource);
		lef.setJpaVendorAdapter(jpaVendorAdapter);
		lef.setPackagesToScan("com.vassarlabs.gp");


		Map<String, Object> jpaProperties = new HashMap<String, Object>();
		jpaProperties.put("hibernate.jdbc.batch_size",500);
		jpaProperties.put("hibernate.order_inserts",true);
		jpaProperties.put("hibernate.batch_versioned_data", true);
		//jpaProperties.put("hibernate.generate_statistics", true);

		lef.setJpaPropertyMap(jpaProperties);

		return lef;
	}

	@Bean
	public JpaVendorAdapter jpaVendorAdapter() {
		HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
		hibernateJpaVendorAdapter.setShowSql(false);
		hibernateJpaVendorAdapter.setGenerateDdl(false);
		hibernateJpaVendorAdapter.setDatabase(Database.POSTGRESQL);


		return hibernateJpaVendorAdapter;
	}

	@Bean
	public PlatformTransactionManager transactionManager() {
//        return new JpaTransactionManager(entityManagerFactory(dataSource(),jpaVendorAdapter()).getNativeEntityManagerFactory());
		return new JpaTransactionManager(entityManagerFactory(dataSource(),jpaVendorAdapter()).getObject());
	}

//    @Bean
//    @Primary
//    public JpaTransactionManager jpaTransactionManager() {
//        final JpaTransactionManager transactionManager = new JpaTransactionManager();
//        transactionManager.setDataSource(dataSource());
//        return transactionManager;
//    }

	//TODO : UNCOMMENT from Here for Akeyless
	/*
	@Autowired
	private AkeylessConfig akeylessConfig;

	@Value("${akeyless.databaseName}")
	private String databaseName;

	@Bean
	@ConfigurationProperties(prefix="spring.datasource")
	public DataSource dataSource() throws ApiException {
		SecretValues secrets=akeylessConfig.initializeAkeylessClient();
		return DataSourceBuilder
				.create()
				.url("jdbc:postgresql://"+secrets.getHostname()+":5432/"+databaseName)
				.username(secrets.getUsername())
				.password(secrets.getPassword())
				.build();
	}
	
	@Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource dataSource, JpaVendorAdapter jpaVendorAdapter) {
        LocalContainerEntityManagerFactoryBean lef = new LocalContainerEntityManagerFactoryBean();
        lef.setDataSource(dataSource);
        lef.setJpaVendorAdapter(jpaVendorAdapter);
        lef.setPackagesToScan("com.vassarlabs.gp");
        
		
		 Map<String, Object> jpaProperties = new HashMap<String, Object>();
		 jpaProperties.put("hibernate.jdbc.batch_size",500);
		 jpaProperties.put("hibernate.order_inserts",true);
		 jpaProperties.put("hibernate.batch_versioned_data", true);
		 //jpaProperties.put("hibernate.generate_statistics", true);
		 
		 lef.setJpaPropertyMap(jpaProperties);
		 
        return lef;
    }

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
        hibernateJpaVendorAdapter.setShowSql(false);
        hibernateJpaVendorAdapter.setGenerateDdl(false);
        hibernateJpaVendorAdapter.setDatabase(Database.POSTGRESQL);
        
        
        return hibernateJpaVendorAdapter;
    }

    @Bean
    public PlatformTransactionManager transactionManager() throws ApiException {
//        return new JpaTransactionManager(entityManagerFactory(dataSource(),jpaVendorAdapter()).getNativeEntityManagerFactory());
    	return new JpaTransactionManager(entityManagerFactory(dataSource(),jpaVendorAdapter()).getObject());
    }

//    @Bean
//    @Primary
//    public JpaTransactionManager jpaTransactionManager() {
//        final JpaTransactionManager transactionManager = new JpaTransactionManager();
//        transactionManager.setDataSource(dataSource());
//        return transactionManager;
//    }

	 */

}
