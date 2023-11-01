import numpy as np
import pandas as pd
import os, datetime, sys
import tensorflow as tf
from tensorflow.keras.models import *
from tensorflow.keras.layers import *
from model.transformer import *

import warnings
warnings.filterwarnings('ignore')

class StockTrainHandler():
    def __init__(self, stock_id, train_from_scratch, useCkpId):
        #
        self.stock_id = stock_id
        self.stock_price_path = 'stockData/' + stock_id + '_ind.csv'
        self.train_from_scratch = train_from_scratch # True: Train the model, False: Use the pre-train checkpoints
        self.useCkpId = useCkpId # When train_from_scratch is False and use other checkpoint Id for prediction

        #specify the gpu
        #os.environ["CUDA_VISIBLE_DEVICES"] = self.gpu_device

        #Hyperparameters
        self.batch_size = 64
        self.seq_len = 43 # seq_len=43 意思是用43天的数据预测第44天后的趋势
        self.column_len = 10 #the number of column
        self.predict_column = 9 #which column to be predict, from 0 to column_len-1
        self.d_k = 256
        self.d_v = 256
        self.n_heads = 12
        self.ff_dim = 256


    def load_stock_data(self):
        #Load data and prepare moving average data
        if not os.path.exists(self.stock_price_path):
            print(self.stock_price_path + ' file is not found, sys.exit')
            sys.exit(1)

        df = pd.read_csv(self.stock_price_path, delimiter=',', usecols=['date',  'close',  'dif',  'dea',  'macd',  'k',  'd',  'j', 'h1',  'h2',  'h3'])

        # Replace 0 to avoid dividing by 0 later on
        #df['volume'].replace(to_replace=0, method='ffill', inplace=True)
        df.sort_values('date', inplace=True)


        # Apply moving average with a window of 10 days to all columns
        #df[['open', 'high', 'low', 'close', 'volume']] = df[['open', 'high', 'low', 'close', 'volume']].rolling(10).mean()

        # the max close price in next 19 days
        df['Max_Close_Price_In_Next_N_Days'] = df['close'].rolling(20).max().shift(-19)
        # the close price after 10 days
        df['Close_Price_After_N_Days'] = df['close'].shift(-19)

        # Close price is less than the max close price after 19 days
        df['Max_Close_Diff'] = df['Max_Close_Price_In_Next_N_Days'] - df['close']
        df['Close_Diff'] = df['Close_Price_After_N_Days'] - df['close']

        # Result should be a predict number, 1 means the price trend is increase
        # (the max close and close within next 10 days is higher then current price)

        df['Result'] = 0
        df.loc[(df['Max_Close_Diff'] > 0) & (df['Close_Diff'] > 0), ['Result']] = 1

        del df['close']  #use indicator only
        del df['Max_Close_Diff']
        del df['Close_Diff']
        del df['Close_Price_After_N_Days']
        del df['Max_Close_Price_In_Next_N_Days']

        # Drop all rows with NaN values
        df.dropna(how='any', axis=0, inplace=True) # Drop all NaN rows

        #Moving Average - Calculate normalized percentage change for all columns
        '''Calculate percentage change'''

        # Create arithmetic returns column
        df['dif'] = df['dif'].pct_change()
        df['dea'] = df['dea'].pct_change()
        df['macd'] = df['macd'].pct_change()
        df['k'] = df['k'].pct_change()
        df['d'] = df['d'].pct_change()
        df['j'] = df['j'].pct_change()
        #df['rsv'] = df['rsv'].pct_change()
        #df['qsdd_lt'] = df['qsdd_lt'].pct_change()
        #df['qsdd_st'] = df['qsdd_st'].pct_change()
        #df['qsdd_mt'] = df['qsdd_mt'].pct_change()
        #df['wr_lt'] = df['wr_lt'].pct_change()
        #df['wr_st'] = df['wr_st'].pct_change()
        #df['wr_mt'] = df['wr_mt'].pct_change()
        df['h1'] = df['h1'].pct_change()
        df['h2'] = df['h2'].pct_change()
        df['h3'] = df['h3'].pct_change()

        df.dropna(how='any', axis=0, inplace=True) # Drop all rows with NaN values
        print('after df pct change')
        print(df[-60:])

        return df
    
    def normalize_column_by_min_max(self, df, last_10pct, last_20pct, column_name):
        # Min-max normalize volume columns (0-1 range)
        min_v = df[(df.index < last_20pct)][column_name].min(axis=0)
        max_v = df[(df.index < last_20pct)][column_name].max(axis=0)
        df[column_name] = (df[column_name] - min_v) / (max_v - min_v)
        
        return df

    def prepare_train_dataset(self, df):

        '''Create indexes to split dataset'''

        times = sorted(df.index.values)
        last_10pct = sorted(df.index.values)[-int(0.1*len(times))] # Last 10% of series
        last_20pct = sorted(df.index.values)[-int(0.2*len(times))] # Last 20% of series

        ###############################################################################
        '''Normalize all columns'''
        df = self.normalize_column_by_min_max(df, last_10pct, last_20pct, 'dif')
        df = self.normalize_column_by_min_max(df, last_10pct, last_20pct, 'dea')
        df = self.normalize_column_by_min_max(df, last_10pct, last_20pct, 'macd')
        df = self.normalize_column_by_min_max(df, last_10pct, last_20pct, 'k')
        df = self.normalize_column_by_min_max(df, last_10pct, last_20pct, 'd')
        df = self.normalize_column_by_min_max(df, last_10pct, last_20pct, 'j')
        #df = self.normalize_column_by_min_max(df, last_10pct, last_20pct, 'rsv')
        #df = self.normalize_column_by_min_max(df, last_10pct, last_20pct, 'qsdd_lt')
        #df = self.normalize_column_by_min_max(df, last_10pct, last_20pct, 'qsdd_st')
        #df = self.normalize_column_by_min_max(df, last_10pct, last_20pct, 'qsdd_mt')
        #df = self.normalize_column_by_min_max(df, last_10pct, last_20pct, 'wr_lt')
        #df = self.normalize_column_by_min_max(df, last_10pct, last_20pct, 'wr_st')
        #df = self.normalize_column_by_min_max(df, last_10pct, last_20pct, 'wr_mt')
        df = self.normalize_column_by_min_max(df, last_10pct, last_20pct, 'h1')
        df = self.normalize_column_by_min_max(df, last_10pct, last_20pct, 'h2')
        df = self.normalize_column_by_min_max(df, last_10pct, last_20pct, 'h3')
        
        print('after df Normalize')
        print(df[-60:])

        ###############################################################################
        '''Normalize Result column'''

        min_result = df[(df.index < last_20pct)]['Result'].min(axis=0)
        max_result = df[(df.index < last_20pct)]['Result'].max(axis=0)

        # Min-max normalize Result columns (0-1 range)
        df['Result'] = (df['Result'] - min_result) / (max_result - min_result)
        ###############################################################################
        '''Create training, validation and test split'''

        df_train = df[(df.index < last_20pct)]  # Training data are 80% of total data
        df_val = df[(df.index >= last_20pct) & (df.index < last_10pct)]

        if self.train_from_scratch == 'False' and self.useCkpId != '':
            #if train_from_scratch is False, and specify another checkpoint for predict,
            #then I could like to use all the df data for predict
            df_test = df[(df.index >= 0)]
            df_test_with_date = df[(df.index >= 0)]            
        else:
            #if train_from_scratch is True, then use the only last 10 pct for predict
            df_test = df[(df.index >= last_10pct)]
            df_test_with_date = df[(df.index >= last_10pct)]

        # Remove date column
        df_train.drop(columns=['date'], inplace=True)
        df_val.drop(columns=['date'], inplace=True)
        df_test.drop(columns=['date'], inplace=True)

        # Convert pandas columns into arrays
        train_data = df_train.values
        val_data = df_val.values
        test_data = df_test.values

        print('Training data shape: {}'.format(train_data.shape))
        print('Validation data shape: {}'.format(val_data.shape))
        print('Test data shape: {}'.format(test_data.shape))

        #Moving Average - Create chunks of training, validation, and test data
        # Training data
        X_train, y_train = [], []
        for i in range(self.seq_len, len(train_data)):
            X_train.append(train_data[i-self.seq_len:i]) # Chunks of training data with a length of 128 df-rows
            y_train.append(train_data[:, self.predict_column][i]) #Value of 4th column (Close Price) of df-row 128+1
        X_train, y_train = np.array(X_train), np.array(y_train)

        ###############################################################################

        # Validation data
        X_val, y_val = [], []
        for i in range(self.seq_len, len(val_data)):
            X_val.append(val_data[i-self.seq_len:i])
            y_val.append(val_data[:, self.predict_column][i])
        X_val, y_val = np.array(X_val), np.array(y_val)

        ###############################################################################

        # Test data
        X_test, y_test = [], []
        for i in range(self.seq_len, len(test_data)):
            X_test.append(test_data[i-self.seq_len:i])
            y_test.append(test_data[:, self.predict_column][i])

        X_test, y_test = np.array(X_test), np.array(y_test)

        #The len of df for test should be subtract the seq_len
        df_test_with_date = df_test_with_date[-(len(df_test_with_date)-self.seq_len):]

        print('Dataset shape:')
        print(X_train.shape, y_train.shape)
        print(X_val.shape, y_val.shape)
        print(X_test.shape, y_test.shape, df_test_with_date.shape)
        
        print('before train')
        print(X_train[-60:])
        print(X_test[-60:])
        
        print(y_train[-60:])
        print(y_val[-60:])
        
        print(y_test[-60:])
        print(df_test_with_date[-60:])

        return X_train, X_val, X_test, y_train, y_val, y_test, df_test_with_date


    def create_transformer_model(self):

        #Moving Average - Model
        '''Initialize time and transformer layers'''
        time_embedding = Time2Vector(self.seq_len)
        attn_layer1 = TransformerEncoder(self.d_k, self.d_v, self.n_heads, self.ff_dim)
        attn_layer2 = TransformerEncoder(self.d_k, self.d_v, self.n_heads, self.ff_dim)
        attn_layer3 = TransformerEncoder(self.d_k, self.d_v, self.n_heads, self.ff_dim)

        '''Construct model'''
        in_seq = Input(shape=(self.seq_len, self.column_len))
        x = time_embedding(in_seq)
        x = Concatenate(axis=-1)([in_seq, x])
        x = attn_layer1((x, x, x))
        x = attn_layer2((x, x, x))
        x = attn_layer3((x, x, x))
        x = GlobalAveragePooling1D(data_format='channels_first')(x)
        x = Dropout(0.1)(x)
        x = Dense(64, activation='relu')(x)
        x = Dropout(0.1)(x)
        #out = Dense(1, activation='linear')(x)
        out = Dense(1, activation='sigmoid')(x) #predict 1 or 0

        model = Model(inputs=in_seq, outputs=out)
        #model.compile(loss='mse', optimizer='adam', metrics=['mae', 'mape'])#linear use mse
        model.compile(loss='binary_crossentropy', optimizer='adam', metrics=['mae', 'mape'])#sigmoid use binary_crossentropy

        return model


    def train_model(self):
        model = None
        df = self.load_stock_data()
        X_train, X_val, X_test, y_train, y_val, y_test, df_test_with_date = self.prepare_train_dataset(df)

        callback = tf.keras.callbacks.ModelCheckpoint('./checkpoints/Transformer+TimeEmbedding_ind_' + self.stock_id + '.hdf5',
                                                      monitor='val_loss',
                                                      save_best_only=True,
                                                      verbose=1)

        if self.train_from_scratch == 'True':
            model = self.create_transformer_model()
            model.fit(X_train, y_train,
                            batch_size=self.batch_size,
                            epochs=1,
                            callbacks=[callback],
                            validation_data=(X_val, y_val))
        else:
            ckp = './checkpoints/Transformer+TimeEmbedding_ind_' + self.stock_id + '.hdf5'
            if self.useCkpId != '':
                ckp = './checkpoints/Transformer+TimeEmbedding_ind_' + self.useCkpId + '.hdf5'

            model = tf.keras.models.load_model(ckp,
                                               custom_objects={'Time2Vector': Time2Vector,
                                                               'SingleAttention': SingleAttention,
                                                               'MultiAttention': MultiAttention,
                                                               'TransformerEncoder': TransformerEncoder})


        #Calculate predication for training, validation and test data
        if self.train_from_scratch == 'True':
            train_pred = model.predict(X_train)
            val_pred = model.predict(X_val)

        test_pred = model.predict(X_test)

        #Print evaluation metrics for all datasets
        if self.train_from_scratch == 'True':
            train_eval = model.evaluate(X_train, y_train, verbose=0)
            val_eval = model.evaluate(X_val, y_val, verbose=0)

        test_eval = model.evaluate(X_test, y_test, verbose=0)

        if self.train_from_scratch == 'True':
            print(' ')
            print('Evaluation metrics')
            print('Training Data - Loss: {:.4f}, MAE: {:.4f}, MAPE: {:.4f}'.format(train_eval[0], train_eval[1], train_eval[2]))
            print('Validation Data - Loss: {:.4f}, MAE: {:.4f}, MAPE: {:.4f}'.format(val_eval[0], val_eval[1], val_eval[2]))
            print('Test Data - Loss: {:.4f}, MAE: {:.4f}, MAPE: {:.4f}'.format(test_eval[0], test_eval[1], test_eval[2]))
            
        print('test_pred')
        print(test_pred[-60:])
        return test_pred, df_test_with_date
