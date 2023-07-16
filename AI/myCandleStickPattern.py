from candlestick import candlestick
import pandas as pd
import requests
from postgres import PostgresDBHandler
postgres = PostgresDBHandler()

# Find candles where inverted hammer is detected

stock_id = '600547'

print('Process ' + stock_id)
postgres.get_stock_price_and_save_to_file(stock_id)
candles_df = pd.read_csv('./stockData/' + stock_id + '.csv', delimiter=',', usecols=['date', 'open', 'high', 'low', 'close', 'volume'])

#https://www.ig.com/en/trading-strategies/16-candlestick-patterns-every-trader-should-know-180615
#https://blog.elearnmarkets.com/35-candlestick-patterns-in-stock-market/
candles_df = candlestick.inverted_hammer(candles_df, target='invertedHammer')#1 Up
#candles_df = candlestick.doji_star(candles_df, target="dojiStar")#2
candles_df = candlestick.bearish_harami(candles_df, target="bearishHarami")#3 Down
candles_df = candlestick.bullish_harami(candles_df, target="bullishHarami")#4 Up
candles_df = candlestick.dark_cloud_cover(candles_df, target="darkCloudCover")#5 Down
#candles_df = candlestick.doji(candles_df, target="doji")#6
#candles_df = candlestick.dragonfly_doji(candles_df, target="dragonflyDoji")#7
candles_df = candlestick.hanging_man(candles_df, target="hangingMan")#8 Down
#candles_df = candlestick.gravestone_doji(candles_df, target="gravestoneDoji")#9
candles_df = candlestick.bearish_engulfing(candles_df, target="bearishEngulfing")#10 Down
candles_df = candlestick.bullish_engulfing(candles_df, target="bullishEngulfing")#11 Up
candles_df = candlestick.hammer(candles_df, target="hammer")#12 Up
candles_df = candlestick.morning_star(candles_df, target="morningStar")#13 Up
#candles_df = candlestick.morning_star_doji(candles_df, target="morningStarDoji")#14
candles_df = candlestick.piercing_pattern(candles_df, target="piercingPattern")#15 Up
#candles_df = candlestick.rain_drop(candles_df, target="rainDrop")#16
#candles_df = candlestick.rain_drop_doji(candles_df, target="rainDropDoji")#17
#candles_df = candlestick.star(candles_df, target="star")#18
candles_df = candlestick.shooting_star(candles_df, target="shootingStar")#19 Down
candles_df = candlestick.evening_star(candles_df, target="eveningStar")#20 Down
#candles_df = candlestick.evening_star_doji(candles_df, target="eveningStarDoji")#21 
#Three white soldiers Up
#Three black crows Down
#White Marubozu Up
#Tweezer Top Down
#Falling Three Methods
#Rising Three Methods
#Upside Tasuki Gap
#Downside Tasuki Gap


#target='InvertedHammers'
#print(candles_df[candles_df[target] == True][['date', target]])

#candles_df.to_csv(stock_id + '_pattern_result.csv', index=False)

postgres.save_candlestick_pattern_to_db(stock_id, candles_df)
