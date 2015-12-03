package oracle.demo.oow.bd.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.util.Iterator;

import oracle.demo.oow.bd.config.StoreConfig;


import oracle.demo.oow.bd.constant.Constant;

import oracle.demo.oow.bd.constant.SchemaDefinition;
import oracle.demo.oow.bd.to.MovieTO;
import oracle.demo.oow.bd.util.StringUtil;

import oracle.kv.KVStore;
import oracle.kv.KVStoreConfig;
import oracle.kv.KVStoreFactory;
import oracle.kv.Key;
import oracle.kv.Value;
import oracle.kv.ValueVersion;
import oracle.kv.Version;

import oracle.kv.avro.AvroCatalog;

import oracle.kv.table.MultiRowOptions;
import oracle.kv.table.PrimaryKey;
import oracle.kv.table.Row;
import oracle.kv.table.Table;

import oracle.kv.table.TableAPI;

import org.apache.avro.Schema.Parser;

public class BaseDAO {

    private static KVStore kvStore = null;
    private static Connection conn;
    protected static Parser parser = null;
    protected static AvroCatalog catalog = null;
    private static TableAPI tableImpl = null;


    public BaseDAO() {
        super();
        if (parser == null && this.getKVStore()!=null) {
            parser = new Parser();
            catalog = this.getKVStore().getAvroCatalog();

            parser.parse(SchemaDefinition.CUSTOMER_SCHEMA);
            parser.parse(SchemaDefinition.CAST_SCHEMA);
            parser.parse(SchemaDefinition.MOVIE_SCHEMA);
            parser.parse(SchemaDefinition.GENRE_SCHEMA);
            parser.parse(SchemaDefinition.CREW_SCHEMA);
            parser.parse(SchemaDefinition.ACTIVITY_SCHEMA);            

        } //if(parser==null)
    } //BaseDAO

    public static KVStore getKVStore() {
        if (kvStore == null) {
            try {
                kvStore =
                        KVStoreFactory.getStore(new KVStoreConfig(StoreConfig.KVSTORE_NAME, StoreConfig.KVSTORE_URL));
            } catch (Exception e) {
                System.out.println("ERROR: Please make sure Oracle NoSQL Database is up and running at '" +
                                   StoreConfig.KVSTORE_URL + "' with store name as: '" + StoreConfig.KVSTORE_NAME +
                                   "'");
                //e.printStackTrace();
            }
        } //EOF if

        return kvStore;
    } //getKVStore

    public static void put(String key, String value) {
        Key kvKey = Key.createKey(key.toLowerCase());
        put(kvKey, value);
    } //kvPut

    public static Version put(Key kvKey, String value) {
        Value kvValue = null;
        Version version = null;
        if (kvKey != null && StringUtil.isNotEmpty(value)) {
            kvValue = Value.createValue(value.getBytes());
            version = getKVStore().put(kvKey, kvValue);
        }
        return version;
    } //kvPut

    public static Version putIfAbsent(Key kvKey, String value) {
        Value kvValue = null;
        Version version = null;

        if (kvKey != null && StringUtil.isNotEmpty(value)) {
            kvValue = Value.createValue(value.getBytes());
            version = getKVStore().putIfAbsent(kvKey, kvValue);

        }

        return version;
    } //kvPut

    public static String get(String key) {
        Key kvKey = null;
        if (StringUtil.isNotEmpty(key)) {
            kvKey = Key.createKey(key.toLowerCase());
        }
        return get(kvKey);
    } //get

    public static String get(Key kvKey) {
        String value = null;
        ValueVersion vv = null;

        if (kvKey != null) {
            vv = getKVStore().get(kvKey);
            if (vv != null) {
                value = new String(vv.getValue().toByteArray()).trim();
            }
        }
        return value;
    } //kvPut

    public static void delete(String key) {
        Key kvKey = Key.createKey(key.toLowerCase());
        getKVStore().delete(kvKey);
    }

    public static void delete(Key key) {
        getKVStore().delete(key);
    }

//    public static void multiDelete(Key key) {
//        getKVStore().multiDelete(key, null, null);
//    }

    /**
     * When no schema user/password is passed as an argument then connect to DB
     * as moviedemo user
     * @return
     */
    public static Connection getOraConnect() {
        return getOraConnect(Constant.DB_DEMO_USER, Constant.DEMO_PASSWORD);
    }

    public static Table getTable(String tablePath) {

                    try {
                            return getTableAPI().getTable(tablePath);
                    } catch (Exception e) {
                            System.err.println("Failed to get table: " + tablePath);
                            // e.printStackTrace();
                    }
                    return null;
            }

            public Iterator<Row> getRows(Table table) {

                    MultiRowOptions mro = new MultiRowOptions(null, null, null);
                    return tableImpl.tableIterator(table.createPrimaryKey(), mro, null);
            }

            public static TableAPI getTableAPI() {
                    if (tableImpl == null) {
                            tableImpl = getKVStore().getTableAPI();
                    }
                    return tableImpl;
            }

            public static void multiDelete(PrimaryKey key) {
                    getKVStore().getTableAPI().multiDelete(key, null, null);
            }
            
    /**
     *Connects to Oracle Database, when schema user and password is passed
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public static Connection getOraConnect(String user, String password) {

        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");

            if (conn == null) {
                conn = DriverManager.getConnection(Constant.JDBC_URL, user, password);
                conn.setAutoCommit(true);
                System.out.println("Connected to database");
            }


        } catch (SQLException se) {
            //se.printStackTrace();
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return conn;
    }

    public static void main(String[] args) {
        System.out.println("Connecting to DB/s...");
        getKVStore();
        System.out.println("Connection to Oracle NoSQL DB instance was successful.");
        System.out.println("Now checking a DAO.get() ...");
        MovieDAO movieDAO = new MovieDAO();
        MovieTO movieTO = movieDAO.getMovieById(857);
        if (movieTO != null) {
            System.out.println(movieTO.getMovieJsonTxt());
        }
        System.out.println("DAO compiled successfully.");

        //getOraConnect();


    }

}
