/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package oracle.avro;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.interceptor.Interceptor;
import static org.apache.flume.interceptor.StaticInterceptor.Constants.KEY;
import static org.apache.flume.interceptor.StaticInterceptor.Constants.KEY_DEFAULT;
import static org.apache.flume.interceptor.StaticInterceptor.Constants.VALUE;
import static org.apache.flume.interceptor.StaticInterceptor.Constants.VALUE_DEFAULT;
import org.apache.flume.sink.hdfs.AvroEventSerializer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Interceptor class that takes a JSON input from the movie activity logs and converts it to an Avro record.
 * It reuses the StaticInterceptor properties that allow you to specify key/value
 * headers that will be processed downstream by the AvroEventSerializer.
 * The key/value contains either the literal schema or a URL to that schema.<p>
 *
 * Key: <p>
 * AVRO_SCHEMA_LITERAL_HEADER = "flume.avro.schema.literal";
 * AVRO_SCHEMA_URL_HEADER = "flume.avro.schema.url"; <p>
 *
 * Value: <p>
 * The actual Avro schema definition or a URL pointing to that definition.<p>
 *
 * Example flume.conf entry:
 *
 * <code>
 * movieagent.sources.logFile.type=exec<p>
 * movieagent.sources.logFile.command=tail -F /u01/Middleware/logs/activity.out<p>
 * movieagent.sources.logFile.interceptors=jsonToAvro<p>
 * movieagent.sources.logFile.interceptors.jsonToAvro.type=oracle.avro.ActivityJsonToAvroInterceptor$Builder<p>
 * movieagent.sources.logFile.interceptors.jsonToAvro.key=flume.avro.schema.url<p>
 * movieagent.sources.logFile.interceptors.jsonToAvro.value=file:///home/oracle/movie/moviework/flume/activity.avsc<p>
 * </code>
 *
 */
public class ActivityJsonToAvroInterceptor implements Interceptor {

    private static final Logger logger =
        LoggerFactory.getLogger(ActivityJsonToAvroInterceptor.class);

    private final String key;
    private final String value;
    private Schema schema;
    private SpecificDatumWriter<Activity> datumWriter;
    private ByteArrayOutputStream outputStream;
    private BinaryEncoder encoder;
    private ObjectMapper objectMapper = new ObjectMapper();
    

    /**
     * Only {@link ActivityJsonToAvroInterceptor.Builder} can build me
     */
    private ActivityJsonToAvroInterceptor(String key,
                                  String value) {

        this.key = key;
        this.value = value;
    }


    /**
     * Initializes interceptor.
     */
    @Override
    public void initialize() {
        // Check if Schema has been passed as a literal or a file
        // Create the schema appropriately

        logger.info("Initializing  ActivityJsonToAvroInterseptor");
        if (key.equalsIgnoreCase(AvroEventSerializer.AVRO_SCHEMA_URL_HEADER)) {
            // Load from URL
            try {
                logger.info("...Loading schema " + value);
                schema = loadFromUrl(value);
            }
            catch (Exception e) {
                logger.error("Unable to load Url containing schema: " + value);
                logger.error(e.getMessage());
            }
        } else {
            // Load from literal string
            logger.info("...Loading schema from literal: " + value);
            schema = new Schema.Parser().parse(value);
        }
        
        // Initialize objects required to serialize Avro GenericRecord to byte[]
        datumWriter = new SpecificDatumWriter<Activity>(schema);
        outputStream = new ByteArrayOutputStream();
        
        // Use binary encoder
        encoder = EncoderFactory.get().binaryEncoder(outputStream, null);        
        
    }

    /**
     *Loads a schema from a URL
     * @param schemaUrl
     * @return Schema 
     * @throws IOException
     */
    private Schema loadFromUrl(String schemaUrl) throws IOException {
        Configuration conf = new Configuration();
        Schema.Parser parser = new Schema.Parser();
        if (schemaUrl.toLowerCase().startsWith("hdfs://")) {
            FileSystem fs = FileSystem.get(conf);
            FSDataInputStream input = null;
            try {
                input = fs.open(new Path(schemaUrl));
                return parser.parse(input);
            } finally {
                if (input != null) {
                    input.close();
                }
            }
        } else {
            InputStream is = null;
            try {
                is = new URL(schemaUrl).openStream();
                return parser.parse(is);
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }
    }


    /**
     * Adds the Schema details to the event.
     * Also converts the Json body to an encoded Avro record
     */
    @Override
    public Event intercept(Event event) {

        Map<String, String> headers = event.getHeaders();

        // Add schema spec to header        
        if (!headers.containsKey(key))
            headers.put(key, value);
        
        try {
            // Alter the body.  Convert to Avro and encode.
            if (event.getBody().length == 0)
                return null;
            
            Activity record = getActivityRecord(event.getBody());
            
            // Encode
            outputStream.reset();
            
            datumWriter.write(record, encoder);
            encoder.flush();            
            
            // Set the event body
            event.setBody(outputStream.toByteArray());

        }
        catch (Exception e) {
            logger.info("ERROR with JSON: " + event.getBody().toString());            
            return null;  // swallow event
        }
        
        return event;
    }

    private Activity getActivityRecord(byte[] eventBody) throws IOException,
                                                                     JsonProcessingException {
        
        // Create an Activity object and populate
        Activity activity = new Activity();
                
        // Parse the activity node and update the record
        JsonNode jsonNode = objectMapper.readTree(new String(eventBody));
        
        Map.Entry<String,JsonNode> nodeMap = null;        
        Iterator <Map.Entry<String,JsonNode>> iterator = jsonNode.getFields();
        
        while (iterator.hasNext()) {
            
            nodeMap = iterator.next();

            // Populate Activity record from the Json source
            switch (nodeMap.getKey().toLowerCase()) {
            case "activity":
                activity.setActivity(nodeMap.getValue().getValueAsInt());
                break;
            case "custid":
                activity.setCustid(nodeMap.getValue().getValueAsInt());    
                break;                
            case "movieid":
                activity.setMovieid(nodeMap.getValue().getValueAsInt());
                break;                
            case "genreid":
                activity.setGenreid(nodeMap.getValue().getValueAsInt());
                break;                
            case "position":
                activity.setPosition(nodeMap.getValue().getValueAsInt());
                break;                
            case "price":
                activity.setPrice(nodeMap.getValue().getValueAsDouble());
                break;                
            case "rating":
                activity.setRating(nodeMap.getValue().getValueAsInt());
                break;                
            case "recommended":
                activity.setRecommended(nodeMap.getValue().getValueAsText());
                break;                
            case "time":
                activity.setTime(nodeMap.getValue().getValueAsText());
                break;                                        
            }                               
        
        }  // while loop
        
        return activity;
    }


    /**
     * Delegates to {@link #intercept(Event)} in a loop.
     * @param events
     * @return
     */
    @Override
    public List<Event> intercept(List<Event> events) {
        for (Event event : events) {
            intercept(event);
        }
        return events;
    }

    @Override
    public void close() {
        try {
            outputStream.close();
        } catch (IOException e) {
        }
    }

    /**
     * Builder which builds new instance of the ActivityJsonToAvroInterceptor.
     */
    public static class Builder implements Interceptor.Builder {

        private String key;
        private String value;

        @Override
        public void configure(Context context) {
            key = context.getString(KEY, KEY_DEFAULT);
            value = context.getString(VALUE, VALUE_DEFAULT);
            
            if (key == null || value == null) 
                logger.error("Missing Key or Value.  Specify key flume.avro.schema.literal or flume.avro.schema.url with appropriate values.");
        }

        @Override
        public Interceptor build() {
            logger.info(String.format("Creating ActivityJsonToAvroInterceptor: key=%s,value=%s",
                                      key, value));
            return new ActivityJsonToAvroInterceptor(key, value);
        }
    }
}
