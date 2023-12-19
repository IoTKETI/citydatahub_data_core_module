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
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import kr.re.keti.sc.dataservicebroker.common.code.DataServiceBrokerCode;
import kr.re.keti.sc.dataservicebroker.entities.sqlprovider.rdb.RdbEntitySqlProviderImpl;

/**
 * Application DB 접속 설정 클래스
 */
@Configuration
@EnableTransactionManagement
@MapperScan(value = "kr.re.keti.sc.dataservicebroker.entities.sqlprovider.rdb", sqlSessionFactoryRef = "sqlSessionFactory")
public class RdbDataSourceConfiguration {

	@Value("${datasource.driverClassName}")
	private String driverClassName;
	@Value("${datasource.url}")
	private String jdbcUrl;
	@Value("${datasource.username}")
	private String jdbcUserName;
	@Value("${datasource.password}")
	private String jdbcPassword;

	@Value("${datasource.secondary.use.yn}")
	private String secondaryDatasourceUseYn;
	@Value("${datasource.secondary.driverClassName}")
	private String secondaryDriverClassName;
	@Value("${datasource.secondary.url}")
	private String secondaryJdbcUrl;
	@Value("${datasource.secondary.username}")
	private String secondaryJdbcUserName;
	@Value("${datasource.secondary.password}")
	private String secondaryJdbcPassword;

	@Bean
	@Primary
	@Qualifier("dataSource")
	public DataSource dataSource() {
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName(driverClassName);
		dataSource.setUrl(jdbcUrl);
		dataSource.setUsername(jdbcUserName);
		dataSource.setPassword(jdbcPassword);

		return dataSource;
	}
	
	@Bean
	@Primary
	@Qualifier("dataSourceTransactionManager")
	public DataSourceTransactionManager dataSourceTransactionManager(DataSource dataSource) {
		return new DataSourceTransactionManager(dataSource);
	}

	@Bean
	@Primary
	@Qualifier("sqlSessionFactory")
	public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
		SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
		bean.setDataSource(dataSource);
		bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mapper/*Mapper.xml"));
//		bean.setConfigLocation(new DefaultResourceLoader().getResource("classpath:mybatis.xml"));
		org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
		// rdbSqlProvider 설정
		configuration.setDefaultSqlProviderType(RdbEntitySqlProviderImpl.class);
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
	@Primary
	@Qualifier("sqlSession")
	public SqlSessionTemplate sqlSession(SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory);
	}

	@Bean
	@Qualifier("batchSqlSession")
	public SqlSessionTemplate batchSqlSession(SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory, ExecutorType.BATCH);
	}

	@Bean
	@Qualifier("retrieveSqlSession")
	public SqlSessionTemplate secondarySqlSession(SqlSessionFactory sqlSessionFactory) throws Exception {

		// secondary datasource 사용
		if(DataServiceBrokerCode.UseYn.YES.toString().equals(secondaryDatasourceUseYn)) {
			BasicDataSource dataSource = new BasicDataSource();
			dataSource.setDriverClassName(secondaryDriverClassName);
			dataSource.setUrl(secondaryJdbcUrl);
			dataSource.setUsername(secondaryJdbcUserName);
			dataSource.setPassword(secondaryJdbcPassword);

			SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
			bean.setDataSource(dataSource);
			bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mapper/*Mapper.xml"));
//			bean.setConfigLocation(new DefaultResourceLoader().getResource("classpath:mybatis.xml"));
			org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
			// rdbSqlProvider 설정
			configuration.setDefaultSqlProviderType(RdbEntitySqlProviderImpl.class);
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

			return new SqlSessionTemplate(bean.getObject());

		// 기존 datasource 사용
		} else {
			return new SqlSessionTemplate(sqlSessionFactory);
		}
	}
}
