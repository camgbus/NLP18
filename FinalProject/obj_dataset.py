import reader

class DataSet:
	"""A dataset which stores in a disctionars the tokenized sentences for each class"""
	def __init__(self, categories, filelist):
		# A list of (tokenized sentence, class) tuples
		self.data = list()
		# A dictionary where the keys are the classes and the values, the
		# list of indexes in 'data' that belong to that class
		self.classDict = dict()
		for c in categories:
			self.classDict[c] = list()
		dataIndex = 0
		self.ids = dict()
		for f in filelist:
			# Posicion of sentence in file
			fileIndex = 0
			filename = f[5:-4]
			for (s,c) in reader.read_file(f, categories):
				self.data.append((s,c))
				self.classDict[c].append(dataIndex)
				dataIndex += 1
				self.ids[tuple(s)] = filename+'_'+str(fileIndex)
				fileIndex += 1
			
		
