package test.doctor_provider.infrastructure.config;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Manuelle Flyway-Konfiguration.
 * Stellt sicher, dass Flyway die Migrationen ausführt,
 * BEVOR Hibernate/JPA die Tabellen nutzt.
 */
@Configuration
public class FlywayConfig {

	@Bean
	public Flyway flyway(DataSource dataSource) {
		Flyway flyway = Flyway.configure()
				.dataSource(dataSource)
				.locations("classpath:db/migration")
				.baselineOnMigrate(true)
				.encoding("UTF-8")
				.load();
		flyway.migrate();
		return flyway;
	}

	/**
	 * Erzwingt, dass entityManagerFactory erst nach flyway erstellt wird.
	 */
	@Bean
	public static BeanFactoryPostProcessor flywayDependencyPostProcessor() {
		return beanFactory -> {
			if (beanFactory.containsBeanDefinition("entityManagerFactory")) {
				beanFactory.getBeanDefinition("entityManagerFactory").setDependsOn("flyway");
			}
		};
	}
}


