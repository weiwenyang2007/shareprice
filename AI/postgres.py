import psycopg2
import numpy
from psycopg2.extensions import register_adapter, AsIs

def addapt_numpy_float32(numpy_float32):
    return AsIs(numpy_float32)
def addapt_numpy_float64(numpy_float64):
    return AsIs(numpy_float64)
def addapt_numpy_int64(numpy_int64):
    return AsIs(numpy_int64)

register_adapter(numpy.float32, addapt_numpy_float32)
register_adapter(numpy.float64, addapt_numpy_float64)
register_adapter(numpy.int64, addapt_numpy_int64)

class PostgresDBHandler():
    def __init__(self):
        try:
            self.connection = psycopg2.connect(database="easystogu", user='postgres', password='postgres', host='192.168.10.200', port= '5432')
            self.cursor = self.connection.cursor()
        except (Exception, psycopg2.Error) as error:
            print("Error while fetching data from PostgreSQL", error)

    def get_all_stockIds(self):
        query='select DISTINCT stockId from qian_fuquan_stockprice order by stockId limit 10'
        self.cursor.execute(query)
        records = self.cursor.fetchall()
        print('Total stockId len is ' + str(len(records)))
        return records

    def get_stock_price_and_save_to_file(self, stock_id, stock_price_path):
        #It will skip the earlier stock date (the first record) and append the mock 9999-01-01 to the end.
        query = "select date,open,high,low,close,Volume from qian_fuquan_stockprice where stockid='" + stock_id + "' order by date"

        outputquery = "COPY ({0}) TO STDOUT WITH CSV HEADER".format(query)

        with open(stock_price_path, 'w') as f:
            self.cursor.copy_expert(outputquery, f)

        #Remove the first record, since it has big price change and will impact the AI
        lines = []

        with open(stock_price_path, 'r') as f:
            lines = f.readlines()

        #If the stock price len is too small (less than 10 years), then exit
        if len(lines) < 2500:
            print('Len of stockId ' + stock_id + ' is ' + str(len(lines)) + ' too small, ignored')
        else:
            #Append the predict mock date price:
            latest_record = lines[len(lines)-1]
            #Replace the latest record date to 9999-01-01 and append it to the last line of file
            latest_record = '9999-01-01' + latest_record[10:]

            with open(stock_price_path, 'w') as f:
                f.writelines(lines[:1] + lines[2:] + list(latest_record))
            print('stock price ' + stock_id + ' is saved to ' + stock_price_path)

    def save_predict_result_to_db(self, stock_id, test_pred, df_test_with_date):
        #Setting auto commit false
        self.connection.autocommit = True

        #First delete then insert
        index = 0
        for date in df_test_with_date['date'].values:
            result = test_pred[index][0]
            self.cursor.execute("delete from AI_TREND_PREDICTION where stockid=%s and date=%s", (stock_id,date))
            self.cursor.execute("insert into AI_TREND_PREDICTION (stockid,date,result) values(%s, %s, %s)", (stock_id, date, result))
            index += 1

        # Commit your changes in the database
        self.connection.commit()

    def close_db(self):
        # closing database connection.
        if self.connection:
            self.cursor.close()
            self.connection.close()

