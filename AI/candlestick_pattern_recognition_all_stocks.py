#https://github.com/sunilgug/Candlestick_patterns/blob/master/candlestick_pattern_recognition_with_example.py
import pandas as pd
import numpy as np
from postgres import PostgresDBHandler
postgres = PostgresDBHandler()

def candle_score(lst_0,lst_1,lst_2):    
    
    O_0,H_0,L_0,C_0=lst_0[0],lst_0[1],lst_0[2],lst_0[3]
    O_1,H_1,L_1,C_1=lst_1[0],lst_1[1],lst_1[2],lst_1[3]
    O_2,H_2,L_2,C_2=lst_2[0],lst_2[1],lst_2[2],lst_2[3]
    
    DojiSize = 0.1
    
    doji=(abs(O_0 - C_0) <= (H_0 - L_0) * DojiSize)
    
    hammer=(((H_0 - L_0)>3*(O_0 -C_0)) &  ((C_0 - L_0)/(.001 + H_0 - L_0) > 0.6) & ((O_0 - L_0)/(.001 + H_0 - L_0) > 0.6))
    
    invertedHammer=(((H_0 - L_0)>3*(O_0 -C_0)) &  ((H_0 - C_0)/(.001 + H_0 - L_0) > 0.6) & ((H_0 - O_0)/(.001 + H_0 - L_0) > 0.6))
    
    bullishReversal= (O_2 > C_2)&(O_1 > C_1)&doji
    
    bearishReversal= (O_2 < C_2)&(O_1 < C_1)&doji
    
    eveningStar=(C_2 > O_2) & (min(O_1, C_1) > C_2) & (O_0 < min(O_1, C_1)) & (C_0 < O_0 )
    
    morningStar=(C_2 < O_2) & (min(O_1, C_1) < C_2) & (O_0 > min(O_1, C_1)) & (C_0 > O_0 )
    
    shootingStarBearish=(O_1 < C_1) & (O_0 > C_1) & ((H_0 - max(O_0, C_0)) >= abs(O_0 - C_0) * 3) & ((min(C_0, O_0) - L_0 )<= abs(O_0 - C_0)) & invertedHammer
    
    shootingStarBullish=(O_1 > C_1) & (O_0 < C_1) & ((H_0 - max(O_0, C_0)) >= abs(O_0 - C_0) * 3) & ((min(C_0, O_0) - L_0 )<= abs(O_0 - C_0)) & invertedHammer
    
    bearishHarami=(C_1 > O_1) & (O_0 > C_0) & (O_0 <= C_1) & (O_1 <= C_0) & ((O_0 - C_0) < (C_1 - O_1 ))
    
    bullishHarami=(O_1 > C_1) & (C_0 > O_0) & (C_0 <= O_1) & (C_1 <= O_0) & ((C_0 - O_0) < (O_1 - C_1))
    
    bearishEngulfing=((C_1 > O_1) & (O_0 > C_0)) & ((O_0 >= C_1) & (O_1 >= C_0)) & ((O_0 - C_0) > (C_1 - O_1 ))
    
    bullishEngulfing=(O_1 > C_1) & (C_0 > O_0) & (C_0 >= O_1) & (C_1 >= O_0) & ((C_0 - O_0) > (O_1 - C_1 ))
    
    piercingLineBullish=(C_1 < O_1) & (C_0 > O_0) & (O_0 < L_1) & (C_0 > C_1)& (C_0>((O_1 + C_1)/2)) & (C_0 < O_1)

    hangingManBullish=(C_1 < O_1) & (O_0 < L_1) & (C_0>((O_1 + C_1)/2)) & (C_0 < O_1) & hammer

    hangingManBearish=(C_1 > O_1) & (C_0>((O_1 + C_1)/2)) & (C_0 < O_1) & hammer
    
    darkCloudCover=((C_1 > O_1) & (((C_1 + O_1) / 2) > C_0) & (O_0 > C_0) & (O_0 > C_1) & (C_0 > O_1) & ((O_0 - C_0) / (.001 + (H_0 - L_0)) > 0.6))

    strCandle=''
    candle_score=0
    
    if doji:
        strCandle='doji' + ','
    if eveningStar:
        strCandle=strCandle + 'eveningStar' + ','
        candle_score=candle_score-1
    if morningStar:
        strCandle=strCandle + 'morningStar' + ','
        candle_score=candle_score+1
    if shootingStarBearish:
        strCandle=strCandle + 'shootingStarBearish' + ','
        candle_score=candle_score-1
    if shootingStarBullish:
        strCandle=strCandle + 'shootingStarBullish' + ','
        candle_score=candle_score-1
    if hammer:
        strCandle=strCandle + 'hammer' + ','
    if invertedHammer:
        strCandle=strCandle + 'invertedHammer' + ','
    if bearishHarami:
        strCandle=strCandle + 'bearishHarami' + ','
        candle_score=candle_score-1
    if bullishHarami:
        strCandle=strCandle + 'bullishHarami' + ','
        candle_score=candle_score+1
    if bearishEngulfing:
        strCandle=strCandle + 'bearishEngulfing' + ','
        candle_score=candle_score-1
    if bullishEngulfing:
        strCandle=strCandle + 'bullishEngulfing' + ','
        candle_score=candle_score+1
    if bullishReversal:
        strCandle=strCandle + 'bullishReversal' + ','
        candle_score=candle_score+1
    if bearishReversal:
        strCandle=strCandle + 'bearishReversal' + ','
        candle_score=candle_score-1
    if piercingLineBullish:
        strCandle=strCandle + 'piercingLineBullish' + ','
        candle_score=candle_score+1
    if hangingManBearish:
        strCandle=strCandle + 'hangingManBearish' + ','
        candle_score=candle_score-1
    if hangingManBearish:
        strCandle=strCandle + 'hangingManBullish' + ','
        candle_score=candle_score+1
    if darkCloudCover:
        strCandle=strCandle + 'darkCloudCover' + ','
        candle_score=candle_score-1
        
    if strCandle != '':
        strCandle = strCandle[:-1] #remove last character ,           
        
    #return candle_score
    return candle_score,strCandle

def candle_df(df):
    #df_candle=first_letter_upper(df)
    df_candle=df.copy()
    df_candle['score']=0
    df_candle['patterns']=''


    for c in range(2,len(df_candle)):
        cscore,cpattern=0,''
        lst_2=[df_candle['open'].iloc[c-2],df_candle['high'].iloc[c-2],df_candle['low'].iloc[c-2],df_candle['close'].iloc[c-2]]
        lst_1=[df_candle['open'].iloc[c-1],df_candle['high'].iloc[c-1],df_candle['low'].iloc[c-1],df_candle['close'].iloc[c-1]]
        lst_0=[df_candle['open'].iloc[c],df_candle['high'].iloc[c],df_candle['low'].iloc[c],df_candle['close'].iloc[c]]
        cscore,cpattern=candle_score(lst_0,lst_1,lst_2)    
        df_candle['score'].iat[c]=cscore
        df_candle['patterns'].iat[c]=cpattern
    
    df_candle['score_rollsum']=df_candle['score'].rolling(3).sum()
    
    return df_candle


#Main run
stockIds = postgres.get_all_stockIds()
count = 0
for stock_id in stockIds:
    count += 1
    print('Process ' + stock_id + ' ' + str(count) +' of ' + str(len(stockIds)))
    postgres.get_stock_price_and_save_to_file(stock_id)
    data = pd.read_csv('./stockData/' + stock_id + '.csv', delimiter=',', usecols=['date', 'open', 'high', 'low', 'close'])

    df=candle_df(data)

    #print(df[:][['patterns','score_rollsum']])
    #df.to_csv(stock_id + '_pattern_result.csv', index=False)
    postgres.save_candlestick_pattern_to_db(stock_id, df)
