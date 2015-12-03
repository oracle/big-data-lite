package oracle.demo.oow.bd.loader.imdb.formatter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.util.StringTokenizer;

import oracle.demo.oow.bd.constant.Constant;
import oracle.demo.oow.bd.constant.KeyConstant;
import oracle.demo.oow.bd.dao.BaseDAO;
import oracle.demo.oow.bd.to.CastMovieTO;
import oracle.demo.oow.bd.to.CastTO;
import oracle.demo.oow.bd.to.MovieTO;

import oracle.demo.oow.bd.util.KeyUtil;
import oracle.demo.oow.bd.util.StringUtil;


import oracle.kv.Key;


public class MovieCastFormatter extends BaseFormatter{

    private String title = null;
    private String oldTitle = null;
    private int lineCount = 0;
    

    public MovieCastFormatter() {
        super();
    }

    /**
     * This method reads ratings.list file from metadata\imdb folder and
     * associate poster_path from TMDb to IMDb movies.
     * @throws IOException
     */
    public void readIMDbMovieFile(String fileName) throws IOException {

        FileReader fr = null;
        CastTO castTO = null;
        BaseDAO baseDAO = null;

        try {

            baseDAO = new BaseDAO();

            fr = new FileReader(fileName);
            BufferedReader br = new BufferedReader(fr);
            String line = null;

            //Construct MovieTO from JSON object
            while ((line = br.readLine()) != null) {

                lineCount++;

                /**
                 * Make sure line is not empty
                 */
                if (StringUtil.isNotEmpty(line)) {
                    if (this.hasCastInfo(line)) {
                        castTO = this.getCast(line);
                    } else if (castTO != null) {
                        //check to make sure there is no '{' or '(TV)' in the line
                        if (super.isValidMovieLine(line)) {
                            castTO =
                                    this.getCast(castTO.getName() + "\t" + line);

                        }

                    } else {
                        if (castTO == null) {
                            System.out.println(">>>> : " + line);
                        }
                    } //EOF if (this.hasCastInfo(line)) {


                } //EOF if (StringUtil.isNotEmpty(line)) {
            } //while(line=br.readLine()!=null)

        } catch (Exception e) {
            e.printStackTrace();
        }
    } //readIMDbMovieFile

    

   

    public CastTO getCast(String line) {

        String name = null;
        String info = null;
        String character = "";
        String movieKey = null;
        String movieJsonTxt = null;
        String castJsonTxt = null;

        CastTO castTO = null;
        CastMovieTO castMovieTO = null;

        BaseDAO baseDAO = new BaseDAO();
        MovieTO movieTO = null;

        Key kvKey = null;

        int start = -1;
        int end = -1;
        //Many movie Casts don't have order information, so give them -1 just so 
        //they don't show up high in the list
        int order = -1;
        int id = -1;
        int movieId = -1;

        try {

            StringTokenizer st = new StringTokenizer(line, "\t");
            name = st.nextToken();
            //info is where movieName, date, character and order information exit
            info = st.nextToken();

            //System.out.println(name + "|" + info);

            //check if name has comma
            name = super.getFormatedName(name);

            //get the character information
            start = info.lastIndexOf('[');
            end = info.lastIndexOf(']');
            if (start > -1 && end > -1) {
                character = info.substring(start + 1, end);
            }

            //get the order information
            start = info.lastIndexOf('<');
            end = info.lastIndexOf('>');
            if (start > -1 && end > -1) {
                order = Integer.parseInt(info.substring(start + 1, end));
            }

            //get the title information with date
            title = super.getTitleWithDate(info);
            

            //get movie-id from the title only if title is different from the last one
            if (StringUtil.isNotEmpty(title)) {

                if (!title.equals(oldTitle)) {

                    // Get movie-key that will be used for lookup into KV-Store
                    movieKey = getMovieKey(title);

                    if (StringUtil.isNotEmpty(movieKey)) {

                        //Create new CastTO and assign name to it
                        castTO = new CastTO();
                        castTO.setName(name);
                        //create a uique cast id and assign to castTO
                        id = super.getUniqueCastId(name);
                        castTO.setId(id);

                        // Read movie information from KV_Store
                        movieJsonTxt = baseDAO.get(movieKey);

                        //if movie by key exist then get should return JsonTxt
                        if (StringUtil.isNotEmpty(movieJsonTxt)) {
                            movieTO = new MovieTO(movieJsonTxt);
                            movieId = movieTO.getId();


                            //check if cast JSON txt already exist in KV-store
                            kvKey = KeyUtil.getCastInfoKey(id);
                            castJsonTxt = baseDAO.get(kvKey);

                            if (StringUtil.isNotEmpty(castJsonTxt)) {
                                castTO = new CastTO(castJsonTxt);
                            }

                            //add castMovieTO into CastTO
                            castMovieTO = new CastMovieTO();
                            castTO.addCastMovieTO(castMovieTO);

                            //add movie related information
                            castMovieTO.setCharacter(character);
                            castMovieTO.setOrder(order);
                            castMovieTO.setId(movieId);

                            //save cast json txt into KV-store
                            baseDAO.put(kvKey, castTO.getJsonTxt());

                            System.out.println(lineCount + " " +
                                               castTO.getJsonTxt());

                        } //EOF if (StringUtil.isNotEmpty(movieJsonTxt)) {

                    } //if (StringUtil.isNotEmpty(movieJsonTxt
                } //EOF if (!title.equals(oldTitle)) {

            } //if (StringUtil.isNotEmpty(title) )

            oldTitle = title;

        } catch (Exception e) {
            e.getMessage();
        }

        return castTO;
    } //getCast

    
    

    

    public void parseCastInformation() {
        try {
            /**
             * Read the metadata/imdb/actors.list file and store cast-to-movie
             * json objects into KV-Store
             */
            //this.readIMDbMovieFile(Constant.IMDB_ACTOR_FILE_NAME);
            /**
             * Read the metadata/imdb/actresses.list file and store cast-to-movie
             * json objects into KV-Store
             */
            //this.readIMDbMovieFile(Constant.IMDB_ACTRESS_FILE_NAME);

            /**
             * After actors.list is all read, write the cast-to-movie json
             * objects to file metadata/imdb/movie-cast.out
             */
            super.writeMovieInfo(Constant.IMDB_MOVIE_CAST_FILE_NAME, KeyConstant.CAST_TABLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    } //parseCastInformation

    public static void main(String[] args) {
        MovieCastFormatter mccf = new MovieCastFormatter();
        mccf.parseCastInformation();


    }
}
