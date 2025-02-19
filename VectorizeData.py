import json
import numpy
import pickle
from transformers import LongformerTokenizer

# Load database
data_file = "QA_database_EN.json"

start = 0
limit = 3500000

questions =  numpy.empty((limit, 28), dtype='str')
answers = numpy.empty(limit, dtype='str')

iterator = 0
with open(data_file, 'r') as file:
    for line in file:
        if line.strip():
            if iterator >= start:
                data = json.loads(line)

                list = data.get('Questions')
                for idx, question in enumerate(list):
                    questions[iterator][idx] = question
                    
                answers[iterator] = data.get('Answer')

            iterator += 1
            if iterator >= limit:
                break

print("Arrays are ready!")

# Tokenize data
dimensions = 612
tokenizer = LongformerTokenizer.from_pretrained('allenai/longformer-base-4096')

with open('vectorized_questions.pkl', 'ab') as f:
    for question in questions:
        for q in question:
            vector = tokenizer.encode(q, max_length=dimensions, padding="max_length", truncation=True, return_tensors='pt')
            pickle.dump(vector, f)
            del vector

       
print("Questions has been tokenized!")

with open('vectorized_answers.pkl', 'ab') as f:
    for a in answers:
        vector = tokenizer.encode(a, max_length=dimensions, padding="max_length", truncation=True, return_tensors='pt')
        pickle.dump(vector, f)
        del vector

print("Answers has been tokenized!")