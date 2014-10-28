package es.care.sf

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.apache.hadoop.conf.Configuration


object Sparklean extends App {
	  
	println("Hello, sparkLean")
	  
	  // sc is an existing SparkContext.
	  
	val conf = new SparkConf().setAppName("Simple Spark Lean Application")
	val sc = new SparkContext(conf)
	val sqlContext = new SQLContext(sc)
	
	///////////////// JSON SOURCE //////////////////////////
	
	// A JSON dataset is pointed to by path.
	// The path can be either a single text file or a directory storing text files.
	val path = "userActivity.json"
	// Create a SchemaRDD from the file(s) pointed to by path
	val _activity = sqlContext.jsonFile(path)
	
	// The inferred schema can be visualized using the printSchema() method.
	_activity.printSchema()
	// root
	//  |-- age: IntegerType
	//  |-- name: StringType
	
	// Register this SchemaRDD as a table.
	_activity.registerTempTable("userActivity")
	
	// SQL statements can be run by using the sql methods provided by sqlContext		
	val activityRDD = sqlContext.sql("SELECT userId,type,substring(date,0,7) as yearMonth  FROM userActivity WHERE type is not null")
	import sqlContext._
	
	val groupBy_activityRDD = activityRDD.groupBy('type,'yearMonth)('type,'yearMonth,org.apache.spark.sql.catalyst.expressions.Sum(1) as 'totalEvents)
	
	
	////////////// HIVE SOURCE ///////////////////////
	//hive context 
	val hiveContext = new org.apache.spark.sql.hive.HiveContext(sc)
	
	val hive_activity_RDD = hiveContext.sql("SELECT count(*) from userActivity")

	
	////////////// BASE RDD SOURCE  - AS A WAY TO ACCESS MONGO DB DIRECTLY  ///////////////
	import org.bson.BSONObject
	import sqlContext.createSchemaRDD
	
	val config = new Configuration()    
    config.set("mongo.input.uri", "mongodb://business:business@fintonicdev1.cloudapp.net:27017/business.userActivity")
    val mongoRDD = sc.newAPIHadoopRDD(config, classOf[com.mongodb.hadoop.MongoInputFormat], classOf[Object], classOf[BSONObject])
    
    case class Activity(userId: String, activityType: String, yearMonth: String)
    
    val smongoRDD =       
     mongoRDD.filter( arg => arg._2.get("userId") != null && arg._2.get("type") != null && arg._2.get("date") != null).map( arg  => { 
      val userId : String = arg._2.get("userId").toString()        
      val activityType : String= arg._2.get("type").toString()
      val yearMonth = arg._2.get("date").toString().substring(0, 7)     
      Activity(userId,activityType,yearMonth) 
    })
    
    
    
    smongoRDD.registerTempTable("userActivity")
    sqlContext.sql("SELECT count(*),activityType,yearMonth from userActivity group by activityType,yearMonth").collect().foreach(println)
    
    
     	
   
  
}
