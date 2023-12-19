package kr.re.keti.sc.dataservicebroker.datamodel.service.hbase;

import java.io.IOException;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class HBaseTableSVC {

    @Value("${datasource.hbase.xmlPath}")
    private String hbaseXmlPath;
    
    private final Logger logger = LoggerFactory.getLogger(HBaseTableSVC.class);

    private Admin getAdmin() throws Exception {
        Configuration conf = HBaseConfiguration.create();

        conf.addResource(new Path(hbaseXmlPath));

        HBaseAdmin.available(conf);

        Connection connection = ConnectionFactory.createConnection(conf);

        return connection.getAdmin();
    }

    public boolean createHBaseTable(String tableName, List<String> columnList) throws Exception {
        try {
            Admin admin = getAdmin();

            HTableDescriptor desc = new HTableDescriptor(TableName.valueOf(tableName));

            for (String column: columnList) {
                String colName = column.split(" ")[0];
                desc.addFamily(new HColumnDescriptor(colName));
            }

            if (!admin.tableExists(TableName.valueOf(tableName))) {
                admin.createTable(desc);
            }

        } catch (IOException e) {
        	logger.error("HBaseTableSVC createHBaseTable error. tableName=" + tableName, e);
        }

        return true;
    }

    public boolean dropTable(String tableName) throws Exception {
        try {
            Admin admin = getAdmin();
            if (admin.tableExists(TableName.valueOf(tableName))) {

                admin.disableTable(TableName.valueOf(tableName));
                admin.deleteTable(TableName.valueOf(tableName));

            }
            return true;
        } catch (IOException e) {
        	logger.error("HBaseTableSVC dropTable error. tableName=" + tableName, e);
        }

        return false;
    }

}
