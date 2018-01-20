from obj_dataset import DataSet
import heapq
import sys
import math
from itertools import chain

def tf_weight(token, class_sentences):
	"""Term frequency weight of a token appears in sentences of a class"""
	tf = sum([s.count(token) for s in class_sentences])
	if tf>0:
		return 1+math.log(tf)
	else:
		return 0
    
def idf(token, data, num_sentences):
	"""Inverse document frequency of a token in the corpus"""
	document_freq = 0
	for (sentence, c) in data:
		if token in sentence:
			document_freq += 1
	return math.log(num_sentences/document_freq)

def tfidf_weight(tf_weight, idf):
	return tf_weight*idf

def representative(data, classDict, n):
	"""Returns the n tokens with highest tfidf weights for each category"""
	num_sentences = sum([len(classDict[k]) for k in classDict])
	tokens = list(set(chain.from_iterable(s for (s,c) in data)))
	idfs = [idf(t, data, num_sentences) for t in tokens]
	rep = dict()
	for c in classDict:
		largest = heapq.nlargest(n, [(tf_weight(tokens[i], [data[s][0] for s in classDict[c]])*idfs[i], tokens[i]) for i in range(len(tokens))])
		rep[c] = [t for (w,t) in largest]
	return rep

