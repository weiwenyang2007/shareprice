from candlestick import candlestick
import pandas as pd
import requests
from postgres import PostgresDBHandler
postgres = PostgresDBHandler()

# Find candles where inverted hammer is detected

stockIds = postgres.get_all_stockIds()

for stock_id in stockIds:
    print('Process ' + stock_id)
    postgres.get_stock_price_and_save_to_file(stock_id)
    candles_df = pd.read_csv('./stockData/' + stock_id + '.csv', delimiter=',', usecols=['date', 'open', 'high', 'low', 'close', 'volume'])

    candles_df = candlestick.inverted_hammer(candles_df, target='InvertedHammers')#1
    candles_df = candlestick.doji_star(candles_df, target="doji_star")#2
    candles_df = candlestick.bearish_harami(candles_df, target="bearish_harami")#3
    candles_df = candlestick.bullish_harami(candles_df, target="bullish_harami")#4
    candles_df = candlestick.dark_cloud_cover(candles_df, target="dark_cloud_cover")#5
    candles_df = candlestick.doji(candles_df, target="doji")#6
    candles_df = candlestick.dragonfly_doji(candles_df, target="dragonfly_doji")#7
    candles_df = candlestick.hanging_man(candles_df, target="hanging_man")#8
    candles_df = candlestick.gravestone_doji(candles_df, target="gravestone_doji")#9
    candles_df = candlestick.bearish_engulfing(candles_df, target="bearish_engulfing")#10
    candles_df = candlestick.bullish_engulfing(candles_df, target="bullish_engulfing")#11
    candles_df = candlestick.hammer(candles_df, target="hammer")#12
    candles_df = candlestick.morning_star(candles_df, target="morning_star")#13
    candles_df = candlestick.morning_star_doji(candles_df, target="morning_star_doji")#14
    candles_df = candlestick.piercing_pattern(candles_df, target="piercing_pattern")#15
    candles_df = candlestick.rain_drop(candles_df, target="rain_drop")#16
    candles_df = candlestick.rain_drop_doji(candles_df, target="rain_drop_doji")#17
    candles_df = candlestick.star(candles_df, target="star")#18
    candles_df = candlestick.shooting_star(candles_df, target="shooting_star")#19

    #target='InvertedHammers'
    #print(candles_df[candles_df[target] == True][['date', target]])

    #candles_df.to_csv(stock_id + '_pattern_result.csv', index=False)

    postgres.save_candlestick_pattern_to_db(stock_id, candles_df)
