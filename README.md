# InSlick

[![CircleCI](https://circleci.com/gh/rtkaczyk/inslick/tree/main.svg?style=svg)](https://circleci.com/gh/rtkaczyk/inslick/tree/main)

InSlick is a macro extension to [Slick](https://scala-slick.org/) used with plain SQL queries. 
It can expand collections into a sequence of arguments to SQL `IN` operator.

### Problem

Let's say you receive a list of customer names from user input and you want to retrieve full records
of customers identified by those names. The SQL query might look like:

```sql
SELECT * FROM customer WHERE (first_name, last_name) IN (row('John', 'Kowalski'), row('Jane', 'Smith'))
```

You might be tempted to construct a `String` from your collection and splice it with `#` like so:

```scala
val names = List(("John", "Kowalski"), ("Jane", "Smith"))
val namesStr = names.map { case (fn, ln) => s"row('$fn', '$ln')"}.mkString("(", ", ", ")")
val query = sql"SELECT * FROM customer WHERE (first_name, last_name) IN #$namesStr"
```

However, this is **not safe** from [SQL injection](https://xkcd.com/327/)!


### Solution

Add InSlick to your project:

```scala
libraryDependencies += "io.github.rtkaczyk" %% "inslick" % "1.0.0"
```

and use the `sqli` interpolator:

```scala
import accode.inslick.syntax._

val names = List(("John", "Kowalski"), ("Jane", "Smith"))
val query = sqli"SELECT * FROM customer WHERE (first_name, last_name) IN *$names"
```

### User Guide
For the list of [supported databases](doc/userguide.md#supported-rdbmss) and more usage examples see the 
[User Guide](doc/userguide.md).
