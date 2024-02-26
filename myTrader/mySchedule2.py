import sched
import time
from datetime import datetime
import json
import time
import mySanityCheck as sanity
import myLog as myLogger
log = myLogger.setup_custom_logger(__name__)

def my_easy_trade():
    try:
        log.info('my_easy_trade start')
        dt = datetime.now()
        week_day = dt.weekday()
        current_time = dt.strftime("%H:%M:%S")
        if ('09:00:00' <= current_time <= '15:45:00') and (0 <= week_day <= 4):
            target_stocks_f = open("Z:/easytrader/data/target_stocks.json", "r")
            target_stocks = json.load(target_stocks_f)
            target_stocks_f.close()
            if sanity.connect_to_app():
                balance_data = sanity.sanity_check(target_stocks)
                if balance_data:
                    sanity.deal_with_easy_trade(balance_data, target_stocks)
                else:
                    log.warn('sanity_result is false, skip deal_with_easy_trade')
            else:
                log.warn('connect_to_app return None')
        else:
            log.warn('my_easy_trade not run due to date time is outof trade')

        log.info('my_easy_trade end')

    except Exception as ex:
        log.exception(ex)
        log.error('my_easy_trade end with exception')        


scheduler = sched.scheduler(time.time, time.sleep)

def repeat_task():
    scheduler.enter(120, 1, my_easy_trade, ())
    scheduler.enter(120, 1, repeat_task, ())

repeat_task()
scheduler.run()