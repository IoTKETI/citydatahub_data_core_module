package kr.re.keti.sc.dataservicebroker.common.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import kr.re.keti.sc.dataservicebroker.entities.dao.hive.HiveEntityDAO;
import kr.re.keti.sc.dataservicebroker.entities.dao.rdb.RdbEntityDAO;
import kr.re.keti.sc.dataservicebroker.entities.service.hbase.HbaseEntitySVC;
import kr.re.keti.sc.dataservicebroker.entities.service.hive.HiveEntitySVC;
import kr.re.keti.sc.dataservicebroker.entities.service.rdb.RdbEntitySVC;

@Configuration
public class EntityConfiguration {
	
	@Bean
	@Primary
	@Qualifier("rdbDynamicEntitySVC")
    protected RdbEntitySVC rdbDynamicEntitySVC() {
		RdbEntitySVC rdbEntitySVC = new RdbEntitySVC();
		rdbEntitySVC.setEntityDAOInterface(rdbEntityDAO());
        return rdbEntitySVC;
    }
	
	@Bean
	@Primary
	@Qualifier("rdbEntityDAO")
	protected RdbEntityDAO rdbEntityDAO() {
        return new RdbEntityDAO();
    }

	@Bean
	@Qualifier("hiveDynamicEntitySVC")
	@ConditionalOnProperty(value="datasource.hive.use.yn", havingValue = "Y", matchIfMissing = false)
	protected HiveEntitySVC hiveDynamicEntitySVC() {
		HiveEntitySVC hiveEntitySVC = new HiveEntitySVC();
		hiveEntitySVC.setEntityDAOInterface(hiveEntityDAO());
        return hiveEntitySVC;
    }
	
	@Bean
	@Qualifier("hiveEntityDAO")
	@ConditionalOnProperty(value="datasource.hive.use.yn", havingValue = "Y", matchIfMissing = false)
	protected HiveEntityDAO hiveEntityDAO() {
        return new HiveEntityDAO();
    }

	@Bean
	@Qualifier("hbaseDynamicEntitySVC")
	@ConditionalOnProperty(value="datasource.hive.use.yn", havingValue = "Y", matchIfMissing = false)
	protected HbaseEntitySVC hbaseDynamicEntitySVC() {
		HbaseEntitySVC hbaseEntitySVC = new HbaseEntitySVC();
		hbaseEntitySVC.setEntityDAOInterface(hiveEntityDAO());
		return hbaseEntitySVC;
	}
	
}
