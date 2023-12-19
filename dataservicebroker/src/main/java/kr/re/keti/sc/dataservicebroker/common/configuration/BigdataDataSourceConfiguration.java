package kr.re.keti.sc.dataservicebroker.common.configuration;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.ibatis.session.AutoMappingBehavior;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import kr.re.keti.sc.dataservicebroker.entities.sqlprovider.hive.HiveEntitySqlProviderImpl;

/**
 * Bigdata 접속 설정 클래스
 */
@Configuration

@EnableTransactionManagement
@MapperScan(value = "kr.re.keti.sc.dataservicebroker.entities.sqlprovider.hive", sqlSessionFactoryRef = "hiveSqlSessionFactory")
@ConditionalOnProperty(value="datasource.hive.use.yn", havingValue = "Y", matchIfMissing = false)
public class BigdataDataSourceConfiguration {

	@Value("${datasource.hive.driverClassName}")
	private String hiveDriverClassName;
	@Value("${datasource.hive.url}")
	private String hiveJdbcUrl;
	@Value("${datasource.hive.username}")
	private String hiveJdbcUserName;
	@Value("${datasource.hive.password}")
	private String hiveJdbcPassword;
	@Value("${datasource.hive.baseDirPath}")
	private String hiveBaseDirPath;

	@Bean
	@Qualifier("hiveDataSource")
	public DataSource hiveDataSource() {
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName(hiveDriverClassName);
		dataSource.setUrl(hiveJdbcUrl);
		dataSource.setUsername(hiveJdbcUserName);
		dataSource.setPassword(hiveJdbcPassword);
		dataSource.setMinIdle(10);

		// dataSource.setPoolPreparedStatements(true);
		// dataSource.setMaxOpenPreparedStatements(100);
		return dataSource;
	}
	
	@Bean
	@Qualifier("hiveDataSourceTransactionManager")
	public DataSourceTransactionManager hiveDataSourceTransactionManager(@Qualifier("hiveDataSource") DataSource hiveDataSource) {
		return new DataSourceTransactionManager(hiveDataSource);
	}

	@Bean
	@Qualifier("hiveSqlSessionFactory")
	public SqlSessionFactory hiveSqlSessionFactory(@Qualifier("hiveDataSource") DataSource hiveDataSource) throws Exception {
		SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
		bean.setDataSource(hiveDataSource);
		bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mapper/hive/*Mapper.xml"));
		org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
		// BigdataSqlProvider 설정
		configuration.setDefaultSqlProviderType(HiveEntitySqlProviderImpl.class);
		configuration.setCacheEnabled(true);
		configuration.setUseGeneratedKeys(false);
		configuration.setDefaultExecutorType(ExecutorType.SIMPLE);
		configuration.setLazyLoadingEnabled(false);
		configuration.setAggressiveLazyLoading(true);
		configuration.setUseColumnLabel(true);
		configuration.setAutoMappingBehavior(AutoMappingBehavior.PARTIAL);
		configuration.setMultipleResultSetsEnabled(true);
		configuration.setSafeRowBoundsEnabled(true);
		configuration.setMapUnderscoreToCamelCase(false);
		bean.setConfiguration(configuration);
		return bean.getObject();
	}

	@Bean
	@Qualifier("hiveSqlSession")
	public SqlSessionTemplate hiveSqlSession(@Qualifier("hiveSqlSessionFactory") SqlSessionFactory hiveSqlSessionFactory) {
		return new SqlSessionTemplate(hiveSqlSessionFactory);
	}

	@Bean
	@Qualifier("hiveBaseDirPath")
	public String hiveBaseDirPath() {
		return hiveBaseDirPath;
	}
}