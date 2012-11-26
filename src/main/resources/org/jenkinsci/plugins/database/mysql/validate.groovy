import com.mysql.jdbc.jdbc2.optional.MysqlDataSource
import com.mysql.jdbc.ConnectionPropertiesImpl
import java.lang.reflect.Field;

def validNames = new HashSet();

def field = ConnectionPropertiesImpl.class.getDeclaredField("PROPERTY_LIST")
field.accessible = true

def ds = new MysqlDataSource()
field.get(null).each { Field f ->
    f.accessible = true
    def o = f.get(ds)
    validNames << o.propertyName;
}

return validNames;