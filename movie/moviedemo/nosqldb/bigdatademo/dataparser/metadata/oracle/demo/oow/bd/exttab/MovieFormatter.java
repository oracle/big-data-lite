package oracle.demo.oow.bd.exttab;

import java.util.List;

import oracle.demo.oow.bd.dao.MovieDAO;
import oracle.demo.oow.bd.to.MovieTO;

import oracle.demo.oow.bd.util.StringUtil;

import oracle.kv.KVStore;
import oracle.kv.KeyValueVersion;
import oracle.kv.Value;
import oracle.kv.exttab.Formatter;

public class MovieFormatter implements Formatter{
    
    MovieDAO movieDAO = null;
        
    public MovieFormatter() {
        super();
        movieDAO = new MovieDAO();
    }


    public String toOracleLoaderFormat(KeyValueVersion kvv,
                                       KVStore kvStore) {
        final Value value = kvv.getValue();

        String movieJsonTxt = null;
        MovieTO movieTO = null;
        String returnStr = null;

        if (value!=null) {
            movieTO = movieDAO.getMovieTO(value);
            
            returnStr = movieTO.toString();
        }

        return returnStr;
    }//toOracleLoaderFormat
    

    public static void main(String[] args) {
        MovieDAO mDAO = new MovieDAO();
        List<MovieTO> movieList = mDAO.getMovies();
        for (MovieTO movieTO : movieList) {
            System.out.println(movieTO.toString());
        }
    }//main
}
