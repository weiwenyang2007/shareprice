from datetime import datetime
import json
import schedule
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
        if ('09:15:00' <= current_time <= '15:45:00') and (0 <= week_day <= 4):
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
    

# Task scheduling
# After every 4 mins Sanity Check is called.
schedule.every(3).minutes.do(my_easy_trade)

# Loop so that the scheduling task
# keeps on running all time.
while True:
    # Checks whether a scheduled task
    # is pending to run or not
    sleep_seconds = 30
    log.debug('Scheduler is running, will sleep ' + str(sleep_seconds))
    schedule.run_pending()
    time.sleep(sleep_seconds)
