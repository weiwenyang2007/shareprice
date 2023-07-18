import time
import talib
import pandas as pd
from postgres import PostgresDBHandler
postgres = PostgresDBHandler()

#Refer to below 2 link
#https://www.ig.com/en/trading-strategies/16-candlestick-patterns-every-trader-should-know-180615
#https://blog.elearnmarkets.com/35-candlestick-patterns-in-stock-market/

patterns_up = ['morningStar','shootingStarBullish','bullishHarami','bullishEngulfing','bullishReversal','piercingLineBullish','hangingManBullish','3WhiteSoldiers','mathold','onneck','piercing']
patterns_down = ['eveningStar','shootingStarBearish','bearishHarami','bearishEngulfing','bearishReversal','hangingManBearish','darkCloudCover','3BlackCrows']

def pattern_score(pattern):
    if pattern in patterns_up:
        return 1
    if pattern in patterns_down:
        return -1
    return 0

def patterns_score(patterns):
    pattern_set = set(patterns.split(','))
    score = 0
    for p in pattern_set:
        score += pattern_score(p)
    return score

#Using talib to calcuate the pattern
def candle_pattern_by_talib(data):
    candle_df = pd.DataFrame([])

    candle_df['date'] = data['date']

    #Using talib to calcuate the patterns
    candle_df['2Crows'] = talib.CDL2CROWS(data['open'], data['high'], data['low'], data['close'])#
    candle_df['3BlackCrows'] = talib.CDL3BLACKCROWS(data['open'], data['high'], data['low'], data['close'])#Down
    candle_df['3Inside'] = talib.CDL3INSIDE(data['open'], data['high'], data['low'], data['close'])#Three Inside Up or down??? Up or Down
    candle_df['3LineStrike'] = talib.CDL3LINESTRIKE(data['open'], data['high'], data['low'], data['close'])
    candle_df['3Outside'] = talib.CDL3OUTSIDE(data['open'], data['high'], data['low'], data['close'])#Three Outside Up or Down??? Up or Down
    candle_df['3StarsinSouth'] = talib.CDL3STARSINSOUTH(data['open'], data['high'], data['low'], data['close'])
    candle_df['3WhiteSoldiers'] = talib.CDL3WHITESOLDIERS(data['open'], data['high'], data['low'], data['close'])#Up
    #candle_df['abandonedBaby'] = talib.CDLABANDONEDBABY(data['open'], data['high'], data['low'], data['close'])
    #candle_df['advanceBlock'] = talib.CDLADVANCEBLOCK(data['open'], data['high'], data['low'], data['close'])
    #candle_df['belthold'] = talib.CDLBELTHOLD(data['open'], data['high'], data['low'], data['close'])
    #candle_df['breakAway'] = talib.CDLBREAKAWAY(data['open'], data['high'], data['low'], data['close'])
    #candle_df['closingMarubozu'] = talib.CDLCLOSINGMARUBOZU(data['open'], data['high'], data['low'], data['close'])#White Marubozu??? Up
    #candle_df['concealBabysWall'] = talib.CDLCONCEALBABYSWALL(data['open'], data['high'], data['low'], data['close'])
    #candle_df['counterAttack'] = talib.CDLCOUNTERATTACK(data['open'], data['high'], data['low'], data['close'])
    candle_df['darkCloudCover'] = talib.CDLDARKCLOUDCOVER(data['open'], data['high'], data['low'], data['close'])#Down
    #candle_df['doji'] = talib.CDLDOJI(data['open'], data['high'], data['low'], data['close'])
    #candle_df['dojiStar'] = talib.CDLDOJISTAR(data['open'], data['high'], data['low'], data['close'])
    #candle_df['dragonFlyDoji'] = talib.CDLDRAGONFLYDOJI(data['open'], data['high'], data['low'], data['close'])
    #candle_df['engulfing'] = talib.CDLENGULFING(data['open'], data['high'], data['low'], data['close'])#bearishEngulfing or bullishEngulfing???
    #candle_df['eveningDojiStar'] = talib.CDLEVENINGDOJISTAR(data['open'], data['high'], data['low'], data['close'])
    candle_df['eveningStar'] = talib.CDLEVENINGSTAR(data['open'], data['high'], data['low'], data['close'])#Down
    #candle_df['gapSideSideWhite'] = talib.CDLGAPSIDESIDEWHITE(data['open'], data['high'], data['low'], data['close'])
    #candle_df['graveStoneDoji'] = talib.CDLGRAVESTONEDOJI(data['open'], data['high'], data['low'], data['close'])
    candle_df['hammer'] = talib.CDLHAMMER(data['open'], data['high'], data['low'], data['close'])#Up
    candle_df['hangingMan'] = talib.CDLHANGINGMAN(data['open'], data['high'], data['low'], data['close'])#Down
    #candle_df['harami'] = talib.CDLHARAMI(data['open'], data['high'], data['low'], data['close'])#bullishHarami??? Up
    #candle_df['haramiCross'] = talib.CDLHARAMICROSS(data['open'], data['high'], data['low'], data['close'])
    #candle_df['highWave'] = talib.CDLHIGHWAVE(data['open'], data['high'], data['low'], data['close'])
    #candle_df['hikKake'] = talib.CDLHIKKAKE(data['open'], data['high'], data['low'], data['close'])
    #candle_df['hikKakemod'] = talib.CDLHIKKAKEMOD(data['open'], data['high'], data['low'], data['close'])
    #candle_df['homingPigeon'] = talib.CDLHOMINGPIGEON(data['open'], data['high'], data['low'], data['close'])
    candle_df['identical3Crows'] = talib.CDLIDENTICAL3CROWS(data['open'], data['high'], data['low'], data['close'])
    #candle_df['inneck'] = talib.CDLINNECK(data['open'], data['high'], data['low'], data['close'])
    candle_df['invertedHammer'] = talib.CDLINVERTEDHAMMER(data['open'], data['high'], data['low'], data['close'])#Up
    #candle_df['kicking'] = talib.CDLKICKING(data['open'], data['high'], data['low'], data['close'])
    #candle_df['kickingByLength'] = talib.CDLKICKINGBYLENGTH(data['open'], data['high'], data['low'], data['close'])
    candle_df['ladderBottom'] = talib.CDLLADDERBOTTOM(data['open'], data['high'], data['low'], data['close'])#Tweezer Bottom??? Up
    #candle_df['longLeggedDoji'] = talib.CDLLONGLEGGEDDOJI(data['open'], data['high'], data['low'], data['close'])
    #candle_df['longLine'] = talib.CDLLONGLINE(data['open'], data['high'], data['low'], data['close'])
    #candle_df['marubozu'] = talib.CDLMARUBOZU(data['open'], data['high'], data['low'], data['close'])#Black Marubozu??? Down
    #candle_df['matchingLow'] = talib.CDLMATCHINGLOW(data['open'], data['high'], data['low'], data['close'])
    candle_df['mathold'] = talib.CDLMATHOLD(data['open'], data['high'], data['low'], data['close'])#Up
    #candle_df['morningDojiStar'] = talib.CDLMORNINGDOJISTAR(data['open'], data['high'], data['low'], data['close'])
    candle_df['morningStar'] = talib.CDLMORNINGSTAR(data['open'], data['high'], data['low'], data['close'])#Up
    candle_df['onneck'] = talib.CDLONNECK(data['open'], data['high'], data['low'], data['close'])#Up
    candle_df['piercing'] = talib.CDLPIERCING(data['open'], data['high'], data['low'], data['close'])#Up
    #candle_df['rickshawMan'] = talib.CDLRICKSHAWMAN(data['open'], data['high'], data['low'], data['close'])
    candle_df['riseFall3Methods'] = talib.CDLRISEFALL3METHODS(data['open'], data['high'], data['low'], data['close'])
    #candle_df['separatingLines'] = talib.CDLSEPARATINGLINES(data['open'], data['high'], data['low'], data['close'])
    candle_df['shootingStar'] = talib.CDLSHOOTINGSTAR(data['open'], data['high'], data['low'], data['close'])#Down
    #candle_df['shortLine'] = talib.CDLSHORTLINE(data['open'], data['high'], data['low'], data['close'])
    #candle_df['spinningTop'] = talib.CDLSPINNINGTOP(data['open'], data['high'], data['low'], data['close'])
    #candle_df['stalledPattern'] = talib.CDLSTALLEDPATTERN(data['open'], data['high'], data['low'], data['close'])
    #candle_df['stickSandwich'] = talib.CDLSTICKSANDWICH(data['open'], data['high'], data['low'], data['close'])
    #candle_df['takuri'] = talib.CDLTAKURI(data['open'], data['high'], data['low'], data['close'])
    #candle_df['tasukiGap'] = talib.CDLTASUKIGAP(data['open'], data['high'], data['low'], data['close'])
    #candle_df['thrusting'] = talib.CDLTHRUSTING(data['open'], data['high'], data['low'], data['close'])
    #candle_df['triStar'] = talib.CDLTRISTAR(data['open'], data['high'], data['low'], data['close'])
    candle_df['unique3River'] = talib.CDLUNIQUE3RIVER(data['open'], data['high'], data['low'], data['close'])
    candle_df['upsideGap2Rrows'] = talib.CDLUPSIDEGAP2CROWS(data['open'], data['high'], data['low'], data['close'])
    candle_df['xsideGap3Methods'] = talib.CDLXSIDEGAP3METHODS(data['open'], data['high'], data['low'], data['close'])#Rising Three Methods or Falling Three Methods

    #merge pattern to patterns in one column
    excluded_columns = ['date','open','close','high','low','volume','patterns']
    candle_df['patterns']=''
    for index, row in candle_df.iterrows():
        date = row['date']
        pattern = ''
        for ss in range(len(row.index)):
            if row.index[ss] not in excluded_columns and row.values[ss] != 0:
                pattern += row.index[ss] + ','
        if pattern != '':    
            pattern = pattern[:-1] #remove last character ,
            candle_df['patterns'].iat[index]=pattern
            #print('date=' +date+ ',pattern='+pattern)
            
    return candle_df


#This methid is the fastest one to calcuate the pattern
def candle_pattern_by_batch(lst_0,lst_1,lst_2):    
    
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
    
    if doji:
        strCandle='doji' + ','
    if eveningStar:
        strCandle=strCandle + 'eveningStar' + ','
    if morningStar:
        strCandle=strCandle + 'morningStar' + ','
    if shootingStarBearish:
        strCandle=strCandle + 'shootingStarBearish' + ','
    if shootingStarBullish:
        strCandle=strCandle + 'shootingStarBullish' + ','
    if hammer:
        strCandle=strCandle + 'hammer' + ','
    if invertedHammer:
        strCandle=strCandle + 'invertedHammer' + ','
    if bearishHarami:
        strCandle=strCandle + 'bearishHarami' + ','
    if bullishHarami:
        strCandle=strCandle + 'bullishHarami' + ','
    if bearishEngulfing:
        strCandle=strCandle + 'bearishEngulfing' + ','
    if bullishEngulfing:
        strCandle=strCandle + 'bullishEngulfing' + ','
    if bullishReversal:
        strCandle=strCandle + 'bullishReversal' + ','
    if bearishReversal:
        strCandle=strCandle + 'bearishReversal' + ','
    if piercingLineBullish:
        strCandle=strCandle + 'piercingLineBullish' + ','
    if hangingManBearish:
        strCandle=strCandle + 'hangingManBearish' + ','
    if hangingManBullish:
        strCandle=strCandle + 'hangingManBullish' + ','
    if darkCloudCover:
        strCandle=strCandle + 'darkCloudCover' + ','
        
    if strCandle != '':
        strCandle = strCandle[:-1] #remove last character ,           
        
    #return candle_score
    return strCandle


def candle_pattern_by_simple(df):
    df_candle=df.copy()
    df_candle['score']=0
    df_candle['score_rollsum']=0
    df_candle['patterns']=''

    for c in range(2,len(df_candle)):
        cscore,cpattern=0,''
        lst_2=[df_candle['open'].iloc[c-2],df_candle['high'].iloc[c-2],df_candle['low'].iloc[c-2],df_candle['close'].iloc[c-2]]
        lst_1=[df_candle['open'].iloc[c-1],df_candle['high'].iloc[c-1],df_candle['low'].iloc[c-1],df_candle['close'].iloc[c-1]]
        lst_0=[df_candle['open'].iloc[c],df_candle['high'].iloc[c],df_candle['low'].iloc[c],df_candle['close'].iloc[c]]
        
        df_candle['patterns'].iat[c]=candle_pattern_by_batch(lst_0,lst_1,lst_2) 
       
    return df_candle

#Main run
start_ts = time.time()
stockIds = postgres.get_all_stockIds()
count = 0
for stock_id in stockIds:
    count += 1
    print('Process ' + stock_id + ' ' + str(count) +' of ' + str(len(stockIds)))
    postgres.get_stock_price_and_save_to_file(stock_id)
    data = pd.read_csv('./stockData/' + stock_id + '.csv', delimiter=',', usecols=['date', 'open', 'high', 'low', 'close'])

    #candle_df1 = candle_pattern_by_talib(data)    
    candle_df2 = candle_pattern_by_simple(data)
    
    candle_df = pd.DataFrame([])
    candle_df['date'] = data['date']
    candle_df['patterns'] = candle_df2['patterns'] #+ ',' + candle_df1['patterns']
    candle_df['score'] = list(map(patterns_score, candle_df['patterns']))
    candle_df['score_roll'] = 0 #candle_df['score'].rolling(3).sum()

    postgres.save_candlestick_pattern_to_db(stock_id, candle_df)
    
#Print duration
stop_ts = time.time()
seconds = round(stop_ts - start_ts)
minutes = seconds/60
print('Total time usage: ' + str(seconds) + ' seconds, or ' + str(minutes) + ' minutes')
