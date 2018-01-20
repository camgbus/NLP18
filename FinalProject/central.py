from obj_dataset import DataSet
import freq_weights as fw
import evaluation as ev
import sys

categories = ["Auth", "Rev", "Sum", "Sto", "WS", "Char", "O"]

books_eval_set = ['HistoryOfWolves', 'ExitWest', 'SolarBones', 'Reservoir13', 'Elmet', 'TheMinistryOfUtmostHappiness', 'LincolnInTheBardo', 'HomeFire', 'SwingTime', 'TheUndergroundRailroad']
books_dev_set = ['4321', 'DaysWithoutEnd ']

train_files = ["data/4321_1.csv", "data/4321_2.csv"]
test_files = []

# Get training and testing sets
train = DataSet(categories, train_files)
test = DataSet(categories, test_files)

# Get predictions of classifier
# TODO: Implement classifier, it must return a list of class strings, where
# each string is the prediction for the observation in test.data in that position
predictions = ['O' for obs in train.data]

# Calculate results and print tables. 
# TODO: Give experiment a name
exp_name = ''
# TODO: Change training with testing dataset
micro_f1, macro_f1, d = ev.get_results(train.data, predictions, categories)
ev.print_results(micro_f1, macro_f1, d, categories, exp_name)
