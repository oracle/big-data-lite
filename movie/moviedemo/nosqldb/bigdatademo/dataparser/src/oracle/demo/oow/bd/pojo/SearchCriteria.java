package oracle.demo.oow.bd.pojo;

public class SearchCriteria {
    
    private boolean posterPath = true;
    private int releasedYear = 1990;
    private int voteCount = 1000;
    private double popularity = 7;

    public SearchCriteria() {
        super();
    }
    
    public void setPosterPath(boolean posterPath) {
        this.posterPath = posterPath;
    }

    public boolean isPosterPath() {
        return posterPath;
    }

    public void setReleasedYear(int releasedYear) {
        this.releasedYear = releasedYear;
    }

    public int getReleasedYear() {
        return releasedYear;
    }

    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public void setPopularity(double popularity) {
        this.popularity = popularity;
    }

    public double getPopularity() {
        return popularity;
    }
}
