from datetime import datetime, timedelta
from operator import itemgetter
import http.client
import json
import myLog as myLogger
log = myLogger.setup_custom_logger(__name__)

max_hold_number = 600
min_hold_number = 200
base_buy_number = 200
base_sell_number = 200
max_trade_number_per_day = 200
price_date_length = 120
http_headers = {'Content-type': 'application/json'}

def simulate_trade(stock_id):
    try:
        log.debug('get_indicator_from_easystogu {}'.format(stock_id))

        http_connect = http.client.HTTPConnection('192.168.10.200:8080')

        shenXianUIVOs = get_shenxianSell(http_connect, stock_id)
        stockPrices = get_stockprice(http_connect, stock_id)

        if len(shenXianUIVOs) != len(stockPrices):
            log.error('Length is incorrect')
            exit(1)

        balance_remain = 100000.0
        cur_hold_number = 0
        contract_id = 0
        history_trade = []

        for index in range(len(shenXianUIVOs)):
            sxvo = shenXianUIVOs[index]
            spvo = stockPrices[index]

            if sxvo['date'] != spvo['date']:
                log.error('date is incorrect')
                exit(1)

            suggested_buy_price = get_suggested_buy_price(history_trade, sxvo, spvo)
            suggested_sell_price = get_suggested_sell_price(history_trade, sxvo, spvo)

            log.debug('{} {} high {} low {} close {} suggested_buy_price {} suggested_sell_price {}'.format(stock_id, spvo['date'], spvo['high'], spvo['low'], spvo['close'],suggested_buy_price, suggested_sell_price))

            if (cur_hold_number - base_sell_number) >= min_hold_number and suggested_sell_price > 0.0:
                cur_hold_number = cur_hold_number - base_sell_number
                balance_remain = balance_remain + (base_buy_number * suggested_sell_price)
                balance_stockHold = cur_hold_number * float(spvo['close'])
                balance_total = balance_remain + balance_stockHold
                stock = {}
                contract_id += 1
                stock['datetime'] = sxvo['date']
                stock['contract_id'] = contract_id
                stock['number'] = base_sell_number
                stock['price'] = suggested_sell_price
                stock['operation'] = 'Sell'
                history_trade.append(stock)
                log.debug('  Sel@{}  balance_total {}, balance_stockHold {}, balance_remain {}, cur_hold_number {}'
                          .format(suggested_sell_price, balance_total, balance_stockHold, balance_remain, cur_hold_number))

            if (cur_hold_number + base_buy_number) <= max_hold_number and suggested_buy_price > 0.0:
                cur_hold_number = cur_hold_number + base_buy_number
                balance_remain = balance_remain - (base_buy_number * suggested_buy_price)
                balance_stockHold = cur_hold_number * float(spvo['close'])
                balance_total = balance_remain + balance_stockHold
                stock = {}
                contract_id += 1
                stock['datetime'] = sxvo['date']
                stock['contract_id'] = contract_id
                stock['number'] = base_buy_number
                stock['price'] = suggested_buy_price
                stock['operation'] = 'Buy'
                history_trade.append(stock)

                log.debug('  Buy@{}  balance_total {}, balance_stockHold {}, balance_remain {}, cur_hold_number {}'
                          .format(suggested_buy_price, balance_total, balance_stockHold, balance_remain, cur_hold_number))
        #end for
        log.debug('history_trade:')
        log.debug(str(history_trade))

    except Exception as ex:
        log.exception(ex)
        log.debug('simulate_trade {} with exception.'.format(stock_id))


def get_shenxianSell(http_connect, stock_id):
    # use 120 days data for prediction
    days_before_120 = (datetime.today() - timedelta(days=price_date_length)).strftime('%Y-%m-%d')
    today = datetime.today().strftime('%Y-%m-%d')
    day_parm = days_before_120 + '_' + today

    http_connect.request('POST', '/portal/indv3/shenxianSell/' + stock_id + '/' + day_parm, None, http_headers)

    response = http_connect.getresponse()
    return json.loads(response.read().decode())


def get_stockprice(http_connect, stock_id):
    # use 120 days data for prediction
    days_before_120 = (datetime.today() - timedelta(days=price_date_length)).strftime('%Y-%m-%d')
    today = datetime.today().strftime('%Y-%m-%d')
    day_parm = days_before_120 + '_' + today

    http_connect.request('POST', '/portal/pricev3/' + stock_id + '/' + day_parm, None, http_headers)

    response = http_connect.getresponse()
    return json.loads(response.read().decode())


def get_last_price_from_history_trades(history_trade, buyOrSell):
    max_keep_record_number = 3
    history_trade_arr = list(filter(lambda item: (buyOrSell in item['operation']), history_trade))
    if len(history_trade_arr) > max_keep_record_number:
        history_trade_arr.sort(key=itemgetter('datetime'), reverse=True)
        history_trade_arr = history_trade_arr[:max_keep_record_number]

    total_price = 0.0
    total_count = 0
    for item in history_trade_arr:
        total_price += float(item['price'])
        total_count += 1

    if total_count > 0:
        return total_price/total_count

    return 0.0


def get_indicator_from_easystogu(sxvo, buyOrSell):
    if 'B' in sxvo['sellFlagsTitle'] and buyOrSell == 'Buy':
        return sxvo['hc6']
    if 'S' in sxvo['sellFlagsTitle'] and buyOrSell == 'Sell':
        return sxvo['hc5']
    return 0.0


def get_suggested_buy_price(history_trade, sxvo, spvo):
    # first get last Sell trade price from history trades
    # 高抛低吸做T:建议买入价格比上次卖出价格低0.022
    last_avg_sell_price = get_last_price_from_history_trades(history_trade, 'Sell')
    #log.debug('last_avg_sell_price {}'.format(last_avg_sell_price))

    #ignore the last_avg_sell_price if it is not within lowest and highest price
    if last_avg_sell_price > spvo['high']:
        # for example: 卖出后大跌
        last_avg_sell_price = min(last_avg_sell_price * 0.978, spvo['high'])
        return last_avg_sell_price
    elif last_avg_sell_price < spvo['low']:
        # for example: 卖出后大涨
        last_avg_sell_price = 0.0

    #Has risk to based on realtime
    #price = get_realtime_price_from_sina(stock_id)
    #if price:
    #    return price * 0.978

    # else get suggest buy price from shenXian Indicator (hc6 value)
    shenxian_buy_price = get_indicator_from_easystogu(sxvo, 'Buy')
    #log.debug('shenxian_buy_price {}'.format(shenxian_buy_price))

    if last_avg_sell_price > 0.0 and shenxian_buy_price > 0.0:
        return max(last_avg_sell_price * 0.978, shenxian_buy_price)
    elif last_avg_sell_price > 0.0:
        return last_avg_sell_price * 0.978
    elif shenxian_buy_price > 0.0:
        return shenxian_buy_price

    return 0.0


def get_suggested_sell_price(history_trade, sxvo, spvo):
    # first get last Buy trade price from history trades
    # 高抛低吸做T:建议卖出价格比上次买入价格高0.022
    # 优先选择上次买入价格，做T
    last_avg_buy_price = get_last_price_from_history_trades(history_trade, 'Buy')
    #log.debug('last_avg_buy_price {}'.format(last_avg_buy_price))

    if last_avg_buy_price > spvo['high']:
        # for example: 买入后大跌
        last_avg_buy_price = 0.0
    elif last_avg_buy_price < spvo['low']:
        # for example: 买入后大涨
        last_avg_buy_price = max(last_avg_buy_price * 1.022, spvo['low'])
        return last_avg_buy_price

    # realtime_price = get_realtime_price_from_sina(stock_id)
    #if realtime_price:
    #    return realtime_price

    # else get suggest buy price from shenXian Indicator (hc6 value)
    # 其次选择shenxian卖指标，这个值比上面那个要高或者低都有可能
    shenxian_sell_price = get_indicator_from_easystogu(sxvo, 'Sell')
    #log.debug('shenxian_sell_price {}'.format(shenxian_sell_price))

    # select the min price
    if last_avg_buy_price > 0.0 and shenxian_sell_price > 0.0:
        return min(last_avg_buy_price * 1.022, shenxian_sell_price)
    elif last_avg_buy_price > 0.0:
        return last_avg_buy_price * 1.022
    elif shenxian_sell_price > 0.0:
        return shenxian_sell_price

    return 0.0


if __name__ == "__main__":
    simulate_trade('300688')