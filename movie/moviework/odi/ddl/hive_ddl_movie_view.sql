CREATE VIEW movie_view AS SELECT t1.movie_id, t1.title, t1.year, t1.budget, t1.gross, t1.plot_summary  FROM default.movie_updates t1
JOIN
    (SELECT m.movie_id, max(m.ts) max_modified 
     FROM default.movie_updates m
     GROUP BY m.movie_id) s 
ON t1.movie_id = s.movie_id AND t1.ts = s.max_modified
where t1.op != 'D'
