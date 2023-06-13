import time
import argparse
from train import StockTrainHandler
from postgres import PostgresDBHandler

if __name__ == "__main__":
    # Initialize parser
    parser = argparse.ArgumentParser()

    # Adding optional argument
    parser.add_argument("-id", "--StockId")
    parser.add_argument("-tfs", "--TrainFromScratch")
    parser.add_argument("-gpu", "--GpuDevice")

    # Read arguments from command line
    args = parser.parse_args()

    #
    stock_id = args.StockId
    train_from_scratch = args.TrainFromScratch  # True: Train the model, False: Use the pre-train checkpoints
    gpu_device = args.GpuDevice
    stock_price_path = 'stockData/' + stock_id + '.csv'

    start_ts = time.time()

    postgres = PostgresDBHandler()
    train = StockTrainHandler(stock_id, train_from_scratch, gpu_device, stock_price_path)

    postgres.get_stock_price_and_save_to_file(stock_id, stock_price_path)
    train.train_model()
    postgres.save_predict_result_to_db(stock_id, test_pred, df_test_with_date)

    stop_ts = time.time()
    print('Total time usage: ' + str(round(stop_ts - start_ts)) + ' seconds')

