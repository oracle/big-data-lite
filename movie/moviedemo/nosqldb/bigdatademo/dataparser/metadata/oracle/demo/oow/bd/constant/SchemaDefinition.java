package oracle.demo.oow.bd.constant;

public interface SchemaDefinition {
    
    public static final String MOVIE_SCHEMA = "{\n" +
        "  \"type\" : \"record\",\n" +
        "  \"name\" : \"Movie\",\n" +
        "  \"namespace\" : \"oracle.avro\",\n" +
        "  \"fields\" : [ {\n" +
        "    \"name\" : \"id\",\n" +
        "    \"type\" : \"int\",\n" +
        "    \"default\" : 0\n" +
        "  }, {\n" +
        "    \"name\" : \"original_title\",\n" +
        "    \"type\" : \"string\",\n" +
        "    \"default\" : \"\"\n" +
        "  }, {\n" +
        "    \"name\" : \"overview\",\n" +
        "    \"type\" : \"string\",\n" +
        "    \"default\" : \"\"\n" +
        "  }, {\n" +
        "    \"name\" : \"poster_path\",\n" +
        "    \"type\" : \"string\",\n" +
        "    \"default\" : \"\"\n" +
        "  }, {\n" +
        "    \"name\" : \"release_date\",\n" +
        "    \"type\" : \"string\",\n" +
        "    \"default\" : \"0\"\n" +
        "  }, {\n" +
        "    \"name\" : \"vote_count\",\n" +
        "    \"type\" : \"int\",\n" +
        "    \"default\" : 0\n" +
        "  }, {\n" +
        "    \"name\" : \"runtime\",\n" +
        "    \"type\" : \"int\",\n" +
        "    \"default\" : 0\n" +
        "  }, {\n" +
        "    \"name\" : \"popularity\",\n" +
        "    \"type\" : \"double\",\n" +
        "    \"default\" : 0\n" +
        "  }, {\n" +
        "    \"name\" : \"genres\",\n" +
        "    \"type\" : {\n" +
        "      \"type\" : \"array\",\n" +
        "      \"items\" : {\n" +
        "        \"type\" : \"record\",\n" +
        "        \"name\" : \"MovieGenre\",\n" +
        "        \"fields\" : [ {\n" +
        "          \"name\" : \"id\",\n" +
        "          \"type\" : \"int\",\n" +
        "          \"default\" : 0\n" +
        "        }, {\n" +
        "          \"name\" : \"name\",\n" +
        "          \"type\" : \"string\",\n" +
        "          \"default\" : \"\"\n" +
        "        } ]\n" +
        "      }\n" +
        "    },\n" +
        "    \"default\" : [ ]\n" +
        "  } ]\n" +
        "}";


    public static final String GENRE_SCHEMA = "{\n" +
        "  \"type\" : \"record\",\n" +
        "  \"name\" : \"Genre\",\n" +
        "  \"namespace\" : \"oracle.avro\",\n" +
        "  \"fields\" : [ {\n" +
        "    \"name\" : \"id\",\n" +
        "    \"type\" : \"int\",\n" +
        "    \"default\" : 0\n" +
        "  }, {\n" +
        "    \"name\" : \"name\",\n" +
        "    \"type\" : \"string\",\n" +
        "    \"default\" : \"\"\n" +
        "  } ]\n" +
        "}";
    public static final String CAST_SCHEMA = "{\n" +
        "  \"type\" : \"record\",\n" +
        "  \"name\" : \"Cast\",\n" +
        "  \"namespace\" : \"oracle.avro\",\n" +
        "  \"fields\" : [ {\n" +
        "    \"name\" : \"id\",\n" +
        "    \"type\" : \"int\",\n" +
        "    \"default\" : 0\n" +
        "  }, {\n" +
        "    \"name\" : \"name\",\n" +
        "    \"type\" : \"string\",\n" +
        "    \"default\" : \"\"\n" +
        "  }, {\n" +
        "    \"name\" : \"movies\",\n" +
        "    \"type\" : {\n" +
        "      \"type\" : \"array\",\n" +
        "      \"items\" : {\n" +
        "        \"type\" : \"record\",\n" +
        "        \"name\" : \"CastMovie\",\n" +
        "        \"fields\" : [ {\n" +
        "          \"name\" : \"id\",\n" +
        "          \"type\" : \"int\",\n" +
        "          \"default\" : 0\n" +
        "        }, {\n" +
        "	  \"name\" : \"order\",\n" +
        "          \"type\" : \"int\",\n" +
        "          \"default\" : 0\n" +
        "        }, {\n" +
        "          \"name\" : \"character\",\n" +
        "          \"type\" : \"string\",\n" +
        "          \"default\" : \"\"\n" +
        "        } ]\n" +
        "      }\n" +
        "    },\n" +
        "    \"default\" : [ ]\n" +
        "  } ]\n" +
        "}";

    public static final String CREW_SCHEMA = "{\n" +
        "  \"type\" : \"record\",\n" +
        "  \"name\" : \"Crew\",\n" +
        "  \"namespace\" : \"oracle.avro\",\n" +
        "  \"fields\" : [ {\n" +
        "    \"name\" : \"id\",\n" +
        "    \"type\" : \"int\",\n" +
        "    \"default\" : 0\n" +
        "  }, {\n" +
        "    \"name\" : \"name\",\n" +
        "    \"type\" : \"string\",\n" +
        "    \"default\" : \"\"\n" +
        "  }, {\n" +
        "    \"name\" : \"job\",\n" +
        "    \"type\" : \"string\",\n" +
        "    \"default\" : \"\"\n" +
        "  }, {\n" +
        "    \"name\" : \"movies\",\n" +
        "    \"type\" : {\n" +
        "      \"type\" : \"array\",\n" +
        "      \"items\" : {\n" +
        "        \"type\" : \"record\",\n" +
        "        \"name\" : \"CrewMovie\",\n" +
        "        \"fields\" : [ {\n" +
        "          \"name\" : \"id\",\n" +
        "          \"type\" : \"int\",\n" +
        "          \"default\" : 0\n" +
        "        } ]\n" +
        "      }\n" +
        "    },\n" +
        "    \"default\" : [ ]\n" +
        "  } ]\n" +
        "}";

    public static final String CUSTOMER_SCHEMA = "{\n" +
        "  \"type\" : \"record\",\n" +
        "  \"name\" : \"Customer\",\n" +
        "  \"namespace\" : \"oracle.avro\",\n" +
        "  \"fields\" : [ {\n" +
        "    \"name\" : \"id\",\n" +
        "    \"type\" : \"int\",\n" +
        "    \"default\" : 0\n" +
        "  }, {\n" +
        "    \"name\" : \"name\",\n" +
        "    \"type\" : \"string\",\n" +
        "    \"default\" : \"\"\n" +
        "  }, {\n" +
        "    \"name\" : \"email\",\n" +
        "    \"type\" : \"string\",\n" +
        "    \"default\" : \"\"\n" +
        "  }, {\n" +
        "    \"name\" : \"username\",\n" +
        "    \"type\" : \"string\",\n" +
        "    \"default\" : \"\"\n" +
        "  }, {\n" +
        "    \"name\" : \"password\",\n" +
        "    \"type\" : \"string\",\n" +
        "    \"default\" : \"\"\n" +
        "  } ]\n" +
        "}";

    public static final String ACTIVITY_SCHEMA = "{\n" +
        "  \"type\" : \"record\",\n" +
        "  \"name\" : \"Activity\",\n" +
        "  \"namespace\" : \"oracle.avro\",\n" +
        "  \"fields\" : [ {\n" +
        "    \"name\" : \"custId\",\n" +
        "    \"type\" : \"int\",\n" +
        "    \"default\" : 0\n" +
        "  }, {\n" +
        "    \"name\" : \"movieId\",\n" +
        "    \"type\" : \"int\",\n" +
        "    \"default\" : 0\n" +
        "  }, {\n" +
        "    \"name\" : \"activity\",\n" +
        "    \"type\" : \"int\",\n" +
        "    \"default\" : 0\n" +
        "  }, {\n" +
        "    \"name\" : \"recommended\",\n" +
        "    \"type\" : \"string\",\n" +
        "    \"default\" : \"\"\n" +
        "  }, {\n" +
        "    \"name\" : \"time\",\n" +
        "    \"type\" : \"string\",\n" +
        "    \"default\" : \"\"\n" +
        "  }, {\n" +
        "    \"name\" : \"rating\",\n" +
        "    \"type\" : \"int\",\n" +
        "    \"default\" : 0\n" +
        "  }, {\n" +
        "    \"name\" : \"price\",\n" +
        "    \"type\" : \"double\",\n" +
        "    \"default\" : 0\n" +
        "  }, {\n" +
        "    \"name\" : \"position\",\n" +
        "    \"type\" : \"int\",\n" +
        "    \"default\" : 0\n" +
        "  } ]\n" +
        "}";

}
