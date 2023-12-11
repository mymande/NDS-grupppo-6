package it.polimi.nsds.spark.lab.friends;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;

/**
 * Implement an iterative algorithm that implements the transitive closure of
 * friends
 * (people that are friends of friends of ... of my friends).
 *
 * Set the value of the flag useCache to see the effects of caching.
 */
public class FriendsComputation {
    private static final boolean useCache = true;

    public static void main(String[] args) {
        final String master = args.length > 0 ? args[0] : "local[4]";
        final String filePath = args.length > 1 ? args[1] : "./";
        final String appName = useCache ? "FriendsCache" : "FriendsNoCache";

        final SparkSession spark = SparkSession
                .builder()
                .master(master)
                .appName(appName)
                .getOrCreate();
        spark.sparkContext().setLogLevel("ERROR");

        final List<StructField> fields = new ArrayList<>();
        fields.add(DataTypes.createStructField("person", DataTypes.StringType, false));
        fields.add(DataTypes.createStructField("friend", DataTypes.StringType, false));
        final StructType schema = DataTypes.createStructType(fields);

        Dataset<Row> input = spark
                .read()
                .option("header", "false")
                .option("delimiter", ",")
                .schema(schema)
                .csv(filePath + "files/friends/friends.csv");

        JavaRDD<Tuple2<String, String>> reachableFriends = input.toJavaRDD().map(r -> {
            return new Tuple2<>(r.getString(1), r.getString(2));
        });

        // TODO
        Dataset<Row> allFriends = input;
        long oldCount = 0;
        long newCount = allFriends.count();

        while (newCount > oldCount) {
            // tmp = input.count();
            // input = input.as("input2").join(input.as("input1"), col("input2.person") ===
            // col("input1.friend"))
            // .groupBy("person", "friend")
            // .count();
            // input.cache();
            Dataset<Row> newFriends = allFriends
                    .withColumnRenamed("friend", "to-join")
                    .join(input.withColumnRenamed("person", "to-join"), "to-join")
                    .drop("to-join");

            allFriends = newFriends.union(allFriends).distinct();
            oldCount = newCount;
            newCount = allFriends.count();
            allFriends.show();

        }
        // spark.close();
    }

    // private boolean isClosed(JavaRDD<Tuple2<String, String>> reachableFriends) {
    // while()
    // }
}
