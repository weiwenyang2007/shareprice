from datetime import datetime, timedelta
import http.client
import json
import myLog as myLogger
log = myLogger.setup_custom_logger(__name__)


def get_suggested_buy_price(target_stock):
    log.debug("get_suggested_buy_price {}".format(target_stock['stock_id']))
    price_delta = target_stock['price_delta']
    price_strategy = target_stock['buy_price_strategy']

    if price_strategy == 'HistoryTrade':
        # 高抛低吸做T:建议卖出价格比上次买入价格高price_delta(比如0.022)
        # 优先选择上次买入价格，做T
        last_avg_sell_price = get_last_price_from_history_trades(target_stock['stock_id'], 'Sell')
        log.debug('last_avg_sell_price {}'.format(last_avg_sell_price))
        if last_avg_sell_price > 0.0:
            return last_avg_sell_price * (1.0 - price_delta)

    if price_strategy == 'Shenxian':
        # get suggest buy price from shenXian Indicator (hc5 value)
        # 选择shenxian卖指标，这个值比上面那个last_avg_sell_price要高或者低都有可能
        shenxian_buy_price, last_close = get_indicator_from_easystogu(target_stock['stock_id'], 'Buy')
        log.debug('shenxian_buy_price {}, last_close {}'.format(shenxian_buy_price, last_close))
        if shenxian_buy_price > 0.0:
            return shenxian_buy_price

    if price_strategy == 'HistoryTrade_and_Shenxian':
        last_avg_sell_price = get_last_price_from_history_trades(target_stock['stock_id'], 'Sell')
        shenxian_buy_price, last_close = get_indicator_from_easystogu(target_stock['stock_id'], 'Buy')
        log.debug('last_avg_sell_price {}, shenxian_buy_price {}, last_close {}'.format(last_avg_sell_price, shenxian_buy_price, last_close))
        if last_avg_sell_price > 0.0 and shenxian_buy_price > 0.0:
            return max(last_avg_sell_price * (1.0 - price_delta), shenxian_buy_price)

    if price_strategy == 'HistoryTrade_and_Shenxian_and_PriceDelta':
        last_avg_sell_price = get_last_price_from_history_trades(target_stock['stock_id'], 'Sell')
        shenxian_buy_price, last_close = get_indicator_from_easystogu(target_stock['stock_id'], 'Buy')
        log.debug('last_avg_sell_price {}, shenxian_buy_price {}, last_close {}'.format(last_avg_sell_price, shenxian_buy_price, last_close))
        if last_avg_sell_price > 0.0 and shenxian_buy_price > 0.0 and last_close > 0.0:
            return max(last_avg_sell_price * (1.0 - price_delta), shenxian_buy_price, last_close * (1.0 - price_delta))
               
    log.debug('Can not get_suggested_buy_price for ' + target_stock['stock_id'])
    
    return None     


def get_suggested_sell_price(target_stock):
    log.debug("get_suggested_sell_price {}".format(target_stock))
    price_delta = target_stock['price_delta']
    price_strategy = target_stock['sell_price_strategy']

    if price_strategy == 'HistoryTrade':
        # 高抛低吸做T:建议卖出价格比上次买入价格高price_delta(比如0.022)
        # 优先选择上次买入价格，做T
        last_avg_buy_price = get_last_price_from_history_trades(target_stock['stock_id'], 'Buy')
        log.debug('last_avg_buy_price {}'.format(last_avg_buy_price))
        if last_avg_buy_price > 0.0:
            return last_avg_buy_price * (1.0 + price_delta)

    if price_strategy == 'Shenxian':
        # get suggest buy price from shenXian Indicator (hc6 value)
        # 选择shenxian卖指标，这个值比上面那个last_avg_buy_price要高或者低都有可能
        shenxian_sell_price, last_close = get_indicator_from_easystogu(target_stock['stock_id'], 'Sell')
        log.debug('shenxian_sell_price {}'.format(shenxian_sell_price))
        if shenxian_sell_price > 0.0:
            return shenxian_sell_price

    if price_strategy == 'HistoryTrade_and_Shenxian':
        last_avg_buy_price = get_last_price_from_history_trades(target_stock['stock_id'], 'Buy')
        shenxian_sell_price, last_close = get_indicator_from_easystogu(target_stock['stock_id'], 'Sell')
        log.debug('last_avg_buy_price {}, shenxian_sell_price {}'.format(last_avg_buy_price, shenxian_sell_price))
        if last_avg_buy_price > 0.0 and shenxian_sell_price > 0.0:
            # select the min price
            return min(last_avg_buy_price * (1.0 + price_delta), shenxian_sell_price)

    log.debug('Can not get_suggested_sell_price for ' + target_stock['stock_id'])
    
    return None     


def get_realtime_price_from_sina(stock_id):
    try:
        log.debug('get_realtime_price_from_sina for ' + stock_id)
        
        symbol_value = ''
        if stock_id.startswith('6'):
            symbol_value = 'sh' + stock_id
        elif stock_id.startswith('3') or stock_id.startswith('0'):
            symbol_value = 'sz' + stock_id
            
        conn = http.client.HTTPConnection('vip.stock.finance.sina.com.cn')
        headers = {'Content-type': 'application/json'}
        conn.request('GET', '/quotes_service/view/vML_DataList.php?asc=j&symbol=' + symbol_value + '&num=1', None, headers)

        response = conn.getresponse()
        resp = response.read().decode()
        log.debug('response for ' + stock_id + ' is ' + resp)
        # var minute_data_list = [['15:00:00', '24.89', '215872']];
        arrs = resp.split(',')
        if len(arrs) == 3:
            price = arrs[1].strip()
            log.debug('realtime price for ' + stock_id + ' is ' + price)
            return float(price.replace("'", ""))

        log.warn('Can not get real time price for ' + stock_id)
        return 0.0
        
    except Exception as ex:
        log.exception(ex)
        log.error('get_realtime_price_from_sina End with exception')
        return 0.0


def get_last_price_from_history_trades(stock_id, buyOrSell):
    try:
        log.debug('get_last_price_from_history_trads: {} {}'.format(stock_id, buyOrSell))
        
        his_trade_file = open("Z:/easytrader/data/history_trade.json", "r")
        his_trade_data = json.load(his_trade_file)
        # log.debug('his_trade_data for ' + stock_id + ' is ' + str(his_trade_data))
        # for buy/sell price, we count the average price of the history trade
        total_price = 0.0
        total_count = 0
        for item in his_trade_data:
            if item['stock_id'] == stock_id:
                if item['operation'] == 'Buy' and buyOrSell == 'Buy':
                    total_price += float(item['price'])
                    total_count += 1
                elif item['operation'] == 'Sell' and buyOrSell == 'Sell':
                    total_price += float(item['price'])
                    total_count += 1

        if total_count > 0:
            log.debug('last {} avg price for {} is {}'.format(buyOrSell, stock_id, round(total_price/total_count,2)))
            return round(total_price/total_count,2)

        log.debug('Can not get_last_price_from_history_trads: {} {}'.format(stock_id, buyOrSell))
        
        return 0.0
        
    except Exception as ex:
        log.exception(ex)
        log.error('get_last ' + buyOrSell + ' price_from_history_trads End with exception')
        return 0.0


def get_indicator_from_easystogu(stock_id, buyOrSell):
    try:
        log.debug('get_indicator_from_easystogu {} {}'.format(stock_id, buyOrSell))
        
        conn = http.client.HTTPConnection('192.168.10.200:8080')

        headers = {'Content-type': 'application/json'}

        # use 120 days data for prediction
        days_before_120 = (datetime.today() - timedelta(days=120)).strftime('%Y-%m-%d')  
        today = datetime.today().strftime('%Y-%m-%d')
        dayParm = days_before_120 + '_' + today

        if buyOrSell == 'Buy':
            # Buy
            conn.request('POST', '/portal/indv3/predictTodayBuy/' + stock_id + '/' + dayParm, None, headers)

            response = conn.getresponse()
            respJson = json.loads(response.read().decode())
            log.debug('predictTodayBuy for ' + stock_id + 'is ' + str(respJson))

            if 'B' in respJson['sellFlagsTitle']:
                log.debug('{} ShenxianBuy@{}, lastClose@{}'.format(stock_id,respJson['hc6'],respJson['lastClose']))
                return respJson['hc6'], respJson['lastClose']
        else:
            # Sell
            conn.request('POST', '/portal/indv3/predictTodaySell/' + stock_id + '/' + dayParm, None, headers)

            response = conn.getresponse()
            respJson = json.loads(response.read().decode())
            log.debug('predictTodaySell for ' + stock_id + 'is ' + str(respJson))

            if 'S' in respJson['sellFlagsTitle']:
                log.debug('{} ShenxianSell@{}, lastClose@{}'.format(stock_id,respJson['hc5'],respJson['lastClose']))
                return respJson['hc5'], respJson['lastClose']
                
        #        
        log.debug('Can not get_indicator_from_easystogu {} {}'.format(stock_id, buyOrSell))
        
        return 0.0, 0.0
        
    except Exception as ex:
        log.exception(ex)
        log.debug('get_indicator_from_easystogu {} {} with exception.'.format(stock_id, buyOrSell))
        return 0.0, 0.0
        

if __name__ == "__main__":
    target_stocks_f = open("Z:/easytrader/data/target_stocks.json", "r")
    target_stocks = json.load(target_stocks_f)
    for target_stock in target_stocks:
        rtn = get_suggested_sell_price(target_stock)
        print(rtn)

