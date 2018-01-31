import os
import time
import sys
from subprocess import call
import numpy as np
from scipy import spatial

'''
param class tokens: list of most representative tokens for a category[str1, str2, ...]
result: vector representation

Test : 
Sent2vec_PATH = "/Volumes/TOSHIBA_EXT/sent2vec-master/"
model_PATH = Sent2vec_PATH + "wiki_unigrams.bin"
Tokens = ["Auster", "delves", "deeply", "into", "this", "notion", "while", "leaving"]
getSentenceVector(Tokens, Sent2vec_PATH, model_PATH)
'''
def getSentenceVector(Tokens, Sent2vec_PATH, model_PATH):
    fasttext_exec_path = os.path.abspath(Sent2vec_PATH + "./fasttext")
    model = os.path.abspath(model_PATH)
    timestamp = str(time.time())
    Sentence =' '.join(str(s) for s in Tokens)
    Sentence_file = open(Sent2vec_PATH +'./'+timestamp+'_sentence.txt',"w")
    Sentence_file.write(Sentence)
    Sentence_file.close();
    Sentence_path = os.path.abspath(Sent2vec_PATH +'./'+timestamp+'_sentence.txt')
    Vector_path = os.path.abspath(Sent2vec_PATH +'./'+timestamp+'_vector.txt')
    call(fasttext_exec_path +
         ' print-sentence-vectors '+
         MODEL +
         ' < ' + Sentence_path + 
         ' > ' + Vector_path, shell=True)
    vector_reader = open(Vector_path,"r")
    line = vector_reader.readline();
    os.remove(Sentence_path)
    os.remove(Vector_path)
    SentenceVector = np.fromstring(line,sep=' ')
    return SentenceVector



'''
param SentenceVector1: vector rep
param SentenceVector2: vector rep
result: cosine similarty between vectors
'''
'''
Test : 
Sent2vec_PATH = "/Volumes/TOSHIBA_EXT/sent2vec-master/"
model_PATH = Sent2vec_PATH + "wiki_unigrams.bin"
Tokens1 = ["Auster", "delves", "deeply", "into", "this", "notion", "while", "leaving"]
Tokens2 = ["At", "least", "that", "what", "happened", "in", "my", "case"]
vec1 = getSentenceVector(Tokens1, Sent2vec_PATH, model_PATH)
vec2 = getSentenceVector(Tokens2, Sent2vec_PATH, model_PATH)
SentenceVectorDistane(vec1, vec2)
'''

def SentenceVectorDistane(SentenceVector1, SentenceVector2):
    return 1 - spatial.distance.cosine(SentenceVector1, SentenceVector2)
