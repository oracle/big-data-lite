package oracle.demo.oow.bd.constant;

public interface KeyConstant {

    //Table Names
    public static final String MOVIE_TABLE = "MV";
    public static final String MOVIE_CAST_CREW_TABLE = "MVCC";
    public static final String MOVIE_NAME_TABLE = "MVN";
    public static final String GENRE_TABLE = "GN";
    public static final String CAST_TABLE = "CT";
    public static final String CUSTOMER_TABLE = "CUST";    
    public static final String CREW_TABLE = "CW";
    
    //Queues for maintaining customer's movie status
    public static final String CUSTOMER_CURRENT_WATCH_LIST = "CT_CWL";
    public static final String CUSTOMER_HISTORICAL_WATCH_LIST = "CT_HWL";
    public static final String CUSTOMER_BROWSE_LIST = "CT_BL";
    public static final String COMMON_CURRENT_WATCH_LIST = "CM_CWL";
    
    
    //Intersection Tables
    public static final String GENRE_MOVIE_TABLE = "GN_MV";    
    public static final String CAST_MOVIE_TABLE = "CA_MV";
    public static final String MOVIE_CAST_TABLE = "MV_CA";
    public static final String MOVIE_CREW_TABLE = "MV_CW";
    public static final String CUSTOMER_GENRE_TABLE = "CT_GN";
    public static final String CUSTOMER_MOVIE_TABLE = "CT_MV";
    
    
    public static final String CUSTOMER_GENRE_MOVIE_TABLE = "CT_GN_MV";

    //Columns name    
    public static final String INFO_COLUMN = "info";
    public static final String GENRE_COLUMN = "genre";
    public static final String CAST_CREW_COLUMN = "cc";
    public static final String ID = "id";

    public static final String CREW_MOVIE_KEY_PREFIX = "CW_MV";


}
