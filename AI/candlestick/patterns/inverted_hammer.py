from candlestick.patterns.candlestick_finder import CandlestickFinder


class InvertedHammer(CandlestickFinder):
    def __init__(self, target=None):
        super().__init__(self.get_class_name(), 1, target=target)

    def logic(self, idx):
        candle = self.data.iloc[idx]
        prev_candle = self.data.iloc[idx + 1 * self.multi_coeff]
        b_prev_candle = self.data.iloc[idx + 2 * self.multi_coeff]

        close = candle[self.close_column]
        open = candle[self.open_column]
        high = candle[self.high_column]
        low = candle[self.low_column]

        prev_close = prev_candle[self.close_column]
        prev_open = prev_candle[self.open_column]
        prev_high = prev_candle[self.high_column]
        prev_low = prev_candle[self.low_column]

        b_prev_close = b_prev_candle[self.close_column]
        b_prev_open = b_prev_candle[self.open_column]
        b_prev_high = b_prev_candle[self.high_column]
        b_prev_low = b_prev_candle[self.low_column]

        original_checking = (((high - low) > 3 * (open - close)) and
                ((high - close) / (.001 + high - low) > 0.6)
                and ((high - open) / (.001 + high - low) > 0.6))
        
        #just for my understanding
        rtn = ((close - open)/open >= 0.02) and (abs((open - prev_close)/prev_close) >= 0.0075) and (((high - close)/close) <= 0.015) and (low >= (abs(prev_close - prev_open)/2.0 + min(prev_open, prev_close)))
    
        prev_rtn = (((prev_high - prev_low) > 3 * (prev_open - prev_close)) and
                ((prev_high - prev_close) / (.001 + prev_high - prev_low) > 0.6)
                and ((prev_high - open) / (.001 + prev_high - prev_low) > 0.6))
               
        b_prev_rtn = ((b_prev_open - b_prev_close)/b_prev_close >= 0.02) and (abs((b_prev_close - prev_close)/prev_close) >= 0.0075) and (((b_prev_high - b_prev_open)/b_prev_open) <= 0.015)
                                             
        #return rtn and prev_rtn and b_prev_rtn
        #just for my understanding end
        
        return original_checking
        
