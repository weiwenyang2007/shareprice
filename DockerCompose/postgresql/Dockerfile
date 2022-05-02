FROM bitnami/postgresql:14

RUN rm -r /opt/bitnami/postgresql/conf/pg_hba.conf
RUN rm -r /opt/bitnami/postgresql/conf/postgresql.conf
COPY ./conf/pg_hba.conf /opt/bitnami/postgresql/conf/pg_hba.conf
COPY ./conf/postgresql.conf /opt/bitnami/postgresql/conf/postgresql.conf
RUN chmod g+rwX /opt/bitnami
