import time, os, sys
import argparse
import tensorflow as tf
from train import StockTrainHandler
from postgres import PostgresDBHandler

if __name__ == "__main__":
    # Initialize parser
    parser = argparse.ArgumentParser()

    # Adding optional argument
    parser.add_argument("-prefix", "--Prefix", default=None) # Prefix of the stockId (the first digit of the stockId), for batch process
    parser.add_argument("-sufix", "--Sufix", default=None)   # Sufix of the stockId  (the last digit of the stockId), for batch process
    parser.add_argument("-tfs", "--TrainFromScratch") # True: train the model and save to ckp; False: use the pre-train ckp for prediction.
    parser.add_argument("-gpu", "--GpuDevice", type=int, default=0) # 0,1,2 etc
    parser.add_argument("-mem", "--gpuMemory", type=int, default=1024) #limit the gpu memory for each process, with rtx3090, each gpu can run 2 train process with 10g memory, 0 or -1 will ignore this args
    parser.add_argument("-ckp", "--UseCheckPointId", default=None) # Use other checkpoint Id for predict (TrainFromScratch is False)
    parser.add_argument("-desc", "--OrderByDesc", default=None) #temp args, remove it once all stockId complate pre-train
    #sufix and desc is used for batch process the pre-train or predict async, into multiple gpu system
    parser.add_argument("-preictLen", "--predictTestDateLength", type=int, default=-1)#Length of test date for predict, -1 means this args is ignore (10% pct is use for test);0 means predict the next date, 1 means predict today and next date... 
    
    # Read arguments from command line
    args = parser.parse_args()

    #
    prefix = args.Prefix
    sufix = args.Sufix
    train_from_scratch = args.TrainFromScratch  # True: Train the model, False: Use the pre-train checkpoints
    gpu_device = args.GpuDevice
    gpuMemory = args.gpuMemory
    useCkpId = args.UseCheckPointId
    desc = args.OrderByDesc
    preictLen = args.predictTestDateLength
    
    if prefix and sufix:
        print('Only prefix or sufix is allow, not both')
        sys.exit()

    if train_from_scratch == 'True' and useCkpId != '':
        print('Invalid args: TrainFromScratch is True but UseCheckPointId is Not null, exit')
        sys.exit()

    print('Use GPU ' + str(gpu_device))

    #if set visible devices to 1, then list_physical_devices only return this only one visible gpu
    os.environ["CUDA_VISIBLE_DEVICES"] = str(gpu_device)

    gpus = tf.config.experimental.list_physical_devices('GPU')
    print('GPU:' + str(gpus[0]))
    #PhysicalDevice(name='/physical_device:GPU:0', device_type='GPU')
    if gpuMemory > 0:
        tf.config.set_logical_device_configuration(gpus[0], [tf.config.LogicalDeviceConfiguration(memory_limit=gpuMemory)])


    all_start_ts = time.time()

    postgres = PostgresDBHandler()
    stock_ids = postgres.get_all_stockIds(prefix, sufix, desc)
    counter = 0
    for stock_id in stock_ids:
        ckp = './checkpoints/Transformer+TimeEmbedding_mean_' + stock_id + '.hdf5'
        counter += 1
        if train_from_scratch == 'True':
            #Train the model and save to checkpoints
            if os.path.exists(ckp) :
                #pre-train checkpoint already exist (the price data length is >= 2500 and already pre-train it before)
                print(stock_id + ' pre-train checkpoint already exist, remove this line to train the model from scratch')
                continue
               
            data_length = postgres.get_price_data_length(stock_id)
            if  data_length < 500:   
                #data len less than about 2 years, not enough data for train
                print(stock_id + ' price data length is ' + data_length + ', less than 500, then skip the pre-train')
                continue
            elif data_length >= 2500:   
                #Continue to process
                postgres.get_stock_price_and_save_to_file(stock_id)
            else:
                # data length is between 500~2500
                print(stock_id + ' price data length is ' + data_length + ', between 500 ~ 2500, currently no action for it :)')
                continue
                
        #end if train_from_scratch == 'True'
        else:
            #predict the test data using the pre-train checkpoints
            if os.path.exists(ckp) or useCkpId:
                #pre-train checkpoint exist or specify other checkpoint file for predict
                #Continue to process
                postgres.get_stock_price_and_save_to_file(stock_id)
            else:                    
                print(stock_id + ' pre-train checkpoint not exist, or not specify other checkpoint for predict, skip')                
                continue
            
        #end if train_from_scratch == 'False'
            

        start_ts = time.time()            
        train = StockTrainHandler(stock_id, train_from_scratch, useCkpId, preictLen)            
            
        test_pred, df_test_with_date = train.train_model()
        postgres.save_predict_result_to_db(stock_id, test_pred, df_test_with_date)

        stop_ts = time.time()
        print('One time usage: ' + str(round(stop_ts - start_ts)) + ' seconds, ' + str(counter) + '/' + str(len(stock_ids)))
        
    #end for    

    stop_ts = time.time()
    print('Total time usage: ' + str(round(stop_ts - all_start_ts)) + ' seconds')
