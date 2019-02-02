# Spark at its best #
## Introduction to Apache spark
Apache	Spark	is	a	general-purpose	cluster	computing	system	to	process	big	data	workloads.
It	was	originally developed	at	AMPLab,	UC	Berkeley,	in	2009.	It	was	made	open	source
in	2010	under the	BSD	license	and	switched	to	the	Apache	2.0	license	in	2013.

Spark can process bigdata workload in subsecond latency by using memory for storage (caching)
data. In	MapReduce,	memory	is	primarily	used	for	the	actual computation.	Spark	uses	memory	both	to	compute	and	store	objects.

## Spark SQL ##
Spark	SQL	is	a	component	of	the	Spark	ecosystem, introduced	in	Spark	1.0	for	the	first	time.	It
incorporates a	project	named	**Shark**,	which	was	an	attempt	to	make	Hive	run	on	Spark.
but	very	soon,	Spark	developers	hit	roadblocks	and	could	not	optimize	it	any further. Finally,	they	decided	to	write	the	SQL	engine	from	scratch,	and	this	gave	birth	to	Spark SQL.

Spark	SQL	took	care	of	all	the	performance	challenges,	but	it	had	to	provide	compatibility	with Hive,	and for	that	reason,	a	new	wrapper	context,	 HiveContext ,was	created	on	top	of SQLContext.
One	big	reason	to use spark SQL is to	helps and	run	Spark	programs	faster.	It	lets	developers	to	write	less	code,	the	program	to	read	less	data and	the	**Catalyst	optimizer**	to	do	all	the	heavy	lifting.

### DATAFRAME ###

Spark	SQL	uses	a	programming	abstraction	called	DataFrame.	It	is	a	distributed	collection	of	data, organized	in	named	columns where as n	RDD	is	an	opaque	collection	of	objects	with	no	idea	about	the format	of	the	underlying	data. DF is evolved from SchemaRDD (spark1.2)
 Benefits of DF:
 1. Support to load data from hive, parquet, Json, CSV, DB using JDBC.
 2. Support in Java, Python and R (spark 1.4)
 
 * * *
 *   We replicate the data in the small rdd N times by creating a new key (original_key, v) where v takes values between 0 and N. The value does not change, i.e. it is the same value that was associated to the original key.
*   We take the large skewed rdd and modify the key to add some randomness by doing (original\_key, random\_int) where random_int takes a value between 0 and N. Note that in this case we are NOT replicating the data in the large rdd. We are simply splitting the keys so that values associated to the same original key are now split into N buckets.
*   Finally, we perform the join between these datasets.
*   We remove the random_int from the key to have the final result of the join.
 ### DATASET ###
 
 Introduced in 1.6	to	provide	strong	typing	to	DataFrames. In	Spark	2.0,	Dataset	and	the
DataFrame	API	were	merged	to	provide	one	single	abstraction.
 `DataFrame = DataSet<ROW>`

Example
`case class Employee (id:Int, name:String, department:String, exp:Float, salary:Long)`
 Loading Json into Dataset\
 `val employee = spark.read.json("/mypath/file.json").as [Employee]`
 this is Type safe Scala JVM object

### RDD — Resilient	Distributed	Dataset ###
 Resilient	Distributed	Dataset	(aka	RDD)	is	the	primary	data	abstraction	in	Apache	Spark
and	the	core	of	SparK
The	features	of	RDDs	(decomposing	the	name):
- Resilient,	i.e.	fault-tolerant	with	the	help	of	RDD	lineage	graph	and	so	able	to
  recompute	missing	or	damaged	partitions	due	to	node	failures.
- Distributed	with	data	residing	on	multiple	nodes	in	a	cluster.
- Dataset	is	a	collection	of	partitioned	data	with	primitive	values	or	values	of	values


 **PERFORMANCE**
DataFrame and Dataset APIs are built on top of the Spark SQL engine, it uses Catalyst to generate an optimized logical and physical query plan. Across R, Java, Scala, or Python DataFrame/Dataset APIs, all relation type queries undergo the same code optimizer, providing the space and speed efficiency. 

> *The Dataset[T] typed API is optimized for data engineering tasks, the untyped_Dataset[Row] (an alias of DataFrame) is even faster and suitable for interactive analysis*

## Catalyst Optimizer ##
Before Talking about Catalyst, Lets first see Trees
**TREE**
The main data type in Catalyst is a tree composed of node objects. Each node has a node type and zero or more children. New node types are defined in Scala as subclasses of the TreeNode class. These objects are immutable and can be manipulated using functional transformations.
Example:
*   `Literal(value: Int)`: a constant value
*   `Attribute(name: String):`an attribute from an input row, e.g.,“x”
*   `Add(left: TreeNode, right: TreeNode):`sum of two expressions.

`Add(Attribute(x),  Add(Literal(1),  Literal(2)))`

![Tree](/home/npatodi/process@learning/img/Tree.png)

**Rules**

Trees can be manipulated using rules, which are functions from a tree to another tree.
`Tree1 -> Tranform(Rule1) -> Tree2`

## Example
```java
  tree.transform { 
  case  Add(Literal(c1),  Literal(c2))  =>  Literal(c1+c2)  
  case  Add(left,  Literal(0))  => left  
  case  Add(Literal(0), right)  => right 
  }
```
**Definition**

The	Catalyst	optimizer is a framework library for representing trees and applying rules to manipulate them. It primarily	leverages	functional	programming	constructs	of	Scala,	such	as	pattern matching.

It work in 4 Phases:
1. **Analysis**: 
  - It starts by building an “unresolved logical plan” tree with unbound attributes and data types
  - Syntax Error check and map name attribute to relation by catalog ( this is to find missing cols)
  - if it have attributes with same value (i.e. name), it give unique id to each 
    e.g. empId#1 empId#2 
  - Derive data type of expression in tree e.g. 1+ col = ?? 

2. **Logical Optimizations**:
The	logical	plan	optimization	phase	applies	standard	rule-based	optimizations	to	the	logical	plan.	These include	constant	folding,	predicate	pushdown,	projection	pruning,	null	propagation, Boolean	expression simplification,	and	other	rules.
 * * *
 ![Constant Folding](/home/npatodi/process@learning/img/folding.png)
 * * *
 ![Predicate PushDown](/home/npatodi/process@learning/img/pushDown.png)
 * * *
 ![Pruning](/home/npatodi/process@learning/img/pruning.png)
 * * *
 ![Final](/home/npatodi/process@learning/img/final.png)
 * * *
3. **Physical Planning**:
In	the	Physical Planning	phase, Spark	SQL	takes	a	logical	plan	and	generates	one	or	more	physical	plans. It	then	measures	the	cost	of	each	Physical	Plan	and	generates	one	Physical	Plan	based	on it.
 
4. **Code Generation**:
The	final	phase	of	query	optimization	involves	generating	the	Java	byte-code	to	run	on	each	machine. Catalyst relies on a special feature of the Scala language, quasiquotes, to make code generation simpler. Quasiquotes allow the programmatic construction of abstract syntax trees (ASTs) in the Scala language, which can then be fed to the Scala compiler at runtime to generate bytecode.

![Full](/home/npatodi/process@learning/img/full.png)
* * *
## Spark Architecture ##

Spark is a Master slave architecture with concept of cluster manager for orchestracting processing.

![Cluster Manager](/home/npatodi/process@learning/img/ClusterManager.png)
**Cluster manager**  is responsible for :
- The Spark context connects to the Spark cluster manager, it then allocates resources across the
  worker nodes for the application ( in Yarn it is containers)
- Allocates executors across the cluster worker nodes.
- Copies the application jar file to the workers, and finally it allocates tasks.
* * * *  
![Architechture](/home/npatodi/process@learning/img/arch.png)

**Driver**
A	Spark	driver	(aka	an	application’s	driver	process)	is	a	JVM	process	that	hosts
SparkContext	for	a	Spark	application.	It	is	the	master	node	in	a	Spark	application.
 
**Executor**
Executor is	a	distributed	agent	that	is	responsible	for	executing	tasks. 
Executor typically	runs	for	the	entire	lifetime	of	a	Spark	application	which	is	called	static
allocation	of	executors	(but	you	could	also	opt	in	for	dynamic	allocation). It	reports	heartbeat	and	partial	metrics	for	active	tasks	to	 	HeartbeatReceiver	 	RPC Endpoint	on	the	driver.
Executors	provide	in-memory	storage	for	RDDs	that	are	cached	in	Spark	applications	(via
Block	Manager).
Executors	can	run	multiple	tasks	over	its	lifetime,	both	in	parallel	and	sequentially.	They
track	running	tasks	(by	their	task	ids	in	runningTasks	internal	registry).	Consult	Launching
Tasks	section.
Executors	use	a	Executor	task	launch	worker	thread	pool	for	launching	tasks.
Executors	send	metrics	(and	heartbeats)	using	the	internal	heartbeater	-	Heartbeat	Sender
Thread.
It	is	recommended	to	have	as	many	executors	as	data	nodes	and	as	many	cores	as	you	can
get	from	the	cluster.

**Master**
A	master	is	a	running	Spark	instance	that	connects	to	a	cluster	manager	for	resources.

**Workers**
Workers	(aka	slaves)	are	running	Spark	instances	where	executors	live	to	execute	tasks.
It	hosts	a	local	Block	Manager	that	serves	blocks	to	other	workers	in	a	Spark	cluster.
Workers	communicate	among	themselves	using	their	Block	Manager	instances.

When	you	create	SparkContext,	each	worker	starts	an	executor.	This	is	a	separate	process
(JVM),	and	it	loads	your	jar,	too.	The	executors	connect	back	to	your	driver	program.	Now
the	driver	can	send	them	commands,	like	 	flatMap	 ,	 	map	 	and	 	reduceByKey	 .
***
><https://umbertogriffo.gitbooks.io/apache-spark-best-practices-and-tuning/content/treereduce_and_treeaggregate_demystified.html>

------------------

>data storage in memory for quick access, as well as lightning-fast task startup time.
>Spark supports two modes for running on YARN, “yarn-cluster” mode and “yarn-client” mode.  Broadly, yarn-cluster mode makes sense for production jobs, while yarn-client mode makes sense for interactive and debugging uses where you want to see your application’s output immediately.

>. In YARN, each application instance has an Application Master process, which is the first container started for that application. The application is responsible for requesting resources from the ResourceManager, and, when allocated them, telling NodeManagers to start containers on its behalf.

>In yarn-cluster mode, the driver runs in the Application Master. This means that the same process is responsible for both driving the application and requesting resources from YARN, and this process runs inside a YARN container. The client that starts the app doesn’t need to stick around for its entire lifetime.

>In yarn-client mode, the Application Master is merely present to request executor containers from YARN. The client communicates with those containers to schedule work after they start:

-------
Performance profiling in Spark

Monitor your running jobs regularly for performance issues. If you need more insight into certain issues, consider one of the following performance profiling tools:

*   [Intel PAL Tool](https://github.com/intel-hadoop/PAT)monitors CPU, storage, and network bandwidth utilization.
*   [Oracle Java 8 Mission Control](https://www.oracle.com/technetwork/java/javaseproducts/mission-control/java-mission-control-1998576.html) profiles Spark and executor code.
----
* * *
   ![Yarn Cluster](/home/npatodi/process@learning/img/yarnCluster.png)
 -----
 
  ![Yarn Client](/home/npatodi/process@learning/img/yarnClient.png)

* * *
## Core calculation ##

**According to the recommendations which we discussed above:**

*   Based on the recommendations mentioned above, Let’s assign 5 core per executors =>`--executor-cores = 5`(for good HDFS throughput)
*   Leave 1 core per node for Hadoop/Yarn daemons => Num cores available per node = 16-1 = 15
*   So, Total available of cores in cluster = 15 x 10 = 150
*   `Number of available executors = (total cores/num-cores-per-executor) = 150/5 = 30`
*   Leaving 1 executor for ApplicationManager =>`--num-executors`= 29
*   Number of executors per node = 30/10 = 3
*   Memory per executor = 64GB/3 = 21GB
*   Counting off heap overhead = 7% of 21GB = 3GB. So, actual`--executor-memory`= 21 - 3 = 18GB

______
Tungsten
It is execution engine for spark for 1.5
It focuses on substantially improving the efficiency of_memory and CPU_for[Spark applications](https://databricks.com/glossary/what-are-spark-applications "Glossary: Spark Applications"), to push performance closer to the limits of modern hardware


>1.  Optimise query plan - solved via**Whole-Stage Code-Generation**
>2.  Speed up query execution - solved via **Supporting Vectorized in-memory columnar data**
>### Volcano Iterator Model --> runtime polimorphisum
>
Please read [Spark 2.x - 2nd generation Tungsten Engine \| spark-notes](https://spoddutur.github.io/spark-notes/second_generation_tungsten_engine.html)
 ______

TimSort
[Deep Understanding of Spark Memory Management Model - TutorialDocs] for Sprak Memor model(https://www.tutorialdocs.com/article/spark-memory-management.html)
_________________
[Working with Skewed Data: The Iterative Broadcast - Databricks](https://databricks.com/session/working-with-skewed-data-the-iterative-broadcast)


----------
[TreeReduce and TreeAggregate Demystified · Apache Spark - Best Practices and Tuning](https://umbertogriffo.gitbooks.io/apache-spark-best-practices-and-tuning/content/treereduce_and_treeaggregate_demystified.html)
_____

