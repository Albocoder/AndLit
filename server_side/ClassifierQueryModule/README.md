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
