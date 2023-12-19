package kr.re.keti.sc.dataservicebroker.datamodel.service.hive;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import kr.re.keti.sc.dataservicebroker.datamodel.dao.hive.HiveTableDAO;
import kr.re.keti.sc.dataservicebroker.datamodel.service.hbase.HBaseTableSVC;

@Service
@ConditionalOnProperty(value = "datasource.hive.use.yn", havingValue = "Y", matchIfMissing = false)
public class HiveTableSVC {

    @Autowired
    private HiveTableDAO hiveDAO;

    @Autowired
    private String hiveBaseDirPath;

    @Autowired
    private HBaseTableSVC hBaseTableSVC;

    @Value("${datasource.hbase.phoenixUrl}")
    private String phoenixUrl;

    private final Logger logger = LoggerFactory.getLogger(HiveTableSVC.class);

    public int createTable(String ddl) {
        logger.info("HiveTableSVC createTable. ddl={}", ddl);
        return hiveDAO.createTable(ddl);
    }
    public int dropTable(String ddl) {
        logger.info("HiveTableSVC dropTable. ddl={}", ddl);
        return hiveDAO.dropTable(ddl);
    }
    public int cacheTable(String ddl) {
        logger.info("HiveTableSVC cacheTable. ddl={}", ddl);
        return hiveDAO.cacheTable(ddl);
    }

    public int updateTableScheme() {
        return hiveDAO.updateTableScheme();
    }

    private String LINE_SEPARATOR = "\n";
    private String TEMP_TABLE_PREFIX = "_tmp";

    public List<String> getTableScheme(String tableName) {
        return hiveDAO.getTableScheme(tableName);
    }

    public String getIndex(String columnName, String tableName, String id) {
        return hiveDAO.getIndex(columnName, tableName, id);
    }

    public int getCount(String tableName, String id) {
        return hiveDAO.getCount(tableName, id);
    }

    public String getFullDdl(String statement, boolean storeInHbase, String dataSetId) throws Exception {
        String tableName = "";
        List<String> columnList = new ArrayList<String>();

        for (String status : statement.split("\n")) {
            if (status.startsWith("TableName: ")) {
                tableName = status.replace("TableName: ", "");
            } else if (status.startsWith("Columns: ")) {
                columnList.add(status.replace("Columns: ", ""));
            } else {
                columnList.add(status.replace(", ", ""));
            }
        }

        if (storeInHbase) {
            if (!tableName.contains("fullhist") & !tableName.contains("partialhist")) {
                return fullHbaseHiveFullDDLForNormalTable(tableName, columnList);
            }

            // hBaseTableSVC.createHBaseTable(tableName, columnList);
            return fullHbaseHiveFullDDL(tableName, columnList);
        } else {
            return getHiveFullDDL(tableName, columnList, dataSetId);
        }
    }

    private String getHiveFullDDL(String tableName, List<String> columnList, String dataSetId) {
        StringBuilder sql = new StringBuilder();

        sql.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(LINE_SEPARATOR).append("(")
                .append(LINE_SEPARATOR).append(String.join("\n, ", columnList)).append(")");
//        String tmpSql = sql + " USING parquet OPTIONS (charset 'UTF-8') LOCATION '" + hiveBaseDirPath + "/" + tableName;
        String tmpSql = sql + " USING delta OPTIONS (charset 'UTF-8') LOCATION '" + hiveBaseDirPath + "/" + tableName;

        if (dataSetId.isEmpty()) { // main table 생성
            return tmpSql + "';";
        } else { // 특정 dataset에 대한 파일 및 테이블 생
            return tmpSql + "/" + dataSetId + "';";
        }
    }

    private String fullHbaseHiveFullDDL(String tableName, List<String> columnList) {
        StringBuilder sql = new StringBuilder();

        List<String> columnListWithoutRELY = new ArrayList<String>();

        for (String column : columnList) {
            String[] tokens = column.split(" ");
            String colName = tokens[0];
            String colType = tokens[1];
            columnListWithoutRELY.add(colName + " " + colType);
        }

        StringBuilder hiveColumnsBuilder = new StringBuilder();
        hiveColumnsBuilder.append(":key");

        for (String column : columnList) {
            String colName = column.split(" ")[0];
            hiveColumnsBuilder.append(",").append(colName + ":value");
        }

        sql.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(LINE_SEPARATOR).append(" (")
                .append(String.join("\n, ", columnListWithoutRELY)).append(") ").append("using phoenix options (url '")
                .append(phoenixUrl).append("', primary 'ID,MODIFIED_AT -> PHOENIX_PRIMARY_KEY=INT');");

        return sql.toString();
    }

    private String fullHbaseHiveFullDDLForNormalTable(String tableName, List<String> columnList) {
        StringBuilder sql = new StringBuilder();

        List<String> columnListWithoutRELY = new ArrayList<String>();

        for (String column : columnList) {
            String[] tokens = column.split(" ");
            String colName = tokens[0];
            String colType = tokens[1];
            columnListWithoutRELY.add(colName + " " + colType);
        }

        StringBuilder hiveColumnsBuilder = new StringBuilder();
        hiveColumnsBuilder.append(":key");

        for (String column : columnList) {
            String colName = column.split(" ")[0];
            hiveColumnsBuilder.append(",").append(colName + ":value");
        }

        sql.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(LINE_SEPARATOR).append(" (")
                .append(String.join("\n, ", columnListWithoutRELY)).append(") ").append("using phoenix options (url '")
                .append(phoenixUrl).append("', primary 'ID');");

        return sql.toString();
    }

    public String getTmpTableQuery(String tableName) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE IF NOT EXISTS ").append(tableName).append(TEMP_TABLE_PREFIX).append(" AS SELECT * FROM ").append(tableName).append(" LIMIT 0;");
        return sql.toString();
        }

    public String cacheTableQuery(String tableName) {
        StringBuilder sql = new StringBuilder();
        sql.append("CACHE TABLE ").append(tableName).append(TEMP_TABLE_PREFIX).append(";");
        return sql.toString();
    }

    public String dropTmpTableQuery(String tableName) {
        StringBuilder sql = new StringBuilder();
        sql.append("DROP TABLE ").append(tableName).append(TEMP_TABLE_PREFIX).append(";");
        return sql.toString();
    }
}
