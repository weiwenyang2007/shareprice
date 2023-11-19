from datetime import datetime
import mySanityCheck as sanity
import myLog as myLogger
log = myLogger.setup_custom_logger(__name__)

def my_easy_trade():
    try:    
        log.info('my_easy_trade start')
        dt = datetime.now()
        week_day = dt.weekday()
        current_time = dt.strftime("%H:%M:%S")
        if (current_time >= '09:15:00' and current_time < '15:15:00') and (week_day >=0 and week_day <=4):
            balance_data = sanity.sanity_check()
            if balance_data:
                sanity.deal_with_easy_trade(balance_data)
            else:
                log.warn('sanity_result is false, skip deal_with_easy_trade')    
        else:
            log.info('my_easy_trade not run due to date time is outof trade')
            
        log.info('my_easy_trade end')    
            
    except Exception as ex:
        log.exception(ex)
        log.error('my_easy_trade end with exception')        
    

if __name__ == "__main__":
    my_easy_trade()
