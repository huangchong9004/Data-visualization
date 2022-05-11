// Databricks notebook source
// STARTER CODE - DO NOT EDIT THIS CELL
import org.apache.spark.sql.functions.desc
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import spark.implicits._
import org.apache.spark.sql.expressions.Window

// COMMAND ----------

// STARTER CODE - DO NOT EDIT THIS CELL
val customSchema = StructType(Array(StructField("lpep_pickup_datetime", StringType, true), StructField("lpep_dropoff_datetime", StringType, true), StructField("PULocationID", IntegerType, true), StructField("DOLocationID", IntegerType, true), StructField("passenger_count", IntegerType, true), StructField("trip_distance", FloatType, true), StructField("fare_amount", FloatType, true), StructField("payment_type", IntegerType, true)))

// COMMAND ----------

// STARTER CODE - YOU CAN LOAD ANY FILE WITH A SIMILAR SYNTAX.
val df = spark.read
   .format("com.databricks.spark.csv")
   .option("header", "true") // Use first line of all files as header
   .option("nullValue", "null")
   .schema(customSchema)
   .load("/FileStore/tables/nyc_tripdata.csv") // the csv file which you want to work with
   .withColumn("pickup_datetime", from_unixtime(unix_timestamp(col("lpep_pickup_datetime"), "MM/dd/yyyy HH:mm")))
   .withColumn("dropoff_datetime", from_unixtime(unix_timestamp(col("lpep_dropoff_datetime"), "MM/dd/yyyy HH:mm")))
   .drop($"lpep_pickup_datetime")
   .drop($"lpep_dropoff_datetime")

// COMMAND ----------

// LOAD THE "taxi_zone_lookup.csv" FILE SIMILARLY AS ABOVE. CAST ANY COLUMN TO APPROPRIATE DATA TYPE IF NECESSARY.

// ENTER THE CODE BELOW
val zoneSchema = StructType(Array(StructField("LocationID", IntegerType, true), StructField("Borough", StringType, true), StructField("Zone", StringType, true), StructField("service_zone", StringType, true)))
val df_zone = spark.read
   .format("com.databricks.spark.csv")
   .option("header", "true") // Use first line of all files as header
   .option("nullValue", "null")
   .schema(zoneSchema)
   .load("/FileStore/tables/taxi_zone_lookup.csv") // the csv file which you want to work with
//df_zone.show(5)

// COMMAND ----------

// STARTER CODE - DO NOT EDIT THIS CELL
// Some commands that you can use to see your dataframes and results of the operations. You can comment the df.show(5) and uncomment display(df) to see the data differently. You will find these two functions useful in reporting your results.
// display(df)
df.show(5) // view the first 5 rows of the dataframe

// COMMAND ----------

// STARTER CODE - DO NOT EDIT THIS CELL
// Filter the data to only keep the rows where "PULocationID" and the "DOLocationID" are different and the "trip_distance" is strictly greater than 2.0 (>2.0).

// VERY VERY IMPORTANT: ALL THE SUBSEQUENT OPERATIONS MUST BE PERFORMED ON THIS FILTERED DATA

val df_filter = df.filter($"PULocationID" =!= $"DOLocationID" && $"trip_distance" > 2.0)
df_filter.show(5)

// COMMAND ----------

// PART 1a: The top-5 most popular drop locations - "DOLocationID", sorted in descending order - if there is a tie, then one with lower "DOLocationID" gets listed first
// Output Schema: DOLocationID int, number_of_dropoffs int 

// Hint: Checkout the groupBy(), orderBy() and count() functions.

// ENTER THE CODE BELOW
val df_top5DO = df_filter.groupBy("DOLocationID").count().orderBy(desc("count"), asc("DOLocationID")).limit(5)
val df_1a = df_top5DO.withColumnRenamed("count", "number_of_dropoffs")
//df_1a.show()

// COMMAND ----------

// PART 1b: The top-5 most popular pickup locations - "PULocationID", sorted in descending order - if there is a tie, then one with lower "PULocationID" gets listed first 
// Output Schema: PULocationID int, number_of_pickups int

// Hint: Code is very similar to part 1a above.

// ENTER THE CODE BELOW
val df_top5PU = df_filter.groupBy("PULocationID").count().orderBy(desc("count"), asc("PULocationID")).limit(5)
val df_1b = df_top5PU.withColumnRenamed("count", "number_of_dropoffs")
//df_1b.show()

// COMMAND ----------

// PART 2: List the top-3 locations with the maximum overall activity, i.e. sum of all pickups and all dropoffs at that LocationID. In case of a tie, the lower LocationID gets listed first.
// Output Schema: LocationID int, number_activities int

// Hint: In order to get the result, you may need to perform a join operation between the two dataframes that you created in earlier parts (to come up with the sum of the number of pickups and dropoffs on each location). 

// ENTER THE CODE BELOW
val df_PUID = df_filter.groupBy("PULocationID").count().withColumnRenamed("count", "count_PU")
val df_DOID = df_filter.groupBy("DOLocationID").count().withColumnRenamed("count", "count_DO")
val df_join = df_PUID.join(df_DOID, df_PUID("PULocationID") === df_DOID("DOLocationID"))
val df_join2 = df_join.withColumn("number_activities", df_join("count_PU") + df_join("count_DO")).withColumnRenamed("PULocationID", "LocationID")
//df_join.show()
val df_2 = df_join2.select(df_join2("LocationID"), df_join2("number_activities")).orderBy(desc("number_activities"), asc("LocationID")).limit(3)
//df_2.show()

// COMMAND ----------

// PART 3: List all the boroughs in the order of having the highest to lowest number of activities (i.e. sum of all pickups and all dropoffs at that LocationID), along with the total number of activity counts for each borough in NYC during that entire period of time.
// Output Schema: Borough string, total_number_activities int

// Hint: You can use the dataframe obtained from the previous part, and will need to do the join with the 'taxi_zone_lookup' dataframe. Also, checkout the "agg" function applied to a grouped dataframe.

// ENTER THE CODE BELOW
val df_borough = df_join2.join(df_zone, df_join2("LocationID") === df_zone("LocationID")).groupBy("Borough").agg(sum("number_activities")).withColumnRenamed("sum(number_activities)", "total_number_activities").orderBy(desc("total_number_activities"))
//df_borough.show()

// COMMAND ----------

// PART 4: List the top 2 days of week with the largest number of (daily) average pickups, along with the values of average number of pickups on each of the two days. The day of week should be a string with its full name, for example, "Monday" - not a number 1 or "Mon" instead.
// Output Schema: day_of_week string, avg_count float

// Hint: You may need to group by the "date" (without time stamp - time in the day) first. Checkout "to_date" function.

// ENTER THE CODE BELOW
val df_topDays = df_filter.withColumn("date",to_date(df_filter("pickup_datetime"))).groupBy("date").agg(count("pickup_datetime"))
//df_topDays.show()
val df_topWKDays = df_topDays.withColumn("day_of_week",date_format(df_topDays("date"), "EEEE")).groupBy("day_of_week").agg(mean("count(pickup_datetime)")).withColumnRenamed("avg(count(pickup_datetime))", "avg_count").orderBy(desc("avg_count")).limit(2)
//df_topWKDays.show()

// COMMAND ----------

// PART 5: For each particular hour of a day (0 to 23, 0 being midnight) - in their order from 0 to 23, find the zone in Brooklyn borough with the LARGEST number of pickups. 
// Output Schema: hour_of_day int, zone string, max_count int

// Hint: You may need to use "Window" over hour of day, along with "group by" to find the MAXIMUM count of pickups

// ENTER THE CODE BELOW
val df_hour = df_filter.withColumn("hour_of_day",hour(df_filter("pickup_datetime")))
val df_join5 = df_hour.join(df_zone, df_hour("PULocationID") === df_zone("LocationID")).filter($"Borough" === "Brooklyn").groupBy("hour_of_day", "Zone").agg(count("Zone")).orderBy(asc("hour_of_day"), desc("count(Zone)"))
val window = Window.partitionBy($"hour_of_day").orderBy($"count(Zone)".desc)
val df_5 = df_join5.withColumn("rn", row_number.over(window)).where($"rn" === 1).drop($"rn").withColumnRenamed("count(Zone)", "max_count").withColumnRenamed("Zone", "zone").orderBy(asc("hour_of_day"))
//df_5.show(24)

// COMMAND ----------

// PART 6 - Find which 3 different days of the January, in Manhattan, saw the largest percentage increment in pickups compared to previous day, in the order from largest increment % to smallest increment %. 
// Print the day of month along with the percent CHANGE (can be negative), rounded to 2 decimal places, in number of pickups compared to previous day.
// Output Schema: day int, percent_change float


// Hint: You might need to use lag function, over a window ordered by day of month.

// ENTER THE CODE BELOW
val df_allDays = df_filter.withColumn("day",to_date(df_filter("pickup_datetime"))).withColumn("month", month(df_filter("pickup_datetime"))).filter($"month" === 1)
val df_join6 = df_allDays.join(df_zone, df_allDays("PULocationID") === df_zone("LocationID")).filter($"Borough" === "Manhattan").groupBy("day").agg(count("day"))
val window = Window.orderBy($"day")
val df_lag = df_join6.withColumn("new_count", lag("count(day)", 1, 0).over(window)).withColumn("day_of_month", dayofmonth(df_join6("day")))
val df_pc = df_lag.withColumn("percent_change1", (df_lag("count(day)") - df_lag("new_count")) *100 /df_lag("new_count")).withColumn("percent_change", round(col("percent_change1"), 2)).filter($"day_of_month" =!= 1)
val df_pc2 = df_pc.drop($"day").withColumnRenamed("day_of_month", "day")
val df_6 = df_pc2.select(df_pc2("day"), df_pc2("percent_change")).orderBy(desc("percent_change")).limit(3)
//df_6.show()

// COMMAND ----------


