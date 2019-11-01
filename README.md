
# jdbc

Simple DataSource implementation and DataSource wrapper with log capabilities. 

## DriverManagerDataSource

DataSource implementation which creates connections using DriverManager class. 
It makes no pooling. Creates new Connection instance each time when getConnection 
is called.

It is usefull for testing jdbc code which uses DataSource instance independently of
environment which creates such instance.

Example of h2 DataSource instance creation
```java
	DataSource ds = DriverManagerDataSource.instance(
			"org.h2.Driver"
			, "jdbc:h2:./target/databasefilename.h2;MODE=PostgreSQL"
			, "user"
			, "pass"
	);
```

## LogDataSource

Wraps DataSource instance with some log capabilities. It logs 
 - connection commits/rollbacks
 - statement executions
 - prepared statement executions with params
 - result set counts 

Example of wrapping DataSource instance
```java

	ds = LogDataSource.wrap(ds, 
			LogConfig.instance(
					() -> jdbclog.isDebugEnabled()
					, (message) -> jdbclog.debug(message))
		);
```

### LogConfig

This class cumulates configuration of logging.

 - Consumer consumer - provides connection to logging real api 
 - ConsumerStatus consumerStatus - provides connection to logging real api
 - boolean logStatement = true - enables loging of statement execution
 - boolean logResultSet = true - enables loging of result set counts (on close)
 - boolean logTransaction = true - enables loging of transaction boundaries

### Log examples

connection boundary with connection identity 10
```
jdbc [10] commit time: 0
```

statement with connection identity 10 and statement identity 8
```
jdbc [10][8] statement: select vs, description from mytable where vs = '1111111111' time: 0
```

prepared statement with connection identity 10 and statement identity 9
```
jdbc [10][9] statement: update mytable set vs=?, description=? where vs = '1111111111' params:  ?1:1111111111 ?2:22222  result: 1 time: 1
```

result set with connection identity 10 and statement identity 8
```
jdbc [10][8] resultset row count: 2 first row time: 0 all row time: 2 close time: 0
```


## Maven usage

```
   <dependency>
      <groupId>com.github.antonsjava</groupId>
      <artifactId>jdbc</artifactId>
      <version>1.0</version>
   </dependency>
```

