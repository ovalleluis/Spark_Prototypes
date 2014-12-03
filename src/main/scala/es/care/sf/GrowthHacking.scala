package es.care.sf

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.Row
import org.apache.spark.sql.StructType
import org.apache.spark.sql.StructField
import org.apache.spark.sql.IntegerType
import org.apache.spark.sql.TimestampType


object GrowthHacking extends App {
  
	val conf = new SparkConf().setAppName("Growth Hacking Application")
	val sc = new SparkContext(conf)
	val sqlContext = new SQLContext(sc)
	import sqlContext._
	
	
	//LOAD SOURCES
	
	val dataFilePath = "/root/growth_hacking/data/"
	  
	//SQL sources	  
	val users_path = dataFilePath + "users.csv"		
	val users_schema = StructType(
					Array( StructField("userId", IntegerType , true), 
						   StructField("registration_date", TimestampType , true), 
					       StructField("unsubscribe_data", TimestampType , true),
					       StructField("amountInvited", IntegerType , true),
					       StructField("referrerId", IntegerType , true))
					)
					
					
	val row_users_RDD = sc.textFile(users_path).map(_.split(",")).map({ r => {
	    val userId = r(0).toInt
	    val registration_date = if ( r(1).toString().equals("null")) null else r(1)
	    val unsusbcribe_date = if ( r(2).toString().equals("null")) null else r(2)
	    val amountInvited = r(3).toInt
	    val referrerId = r(4) 
		Row(userId,registration_date, unsusbcribe_date, amountInvited, referrerId)
	}})		
	val users_RDD = sqlContext.applySchema(row_users_RDD, users_schema)
	users_RDD.registerTempTable("users")
	val users_RDDAliased = users_RDD.as('u);
	
	//JSON SOURCES	
	// A JSON dataset is pointed to by path.
	// The path can be either a single text file or a directory storing text files.
	val bankRegistry_path = dataFilePath + "bankRegistry.json"
	val bankRegistry_RDD = sqlContext.jsonFile(bankRegistry_path)
	val bankRegistry_RDDAliased = bankRegistry_RDD.as('br)
	
	
	
	users_RDDAliased.join(bankRegistry_RDDAliased, org.apache.spark.sql.catalyst.plans.LeftOuter, Some("u.userId".attr === "br.userId".attr) )
	
	sqlContext.jsonFile(bankRegistry_path).registerTempTable("bankRegistry")
	
	
	val userActivity_path = dataFilePath + "userActivity.json"
	sqlContext.jsonFile(userActivity_path).registerTempTable("userActivity")
	
	val balanceSnapshot_path = dataFilePath + "balanceSnapshot.json"
	sqlContext.jsonFile(balanceSnapshot_path).registerTempTable("balanceSnapshot")
	
	val userEvent_path = dataFilePath +  "userEvent.json"
	sqlContext.jsonFile(userEvent_path).registerTempTable("userEvent")	
	
	val profiles_path = dataFilePath + "profiles.json"
	sqlContext.jsonFile(profiles_path).registerTempTable("profile")
	
	val userPrefs_path = dataFilePath + "userPrefs.json"
	sqlContext.jsonFile(userPrefs_path).registerTempTable("userPrefs")	
	
	val budget_path = dataFilePath + "budget.json"
	sqlContext.jsonFile(budget_path).registerTempTable("budget")
 	
	
	
	
	
	
	
	//Register Tables
	
	
	
  
  
  
  

}