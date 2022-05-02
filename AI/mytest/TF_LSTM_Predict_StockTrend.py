#code refer   to: https://github.com/lyshello123/stock_predict_with_LSTM/blob/master/stock_predict_2.py
#document ref to: https://mp.weixin.qq.com/s?__biz=MzU0MTM2ODY3NQ==&mid=2247486219&idx=1&sn=b1502747f5fb0fe536b7cd4ad91a5f3b&chksm=fb2a4307cc5dca1154a08b5417258491b690effbf93d39d97aa44c17b67deecedcba889fa665&mpshare=1&scene=1&srcid=0216Kdtpjxrs0WI75l3EtUHu&sharer_sharetime=1581863872722&sharer_shareid=52a8459aa24054ecab156c5dccf5c338#rd
#coding=utf-8

import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

#import tensorflow as tf
#If you have this error after an upgrade to TensorFlow 2.0, 
#you can still use 1.X API by replacing:
import tensorflow.compat.v1 as tf
tf.disable_v2_behavior()

rnn_unit=50         #隐层神经元的个数 hidden layer num of features
lstm_layers=2       #隐层层数 
input_size=26		#输入参数数据维度,本算法只能支持一维输出
output_size=1		#输出结果维度
lr=0.0006         #学习率
#——————————————————导入数据——————————————————————
f=open('./checkPointData/002352_stockCheckPoint.csv')
df=pd.read_csv(f)     #读入股票数据
data=df.iloc[:,1:(1 + input_size + output_size)].values  #跳过前面两项 为输入参数

train_begin_index=2256 #训练数据开始
train_end_index=6999 #训练数据结束
test_begin_index=7000 #测试数据开始
train_times=100 #训练次数
time_step_number=20 #20
batch_size_number=60 #60

#获取训练集
def get_train_data(batch_size=batch_size_number,time_step=time_step_number,train_begin=train_begin_index,train_end=train_end_index):
    batch_index=[]
    data_train=data[train_begin:train_end]
	
    #normalized_train_data=(data_train-np.mean(data_train,axis=0))/np.std(data_train,axis=0)  #标准化
    normalized_train_data=data_train
	
    train_x,train_y=[],[]   #训练集
    for i in range(len(normalized_train_data)-time_step):
       if i % batch_size==0:
           batch_index.append(i)
       x=normalized_train_data[i:i+time_step,:input_size]
       y=normalized_train_data[i:i+time_step,input_size,np.newaxis]
       train_x.append(x.tolist())
       train_y.append(y.tolist())
    batch_index.append((len(normalized_train_data)-time_step))
    return batch_index,train_x,train_y


#获取测试集
def get_test_data(time_step=time_step_number,test_begin=test_begin_index):
    data_test=data[test_begin:]
    mean=np.mean(data_test,axis=0)
    std=np.std(data_test,axis=0)
    
	#normalized_test_data=(data_test-mean)/std  #标准化
    normalized_test_data=data_test

    size=(len(normalized_test_data)+time_step-1)//time_step  #有size个sample
    test_x,test_y=[],[]
    for i in range(size-1):
       x=normalized_test_data[i*time_step:(i+1)*time_step,:input_size]
       y=normalized_test_data[i*time_step:(i+1)*time_step,input_size:]
       test_x.append(x.tolist())
       test_y.extend(y)
    test_x.append((normalized_test_data[(i+1)*time_step:,:input_size]).tolist())
    test_y.extend((normalized_test_data[(i+1)*time_step:,input_size:]).tolist())
    return mean,std,test_x,test_y


#——————————————————定义神经网络变量——————————————————
#输入层、输出层权重,偏置、dropout参数

weights={
         'in':tf.Variable(tf.random_normal([input_size,rnn_unit])),
         'out':tf.Variable(tf.random_normal([rnn_unit,output_size]))
        }
biases={
        'in':tf.Variable(tf.constant(0.1,shape=[rnn_unit,])),
        'out':tf.Variable(tf.constant(0.1,shape=[output_size,]))
       }
keep_prob = tf.placeholder(tf.float32, name='keep_prob')    
#——————————————————定义神经网络变量——————————————————
def lstmCell():
    #basicLstm单元
    basicLstm = tf.nn.rnn_cell.BasicLSTMCell(rnn_unit)
    # dropout
    drop = tf.nn.rnn_cell.DropoutWrapper(basicLstm, output_keep_prob=keep_prob)
    return basicLstm

def lstm(X):
    
    batch_size=tf.shape(X)[0]
    time_step=tf.shape(X)[1]
    w_in=weights['in']
    b_in=biases['in']
    input=tf.reshape(X,[-1,input_size])  #需要将tensor转成2维进行计算，计算后的结果作为隐藏层的输入
    input_rnn=tf.matmul(input,w_in)+b_in
    input_rnn=tf.reshape(input_rnn,[-1,time_step,rnn_unit])  #将tensor转成3维，作为lstm cell的输入
    cell = tf.nn.rnn_cell.MultiRNNCell([lstmCell() for i in range(lstm_layers)])
    init_state=cell.zero_state(batch_size,dtype=tf.float32)
    output_rnn,final_states=tf.nn.dynamic_rnn(cell, input_rnn,initial_state=init_state, dtype=tf.float32)
    output=tf.reshape(output_rnn,[-1,rnn_unit]) 
    w_out=weights['out']
    b_out=biases['out']
    pred=tf.matmul(output,w_out)+b_out
    return pred,final_states

#————————————————训练模型————————————————————

def train_lstm(batch_size=batch_size_number,time_step=time_step_number,train_begin=train_begin_index,train_end=train_end_index):
    X=tf.placeholder(tf.float32, shape=[None,time_step,input_size])
    Y=tf.placeholder(tf.float32, shape=[None,time_step,output_size])
    batch_index,train_x,train_y=get_train_data(batch_size,time_step,train_begin,train_end)
    with tf.variable_scope("sec_lstm"):
        pred,_=lstm(X)
    loss=tf.reduce_mean(tf.square(tf.reshape(pred,[-1])-tf.reshape(Y, [-1])))
    train_op=tf.train.AdamOptimizer(lr).minimize(loss)
    saver=tf.train.Saver(tf.global_variables(),max_to_keep=15)

    with tf.Session() as sess:
        sess.run(tf.global_variables_initializer())
        for i in range(train_times):     #这个迭代次数，可以更改，越大预测效果会更好，但需要更长时间
            for step in range(len(batch_index)-1):
                _,loss_=sess.run([train_op,loss],feed_dict={X:train_x[batch_index[step]:batch_index[step+1]],Y:train_y[batch_index[step]:batch_index[step+1]],keep_prob:0.5})
            print("Number of iterations:",i," loss:",loss_)
        print("model_save: ",saver.save(sess,'model_save2\\modle.ckpt'))
        #I run the code on windows 10,so use  'model_save2\\modle.ckpt'
        #if you run it on Linux,please use  'model_save2/modle.ckpt'
        print("The train has finished")
train_lstm()

#————————————————预测模型————————————————————
def prediction(time_step=time_step_number):
    X=tf.placeholder(tf.float32, shape=[None,time_step,input_size])
    mean,std,test_x,test_y=get_test_data(time_step)
    with tf.variable_scope("sec_lstm",reuse=tf.AUTO_REUSE):
        pred,_=lstm(X)
    saver=tf.train.Saver(tf.global_variables())
    with tf.Session() as sess:
        #参数恢复
        module_file = tf.train.latest_checkpoint('model_save2')
        saver.restore(sess, module_file)
        test_predict=[]
        for step in range(len(test_x)-1):
          prob=sess.run(pred,feed_dict={X:[test_x[step]],keep_prob:1})
          predict=prob.reshape((-1))
          test_predict.extend(predict)
        test_y=np.array(test_y)*std[input_size]+mean[input_size]
        test_predict=np.array(test_predict)*std[input_size]+mean[input_size]
        acc=np.average(np.abs(test_predict-test_y[:len(test_predict)])/test_y[:len(test_predict)])  #偏差程度
        print("The accuracy of this predict:",acc)
        #以折线图表示结果
        plt.figure()
        plt.plot(list(range(len(test_predict))), test_predict, color='b',)
        plt.plot(list(range(len(test_y))), test_y,  color='r')
        plt.show()

prediction()