## Evaluating Durability of Cloud Distributed Database: MongoDB Atlas
 
This is the test framework for evaluating durability of MongoDB Atlas

**Gradle** has been used for this project

All the source code can be found under the path: `/gra_test/src/main/java/gra_test/`

To run the code in AWS EC2 VM, copy the repo and type in:
`./gradlew run`

The result file containing all the records can be found in `/gra_test/src/main/java`

For data analysis, please use python file: `analysis.py` and change the relative path of result file.
