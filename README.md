## Evaluating Durability of Cloud Distributed Database: MongoDB Atlas
 
This is the test framework for evaluating durability of MongoDB Atlas.

A core promise of database system is that even after a potential server failure, any updates are durable and remain in the database. However, does this property also hold for distributed systems like MongoDB Atlas, a popular cloud-based database system? Since durability issues cannot be directly observed by users, so how do we measure them?  

Our purpose is to develop a test harness that can detect durability bugs in MongoDB Atlas, and that can be used as a tool for database company to verify the bug and make improvements.

We used this test framework to evaluate 2 versions of MongoDB, one with known durability bug. We can see the buggy version lost writes after a simulated server crash, while the latest MongoDB version tested fine. This tool can indeed help database developers to find the bugs in their code.

---
### Configuration

**Gradle 6.4** has been used for this project

All the source code can be found under the path: [`/gra_test/src/main/java/gra_test/`](https://github.com/zacharyjin8948/mongodbatlas_durability/tree/main/gra_test/src/main/java/gra_test)

To run the code in AWS EC2 VM, copy the repo and type in:
`./gradlew run`

The result file containing all the records after run should be placed under `/gra_test/src/main/java`

For data analysis, please use python file: `analysis.py` and specify the relative path of result file.
