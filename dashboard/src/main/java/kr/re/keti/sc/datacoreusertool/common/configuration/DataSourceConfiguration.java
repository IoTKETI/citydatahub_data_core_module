package kr.re.keti.sc.datacoreusertool.common.configuration;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import kr.re.keti.sc.datacoreusertool.common.code.Constants;
import kr.re.keti.sc.datacoreusertool.common.code.DataServiceBrokerCode;

/**
 * Application DB connection setting class
 * @FileName DataSourceConfiguration.java
 * @Project datacore-usertool
 * @Brief 
 * @Version 1.0
 * @Date 2022. 3. 26.
 * @Author Elvin
 */
@Configuration
@EnableTransactionManagement
@MapperScan(Constants.BASE_PACKAGE)
public class DataSourceConfiguration {

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
	public DataSource dataSource() {
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName(driverClassName);
		dataSource.setUrl(jdbcUrl);
		dataSource.setUsername(jdbcUserName);
		dataSource.setPassword(jdbcPassword);

		return dataSource;
	}

	@Bean
	public DataSourceTransactionManager dataSourceTransactionManager() {
		return new DataSourceTransactionManager(dataSource());
	}

	@Bean
	public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
		SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
		bean.setDataSource(dataSource);
		bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mapper/*Mapper.xml"));
		bean.setConfigLocation(new DefaultResourceLoader().getResource("classpath:mybatis.xml"));
		return bean.getObject();
	}

	@Bean
	@Primary
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

		// secondary datasource
		if(DataServiceBrokerCode.UseYn.YES.toString().equals(secondaryDatasourceUseYn)) {
			BasicDataSource dataSource = new BasicDataSource();
			dataSource.setDriverClassName(secondaryDriverClassName);
			dataSource.setUrl(secondaryJdbcUrl);
			dataSource.setUsername(secondaryJdbcUserName);
			dataSource.setPassword(secondaryJdbcPassword);

			SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
			bean.setDataSource(dataSource);
			bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mapper/*Mapper.xml"));
			bean.setConfigLocation(new DefaultResourceLoader().getResource("classpath:mybatis.xml"));

			return new SqlSessionTemplate(bean.getObject());

		// datasource
		} else {
			return new SqlSessionTemplate(sqlSessionFactory);
		}
	}
}