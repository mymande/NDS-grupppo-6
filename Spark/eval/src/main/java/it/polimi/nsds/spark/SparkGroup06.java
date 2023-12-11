package it.polimi.nsds.spark;

import org.apache.spark.sql.*;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.window;

/*
 * Group number: 06
 *
 * Group members
 *  - Andrea Carbonetti
 *  - Federico Mandelli
 *  - Pasquale Scalise
 */

public class SparkGroup06 {
        private static final int numCourses = 3000;

        public static void main(String[] args) throws TimeoutException {
                final String master = args.length > 0 ? args[0] : "local[4]";
                final String filePath = args.length > 1 ? args[1] : "./";

                final SparkSession spark = SparkSession
                                .builder()
                                .master(master)
                                .appName("SparkEval")
                                .getOrCreate();
                spark.sparkContext().setLogLevel("ERROR");

                final List<StructField> profsFields = new ArrayList<>();
                profsFields.add(DataTypes.createStructField("prof_name", DataTypes.StringType, false));
                profsFields.add(DataTypes.createStructField("course_name", DataTypes.StringType, false));
                final StructType profsSchema = DataTypes.createStructType(profsFields);

                final List<StructField> coursesFields = new ArrayList<>();
                coursesFields.add(DataTypes.createStructField("course_name", DataTypes.StringType, false));
                coursesFields.add(DataTypes.createStructField("course_hours", DataTypes.IntegerType, false));
                coursesFields.add(DataTypes.createStructField("course_students", DataTypes.IntegerType, false));
                final StructType coursesSchema = DataTypes.createStructType(coursesFields);

                final List<StructField> videosFields = new ArrayList<>();
                videosFields.add(DataTypes.createStructField("video_id", DataTypes.IntegerType, false));
                videosFields.add(DataTypes.createStructField("video_duration", DataTypes.IntegerType, false));
                videosFields.add(DataTypes.createStructField("course_name", DataTypes.StringType, false));
                final StructType videosSchema = DataTypes.createStructType(videosFields);

                // Professors: prof_name, course_name
                final Dataset<Row> profs = spark
                                .read()
                                .option("header", "false")
                                .option("delimiter", ",")
                                .schema(profsSchema)
                                .csv(filePath + "files/profs.csv");

                // Courses: course_name, course_hours, course_students
                final Dataset<Row> courses = spark
                                .read()
                                .option("header", "false")
                                .option("delimiter", ",")
                                .schema(coursesSchema)
                                .csv(filePath + "files/courses.csv");

                // Videos: video_id, video_duration, course_name
                final Dataset<Row> videos = spark
                                .read()
                                .option("header", "false")
                                .option("delimiter", ",")
                                .schema(videosSchema)
                                .csv(filePath + "files/videos.csv");

                // Visualizations: value, timestamp
                // value represents the video id
                final Dataset<Row> visualizations = spark
                                .readStream()
                                .format("rate")
                                .option("rowsPerSecond", 10)
                                .load()
                                .withColumn("value", col("value").mod(numCourses));

                /**
                 * TODO: Enter your code below
                 */

                /*
                 * Query Q1. Compute the total number of lecture hours per prof
                 */

                /* Caching tables that are used more than once */
                courses.cache();
                videos.cache();

                /* We don't consider events with a delay greater than 1 hour */
                visualizations.withWatermark("timestamp", "1 hour");

                final Dataset<Row> q1 = courses
                                .join(profs, "course_name")
                                .groupBy("prof_name")
                                .sum("course_hours");

                q1.show();

                /*
                 * Query Q2. For each course, compute the total duration of all the
                 * visualizations of videos of that course,
                 * computed over a minute, updated every 10 seconds
                 */

                final Dataset<Row> watchedVideos = videos
                                .join(visualizations, videos.col("video_id").equalTo(visualizations.col("value")));

                final StreamingQuery q2 = watchedVideos
                                .groupBy(col("course_name"), window(col("timestamp"), "60 seconds", "10 seconds"))
                                .sum("video_duration")
                                .withColumnRenamed("sum(video_duration)", "total_watchtime")
                                .select("course_name", "total_watchtime")
                                .writeStream()
                                .outputMode("update")
                                .format("console")
                                .start();

                /*
                 * Query Q3. For each video, compute the total number of visualizations of that
                 * video
                 * with respect to the number of students in the course in which the video is
                 * used.
                 */

                final StreamingQuery q3 = watchedVideos
                                .groupBy("video_id", "course_name")
                                .count()
                                .join(courses, "course_name")
                                .withColumn("ratio", col("count").divide(col("course_students")))
                                .select("video_id", "ratio")
                                .writeStream()
                                .outputMode("update")
                                .format("console")
                                .start();

                try {
                        q2.awaitTermination();
                        q3.awaitTermination();
                } catch (final StreamingQueryException e) {
                        e.printStackTrace();
                }

                /* Empty the cache */
                courses.unpersist();
                videos.unpersist();

                spark.close();
        }
}