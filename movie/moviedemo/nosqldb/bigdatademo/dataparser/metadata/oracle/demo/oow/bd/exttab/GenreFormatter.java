package oracle.demo.oow.bd.exttab;

import java.util.List;

import oracle.demo.oow.bd.dao.GenreDAO;
import oracle.demo.oow.bd.to.GenreTO;

import oracle.demo.oow.bd.util.StringUtil;

import oracle.kv.KVStore;
import oracle.kv.KeyValueVersion;
import oracle.kv.Value;
import oracle.kv.exttab.Formatter;

public class GenreFormatter implements Formatter {
    GenreDAO genreDAO = new GenreDAO();
    
    public GenreFormatter() {
        super();
    }


    public String toOracleLoaderFormat(KeyValueVersion kvv, KVStore kvStore) {

        final Value value = kvv.getValue();

        GenreTO genreTO = null;
        String returnStr = null;

        if (value!=null) {
            
            genreTO = genreDAO.getGenreTO(value);
            returnStr = genreTO.toString();
        }

        return returnStr;
    } //toOracleLoaderFormat


    public static void main(String[] args) {
        GenreDAO genreDAO = new GenreDAO();
        //Show all the Genres
        List<GenreTO> genreList = genreDAO.getGenres();
        for (GenreTO genreTO : genreList) {
            System.out.println(genreTO.toString());
        }
    } //main
}
