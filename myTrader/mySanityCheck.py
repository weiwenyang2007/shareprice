import sys
sys.path.append('Z:/easytrader/github/easytrader')

from datetime import datetime
from operator import itemgetter
from pywinauto import application

import json
import easytrader
import myLog as myLogger
import myGetBuySellPrice

log = myLogger.setup_custom_logger(__name__)

from pytesseract import pytesseract
pytesseract.tesseract_cmd = r'C:\Program Files\Tesseract-OCR\tesseract.exe'

user = easytrader.use('universal_client')
user.enable_type_keys_for_editor()


def start_app():
    try:
        log.info('Start hexin and xiadan app')
        user.start(r'C:\同花顺软件\同花顺\hexin.exe')
        user.start(r'C:\同花顺软件\同花顺\xiadan.exe')        
        return True
    except Exception as ex:
        log.exception(ex)
        log.error('Start hexin and xiadan app with exception')
        return None


def connect_to_app():
    try:
        log.info('Connecting to xiadan app')
        user.connect(r'C:\同花顺软件\同花顺\xiadan.exe')
        return True
    except application.ProcessNotFoundError as error:
        log.error('xiadan application not found')
        
        retry = 1
        if retry <= 3:
            log.info('retry {} to start app'.format(str(retry)))
            try:
                start_app()
                user.connect(r'C:\同花顺软件\同花顺\xiadan.exe')    
                retry += 1
                return True
            except Exception as ex:
                log.exception(ex)
                log.error('retry start_app and Connecting to xiadan app with exception')
                user.exit()
                return None
        
    except Exception as ex:
        log.exception(ex)
        log.error('Connecting to xiadan app with exception')
        user.exit()
        return None


def sanity_check(target_stocks):
    try:
        log.info('Sanity start')

        today = datetime.today().strftime('%Y-%m-%d')
        history_trade = open("Z:/easytrader/data/history_trade.json", "r")
        history_trade_balance_data = json.load(history_trade)
        history_trade.close()

        balance_data = {}
        # 资金
        try:
            log.debug('get user.balance')
            balance = user.balance
            log.debug('当前资金:' + str(balance))
        except Exception as ex:
            log.exception(ex)
            log.error('get user.balance with exception')
            user.exit()
            return None

        money = {'balance_remain': balance['资金余额'],
                 'balance_usable': balance['可用金额'],
                 'balance_stockHold': balance['股票市值'],
                 'balance_total': balance['总资产']}

        balance_data['money'] = money

        # 当前持仓
        try:
            log.debug('get user.position')
            position = user.position
            log.debug('当前持仓:' + str(position))
        except Exception as ex:
            log.exception(ex)
            log.error('get user.position with exception')
            user.exit()
            return None

        stock_holds = []
        for item in position:
            stock = {'stock_id': item['证券代码'],
                     'hold_number': item['股票余额'],
                     'usable_number': item['可用余额'],
                     'freeze_number': item['冻结数量'],
                     'trade_price': item['成本价'],
                     'balance': item['市值']}
            log.debug(stock)
            stock_holds.append(stock)

        if len(stock_holds) == 0 and money['balance_stockHold'] > 0.0:
            log.error('股票市值 不为0, 但是没有查询到持仓股票')
            raise Exception("Sorry, no stock onHold is found") 
            
        balance_data['stock_holds'] = stock_holds

        # 当日成交
        try:
            log.debug('get user.today_trades')
            today_trades = user.today_trades
            log.debug('当日成交:' + str(today_trades))
        except Exception as ex:
            log.exception(ex)
            log.error('get user.today_trades with exception')
            user.exit()
            return None

        stock_today_trades = []
        for item in today_trades:
            stock = {'datetime': today + ' ' + item['成交时间'],
                     'stock_id': item['证券代码'],
                     'number': item['成交数量'],
                     'price': item['成交均价'],
                     'contract_id': item['合同编号'],
                     'operation': 'Buy' if item['操作'] == "证券买入" else 'Sell' if item['操作'] == "证券卖出" else 'Unknown'}
            stock_today_trades.append(stock)
            
            exist = False
            for his_item in history_trade_balance_data:
                log.debug('his_item='+str(his_item))
                if his_item['contract_id'] == stock['contract_id'] and his_item['datetime'] == stock['datetime'] and his_item['stock_id'] == stock['stock_id']:
                    # already exist
                    exist = True
                    break
            
            if not exist:
                # New trade in today, add it to history trade data
                # TODO: at the sametime, remove one of its couterpart from history trade data
                history_trade_balance_data.append(stock)

        balance_data['stock_today_trades'] = stock_today_trades

        # 当日委托
        try:
            log.debug('get user.today_entrusts')
            today_entrusts = user.today_entrusts
            log.debug('当日委托:' + str(today_entrusts))
            # 操作：买入，卖出
            # 备注：未报，已报，已撤，已成
            # 委托类别：委托，撤单
        except Exception as ex:
            log.exception(ex)
            log.error('get user.today_entrusts with exception')
            user.exit()
            return None

        stock_today_entrusts = []
        money_occupy = 0.0
        for item in today_entrusts:
            # 过滤出没有撤销和成交的委托订单,剩下的就是等待成交的委托订单
            if item['成交数量'] == 0 and item['备注'] in ['未报', '已报', '未报待撤', '已报待撤']:
                stock = {'stock_id': item['证券代码'],
                         'number': item['委托数量'],
                         'price': item['委托价格'],
                         'contract_id': item['合同编号'],
                         'operation': 'Buy' if item['操作'] == "买入" else 'Sell' if item['操作'] == "卖出" else item['操作']}

                if stock['operation'] == 'Buy':
                    money_occupy += (int(stock['number']) * float(stock['price']))
                log.debug('Entrust money_occupy: '+str(money_occupy))
                stock_today_entrusts.append(stock)

        balance_data['stock_today_entrusts'] = stock_today_entrusts

        log.debug('balance_data to save is:' +str(balance_data))
        # sanity check for the balance and stock
        # 总资产 = 可用金额 + 股票市值 + 委托占用资金
        # 总资产 - (可用金额 + 股票市值 + 委托占用资金) 的绝对差不能大于某个数。如果上面的查询有异常，资金肯定有很大的出入。
        if abs(float(money['balance_total']) - (float(money['balance_usable']) + float(money['balance_stockHold']) + money_occupy)) >= 100.0:
            log.error("资金验证发现异常,很可能是查询不到某些数据,比如委托订单,当前持仓等")
            raise Exception("Money and stock balance check failure")

        # Sanity Check is OK for this time, update the json file.
        # save to balance_data file
        with open("Z:/easytrader/data/balance.json", "w") as write_file:
            json.dump(balance_data, write_file, indent=2, sort_keys=True)

        after_filter = filter_history_trade_data(history_trade_balance_data, target_stocks)
        if after_filter:
            with open("Z:/easytrader/data/history_trade.json", "w") as write_file:
                json.dump(after_filter, write_file, indent=2, sort_keys=True)
            
        log.info('Sanity success and end')    
        return balance_data
    except Exception as ex:
        log.exception(ex)
        log.error('Sanity end with exception')
        return None


def filter_history_trade_data(history_trade_balance_data, target_stocks):
    try:
        # filter and sort the history_trade_balance_data
        # 只保留当前仓位数量对应的条数
        rtn = []
        for target_stock in target_stocks:
            # To handle Buy:
            # filter condition: operation==Buy and stock_id is balance_item['stock_id']
            history_trade_balance_buy_data = list(filter(lambda item: ('Buy' in item['operation'] and target_stock['stock_id'] == item['stock_id']), history_trade_balance_data))
            if len(history_trade_balance_buy_data) <= target_stock['max_keep_history_trade_number']:
                log.debug('Keep the current history trade buy record')
            else:
                log.debug('Filter out the oldest history trade buy record')
                history_trade_balance_buy_data.sort(key=itemgetter('datetime'), reverse=True)
                history_trade_balance_buy_data = history_trade_balance_buy_data[:target_stock['max_keep_history_trade_number']]

            rtn.extend(history_trade_balance_buy_data)

            # To handle Sell:
            # filter condition: operation==Sell and stock_id is balance_item['stock_id']
            history_trade_balance_sell_data = list(filter(lambda item: ('Sell' in item['operation'] and target_stock['stock_id'] == item['stock_id']), history_trade_balance_data))

            if len(history_trade_balance_sell_data) <= target_stock['max_keep_history_trade_number']:
                log.debug('Keep the current history trade sell record')
            else:
                log.debug('Filter out the oldest history trade sell record')
                history_trade_balance_sell_data.sort(key=itemgetter('datetime'), reverse=True)
                history_trade_balance_sell_data = history_trade_balance_sell_data[:target_stock['max_keep_history_trade_number']]

            rtn.extend(history_trade_balance_sell_data)

        log.debug("after_filter_history_trade_balance_data is: %s", str(rtn))
        return rtn
    except Exception as ex:
        log.exception(ex)
        log.error('filter_history_trade_data end with exception')
        return None


def check_buy_condition(balance_data, target_stock):
    try:
        log.debug('check_buy_condition for ' + target_stock['stock_id'])

        curr_hold_number = 0
        curr_entrust_number = 0
        curr_trades_number = 0
        money = balance_data['money']

        # 检查当前持股数量
        if 'stock_holds' in balance_data:
            stock_holds = balance_data['stock_holds']
            for stock in stock_holds:
                if stock['stock_id'] == target_stock['stock_id']:
                    curr_hold_number += stock['hold_number']
                    
        # 检查今日委托买入数量(还在等待成交的)
        if 'stock_today_entrusts' in balance_data:
            stock_today_entrusts = balance_data['stock_today_entrusts']
            for stock in stock_today_entrusts:
                if stock['stock_id'] == target_stock['stock_id'] and stock['operation'] == 'Buy':
                    curr_entrust_number += stock['number']
                    
        # 检查当日成交数量
        if 'stock_today_trades' in balance_data:
            stock_today_trades = balance_data['stock_today_trades']
            for stock in stock_today_trades:
                if stock['stock_id'] == target_stock['stock_id'] and stock['operation'] == 'Buy':
                    curr_trades_number += stock['number']
            
        log.debug('curr_hold_number: {}, curr_entrust_number: {}, curr_trades_number: {}'
                  .format(curr_hold_number, curr_entrust_number, curr_trades_number))
        # 当前持股数量和委托数量都少于预定最大值, 并且没有委托订单(还没有成交的订单),还可以加仓,限制一日成交不超过预定值max_trade_number_per_day
        if curr_trades_number < target_stock['max_trade_number_per_day'] \
                and curr_hold_number < target_stock['max_hold_number'] \
                and curr_entrust_number == 0:
            # Can buy stock now
            buy_price = myGetBuySellPrice.get_suggested_buy_price(target_stock)
            if not buy_price:
                log.debug('There is no suggest buy_price for ' + target_stock['stock_id'])
                return None
            if money['balance_usable'] <= buy_price * target_stock['base_buy_number']:
                log.debug('Not enough money to buy ' + target_stock['stock_id'])
                return None

            buy_items = {'buy_price': buy_price,
                         'stock_id': target_stock['stock_id'],
                         'buy_number': target_stock['base_buy_number']}

            log.debug('check_buy_condition result: ' + str(buy_items))
            return buy_items
            
        #     
        log.debug('Can not buy more stock for ' + target_stock['stock_id'])
        return None    
    except Exception as ex:
        log.exception(ex)
        log.error('check_buy_condition end with exception') 
        return None
        
        
def check_sell_condition(balance_data, target_stock):
    try:
        log.debug('check_sell_condition for ' + target_stock['stock_id'])

        curr_usable_number = 0
        curr_entrust_number = 0
        curr_trades_number = 0
        
        # 检查当前持股数量
        if 'stock_holds' in balance_data:
            stock_holds = balance_data['stock_holds']
            for stock in stock_holds:
                if stock['stock_id'] == target_stock['stock_id']:
                    curr_usable_number = stock['usable_number']
                    
        # 检查今日委托卖出数量(还在等待成交的)
        if 'stock_today_entrusts' in balance_data:
            stock_today_entrusts = balance_data['stock_today_entrusts']
            for stock in stock_today_entrusts:
                if stock['stock_id'] == target_stock['stock_id'] and stock['operation'] == 'Sell':
                    curr_entrust_number = stock['number']

        # 检查当日成交数量
        if 'stock_today_trades' in balance_data:
            stock_today_trades = balance_data['stock_today_trades']
            for stock in stock_today_trades:
                if stock['stock_id'] == target_stock['stock_id'] and stock['operation'] == 'Sell':
                    curr_trades_number += stock['number']                    
            
        log.debug('curr_usable_number: {}, curr_entrust_number: {}, curr_trades_number: {}'
                  .format(curr_usable_number, curr_entrust_number, curr_trades_number))
        # 当前股票可用余额大于最少交易量, 而且没有委托卖单,说明还可以卖 (min_hold_number==curr_usable_number是保留低仓,不会清仓),
        # 限制一日成交不超过预定值max_trade_number_per_day
        if curr_trades_number < target_stock['max_trade_number_per_day'] \
                and (curr_usable_number - target_stock['base_sell_number']) >= target_stock['min_hold_number'] \
                and curr_usable_number >= target_stock['base_sell_number'] \
                and curr_entrust_number == 0:
            # Can sell for stock now
            sell_price = myGetBuySellPrice.get_suggested_sell_price(target_stock)
            if not sell_price:
                log.debug('There is no suggest sell_price for ' + target_stock['stock_id'])
                return None
            log.debug('sell price for ' + target_stock['stock_id'] + ' is ' + str(sell_price))

            sell_items = {'sell_price': sell_price,
                          'stock_id': target_stock['stock_id'],
                          'sell_number': target_stock['base_sell_number']}

            log.debug('check_sell_condition result: ' + str(sell_items))
            return sell_items
            
        #     
        log.debug('Can not sell sny stock ' + target_stock['stock_id'])
        return None    
    except Exception as ex:
        log.exception(ex)
        log.error('check_sell_condition end with exception') 
        return None
        
        
def deal_with_easy_trade(balance_data, target_stocks):
    try:
        log.debug('deal_with_easy_trade start')
        balance_data_cur = balance_data
        if not balance_data_cur:
            log.error('balance_data from Sanity Check is None, skip deal_with_easy_trade')
            return None
            
        current_time = datetime.now().strftime("%H:%M:%S")
        if '09:27:00' <= current_time <= '14:59:00':
            log.debug('within trade time, check buy and sell operation')
            for target_stock in target_stocks:
                if target_stock['enabled']:
                    # Sell
                    sell_item = check_sell_condition(balance_data_cur, target_stock)
                    if sell_item:
                        log.debug('sell_item is ' + str(sell_item))
                        entrust_no = user.sell(sell_item['stock_id'], price=sell_item['sell_price'], amount=sell_item['sell_number'])
                        log.debug('sell result: ' + str(entrust_no))
                        sell_item = None
                        # update the balance after successful sell
                        balance_data_updated = sanity_check(target_stocks)
                        if not balance_data_updated:
                            balance_data_cur = balance_data_updated
                    else:
                        log.debug('no sell for ' + str(target_stock['stock_id']))

                    # Buy
                    buy_item = check_buy_condition(balance_data_cur, target_stock)
                    if buy_item:
                        log.debug('buy_item is ' + str(buy_item))
                        entrust_no = user.buy(buy_item['stock_id'], price=buy_item['buy_price'], amount=buy_item['buy_number'])
                        log.debug('buy result: ' + str(entrust_no))
                        buy_item = None
                        # update the balance after successful buy
                        balance_data_updated = sanity_check(target_stocks)
                        if not balance_data_updated:
                            balance_data_cur = balance_data_updated
                    else:
                        log.debug('no buy for ' + str(target_stock['stock_id']))
                else:
                    log.debug('stockId {} is disabled, will not check buy and sell condition'.format(target_stock['stock_id']))
        else:
            log.debug('Time is out of trade time, no buy or sell operation')        
        
        # Check Sell condiction and make Sell
    except Exception as ex:
        log.exception(ex)
        log.error('deal_with_easy_trade end with exception')  
        

if __name__ == "__main__":
    if connect_to_app():
        target_stocks_f = open("Z:/easytrader/data/target_stocks.json", "r")
        target_stocks = json.load(target_stocks_f)
        target_stocks_f.close()
        sanity_check(target_stocks)



