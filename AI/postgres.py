import pandas as pd
import psycopg2
import numpy
import sys, os
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
            self.connection.autocommit = True
        except (Exception, psycopg2.Error) as error:
            print("Error while fetching data from PostgreSQL", error)

    def get_all_stockIds(self, prefix=None, sufix=None, desc=None):
        #do not use get_all_stockIds due to AI spend much time for train and prediction, use the filter one (about 1250 Ids)
        query='select DISTINCT stockId from qian_fuquan_stockprice order by stockId'
        if desc:
            query = query + ' ' + desc
            
        self.cursor.execute(query)
        records = self.cursor.fetchall()
        print('Total stockId len is ' + str(len(records)))
        new_records = []

        for stock_id in records:
            sid = stock_id[0]            
            if prefix or sufix:
                if prefix:
                    if sid.startswith(prefix):
                        new_records.append(sid)
                if sufix:
                    if sid.endswith(sufix):
                        new_records.append(sid)
            else:        
                new_records.append(sid)

        print('Will process stockId len is ' + str(len(new_records)))
        return new_records
    
    def get_favorites_ai_filter_stockIds(self, prefix=None, sufix=None, desc=None):
        #do not use get_all_stockIds due to AI spend much time for train and prediction, use the filter one (about 1250 Ids)
        query="select stockId from favorites_filter_stock where filter='AI_Earn' order by stockId"
        if desc:
            query = query + ' ' + desc
            
        self.cursor.execute(query)
        records = self.cursor.fetchall()
        print('Total favorites AI filter stockId len is ' + str(len(records)))
        new_records = []

        for stock_id in records:
            sid = stock_id[0]            
            if prefix or sufix:
                if prefix:
                    if sid.startswith(prefix):
                        new_records.append(sid)
                if sufix:
                    if sid.endswith(sufix):
                        new_records.append(sid)
            else:        
                new_records.append(sid)

        print('Will process favorites AI filter stockId len is ' + str(len(new_records)))
        return new_records
    
    def get_price_data_length(self, stock_id):
        query = "select count(*) from qian_fuquan_stockprice where stockid='" + stock_id + "'"
        self.cursor.execute(query)
        count = self.cursor.fetchone()
        return count[0]

    def get_stock_price_and_save_to_file(self, stock_id):
        #It will skip the earlier stock date (the first record) and append the mock 9999-01-01 to the end.
        stock_price_path = 'stockData/' + stock_id + '.csv'
        query = "select date,open,high,low,close,volume from qian_fuquan_stockprice where stockid='" + stock_id + "' order by date"

        outputquery = "COPY ({0}) TO STDOUT WITH CSV HEADER".format(query)

        with open(stock_price_path, 'w') as f:
            self.cursor.copy_expert(outputquery, f)

        #Remove the first record, since it has big price change and will impact the AI
        lines = []

        with open(stock_price_path, 'r') as f:
            lines = f.readlines()

        #Append the predict mock date price:
        latest_record = lines[len(lines)-1]
        #Replace the latest record date to 9999-01-01 and append it to the last line of file
        latest_record = '9999-01-01' + latest_record[10:]

        with open(stock_price_path, 'w') as f:
            f.writelines(lines[:1] + lines[2:] + list(latest_record))

        #print('stock price ' + stock_id + ' is saved to ' + stock_price_path)

        return len(lines)

    def get_stock_price_indicator_and_save_to_file(self, stock_id):
        stock_price_path = 'stockData/' + stock_id + '_ind.csv'
        #query close price
        query="select date,open,high,low,close,volume from qian_fuquan_stockprice where stockid='" + stock_id + "' order by date"
        self.cursor.execute(query)
        price = self.cursor.fetchall()
        price_df = pd.DataFrame(data=price, columns=['date', 'open', 'high', 'low', 'close', 'volume'])
        
        #query mscd ind
        query="select date,dif,dea,macd from ind_macd where stockid='" + stock_id + "' order by date"
        self.cursor.execute(query)
        macd = self.cursor.fetchall()
        macd_df = pd.DataFrame(data=macd, columns=['date','dif', 'dea', 'macd'])
        
        #query kdj ind
        query="select date,k,d,j,rsv from ind_kdj where stockid='" + stock_id + "' order by date"
        self.cursor.execute(query)
        kdj = self.cursor.fetchall()
        kdj_df = pd.DataFrame(data=kdj, columns=['date','k', 'd', 'j', 'rsv'])        
        
        #query qsdd ind
        query="select date,lonterm,shoterm,midterm from ind_qsdd where stockid='" + stock_id + "' order by date"
        self.cursor.execute(query)
        qsdd = self.cursor.fetchall()
        qsdd_df = pd.DataFrame(data=qsdd, columns=['date','qsdd_lt', 'qsdd_st', 'qsdd_mt'])
        
        #query wr ind
        query="select date,lonterm,shoterm,midterm from ind_wr where stockid='" + stock_id + "' order by date"
        self.cursor.execute(query)
        wr = self.cursor.fetchall()
        wr_df = pd.DataFrame(data=wr, columns=['date','wr_lt', 'wr_st', 'wr_mt'])
        
        #query shenxian ind
        query="select date,h1,h2,h3 from ind_shenxian where stockid='" + stock_id + "' order by date"
        self.cursor.execute(query)
        shenxian = self.cursor.fetchall()
        shenxian_df = pd.DataFrame(data=shenxian, columns=['date','h1', 'h2', 'h3'])
        
        #check the length of indicators make sure all the length is same
        if len(price_df) != len(macd_df) or len(price_df) != len(kdj_df) or len(price_df) != len(qsdd_df) or len(price_df) != len(wr_df) or len(price_df) != len(shenxian_df) :
            print(stock_id + ' length of price and indicators are different, pls run Sanity check.')
            if os.path.exists(stock_price_path):
                os.remove(stock_price_path)
            return 0
        
        #Check the len must be same before merge them into single df
        df = pd.merge(price_df, macd_df, left_on='date', right_on='date')
        df = pd.merge(df, kdj_df, left_on='date', right_on='date')
        df = pd.merge(df, qsdd_df, left_on='date', right_on='date')
        df = pd.merge(df, wr_df, left_on='date', right_on='date')
        df = pd.merge(df, shenxian_df, left_on='date', right_on='date')
        #print(df)
        print('save indicator to ' + stock_price_path)
        #do not save index column to csv file
        df.to_csv(stock_price_path, index=False)
        
        return len(df)        

    def get_stock_price_and_save_candlestick_to_file(self, stock_id):
        #It will skip the earlier stock date (the first record) and append the mock 9999-01-01 to the end.
        candlestick_path = 'stockData/' + stock_id + '_candlestick.csv'
        query="select date,open,high,low,close,volume,lastclose from stockprice where stockid='" + stock_id + "' order by date"
        self.cursor.execute(query)
        records = self.cursor.fetchall()
        df = pd.DataFrame(data=records, columns=['date', 'open', 'high', 'low', 'close', 'volume', 'lastclose'])
        
        #Remove the first row
        df=df.drop(index=0)
        print(df.head(2))

        #process the candlestick
        df['difC']=df['close'] - df['lastclose']
        
        #High shadow
        df['hs'] = df[['open', 'high', 'low', 'close']].apply(lambda x: (x['high'] - x['close']) if (x['close']>=x['open']) else (x['high'] - x['open']), axis=1)
        #Candelstick body
        #df['body'] = df[['open', 'high', 'low', 'close', 'difC']].apply(lambda x: (x['close'] - x['open']) if (x['difC']>=0) else (x['open'] - x['close']), axis=1)
        df['body']=df['close'] - df['open']
        #Low shadow
        df['ls'] = df[['open', 'high', 'low', 'close']].apply(lambda x: (x['open'] - x['low']) if (x['close']>=x['open']) else (x['close'] - x['low']), axis=1)

        print(df.tail(10))
        #Change to percentage (with precision is 0) and then change to int (0,1,2...9)
        df['difC']=(df['difC'].astype('float')*100.0/df['lastclose'].astype('float')).round(2)#.astype('int')
        df['hs']=(df['hs'].astype('float')*100.0/df['lastclose'].astype('float')).round(2)#.astype('int')
        df['body']=(df['body'].astype('float')*100.0/df['lastclose'].astype('float')).round(2)#.astype('int')
        df['ls']=(df['ls'].astype('float')*100.0/df['lastclose'].astype('float')).round(2)#.astype('int')
        
        #Change to words token
        #df['difC']='difC_'+df['difC'].astype('string')
        #df['hs']='hs_'+df['hs'].astype('string')
        #df['body']='body_'+df['body'].astype('string')
        #df['ls']='ls_'+df['ls'].astype('string')

        print(df.head(2))
        print(df.tail(2))
        #end
        print('save candlestick to ' + candlestick_path)
        #do not save index column to csv file
        df.to_csv(candlestick_path, index=False)
        
        return len(df)          
        
    def save_predict_result_to_db(self, stock_id, test_pred, df_test_with_date):
        #First delete then insert
        index = 0
        for date in df_test_with_date['date'].values:
            result = test_pred[index][0]
            #print(stock_id, date, result)
            self.cursor.execute("delete from AI_TREND_PREDICTION where stockid=%s and date=%s", (stock_id,date))
            self.cursor.execute("insert into AI_TREND_PREDICTION (stockid,date,result) values(%s, %s, %s)", (stock_id, date, result))
            index += 1

        # Commit your changes in the database
        self.connection.commit()

    def save_candlestick_pattern_to_db(self, stock_id, candel_df):   
        excluded_columns = ['date','open','close','high','low','volume']
        
        for index, row in candel_df.iterrows():
            date = row['date']
            patterns = row['patterns']
            
            if patterns == None or patterns == '' or len(patterns) == 0:
                continue
            
            self.cursor.execute("select pattern from CANDLESTICK_PATTERN where stockid=%s and date=%s", (stock_id,date))
            query_result = self.cursor.fetchone()
            existing_pattern_set = set()
            if query_result != None:
                existing_pattern_set = set(query_result[0].split(","))

            print('existing_patterns=' + str(existing_pattern_set))
            pattern_list = patterns.split(",")
            #add new pattern list to existing pattern set
            existing_pattern_set.update(pattern_list)
            
            patterns = ','.join(str(s) for s in existing_pattern_set)
                    
            if patterns != '':    
                print('date=' +date+ ',patterns='+patterns)
                self.cursor.execute("delete from CANDLESTICK_PATTERN where stockid=%s and date=%s", (stock_id,date))
                self.cursor.execute("insert into CANDLESTICK_PATTERN (stockid,date,pattern) values(%s, %s, %s)", (stock_id, date, patterns))

        # Commit your changes in the database
        self.connection.commit()               

    def close_db(self):
        # closing database connection.
        if self.connection:
            self.cursor.close()
            self.connection.close()
