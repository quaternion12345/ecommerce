FROM mysql:8.0.0
ENV MYSQL_ROOT_PASSWORD test1357
ENV MYSQL_DATABASE mydb
# COPY ./mysql_data/mysql /var/lib/mysql
COPY ./mydb.sql /docker-entrypoint-initdb.d/
EXPOSE 3306
# ENTRYPOINT ["mysqld", "--user=root"]