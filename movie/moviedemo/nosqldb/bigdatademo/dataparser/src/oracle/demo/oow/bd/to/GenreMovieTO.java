package oracle.demo.oow.bd.to;

import java.util.ArrayList;
import java.util.List;

import oracle.demo.oow.bd.constant.Constant;

public class GenreMovieTO extends BaseTO{
    private GenreTO genreTO = null;
    private List<MovieTO> movieList = new ArrayList<MovieTO>();
    private int genreId;
    private int movieId;

    public GenreMovieTO() {
        super();
    }

    public void setGenreTO(GenreTO genreTO) {
        this.genreTO = genreTO;
    }

    public GenreTO getGenreTO() {
        return genreTO;
    }

    public void setMovieList(List<MovieTO> movieList) {
        this.movieList = movieList;
    }

    public List<MovieTO> getMovieList() {
        return movieList;
    }
    
    public void addMovieTO(MovieTO movieTO){
        this.movieList.add(movieTO);    
    }

    public void setGenreId(int genreId) {
        this.genreId = genreId;
    }

    public int getGenreId() {
        return genreId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public int getMovieId() {
        return movieId;
    }
    public String toString(){
        String genreMovieStr = "" + 
                this.getGenreId() + Constant.DELIMITER + this.getMovieId();

        return genreMovieStr;
    }

    @Override
    public String toJsonString() {
        // TODO Implement this method
        return null;
    }
}
