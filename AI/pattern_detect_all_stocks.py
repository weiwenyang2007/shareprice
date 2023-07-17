import time
import talib
import pandas as pd
from postgres import PostgresDBHandler
postgres = PostgresDBHandler()

#Refer to below 2 link
#https://www.ig.com/en/trading-strategies/16-candlestick-patterns-every-trader-should-know-180615
#https://blog.elearnmarkets.com/35-candlestick-patterns-in-stock-market/

#Main run
start_ts = time.time()
stockIds = postgres.get_all_stockIds()
count = 0
for stock_id in stockIds:
    count += 1
    print('Process ' + stock_id + ' ' + str(count) +' of ' + str(len(stockIds)))
    postgres.get_stock_price_and_save_to_file(stock_id)
    data = pd.read_csv('./stockData/' + stock_id + '.csv', delimiter=',', usecols=['date', 'open', 'high', 'low', 'close'])

    candle_df = pd.DataFrame([])

    candle_df['date'] = data['date']

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

    #data.to_csv(stock_id + '_pattern_result.csv', index=False)

    postgres.save_candlestick_pattern_to_db(stock_id, candle_df)
    
#Print duration
stop_ts = time.time()
seconds = round(stop_ts - start_ts)
minutes = seconds/60
print('Total time usage: ' + str(seconds) + ' seconds, or ' + str(minutes) + ' minutes')