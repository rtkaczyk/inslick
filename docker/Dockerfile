FROM openjdk:11.0.13-jdk-slim-buster

ARG SBT_VERSION=1.5.5
ARG POSTGRES_VERSION=14+232.pgdg100+1
ARG MYSQL_VERSION=8.0.27-1debian10

ENV PATH $PATH:/usr/lib/postgresql/14/bin
ENV PGDATA /usr/lib/postgresql/data

RUN apt-get update && \
    apt-get install -y apt-transport-https gnupg sudo

RUN set -ex; \
    echo "deb http://apt.postgresql.org/pub/repos/apt/ buster-pgdg main" > /etc/apt/sources.list.d/postgresql.list; \
    apt-key adv --keyserver keyserver.ubuntu.com --recv-keys 7FCC7D46ACCC4CF8; \
    echo "deb http://repo.mysql.com/apt/debian/ buster mysql-8.0" > /etc/apt/sources.list.d/mysql.list; \
    apt-key adv --keyserver keyserver.ubuntu.com --recv-keys 8C718D3B5072E1F5; \
    echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" > /etc/apt/sources.list.d/sbt.list; \
    apt-key adv --keyserver keyserver.ubuntu.com --recv-keys 99E82A75642AC823; \
    apt-get update

RUN apt-get install -y sbt=${SBT_VERSION}

RUN apt-get install -y postgresql=${POSTGRES_VERSION}

RUN DEBIAN_FRONTEND=noninteractive apt-get install -y \
        mysql-community-server=${MYSQL_VERSION} \
        mysql-community-client=${MYSQL_VERSION}

RUN set -eux; \
    chown -R postgres:postgres /usr/lib/postgresql; \
    su postgres -c initdb; \
    echo "listen_addresses = '*'" >> $PGDATA/postgresql.conf; \
    echo "host all all 0.0.0.0/0 trust" >> $PGDATA/pg_hba.conf; \
    echo "bind-address = 0.0.0.0" >> /etc/mysql/mysql.conf.d/mysqld.cnf


EXPOSE 5432/tcp
EXPOSE 3306/tcp

ENTRYPOINT set -ex; \
           su postgres -c "pg_ctl start"; \
           mysqld -u root -D; \
           mysql -u root -e "CREATE USER 'root'@'%'; GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION; FLUSH PRIVILEGES;"; \
           /bin/bash
