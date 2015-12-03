package oracle.demo.oow.bd.loader.imdb.formatter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.util.StringTokenizer;

import oracle.demo.oow.bd.constant.Constant;
import oracle.demo.oow.bd.constant.JsonConstant;
import oracle.demo.oow.bd.constant.KeyConstant;
import oracle.demo.oow.bd.dao.BaseDAO;
import oracle.demo.oow.bd.to.CrewTO;
import oracle.demo.oow.bd.to.MovieTO;
import oracle.demo.oow.bd.util.KeyUtil;
import oracle.demo.oow.bd.util.StringUtil;

import oracle.kv.Key;

public class MovieCrewFormatter extends BaseFormatter {

    private String title = null;
    private int lineCount = 0;

    public MovieCrewFormatter() {
        super();
    }

    /**
     * This method reads ratings.list file from metadata\imdb folder and
     * associate poster_path from TMDb to IMDb movies.
     * @throws IOException
     */
    public void readIMDbMovieFile(String fileName,
                                  String job) throws IOException {

        FileReader fr = null;
        CrewTO crewTO = null;
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
                        crewTO = this.getCrew(line, job);
                    } else if (crewTO != null) {
                        //check to make sure there is no '{' or '(TV)' in the line
                        if (super.isValidMovieLine(line)) {
                            crewTO =
                                    this.getCrew(crewTO.getName() + "\t" + line,
                                                 job);

                        }

                    } else {
                        if (crewTO == null) {
                            System.out.println(">>>> : " + line);
                        }
                    }
                } //while(line=br.readLine()!=null)

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    } //readIMDbMovieFile

    public CrewTO getCrew(String line, String job) {

        String name = null;
        String info = null;
        String movieKey = null;
        String movieJsonTxt = null;
        String crewJsonTxt = null;

        CrewTO crewTO = null;

        BaseDAO baseDAO = new BaseDAO();
        MovieTO movieTO = null;
        int movieId = -1;

        Key kvKey = null;
        int id = -1;

        try {

            StringTokenizer st = new StringTokenizer(line, "\t");
            name = st.nextToken();
            //info is where movieName, date, character and order information exit
            info = st.nextToken();

            //check if name has comma
            name = super.getFormatedName(name);

            //get the title information with date
            title = super.getTitleWithDate(info);

            //get movie key for lookup
            movieKey = super.getMovieKey(title);

            if (StringUtil.isNotEmpty(movieKey)) {

                //System.out.println("*** " + movieKey);
                // Read movie information from KV_Store
                movieJsonTxt = baseDAO.get(movieKey);


                //create unique id for the crew
                id = super.getUniqueCastId(name);
                crewTO = new CrewTO();
                crewTO.setName(name);
                crewTO.setId(id);
                crewTO.setJob(job);

                //if movie by key exist then get should return JsonTxt
                if (StringUtil.isNotEmpty(movieJsonTxt)) {
                    movieTO = new MovieTO(movieJsonTxt);
                    movieId = movieTO.getId();

                    //check if crew JSON txt already exist in KV-store
                    kvKey = KeyUtil.getCrewInfoKey(id);
                    crewJsonTxt = baseDAO.get(kvKey);

                    if (StringUtil.isNotEmpty(crewJsonTxt)) {
                        crewTO = new CrewTO(crewJsonTxt);
                    }

                    //add castMovieTO into CastTO
                    crewTO.addMovieId(movieId);

                    //save crew json txt into KV-store
                    baseDAO.put(kvKey, crewTO.getJsonTxt());
                    //TODO - comment out
                    //baseDAO.delete(kvKey);

                    System.out.println(lineCount + " " + crewTO.getJsonTxt());


                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return crewTO;
    }


    public void parseCrewInformation() {
        try {
            /**
             * Read the metadata/imdb/directors.list file and store json objects
             * into KV-Store
             */
            this.readIMDbMovieFile(Constant.IMDB_DIRECTOR_FILE_NAME,
                                   JsonConstant.DIRECTOR);

            /**
             * Read the metadata/imdb/writers.list file and store json objects
             * into KV-Store
             */
            this.readIMDbMovieFile(Constant.IMDB_WRITER_FILE_NAME,
                                   JsonConstant.WRITER);

            /**
             * After actors.list is all read, write the cast-to-movie json
             * objects to file metadata/imdb/movie-cast.out
             */
            writeMovieInfo(Constant.IMDB_MOVIE_CREW_FILE_NAME,
                           KeyConstant.CREW_MOVIE_KEY_PREFIX);
        } catch (Exception e) {
            e.printStackTrace();
        }
    } //parseCastInformation


    public static void main(String[] args) {
        CrewTO crewTO = null;
        MovieCrewFormatter mcf = new MovieCrewFormatter();
        mcf.parseCrewInformation();
        //crewTO = mcf.getCrew("Abdallah, Fatima	Soul (2011)");

    } //main


}
