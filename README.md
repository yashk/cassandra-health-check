# CassandraHealthCheck
Manage and maintain a cross-cluster health check, usable standalone or with consul.
We used [Cassandra 2.0.15](https://issues.apache.org/jira/browse/CASSANDRA/fixforversion/12329873)
for our testing, but it should work with other versions.

This command-line tool is used to check for network partitions and other intra-cluster problems.
It works by making a connection to only one node, which will be the coordinator for the request.
It then verifies that there is a namespace that has a replication factor equal to the number of
nodes in the cluster, and adds a column family with a single row. It then selects that row using
a consistency of `CL_ALL`, with a downgrade policy. When the row comes back, if it downgraded, you
get an exit code of 1 (which consul considers a warning). If the coordinator can't read the data
at all (which usually means the local node isn't working or you can't connect for some reason)
then you get an exit code of 2.

Each time the code runs, it checks the cluster for changes and adjusts the keyspace so that all
the data is always on every node in the cluster (or should be).

## Building

This project uses gradle for builds. to build it, just run:

  `./gradlew`

When it completes, you end up with a fat jar that has all the dependencies wrapped
in it, located in
in `build/libs/CassandraHealthCheck-all*.jar`

## Testing

To run the tests, just run:

  `./gradlew test`

## Running it

To execute it, you'll need to point it at an existing node in a cluster, like this:

  `java -jar build/libs/CassandraHealthCheck-all*.jar -host HOSTNAME -port PORT -username USER -password PASSWORD`

All the options are optional, the defaults can be seen by running with -help.
There is no default username and password;
if one is not supplied it will attempt to connect without authorization information.
