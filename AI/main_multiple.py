import time, os, sys
import argparse
import tensorflow as tf
from train import StockTrainHandler
from postgres import PostgresDBHandler

if __name__ == "__main__":
    # Initialize parser
    parser = argparse.ArgumentParser()

    # Adding optional argument
    parser.add_argument("-sufix", "--SufixId") # Sufix of the stockId (the last digit of the stockId)
    parser.add_argument("-tfs", "--TrainFromScratch")
    parser.add_argument("-gpu", "--GpuDevice", type=int, default=0) # 0,1,2 etc
    parser.add_argument("-mem", "--gpuMemory", type=int, default=1024) #limit the gpu memory for each process, with rtx3090, each gpu can run 2 train process with 10g memory
    parser.add_argument("-ckp", "--UseCheckPointId", default='') # Use other checkpoint Id for predict (TrainFromScratch is False)

    # Read arguments from command line
    args = parser.parse_args()

    #
    sufix = args.SufixId
    train_from_scratch = args.TrainFromScratch  # True: Train the model, False: Use the pre-train checkpoints
    gpu_device = args.GpuDevice
    gpuMemory = args.gpuMemory
    useCkpId = args.UseCheckPointId

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
    stock_ids = postgres.get_all_stockIds(sufix)
    count = 0
    for stock_id in stock_ids:
        ckp = './checkpoints/Transformer+TimeEmbedding_mean_' + stock_id + '.hdf5'
        if os.path.exists(ckp):
            print('checkpoint file exist, skip the train ' + stock_id)
            continue

        start_ts = time.time()

        train = StockTrainHandler(stock_id, train_from_scratch, useCkpId)

        len = postgres.get_stock_price_and_save_to_file(stock_id)
        if len < 500:
            print('Length of stock price ' + stock_id + ' is too small, ignore')
            continue

        test_pred, df_test_with_date = train.train_model()
        postgres.save_predict_result_to_db(stock_id, test_pred, df_test_with_date)

        count += 1
        stop_ts = time.time()
        print('One time usage: ' + str(round(stop_ts - start_ts)) + ' seconds, ' + str(count) + '/' + str(len(stock_ids)))

    stop_ts = time.time()
    print('Total time usage: ' + str(round(stop_ts - all_start_ts)) + ' seconds')
