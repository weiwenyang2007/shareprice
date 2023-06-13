import psycopg2
import subprocess
try:
    connection = psycopg2.connect(database="easystogu", user='postgres', password='postgres', host='192.168.10.200', port= '5432')
    cursor = connection.cursor()
    query='select DISTINCT stockId from qian_fuquan_stockprice order by stockId limit 10'
    cursor.execute(query)
    records = cursor.fetchall()
    print('Total stockId len is ' + str(len(records)))
    for row in records:
        stockId = row[0]
        subprocess.call("./train.py -tfs True -gpu 0 -id " + stockId, shell=True)

except (Exception, psycopg2.Error) as error:
    print("Error while fetching data from PostgreSQL", error)

finally:
    # closing database connection.
    if connection:
        cursor.close()
        connection.close()

