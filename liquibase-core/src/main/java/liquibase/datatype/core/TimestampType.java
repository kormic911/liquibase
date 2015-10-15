package liquibase.datatype.core;

import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.util.StringUtils;
import liquibase.logging.LogFactory;

@DataTypeInfo(name = "timestamp", aliases = {"java.sql.Types.TIMESTAMP", "java.sql.Timestamp", "timestamptz"}, minParameters = 0, maxParameters = 1, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class TimestampType extends DateTimeType {

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        String originalDefinition = StringUtils.trimToEmpty(getRawDefinition());
	boolean allowFractional = supportsFractionalDigits(database);
        if (database instanceof MySQLDatabase) {
            //if (getRawDefinition().contains(" ") || getRawDefinition().contains("(")) {
            //    return new DatabaseDataType(getRawDefinition());
            //}
            //return super.toDatabaseDataType(database);
	    if (getParameters().length == 0 || !allowFractional) {
	        return super.toDatabaseDataType(database);
            }

            Object[] params = getParameters();
	    Integer precision = Integer.valueOf(params[0].toString());
	    if (precision > 6) {
		LogFactory.getInstance().getLog().warning(
                        "MySQL does not support a timestamp precision"
                                + " of '" + precision + "' - resetting to"
                                + " the maximum of '6'");
                params = new Object[] {6};
            }
            return new DatabaseDataType(getName(), params);
        }
        if (database instanceof MSSQLDatabase) {
            if (!LiquibaseConfiguration.getInstance().getProperty(GlobalConfiguration.class, GlobalConfiguration.CONVERT_DATA_TYPES).getValue(Boolean.class) && originalDefinition.toLowerCase().startsWith("timestamp")) {
                return new DatabaseDataType(database.escapeDataTypeName("timestamp"));
            }

            return new DatabaseDataType(database.escapeDataTypeName("datetime"));
        }
        return super.toDatabaseDataType(database);
    }
}
