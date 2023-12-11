package it.polimi.nsds.spark.lab.cities;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.StreamingQueryException;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import static org.apache.spark.sql.functions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class Cities {
    public static void main(String[] args) throws TimeoutException {
        final String master = args.length > 0 ? args[0] : "local[4]";
        final String filePath = args.length > 1 ? args[1] : "./";

        final SparkSession spark = SparkSession
                .builder()
                .master(master)
                .appName("SparkEval")
                .getOrCreate();
        spark.sparkContext().setLogLevel("ERROR");

        final List<StructField> citiesRegionsFields = new ArrayList<>();
        citiesRegionsFields.add(DataTypes.createStructField("city", DataTypes.StringType, false));
        citiesRegionsFields.add(DataTypes.createStructField("region", DataTypes.StringType, false));
        final StructType citiesRegionsSchema = DataTypes.createStructType(citiesRegionsFields);

        final List<StructField> citiesPopulationFields = new ArrayList<>();
        citiesPopulationFields.add(DataTypes.createStructField("id", DataTypes.IntegerType, false));
        citiesPopulationFields.add(DataTypes.createStructField("city", DataTypes.StringType, false));
        citiesPopulationFields.add(DataTypes.createStructField("population", DataTypes.IntegerType, false));
        final StructType citiesPopulationSchema = DataTypes.createStructType(citiesPopulationFields);

        final Dataset<Row> citiesPopulation = spark
                .read()
                .option("header", "true")
                .option("delimiter", ";")
                .schema(citiesPopulationSchema)
                .csv(filePath + "files/cities/cities_population.csv");

        final Dataset<Row> citiesRegions = spark
                .read()
                .option("header", "true")
                .option("delimiter", ";")
                .schema(citiesRegionsSchema)
                .csv(filePath + "files/cities/cities_regions.csv");

        // TODO: add code here if necessary

        final Dataset<Row> q1 = citiesRegions
                .join(citiesPopulation, citiesRegions.col("city").equalTo(citiesPopulation.col("city")))
                .groupBy("region")
                .sum("population")
                .orderBy("sum(population)");

        q1.show();

        final Dataset<Row> q2 = citiesRegions
                .join(citiesPopulation, "city")
                .groupBy("region")
                .agg(count(citiesPopulation.col("city")), max("population"));
        // .groupBy("region")
        // .count();

        q2.show();

        // JavaRDD where each element is an integer and represents the population of a
        // city
        JavaRDD<Integer> population = citiesPopulation.toJavaRDD().map(r -> r.getInt(2));
        // TODO: add code here to produce the output for query Q3

        int populationSum = population.reduce((a, b) -> a + b);
        int year = 2023;
        System.out.println(populationSum);

        while (populationSum < 100000000) {
            population = (JavaRDD<Integer>) population.map(
                    i -> {
                        if (i < 1000) {
                            return (int) Math.round(i - i * 0.01);
                        } else {
                            return (int) Math.round(1.01 * i);
                        }
                    });
            populationSum = population.reduce((a, b) -> a + b);
            population.cache();
            year++;
            System.out.println("year" + year + " " + populationSum);
        }

        // Bookings: the value represents the city of the booking
        final Dataset<Row> bookings = spark
                .readStream()
                .format("rate")
                .option("rowsPerSecond", 100)
                .load();

        Dataset<Row> bookingsDF = bookings.toDF("timestamp", "value");

        final Dataset<Row> joinedDataset = citiesRegions
                .join(citiesPopulation, citiesRegions.col("city").equalTo(citiesPopulation.col("city")))
                .select(citiesPopulation.col("id"),
                        citiesPopulation.col("city"),
                        citiesRegions.col("region"),
                        citiesPopulation.col("population"));

        final StreamingQuery q4 = bookingsDF
                .join(joinedDataset,
                        bookingsDF.col("value").equalTo(joinedDataset.col("id")))
                .drop("value", "population")
                .groupBy(
                        window(col("timestamp"), "30 seconds", "5 seconds"),
                        col("region"))
                .count()
                .writeStream()
                .outputMode("update")
                .format("console")
                .start();
        ; // TODO query Q4

        try {
            q4.awaitTermination();
        } catch (final StreamingQueryException e) {
            e.printStackTrace();
        }

        spark.close();
    }
}