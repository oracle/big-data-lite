import module "oxh:avro";
import module "oxh:text";
import module "oxh:kv-table";
import module "oxh:solr";
import module "oxh:json";

for $log_line in text:collection(oxh:property("movieapp.log"))

let $log_record := json:parse-as-xml($log_line)

let $movie_id := data($log_record/json:get("movieId"))

where exists($movie_id)

group by $movie_id

let $movie := kv-table:get-jsontext("movie", '{ "id":' || $movie_id || '}')

where exists($movie)

let $m := json:parse-as-xml($movie)

let $original_title := data($m/json:get("original_title"))
let $overview := data($m/json:get("overview"))
let $release_date := data($m/json:get("release_date"))
let $poster_path := data($m/json:get("poster_path"))
let $vote_count := data($m/json:get("vote_count"))

let $activity_count := count($log_record)

(:
return oxh:println($movie_id || "[" || $original_title || "][ " || $release_date || " ][" || $poster_path || " ][" || $vote_count)
:)

return solr:put
(
  <doc boost="{ $vote_count }">
    <field name="id">{ $movie_id }</field>
    <field name="movie_title_en">{ $original_title }</field>
    <field name="movie_summary_en">{ $overview }</field>
    <field name="box_office">{ $vote_count }</field>
    <field name="movieplex_popularity">{ $activity_count }</field>
    <field name="poster_path_s">{ $poster_path }</field>
    <field name="year">{ $release_date }</field>
  </doc>
)
