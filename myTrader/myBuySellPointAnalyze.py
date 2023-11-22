from datetime import datetime, timedelta
import http.client
import json
import myLog as myLogger
log = myLogger.setup_custom_logger(__name__)

def get_indicator_from_easystogu(stock_id):
    try:
        log.debug('get_indicator_from_easystogu {}'.format(stock_id))

        conn = http.client.HTTPConnection('192.168.10.200:8080')

        headers = {'Content-type': 'application/json'}

        # use 120 days data for prediction
        days_before_120 = (datetime.today() - timedelta(days=100)).strftime('%Y-%m-%d')
        today = datetime.today().strftime('%Y-%m-%d')
        dayParm = days_before_120 + '_' + today

        log.debug('dayParm {}'.format(dayParm))

        conn.request('POST', '/portal/indv3/shenxianSell/' + stock_id + '/' + dayParm, None, headers)

        response = conn.getresponse()
        respJson = json.loads(response.read().decode())

        balance_remain = 100000.0
        max_hold_number = 600
        min_hold_number = 200
        base_buy_number = 200
        base_sell_number = 200
        max_trade_number_per_day = 200

        cur_hold_number = 0

        for sxvo in respJson:
            if 'B' in sxvo['sellFlagsTitle']:
                log.debug('{} {} buy@{} close@{}'.format(stock_id, sxvo['date'], sxvo['hc6'], sxvo['close']))
                if (cur_hold_number + base_buy_number) <= max_hold_number:
                    cur_hold_number = cur_hold_number + base_buy_number
                    balance_remain = balance_remain - (base_buy_number * float(sxvo['hc6']))
                    balance_stockHold = cur_hold_number * float(sxvo['close'])
                    balance_total = balance_remain + balance_stockHold
                    log.debug('  Buy.  balance_total {}, balance_stockHold {}, balance_remain {}, cur_hold_number {}'
                              .format(balance_total, balance_stockHold, balance_remain, cur_hold_number))
            elif 'S' in sxvo['sellFlagsTitle']:
                log.debug('{} {} sell@{} close@{}'.format(stock_id, sxvo['date'], sxvo['hc5'], sxvo['close']))
                if (cur_hold_number - base_sell_number) >= min_hold_number:
                    cur_hold_number = cur_hold_number - base_sell_number
                    balance_remain = balance_remain + (base_buy_number * float(sxvo['hc5']))
                    balance_stockHold = cur_hold_number * float(sxvo['close'])
                    balance_total = balance_remain + balance_stockHold
                    log.debug('  Sel.  balance_total {}, balance_stockHold {}, balance_remain {}, cur_hold_number {}'
                              .format(balance_total, balance_stockHold, balance_remain, cur_hold_number))

        return 0.0

    except Exception as ex:
        log.exception(ex)
        log.debug('get_indicator_from_easystogu {} with exception.'.format(stock_id))
        return 0.0


if __name__ == "__main__":
    get_indicator_from_easystogu('300688')