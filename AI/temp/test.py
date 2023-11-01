import time, os, sys
import argparse
import tensorflow as tf
from train import StockTrainHandler
from postgres import PostgresDBHandler

if __name__ == "__main__":
    postgres = PostgresDBHandler()
    postgres.get_stock_price_indicator_and_save_to_file('300059')
    
