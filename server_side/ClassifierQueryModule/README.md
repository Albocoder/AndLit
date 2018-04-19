# What is this?

This is the module that handles loading a classifier file, a database and a face to query and give back results in JSON

# How to use?

1. Go in this directory and type `mvn package`
2. Go to `target` directory and you will find a file named `LBPHQuerier.jar`
3. Run the jar like so: `java -jar LBPHQuerier.jar <classifier_path> <database_path> <image_path>`
4. Then you will get a result in stdout in JSON format.

# Format of the result

A result will always have a field called error which in success will be 0 and in error > 0. If there is an error another field called "message" will spill an informative message.
In case of no error the other fields will be `name`,`last`,`distance`.

# Example run

To run you have to type something like this: `java -jar LBPHQuerier.jar /home/server/userx/classifier.yml /home/server/userx/database.db /home/server/userz/query.png`

The result will then be something like this:

`
{
  "error":"0",
  "name":"Erin",
  "last":"Avllazagaj",
  "distance":"45.2341"
}
`
OR like this:

`
{
  "error":"1"
  "message":"not enough arguments supplied"
}
`

# Error codes

1.  Not enough arguments supplied
2.  JDBC driver class not found
3.  Couldn't connect to user's database
456. SQLException (god knows what) (query error? if so then DB scheme might be wrong)
