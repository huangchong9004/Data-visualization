CREATE DATABASE movie;
select * from movie.movies;
select 12 from movie.movies;
select * from movie.movie_cast;
show columns from movie.movies;
ALTER TABLE movie.movies RENAME COLUMN Null  TO 'id' ;