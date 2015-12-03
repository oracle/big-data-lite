package oracle.demo.oow.bd.pojo;

public enum ActivityType {
    RATE_MOVIE(1),
    COMPLETED_MOVIE(2),
    PAUSED_MOVIE(3),
    STARTED_MOVIE(4),
    BROWSED_MOVIE(5),
    LIST_MOVIES(6),
    SEARCH_MOVIE(7),
    LOGIN(8),
    LOGOUT(9),
    PURCHASED_MOVIE(11);

    private int activity;

    private ActivityType(int activity) {
        this.activity = activity;
    }

    public static ActivityType getType(int activity) {
        ActivityType type = null;
        for (ActivityType at : ActivityType.values()) {
            if (activity == at.activity) {
                type = at;
                break;
            }
        } //EOF for

        return type;
    } //valueOf

    public int getValue() {

        switch (this) {
        case RATE_MOVIE:
            activity = 1;
            break;
        case COMPLETED_MOVIE:
            activity = 2;
            break;
        case PAUSED_MOVIE:
            activity = 3;
            break;
        case STARTED_MOVIE:
            activity = 4;
            break;
        case BROWSED_MOVIE:
            activity = 5;
            break;
        case LIST_MOVIES:
            activity = 6;
            break;
        case SEARCH_MOVIE:
            activity = 7;
            break;
        case LOGIN:
            activity = 8;
            break;
        case LOGOUT:
            activity = 9;
            break;
        case PURCHASED_MOVIE:
            activity = 11;
            break;
        }

        return activity;
    }

    public static void main(String[] args) {
        ActivityType type = ActivityType.COMPLETED_MOVIE;
        System.out.println(type.getValue());
    }
}
