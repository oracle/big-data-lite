package oracle.demo.oow.bd.exttab;

import java.util.List;

import oracle.demo.oow.bd.dao.GenreDAO;
import oracle.demo.oow.bd.to.GenreMovieTO;

import oracle.kv.KVStore;
import oracle.kv.Key;
import oracle.kv.KeyValueVersion;
import oracle.kv.exttab.Formatter;

public class GenreMovieFormatter implements Formatter {
    public GenreMovieFormatter() {
        super();
    }

    public String toOracleLoaderFormat(KeyValueVersion kvv, KVStore kvStore) {
        final Key key = kvv.getKey();
        String returnStr = null;
        String movieIdStr = null;
        String genreIdStr = null;
        GenreMovieTO genreMovieTO = new GenreMovieTO();

        if (key != null) {
            movieIdStr = key.getMinorPath().get(0);
            genreIdStr = key.getMajorPath().get(1);

            genreMovieTO.setMovieId(Integer.parseInt(movieIdStr));
            genreMovieTO.setGenreId(Integer.parseInt(genreIdStr));
            returnStr = genreMovieTO.toString();

        }

        return returnStr;
    } //toOracleLoaderFormat


    public static void main(String[] args) {
        GenreDAO genreDAO = new GenreDAO();
        //Show all the Genres
        List<GenreMovieTO> genreMovieList = genreDAO.getGenreMovies();

        //Show movies by genres
        for (GenreMovieTO genreMovieTO : genreMovieList) {
            System.out.println(genreMovieTO.toString());
        }
    } //main

}
