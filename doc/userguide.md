# InSlick User Guide

## Introduction

InSlick is a macro extension to [Slick](https://scala-slick.org/) library that enables safe and 
convenient construction of arguments to `IN` operator in plain SQL queries. 

An example such query might look like:
```sql
SELECT * FROM character c WHERE (c.first_name, c.last_name) IN (
  row('Tyler', 'Durden'),
  row('Marla', 'Singer')
)
```

InSlick provides an additional interpolator `sqli` which can automatically expand an `Iterable[T]` to build an 
`IN` condition to be used in `WHERE` clause of a `SELECT`, `UPDATE` or `DELETE` query. The interpolator can also 
be used to construct an `INSERT INTO table (column, ...) VALUES ...` query.

**Note**: syntactically correct construction can also be achieved by converting an `Iterable[T]` to `String`
and splicing it using `#$` operator of the standard `sql` interpolator, like so:

```scala
val names: String = List("Tyler", "Marla").map(s => s"'$s'").mkString("(", ", ", ")")
val query = sql"SELECT * FROM character WHERE first_name IN #$names"
```

This approach is **not safe** from SQL injection

## Adding InSlick to Your Project

In your SBT build file add:

```scala
libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % <your_version>,
  "io.github.rtkaczyk" %% "inslick" % "1.1.0"
)
```

InSlick is available for all Slick `3.x.y` versions and Scala versions from `2.11` to `2.13`. 
InSlick does not provide a dependency for Slick, so it has to be explicitly included.


## Using InSlick

### Import

To enable the functionality add the following import:
```scala
import accode.inslick.syntax._
```

### Simple Queries

`sqli` can expand any collection that extends `Iterable`. Simply precede the parameter with an 
asterisk (`*`). This works when the collection contains either singular elements or tuples of any size.
`sqli` produces the same result type as the standard `sql` interpolator. 

```scala
import accode.inslick.syntax._

val ids = Set(1, 2, 3)
val names = List(("Tyler", "Durden"), ("Marla", "Singer"))

val q = sqli"""
  SELECT age FROM character c WHERE 
    c.id IN *$ids or (c.first_name, c.last_name) IN *$names"""

db.run(q.as[Int])
```

### Custom Types

The examples above work because there exist implicit instances of `SetParameter[Int]` and 
`SetParameter[String]`. See the 
[implementation](https://github.com/slick/slick/blob/v3.3.3/slick/src/main/scala/slick/jdbc/SetParameter.fm)
of `slick.jdbc.SetParameter` for the complete list. For other types you will have to provide an appropriate 
implicit manually:

```scala
import slick.jdbc.SetParameter
import accode.inslick.syntax._

case class Status(status: String)
val v: List[(Int, Status)] = List((1, Status("new")), (2, Status("active")))
implicit val spStatus = SetParameter[Status]((s, pp) => pp.setString(s.status))

val q = sqli"SELECT * FROM customer WHERE (rating, status) IN *$v"
db.run(q.as[Customer])
```

The prerequisites for returning a custom type are the same as for the standard `sql` interpolator
(implicit `GetResult` is required).

### Case Classes

Case classes are often used to represent whole rows or their subsets. InSlick can automatically expand
case classes in query parameters, as long as all case class fields have a corresponding `SetParameter`
defined. The order of columns in the query correspond the order of case class fields:

```scala
case class Customer(name: String, rating: Int, status: Status)
val customers: List[Customer] = ???
// implicit SetParameter[Status] created in the example above

val q = sqli"SELECT COUNT(*) FROM customer WHERE (name, rating, status) IN *${customers.map(_.tuple)}"
```

Alternatively a custom `IterParam[List[Customer]]` may be used, see [here](#macro-expansion). A future 
release may provide automatic expansion of case classes.

### UPDATE/DELETE

Using the `sqli` with `UPDATE` and `DELETE` is the same as for `SELECT` queries:

```scala
import accode.inslick.syntax._
val ids = List(1, 2, 3)

val a1: DBIO[Int] = sqli"UPDATE customer SET status='inactive' WHERE id IN *$ids".asUpdate
val a2: DBIO[Int] = sqli"DELETE FROM customer WHERE id IN *$ids".asUpdate
```

Calling `.asUpdate` is equivalent to using `sqlu` macro.

### INSERT

Slightly different syntax is needed for `INSERT` therefore `*i` operator should be used:

```scala
import accode.inslick.syntax._
val customers = List(("Mary", 1), ("John", 2))

val a: DBIO[Int] = sqli"INSERT INTO customer (name, rating) VALUES *i$customers".asUpdate
```

For more information see [Syntax Flavours](#syntax-flavours).


### Optional/Nullable Values

Care should be taken when using `Option` values, which correspond to nullable columns. The result of the following
query may be unintuitive, in that rows where the specified value is `NULL` will not be returned:

```sql
SELECT * FROM customer WHERE name IN ('Tyler', 'Marla', NULL)
```

Instead, the query should be rewritten as follows:

```sql
SELECT * FROM customer WHERE name IN ('Tyler', 'Marla') OR name IS NULL
```

This is simply a note on SQL semantics and has nothing to do with InSlick itself.

### Empty Collections
Empty collections are not supported. Appropriate guards should be added before running a query or
an `IllegalArgumentException` will be thrown on runtime:

```scala
val ids: List[Int] = ???

lazy val q = sqli"SELECT name FROM customer WHERE id IN *$ids"
if (ids.isEmpty) DBIO.successful(Vector.empty) else q.as[String]
```

This may change in a future release.

### Escaping

The `*` operators can be escaped by doubling:

```scala
val (r, n) = (2, 10)
sqli"SELECT * FROM customer WHERE rating**$r > #$n and id IN ${List(1, 2, 3)}"
```

Splicing values with `#` also works.

## Implementations Details

### Macro Expansion

Under the hood Slick uses `java.sql.PreparedStatement` which represents a parameterised SQL statement, where each
input parameter is expressed as `?`. These `?` are then safely replaced with arguments' values.

Slick's `sql` macro always produces a single `?` for each of its input parameters. InSlick's `sqli` macro therefore 
rewrites the query so that multiple `?`s are added, e.g. such query:

```scala
sqli"SELECT * FROM customer WHERE id IN *$ids"
```

during compilation, will essentially be rewritten to:

```scala
sql"SELECT * FROM customer WHERE id IN #$before$ids#$after"
```

where:
* `before` is the beginning of the sequence of groups of `?` parameters, e.g. an open paren `(`,
* `after` is a sequence of correctly grouped `?` of length corresponding the collection size and its element dimension.

During runtime an implicit instance of `accode.inslick.IterParam` assures that each `?` is set to the correct value
from the input collection. Implicit `IterParam`s are generated automatically by the `sqli` macro, however one may
override this behaviour by manually providing an implicit `IterParam`. This may be useful for supporting
different collecion types.

### Syntax Flavours

The table below summarises the available syntax flavours:

| Import | Op | Dim-1 | Dim-N | Purpose |
| --- | --- | --- | --- | --- |
| `rows.syntax._` | `*r` | `(?, ?, ...)` | `(row(?, ?, ...), row(?, ?, ...), ...)` | Most tested DBs |
| `values.syntax._` | `*v` | `(?, ?, ...)` | `(values (?, ?, ...), (?, ?, ...), ...)` | SQLite |
| N/A | `*i` | `(?), (?), ...` | `(?, ?, ...), (?, ?, ...), ...` | `INSERT` statement |
| `syntax._` | `*` | same as `rows` | same as `rows` | Most common syntax |

An appropriate syntax can be selected by importing it. The imports in the table are shortened, precede them with 
`accode.inslick.`, e.g.:

```scala
import accode.inslick.values.syntax._
```

Syntax can also be chosen with an operator (**Op**), e.g.:

```scala
sqli"SELECT * FROM customer WHERE id IN *r${List(1, 2)}"
```

Columns **Dim-1**/**Dim-N** show how a collection is expanded into a sequence of '?' parameter depending
or whether the element type is singular or a tuple.

Currently only SQLite is known to require a different syntax.


## Supported RDBMSs

The following is a list of databases that are known to be working and are tested on the CI:

| RDBMS | Version | Syntax[^](#syntax-flavours) |
| --- | --- | --- |
| H2 | 2.0 | `rows` |
| MariaDB | 10.6 | `rows` |
| MySQL | 8.0 | `rows` |
| PostgreSQL | 14.0, 9.6 | `rows` |
| SQLite | 3.36 | `values` |

Other databases are likely to work with one of the available syntax, however this little project has no means
to test against commercial DBs like Oracle, SQL Server or DB2 at this time.
