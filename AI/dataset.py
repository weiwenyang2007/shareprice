import tensorflow as tf

class AdditionDataset():
    def __init__(self, X_data, y_data):        
        assert(len(X_data)==len(y_data), 'Len of X data and y data must be same')
        self.ixes = X_data
        self.iyes = y_data

    def __len__(self):
        return len(self.ixes)

    def __iter__(self):
        for idx in range(self.__len__()):
            x = self.ixes[idx][0]
            y = [self.iyes[idx]]
            
            x = tf.convert_to_tensor(x, dtype=tf.string)
            y = tf.convert_to_tensor(y, dtype=tf.string)
            print('tf x = ' + str(x))
            print('tf y = ' + str(y))
            #tf x = tf.Tensor([b'difC_PT' b'hs_PB' b'body_PH' b'ls_PL'], shape=(4,), dtype=string)
            #tf y = tf.Tensor([b'Res_NT'], shape=(1,), dtype=string)
            
            yield x, y

    __call__ = __iter__

