# InSlick

[![CircleCI](https://circleci.com/gh/rtkaczyk/inslick/tree/main.svg?style=svg)](https://circleci.com/gh/rtkaczyk/inslick/tree/main)

## Usage

```scala
import com.accode.inslick.api._

val people = List(
  ("John", "Kowalski"),
  ("Jane", "Smith")
)

val query = sqli"select * from people where (first_name, last_name) in *$people"
```
